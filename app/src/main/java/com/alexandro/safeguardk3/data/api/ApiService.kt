package com.alexandro.safeguardk3.data.api

import com.alexandro.safeguardk3.data.model.HealthResponse
import com.alexandro.safeguardk3.data.model.PredictionResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

/**
 * Komunikasi Android dengan backend FastAPI + YOLOv8.
 *
 * Koneksi memakai ADB Reverse:
 * adb reverse tcp:8000 tcp:8000
 *
 * Dengan begitu, emulator dan HP fisik memakai URL yang sama.
 */
class ApiService(
    baseUrl: String = LOCAL_REVERSE_URL,
    private val client: OkHttpClient = defaultClient
) {

    companion object {
        // URL localhost Android setelah ADB Reverse diaktifkan.
        const val LOCAL_REVERSE_URL = "http://127.0.0.1:8000"

        // Nama lama tetap disediakan agar kode UI lain tidak error.
        const val EMULATOR_URL = LOCAL_REVERSE_URL
        const val PHYSICAL_DEVICE_URL = LOCAL_REVERSE_URL

        // Client HTTP dengan pengaturan batas waktu koneksi.
        private val defaultClient: OkHttpClient by lazy {
            OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(45, TimeUnit.SECONDS)
                .writeTimeout(45, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build()
        }
    }

    // Menyimpan URL backend yang sedang digunakan.
    var activeBaseUrl: String = baseUrl
        private set

    // Label untuk chip status koneksi pada aplikasi.
    val activeServerTypeLabel: String
        get() = "ADB Reverse"

    /**
     * Mengecek status backend melalui endpoint GET /health.
     */
    suspend fun checkHealth(): Result<HealthResponse> = withContext(Dispatchers.IO) {
        val healthEndpoint = "$activeBaseUrl/health"

        val request = Request.Builder()
            .url(healthEndpoint)
            .get()
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string()

                // Backend harus mengirim HTTP 200 dan isi JSON.
                if (!response.isSuccessful || bodyString.isNullOrBlank()) {
                    return@withContext Result.failure(
                        Exception("Server mengembalikan HTTP ${response.code}.")
                    )
                }

                // Mengubah JSON respons backend menjadi model Kotlin.
                val health = HealthResponse.fromJson(bodyString)

                Result.success(health)
            }
        } catch (e: ConnectException) {
            Result.failure(
                Exception(
                    "Tidak dapat terhubung ke backend. " +
                            "Jalankan ADB Reverse dan pastikan server FastAPI aktif."
                )
            )
        } catch (e: SocketTimeoutException) {
            Result.failure(
                Exception("Waktu koneksi ke backend habis.")
            )
        } catch (e: IOException) {
            Result.failure(
                Exception(
                    "Koneksi backend gagal: " +
                            (e.localizedMessage ?: "Tidak dapat terhubung")
                )
            )
        } catch (e: Exception) {
            Result.failure(
                Exception(
                    "Respons backend tidak dapat dibaca: " +
                            (e.localizedMessage ?: "Kesalahan tidak diketahui")
                )
            )
        }
    }

    /**
     * Mengirim foto JPEG ke endpoint POST /predict untuk dianalisis YOLOv8.
     */
    suspend fun predict(
        imageBytes: ByteArray
    ): Result<PredictionResponse> = sendImageForPrediction(imageBytes)

    suspend fun sendImageForPrediction(
        imageBytes: ByteArray
    ): Result<PredictionResponse> = withContext(Dispatchers.IO) {
        if (imageBytes.isEmpty()) {
            return@withContext Result.failure(
                Exception("Foto tidak dapat diproses: data gambar kosong.")
            )
        }

        val predictEndpoint = "$activeBaseUrl/predict"

        // Membuat body gambar bertipe JPEG.
        val imageBody = imageBytes.toRequestBody(
            "image/jpeg".toMediaTypeOrNull()
        )

        // Field "file" harus sesuai dengan parameter endpoint FastAPI.
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                "inspeksi_${System.currentTimeMillis()}.jpg",
                imageBody
            )
            .build()

        val request = Request.Builder()
            .url(predictEndpoint)
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string()

                when {
                    response.isSuccessful && !bodyString.isNullOrBlank() -> {
                        Result.success(PredictionResponse.fromJson(bodyString))
                    }

                    response.code == 400 -> {
                        Result.failure(
                            Exception("Foto tidak dapat diproses oleh server.")
                        )
                    }

                    response.code == 503 -> {
                        Result.failure(
                            Exception("Model YOLOv8 di server belum siap.")
                        )
                    }

                    else -> {
                        Result.failure(
                            Exception("Server mengembalikan HTTP ${response.code}.")
                        )
                    }
                }
            }
        } catch (e: ConnectException) {
            Result.failure(
                Exception("Gagal terhubung ke backend untuk menganalisis foto.")
            )
        } catch (e: SocketTimeoutException) {
            Result.failure(
                Exception("Waktu tunggu analisis habis.")
            )
        } catch (e: IOException) {
            Result.failure(
                Exception(
                    "Koneksi terputus: " +
                            (e.localizedMessage ?: "Periksa backend")
                )
            )
        } catch (e: Exception) {
            Result.failure(
                Exception(
                    "Gagal menganalisis gambar: " +
                            (e.localizedMessage ?: "Kesalahan tidak dikenal")
                )
            )
        }
    }
}