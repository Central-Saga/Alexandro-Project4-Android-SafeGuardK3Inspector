"""
Modul inferensi objek APD K3 berbasis Ultralytics YOLOv8.
SafeGuard K3 Inspector - Deteksi APD Pekerja (Person, Hard_hat, Vest, Mask, Gloves, Safety_boots).
"""
import os
import sys
from typing import List, Dict, Any, Union
from PIL import Image
from ultralytics import YOLO

def get_default_model_path() -> str:
    candidates = [
        os.environ.get("MODEL_PATH", ""),
        os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "artifacts", "best.pt"),
        "artifacts/best.pt",
        "backend-api/artifacts/best.pt",
        "best.pt"
    ]
    for c in candidates:
        if c and os.path.exists(c):
            return c
    return "artifacts/best.pt"

def load_yolo_model(model_path: str = None) -> YOLO:
    path = model_path or get_default_model_path()
    if not os.path.exists(path):
        raise FileNotFoundError(f"File model YOLOv8 tidak ditemukan di: {path}")
    return YOLO(path)

def predict_image(image_input: Union[str, Image.Image], conf: float = 0.20, model_path: str = None) -> List[Dict[str, Any]]:
    """
    Menjalankan deteksi objek APD pada gambar tunggal.
    :param image_input: Path file gambar atau instance PIL.Image
    :param conf: Ambang batas confidence (rentang: 0.05 - 0.95)
    :param model_path: Path opsional ke bobot model .pt
    :return: List dictionary hasil deteksi (class_id, class_name, confidence, bbox)
    """
    if conf < 0.05 or conf > 0.95:
        raise ValueError("Parameter conf harus berada di antara 0.05 dan 0.95")

    model = load_yolo_model(model_path)
    results = model(image_input, conf=conf)
    detections = []

    for r in results:
        for box in r.boxes:
            cls_id = int(box.cls[0])
            conf_val = float(box.conf[0])
            xyxy = [round(float(coord), 1) for coord in box.xyxy[0]]
            detections.append({
                "class_id": cls_id,
                "class_name": model.names[cls_id],
                "confidence": round(conf_val, 2),
                "bbox": xyxy
            })

    return detections

if __name__ == "__main__":
    if len(sys.argv) > 1:
        target_img = sys.argv[1]
        print(f"=== Menjalankan Deteksi APD K3: {target_img} ===")
        try:
            preds = predict_image(target_img)
            print(f"Total objek terdeteksi: {len(preds)}")
            for idx, p in enumerate(preds, 1):
                print(f"  {idx}. {p['class_name']:<12} | Conf: {p['confidence']*100:.1f}% | Box: {p['bbox']}")
        except Exception as e:
            print(f"Error saat deteksi: {e}")
    else:
        print("Penggunaan CLI: python src/predict.py <path_ke_file_gambar.jpg>")
