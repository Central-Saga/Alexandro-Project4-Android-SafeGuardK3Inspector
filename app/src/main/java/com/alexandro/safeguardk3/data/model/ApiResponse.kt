package com.alexandro.safeguardk3.data.model

import org.json.JSONArray
import org.json.JSONObject

/**
 * Model data utama untuk respons endpoint prediksi YOLOv8 (/predict).
 */
data class PredictionResponse(
    val success: Boolean,
    val message: String,
    val classification: String,
    val classificationLabel: String,
    val reason: String,
    val summary: SummaryData,
    val detections: List<DetectionItem>,
    val workers: List<WorkerItem>
) {
    companion object {
        /**
         * Parsing JSON yang aman (fault-tolerant).
         * Tidak langsung crash jika terdapat key yang tidak ditemukan.
         */
        fun fromJson(jsonStr: String): PredictionResponse {
            val root = JSONObject(jsonStr)
            val success = root.optBoolean("success", true)
            val message = root.optString("message", "Analisis selesai") ?: "Analisis selesai"
            val classification = root.optString("classification", "PERLU_PEMERIKSAAN") ?: "PERLU_PEMERIKSAAN"
            val classificationLabel = root.optString("classification_label", classification.replace("_", " ")) ?: classification.replace("_", " ")
            val reason = root.optString("reason", "Hasil analisis kepatuhan APD.") ?: "Hasil analisis kepatuhan APD."

            // Parsing detections terlebih dahulu agar dapat dijadikan fallback hitungan objek
            val detectionsList = mutableListOf<DetectionItem>()
            val detArray = root.optJSONArray("detections") ?: root.optJSONArray("data")
            if (detArray != null) {
                for (i in 0 until detArray.length()) {
                    val item = detArray.optJSONObject(i) ?: continue
                    val className = item.optString("class_name", item.optString("class", "Unknown"))
                    val originalClass = item.optString("original_class", className)
                    val confidence = item.optDouble("confidence", 0.0).toFloat()
                    val bboxArray = item.optJSONArray("bbox")
                    val bbox = mutableListOf<Float>()
                    if (bboxArray != null) {
                        for (j in 0 until bboxArray.length()) {
                            bbox.add(bboxArray.optDouble(j, 0.0).toFloat())
                        }
                    }
                    detectionsList.add(DetectionItem(className, originalClass, confidence, bbox))
                }
            }

            // Parsing summary dengan dukungan 6 kelas model
            val summaryObj = root.optJSONObject("summary")
            val summary = if (summaryObj != null) {
                SummaryData(
                    personCount = summaryObj.optInt("person_count", detectionsList.count { it.className == "Person" }),
                    hardHatCount = summaryObj.optInt("hard_hat_count", detectionsList.count { it.className == "Hard_hat" }),
                    vestCount = summaryObj.optInt("vest_count", detectionsList.count { it.className == "Vest" }),
                    maskCount = summaryObj.optInt("mask_count", detectionsList.count { it.className == "Mask" }),
                    glovesCount = summaryObj.optInt("gloves_count", detectionsList.count { it.className == "Gloves" }),
                    safetyBootsCount = summaryObj.optInt("safety_boots_count", detectionsList.count { it.className == "Safety_boots" })
                )
            } else {
                SummaryData(
                    personCount = detectionsList.count { it.className == "Person" },
                    hardHatCount = detectionsList.count { it.className == "Hard_hat" },
                    vestCount = detectionsList.count { it.className == "Vest" },
                    maskCount = detectionsList.count { it.className == "Mask" },
                    glovesCount = detectionsList.count { it.className == "Gloves" },
                    safetyBootsCount = detectionsList.count { it.className == "Safety_boots" }
                )
            }

            // Parsing workers
            val workersList = mutableListOf<WorkerItem>()
            val workersArray = root.optJSONArray("workers")
            if (workersArray != null) {
                for (i in 0 until workersArray.length()) {
                    val item = workersArray.optJSONObject(i) ?: continue
                    val workerId = item.optInt("worker_id", i + 1)
                    val confidence = item.optDouble("confidence", 0.0).toFloat()
                    val hasHardHat = item.optBoolean("has_hard_hat", false)
                    val hasVest = item.optBoolean("has_vest", false)
                    val status = item.optString("status", "PERLU_PEMERIKSAAN")
                    val workerReason = item.optString("reason", "")
                    val bboxArray = item.optJSONArray("bbox")
                    val bbox = mutableListOf<Float>()
                    if (bboxArray != null) {
                        for (j in 0 until bboxArray.length()) {
                            bbox.add(bboxArray.optDouble(j, 0.0).toFloat())
                        }
                    }
                    workersList.add(WorkerItem(workerId, bbox, confidence, hasHardHat, hasVest, status, workerReason))
                }
            }

            return PredictionResponse(
                success = success,
                message = message,
                classification = classification,
                classificationLabel = classificationLabel,
                reason = reason,
                summary = summary,
                detections = detectionsList,
                workers = workersList
            )
        }
    }
}

/**
 * Ringkasan total objek dari 6 kelas model YOLOv8:
 * Person, Hard_hat, Vest, Mask, Gloves, Safety_boots.
 */
data class SummaryData(
    val personCount: Int,
    val hardHatCount: Int,
    val vestCount: Int,
    val maskCount: Int = 0,
    val glovesCount: Int = 0,
    val safetyBootsCount: Int = 0
)

/**
 * Objek deteksi mentah dari model YOLOv8.
 */
data class DetectionItem(
    val className: String,
    val originalClass: String,
    val confidence: Float,
    val bbox: List<Float>
)

/**
 * Objek pekerja beserta status evaluasi kepatuhan APD-nya.
 */
data class WorkerItem(
    val workerId: Int,
    val bbox: List<Float>,
    val confidence: Float,
    val hasHardHat: Boolean,
    val hasVest: Boolean,
    val status: String,
    val reason: String
)

/**
 * Model data respons status kesehatan server (/health).
 */
data class HealthResponse(
    val status: String,
    val service: String,
    val version: String,
    val modelLoaded: Boolean,
    val message: String
) {
    companion object {
        fun fromJson(jsonStr: String): HealthResponse {
            val root = JSONObject(jsonStr)
            return HealthResponse(
                status = root.optString("status", "unknown") ?: "unknown",
                service = root.optString("service", "SafeGuard K3 API") ?: "SafeGuard K3 API",
                version = root.optString("version", "2.0.0") ?: "2.0.0",
                modelLoaded = root.optBoolean("model_loaded", false),
                message = root.optString("message", "") ?: ""
            )
        }
    }
}
