"""
Pipeline Pelatihan YOLOv8 SafeGuard K3 (Reproducible Training Pipeline).
Memenuhi standar Issue #13: Training pipeline CLI yang dapat direproduksi.
"""
import argparse
import os
import sys
from ultralytics import YOLO

def train_yolo(
    data_yaml: str,
    base_model: str = "yolov8s.pt",
    epochs: int = 80,
    batch_size: int = 4,
    imgsz: int = 640,
    patience: int = 15,
    project: str = "runs/train",
    name: str = "safeguard_k3_yolov8s"
):
    """
    Melatih model deteksi APD K3 berbasis Ultralytics YOLOv8.
    
    :param data_yaml: Path ke file data.yaml (konfigurasi dataset Roboflow / K3)
    :param base_model: Model checkpoint dasar (default: yolov8s.pt)
    :param epochs: Total siklus epoch pelatihan (default: 80)
    :param batch_size: Ukuran batch (default: 4)
    :param imgsz: Dimensi input citra (default: 640)
    :param patience: Early stopping tolerance (default: 15)
    :param project: Folder output artefak training
    :param name: Nama subdirektori eksperimen
    """
    if not os.path.exists(data_yaml):
        print(f"[ERROR] File konfigurasi dataset '{data_yaml}' tidak ditemukan!")
        print("Pastikan dataset berformat YOLOv8 telah diunduh dan data.yaml tersedia.")
        sys.exit(1)

    print("=" * 60)
    print("🚀 SAFEGUARD K3 - YOLOv8 TRAINING PIPELINE")
    print("=" * 60)
    print(f"Dataset Config : {data_yaml}")
    print(f"Base Pretrained: {base_model}")
    print(f"Target Epochs  : {epochs}")
    print(f"Batch Size     : {batch_size}")
    print(f"Image Size     : {imgsz}x{imgsz}")
    print(f"Early Stopping : patience={patience}")
    print(f"Output Path    : {os.path.join(project, name)}")
    print("=" * 60)

    # Inisialisasi model dari checkpoint pretrained
    model = YOLO(base_model)

    # Memulai proses training
    results = model.train(
        data=data_yaml,
        epochs=epochs,
        batch=batch_size,
        imgsz=imgsz,
        patience=patience,
        cache=False,
        plots=True,
        save=True,
        project=project,
        name=name
    )

    print("\n" + "=" * 60)
    print("✅ PELATIHAN SELESAI SECARA REPRODUCIBLE")
    print(f"Hasil bobot model (best.pt) dan metrik (mAP50, mAP50-95):")
    print(f"-> {os.path.join(project, name, 'weights', 'best.pt')}")
    print("=" * 60)
    return results

if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        description="Pipeline Reproducible Training YOLOv8 untuk SafeGuard K3 Inspector"
    )
    parser.add_argument("--data", type=str, default="data.yaml", help="Path ke data.yaml")
    parser.add_argument("--model", type=str, default="yolov8s.pt", help="Base model weights")
    parser.add_argument("--epochs", type=int, default=80, help="Jumlah epoch")
    parser.add_argument("--batch", type=int, default=4, help="Batch size")
    parser.add_argument("--imgsz", type=int, default=640, help="Ukuran resolusi gambar")
    parser.add_argument("--patience", type=int, default=15, help="Early stopping patience")
    parser.add_argument("--project", type=str, default="runs/train", help="Direktori penyimpanan run")
    parser.add_argument("--name", type=str, default="safeguard_k3_yolov8s", help="Nama eksperimen")

    args = parser.parse_args()
    train_yolo(
        data_yaml=args.data,
        base_model=args.model,
        epochs=args.epochs,
        batch_size=args.batch,
        imgsz=args.imgsz,
        patience=args.patience,
        project=args.project,
        name=args.name
    )
