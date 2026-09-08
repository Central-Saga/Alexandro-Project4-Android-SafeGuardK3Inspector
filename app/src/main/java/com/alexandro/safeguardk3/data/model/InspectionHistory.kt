package com.alexandro.safeguardk3.data.model

import org.json.JSONArray
import org.json.JSONObject

/**
 * Entitas model data untuk riwayat inspeksi K3 lokal.
 * Dilengkapi dengan serialized workersJson untuk rincian pekerja individual.
 */
data class InspectionRecord(
    val id: Long = 0,
    val timestamp: Long,
    val formattedTime: String,
    val imagePath: String,
    val classification: String,
    val classificationLabel: String,
    val reason: String,
    val personCount: Int,
    val hardHatCount: Int,
    val vestCount: Int,
    val workersJson: String = ""
) {
    /**
     * Mengurai string workersJson kembali menjadi List<WorkerItem>.
     */
    fun getWorkers(): List<WorkerItem> {
        if (workersJson.isBlank()) return emptyList()
        val list = mutableListOf<WorkerItem>()
        try {
            val jsonArray = JSONArray(workersJson)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val workerId = obj.optInt("worker_id", i + 1)
                val status = obj.optString("status", "")
                val confidence = obj.optDouble("confidence", 0.9).toFloat()
                val hasHardHat = obj.optBoolean("has_hard_hat", false)
                val hasVest = obj.optBoolean("has_vest", false)
                val reason = obj.optString("reason", "")
                val bboxArray = obj.optJSONArray("bbox")
                val bbox = mutableListOf<Float>()
                if (bboxArray != null) {
                    for (j in 0 until bboxArray.length()) {
                        bbox.add(bboxArray.getDouble(j).toFloat())
                    }
                }
                list.add(
                    WorkerItem(
                        workerId = workerId,
                        bbox = bbox,
                        confidence = confidence,
                        hasHardHat = hasHardHat,
                        hasVest = hasVest,
                        status = status,
                        reason = reason
                    )
                )
            }
        } catch (e: Exception) {
            // Silently handle parse error and return what has been parsed
        }
        return list
    }

    companion object {
        /**
         * Konversi daftar WorkerItem menjadi representasi String JSON untuk disimpan di SQLite.
         */
        fun workersToJson(workers: List<WorkerItem>): String {
            val array = JSONArray()
            for (w in workers) {
                val obj = JSONObject().apply {
                    put("worker_id", w.workerId)
                    put("status", w.status)
                    put("confidence", w.confidence.toDouble())
                    put("has_hard_hat", w.hasHardHat)
                    put("has_vest", w.hasVest)
                    put("reason", w.reason)
                    val bboxArr = JSONArray()
                    w.bbox.forEach { bboxArr.put(it.toDouble()) }
                    put("bbox", bboxArr)
                }
                array.put(obj)
            }
            return array.toString()
        }
    }
}
