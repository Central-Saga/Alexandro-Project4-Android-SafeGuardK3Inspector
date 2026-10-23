import io
import unittest
import sys
import os
from fastapi.testclient import TestClient
from PIL import Image

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from app import app, calculate_iou, is_point_in_box, associate_apd_to_workers, normalize_class_name

class SafeGuardK3Tests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.client = TestClient(app)

    def create_dummy_image(self, width=100, height=100, color="blue"):
        buf = io.BytesIO()
        img = Image.new("RGB", (width, height), color=color)
        img.save(buf, format="JPEG")
        buf.seek(0)
        return buf

    def test_health_check(self):
        resp = self.client.get("/health")
        self.assertEqual(resp.status_code, 200)
        data = resp.json()
        self.assertIn("status", data)
        self.assertIn("version", data)

    def test_normalize_class_name(self):
        self.assertEqual(normalize_class_name("hard-hat"), "Hard_hat")
        self.assertEqual(normalize_class_name("Persons"), "Person")
        self.assertEqual(normalize_class_name("vests"), "Vest")

    def test_calculate_iou(self):
        self.assertEqual(calculate_iou([0, 0, 10, 10], [0, 0, 10, 10]), 1.0)
        self.assertEqual(calculate_iou([0, 0, 10, 10], [20, 20, 30, 30]), 0.0)

    def test_is_point_in_box(self):
        self.assertTrue(is_point_in_box((25, 25), [10, 10, 50, 50]))
        self.assertFalse(is_point_in_box((5, 5), [10, 10, 50, 50]))

    def test_conf_bounds_validation(self):
        buf = self.create_dummy_image()
        resp = self.client.post("/predict?conf=0.01", files={"file": ("test.jpg", buf, "image/jpeg")})
        self.assertEqual(resp.status_code, 422)

    def test_unsupported_media_type(self):
        buf = io.BytesIO(b"Not an image")
        resp = self.client.post("/predict", files={"file": ("test.txt", buf, "text/plain")})
        self.assertEqual(resp.status_code, 415)

    def test_valid_image_prediction(self):
        buf = self.create_dummy_image()
        resp = self.client.post("/predict?conf=0.20", files={"file": ("test.jpg", buf, "image/jpeg")})
        self.assertIn(resp.status_code, [200, 503])
        if resp.status_code == 200:
            self.assertTrue(resp.json()["success"])

if __name__ == "__main__":
    unittest.main()
