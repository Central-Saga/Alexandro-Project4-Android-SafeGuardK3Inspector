from fastapi import FastAPI, UploadFile, File, HTTPException, status
from fastapi.responses import JSONResponse
from fastapi.middleware.cors import CORSMiddleware
from ultralytics import YOLO
from PIL import Image
import io
import os
import logging

# Inisialisasi logger untuk pencatatan aktivitas inspeksi K3
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("SafeGuardK3")

# Inisialisasi aplikasi FastAPI
app = FastAPI(
    title="SafeGuard K3 Inspector API",
    description="API Deteksi dan Klasifikasi Kepatuhan APD K3 Pekerja Menggunakan YOLOv8",
    version="2.0.0"
)

# Mengaktifkan CORS agar dapat diakses dari berbagai origin (termasuk development Android / emulator)
# Sesuai standar keamanan CORS (Issue #9): allow_credentials=False saat allow_origins=["*"]
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=False,
    allow_methods=["GET", "POST", "OPTIONS"],
    allow_headers=["*"],
)

def resolve_model_path() -> str:
    """Mencari path file model best.pt secara fleksibel (Issue #15, #16)."""
    candidates = [
        os.environ.get("MODEL_PATH", ""),
        "artifacts/best.pt",
        "backend-api/artifacts/best.pt",
        os.path.join(os.path.dirname(os.path.abspath(__file__)), "artifacts", "best.pt"),
        os.path.join(os.path.dirname(os.path.abspath(__file__)), "best.pt"),
        "best.pt"
    ]
    for candidate in candidates:
        if candidate and os.path.exists(candidate):
            return candidate
    return "artifacts/best.pt"

# Path model YOLOv8 yang sudah dilatih
MODEL_PATH = resolve_model_path()

# Memuat model YOLOv8 saat server mulai
try:
    if os.path.exists(MODEL_PATH):
        model = YOLO(MODEL_PATH)
        logger.info(f"Model YOLOv8 berhasil dimuat dari: {MODEL_PATH}")
        logger.info(f"Daftar kelas model: {model.names}")
    else:
        logger.error(f"File model tidak ditemukan di {MODEL_PATH}")
        model = None
except Exception as e:
    logger.error(f"Gagal memuat model YOLOv8: {e}")
    model = None


def normalize_class_name(raw_name: str) -> str:
    """
    Menormalisasi variasi nama kelas dari model YOLOv8 menjadi nama kanonikal.
    Mendukung variasi penulisan seperti 'Hard_hats', 'hard_hat', 'Vests', 'Persons', dll.
    """
    clean = raw_name.strip().lower().replace("-", "_").replace(" ", "_")
    if "hard_hat" in clean or "hardhat" in clean or "helmet" in clean:
        return "Hard_hat"
    elif "vest" in clean:
        return "Vest"
    elif "person" in clean:
        return "Person"
    elif "boot" in clean or "safety_boots" in clean:
        return "Safety_boots"
    elif "glove" in clean:
        return "Gloves"
    elif "mask" in clean:
        return "Mask"
    return raw_name


def is_point_in_box(point: tuple, box: list) -> bool:
    """Mengecek apakah koordinat (x, y) berada di dalam bounding box [x1, y1, x2, y2]."""
    px, py = point
    x1, y1, x2, y2 = box
    return x1 <= px <= x2 and y1 <= py <= y2


def calculate_iou(boxA: list, boxB: list) -> float:
    """Menghitung Intersection over Union (IoU) antara dua bounding box."""
    xA = max(boxA[0], boxB[0])
    yA = max(boxA[1], boxB[1])
    xB = min(boxA[2], boxB[2])
    yB = min(boxA[3], boxB[3])

    interArea = max(0.0, xB - xA) * max(0.0, yB - yA)
    boxAArea = max(0.0, boxA[2] - boxA[0]) * max(0.0, boxA[3] - boxA[1])
    boxBArea = max(0.0, boxB[2] - boxB[0]) * max(0.0, boxB[3] - boxB[1])

    denom = boxAArea + boxBArea - interArea
    if denom <= 0:
        return 0.0
    return interArea / denom


def associate_apd_to_workers(persons: list, hard_hats: list, vests: list):
    """
    Mencocokkan atribut APD (Helm dan Rompi) dengan pekerja (Person) terdekat
    secara spasial berdasarkan anatomi tubuh (kepala di atas, rompi di badan).
    """
    workers = []
    used_helmets = set()
    used_vests = set()
    has_ambiguity = False

    # Urutkan pekerja dari kiri ke kanan untuk urutan worker_id yang konsisten
    sorted_persons = sorted(persons, key=lambda p: p["bbox"][0])

    for i, person in enumerate(sorted_persons):
        px1, py1, px2, py2 = person["bbox"]
        p_width = px2 - px1
        p_height = py2 - py1

        # Estimasi area kepala pekerja: 35% tinggi bagian atas tubuh
        head_region = [
            px1 - 0.15 * p_width,
            py1 - 0.20 * p_height,
            px2 + 0.15 * p_width,
            py1 + 0.40 * p_height
        ]

        # Estimasi area torso/badan pekerja: 20% s/d 75% tinggi tubuh
        torso_region = [
            px1 - 0.15 * p_width,
            py1 + 0.15 * p_height,
            px2 + 0.15 * p_width,
            py1 + 0.80 * p_height
        ]

        # 1. Mencari helm pengaman yang cocok untuk pekerja ini
        matched_helmet = None
        for h_idx, helmet in enumerate(hard_hats):
            if h_idx in used_helmets:
                continue
            hx1, hy1, hx2, hy2 = helmet["bbox"]
            h_cx = (hx1 + hx2) / 2.0
            h_cy = (hy1 + hy2) / 2.0

            # Cek apakah titik tengah helm berada di area kepala pekerja
            if is_point_in_box((h_cx, h_cy), head_region):
                matched_helmet = h_idx
                break
            # Alternatif: jika IoU signifikan dengan area kepala
            elif calculate_iou(helmet["bbox"], head_region) > 0.1:
                matched_helmet = h_idx
                break

        if matched_helmet is not None:
            used_helmets.add(matched_helmet)

        # 2. Mencari rompi safety yang cocok untuk pekerja ini
        matched_vest = None
        for v_idx, vest in enumerate(vests):
            if v_idx in used_vests:
                continue
            vx1, vy1, vx2, vy2 = vest["bbox"]
            v_cx = (vx1 + vx2) / 2.0
            v_cy = (vy1 + vy2) / 2.0

            # Cek apakah titik tengah rompi berada di area torso pekerja
            if is_point_in_box((v_cx, v_cy), torso_region):
                matched_vest = v_idx
                break
            # Alternatif: jika IoU signifikan dengan area torso
            elif calculate_iou(vest["bbox"], torso_region) > 0.15:
                matched_vest = v_idx
                break

        if matched_vest is not None:
            used_vests.add(matched_vest)

        has_hard_hat = matched_helmet is not None
        has_vest = matched_vest is not None

        # Tentukan status kepatuhan per pekerja
        if has_hard_hat and has_vest:
            w_status = "APD_LENGKAP"
            w_reason = "APD lengkap (Helm dan Rompi terpakai)"
        elif not has_hard_hat and not has_vest:
            w_status = "APD_TIDAK_LENGKAP"
            w_reason = "Helm pengaman dan rompi safety tidak terdeteksi"
        elif not has_hard_hat:
            w_status = "APD_TIDAK_LENGKAP"
            w_reason = "Helm pengaman tidak terdeteksi"
        else:
            w_status = "APD_TIDAK_LENGKAP"
            w_reason = "Rompi safety tidak terdeteksi"

        # Cek jika confidence pekerja terlalu rendah (< 0.35)
        if person["confidence"] < 0.35:
            has_ambiguity = True

        workers.append({
            "worker_id": i + 1,
            "bbox": [round(c, 1) for c in person["bbox"]],
            "confidence": round(person["confidence"], 2),
            "has_hard_hat": has_hard_hat,
            "has_vest": has_vest,
            "status": w_status,
            "reason": w_reason
        })

    # Cek tumpang tindih berlebih antar pekerja (heavy occlusion)
    for i in range(len(sorted_persons)):
        for j in range(i + 1, len(sorted_persons)):
            if calculate_iou(sorted_persons[i]["bbox"], sorted_persons[j]["bbox"]) > 0.65:
                has_ambiguity = True
                break

    return workers, has_ambiguity


@app.get("/health")
def health_check():
    """
    Endpoint pemantauan status kesehatan server backend.
    Digunakan oleh aplikasi Android untuk memverifikasi koneksi ADB Reverse.
    """
    is_ready = model is not None
    supported_classes = ["Gloves", "Hard_hat", "Mask", "Person", "Safety_boots", "Vest"]
    return {
        "status": "ok" if is_ready else "error",
        "service": "SafeGuard K3 Inspector API",
        "version": "2.0.0",
        "model_loaded": is_ready,
        "supported_evaluation_classes": ["Person", "Hard_hat", "Vest"],
        "supported_detection_classes": supported_classes,
        "message": "Server SafeGuard K3 siap menerima analisis" if is_ready else "Model YOLOv8 belum siap"
    }


# Batasan upload & keamanan (Issue #11)
MAX_FILE_SIZE = 5 * 1024 * 1024  # Maksimal 5 MB
ALLOWED_CONTENT_TYPES = {"image/jpeg", "image/png", "image/webp", "image/jpg"}

@app.post("/predict")
async def predict_apd(file: UploadFile = File(...), conf: float = 0.20):
    """
    Endpoint utama prediksi dan klasifikasi kepatuhan APD K3.
    Menerima file gambar via multipart/form-data dengan key 'file'.
    Mendukung parameter query opsional 'conf' (rentang valid: 0.05 s/d 0.95).
    """
    # 1. Validasi rentang parameter conf (Issue #11)
    if conf < 0.05 or conf > 0.95:
        return JSONResponse(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            content={
                "success": False,
                "message": "Parameter 'conf' harus berada dalam rentang 0.05 hingga 0.95."
            }
        )

    # 2. Cek kesiapan model (Issue #15, #16)
    if model is None:
        return JSONResponse(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            content={
                "success": False,
                "message": "Model YOLOv8 belum berhasil dimuat di server. Periksa ketersediaan artifacts/best.pt."
            }
        )

    # 3. Validasi Content-Type / MIME Type (Issue #11)
    if file.content_type and file.content_type.lower() not in ALLOWED_CONTENT_TYPES:
        return JSONResponse(
            status_code=status.HTTP_415_UNSUPPORTED_MEDIA_TYPE,
            content={
                "success": False,
                "message": f"Tipe file '{file.content_type}' tidak didukung. Harap unggah gambar JPG atau PNG."
            }
        )

    # 4. Pembacaan gambar dengan proteksi batas ukuran 5 MB (Issue #11)
    try:
        image_bytes = await file.read()
        if not image_bytes:
            return JSONResponse(
                status_code=status.HTTP_400_BAD_REQUEST,
                content={
                    "success": False,
                    "message": "File gambar yang diunggah kosong."
                }
            )

        if len(image_bytes) > MAX_FILE_SIZE:
            return JSONResponse(
                status_code=status.HTTP_413_REQUEST_ENTITY_TOO_LARGE,
                content={
                    "success": False,
                    "message": f"Ukuran file gambar melebihi batas maksimal 5 MB ({len(image_bytes) / (1024*1024):.2f} MB)."
                }
            )

        # Verifikasi integritas gambar untuk mencegah decompression bomb / corrupt file
        test_img = Image.open(io.BytesIO(image_bytes))
        test_img.verify()

        # Buka gambar untuk proses inferensi
        image = Image.open(io.BytesIO(image_bytes))
        if image.mode != "RGB":
            image = image.convert("RGB")
    except Exception as e:
        logger.warning(f"Validasi/pembacaan file gambar gagal: {e}")
        return JSONResponse(
            status_code=status.HTTP_400_BAD_REQUEST,
            content={
                "success": False,
                "message": "File tidak dapat diproses. Pastikan file adalah gambar JPG atau PNG yang valid."
            }
        )

    # 5. Menjalankan inferensi YOLOv8 dengan penanganan error aman (Issue #11)
    try:
        results = model(image, conf=conf)
    except Exception as e:
        logger.error(f"Error saat inferensi YOLOv8: {e}", exc_info=True)
        return JSONResponse(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            content={
                "success": False,
                "message": "Terjadi kesalahan internal saat memproses deteksi objek."
            }
        )

    # Ekstraksi dan logging seluruh deteksi mentah (6 kelas model YOLOv8)
    detections = []
    persons = []
    hard_hats = []
    vests = []
    masks = []
    gloves = []
    safety_boots = []

    raw_box_count = sum(len(r.boxes) for r in results)
    logger.info(f"--- [DIAGNOSIS INFERENSI] Ambang batas conf={conf:.2f} | Total raw boxes: {raw_box_count} ---")

    for r in results:
        for box in r.boxes:
            class_id = int(box.cls[0])
            raw_class_name = model.names[class_id]
            canonical_name = normalize_class_name(raw_class_name)
            confidence = float(box.conf[0])
            xyxy = [float(coord) for coord in box.xyxy[0]]

            # Logging detail deteksi mentah sesuai kebutuhan diagnosis
            logger.info(
                f"  -> [RAW DETECTION] Kelas: {canonical_name:<12} "
                f"(id: {class_id}, label: '{raw_class_name}'), "
                f"Conf: {confidence:.4f}, BBox: {[round(c, 1) for c in xyxy]}"
            )

            detection_item = {
                "class_name": canonical_name,
                "original_class": raw_class_name,
                "confidence": round(confidence, 2),
                "bbox": [round(c, 1) for c in xyxy]
            }
            detections.append(detection_item)

            # Pengelompokan seluruh 6 kelas model
            if canonical_name == "Person":
                persons.append(detection_item)
            elif canonical_name == "Hard_hat":
                hard_hats.append(detection_item)
            elif canonical_name == "Vest":
                vests.append(detection_item)
            elif canonical_name == "Mask":
                masks.append(detection_item)
            elif canonical_name == "Gloves":
                gloves.append(detection_item)
            elif canonical_name == "Safety_boots":
                safety_boots.append(detection_item)

    person_count = len(persons)
    hard_hat_count = len(hard_hats)
    vest_count = len(vests)
    mask_count = len(masks)
    gloves_count = len(gloves)
    safety_boots_count = len(safety_boots)
    apd_count = hard_hat_count + vest_count + mask_count + gloves_count + safety_boots_count

    # Catatan diagnosis apabila Helm/Rompi terdeteksi namun Person tidak terdeteksi
    if person_count == 0 and (hard_hat_count > 0 or vest_count > 0):
        logger.warning(
            f"[DIAGNOSIS AUDIT] Terdeteksi Helm ({hard_hat_count}) atau Rompi ({vest_count}) "
            f"tetapi Person = 0 pada conf={conf:.2f}. "
            "Hal ini mengindikasikan inkonsistensi anotasi Person pada dataset pelatihan."
        )

    # Menjalankan algoritma asosiasi APD ke setiap pekerja (Helm & Rompi)
    workers, has_ambiguity = associate_apd_to_workers(persons, hard_hats, vests)

    # Aturan Klasifikasi Kepatuhan APD K3:
    # 1. Jika Person == 0 DAN tidak ada APD terdeteksi
    if person_count == 0 and apd_count == 0:
        classification = "TIDAK_ADA_PEKERJA"
        classification_label = "TIDAK ADA PEKERJA"
        reason = "Tidak ada pekerja maupun atribut APD yang terdeteksi pada gambar."
    # 2. Jika Person == 0 TETAPI ada minimal satu APD terdeteksi (Helm, Rompi, Masker, Sarung Tangan, atau Sepatu)
    elif person_count == 0 and apd_count > 0:
        classification = "PERLU_PEMERIKSAAN"
        classification_label = "PERLU PEMERIKSAAN"
        reason = "Atribut APD terdeteksi, tetapi pekerja belum terdeteksi dengan cukup jelas. Ambil foto dengan tubuh pekerja terlihat lebih utuh."
    # 3. Jika Person terdeteksi (> 0)
    elif has_ambiguity:
        classification = "PERLU_PEMERIKSAAN"
        classification_label = "PERLU PEMERIKSAAN"
        reason = "Objek saling bertumpuk atau kualitas foto rendah sehingga asosiasi APD belum dapat dipastikan."
    else:
        # Evaluasi kepatuhan semua pekerja (Hard_hat & Vest)
        incomplete_workers = [w for w in workers if w["status"] == "APD_TIDAK_LENGKAP"]
        if len(incomplete_workers) == 0:
            classification = "APD_LENGKAP"
            classification_label = "APD LENGKAP"
            reason = f"Semua pekerja ({person_count} orang) terdeteksi menggunakan APD lengkap (Helm & Rompi)."
        else:
            classification = "APD_TIDAK_LENGKAP"
            classification_label = "APD TIDAK LENGKAP"
            reason = f"{len(incomplete_workers)} dari {person_count} pekerja tidak memenuhi kelengkapan APD wajib."

    # Log backend yang mudah dibaca untuk setiap analisis
    logger.info(
        f"[ANALISIS K3] Pekerja: {person_count} | Helm: {hard_hat_count} | Rompi: {vest_count} | "
        f"Masker: {mask_count} | Sarung Tangan: {gloves_count} | Sepatu Safety: {safety_boots_count} | "
        f"Status: {classification} ({classification_label})"
    )

    # Format respons API yang konsisten, aman, dan terstandarisasi
    response_data = {
        "success": True,
        "message": "Analisis kepatuhan APD K3 berhasil diselesaikan.",
        "classification": classification,
        "classification_label": classification_label,
        "reason": reason,
        "summary": {
            "person_count": person_count,
            "hard_hat_count": hard_hat_count,
            "vest_count": vest_count,
            "mask_count": mask_count,
            "gloves_count": gloves_count,
            "safety_boots_count": safety_boots_count,
            "total_detections": len(detections)
        },
        "detections": detections,
        "workers": workers
    }

    return response_data


if __name__ == "__main__":
    import uvicorn
    # Menjalankan server uvicorn pada port 8000
    uvicorn.run(app, host="0.0.0.0", port=8000)
