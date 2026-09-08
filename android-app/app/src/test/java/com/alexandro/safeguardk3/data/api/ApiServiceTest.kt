package com.alexandro.safeguardk3.data.api

import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

/**
 * Unit Test untuk ApiService (koneksi backend FastAPI http://127.0.0.1:8000 via ADB Reverse).
 */
class ApiServiceTest {

    private fun createMockOkHttpClient(
        healthSuccess: Boolean,
        capturedRequests: MutableList<String>? = null
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                val urlStr = request.url.toString()
                capturedRequests?.add(urlStr)

                if (urlStr.contains("/health")) {
                    if (healthSuccess) {
                        Response.Builder()
                            .request(request)
                            .protocol(Protocol.HTTP_1_1)
                            .code(200)
                            .message("OK")
                            .body("""{"status": "ok", "version": "2.0.0", "model_loaded": true}""".toResponseBody("application/json".toMediaType()))
                            .build()
                    } else {
                        throw IOException("Connection refused")
                    }
                } else if (urlStr.contains("/predict")) {
                    Response.Builder()
                        .request(request)
                        .protocol(Protocol.HTTP_1_1)
                        .code(200)
                        .message("OK")
                        .body(
                            """{
                                "success": true,
                                "classification": "APD_LENGKAP",
                                "classification_label": "APD LENGKAP",
                                "reason": "Semua pekerja terdeteksi menggunakan APD lengkap.",
                                "summary": {
                                    "person_count": 1,
                                    "hard_hat_count": 1,
                                    "vest_count": 1,
                                    "mask_count": 1,
                                    "gloves_count": 2,
                                    "safety_boots_count": 2
                                },
                                "detections": [],
                                "workers": []
                            }""".trimIndent().toResponseBody("application/json".toMediaType())
                        )
                        .build()
                } else {
                    throw IOException("Unknown endpoint: $urlStr")
                }
            }
            .build()
    }

    @Test
    fun checkHealth_succeeds_whenServerReturns200() = runBlocking {
        val captured = mutableListOf<String>()
        val client = createMockOkHttpClient(healthSuccess = true, capturedRequests = captured)
        val apiService = ApiService(client = client)

        val result = apiService.checkHealth()

        assertTrue("Check health harus berhasil, err: ${result.exceptionOrNull()?.message}", result.isSuccess)
        assertEquals("http://127.0.0.1:8000", apiService.activeBaseUrl)
        assertTrue(captured.any { it.contains("127.0.0.1:8000/health") })
    }

    @Test
    fun checkHealth_handlesError_whenServerFails() = runBlocking {
        val client = createMockOkHttpClient(healthSuccess = false)
        val apiService = ApiService(client = client)

        val result = apiService.checkHealth()

        assertTrue("Check health harus gagal saat server tidak aktif", result.isFailure)
    }

    @Test
    fun sendImageForPrediction_sendsToPredictEndpoint() = runBlocking {
        val capturedUrls = mutableListOf<String>()
        val client = createMockOkHttpClient(healthSuccess = true, capturedRequests = capturedUrls)
        val apiService = ApiService(client = client)

        val sampleImage = byteArrayOf(1, 2, 3, 4)
        val predResult = apiService.sendImageForPrediction(sampleImage)

        assertTrue("Prediksi harus berhasil, err: ${predResult.exceptionOrNull()?.message}", predResult.isSuccess)
        assertTrue(
            "Request /predict harus dikirim ke activeBaseUrl",
            capturedUrls.any { it.contains("127.0.0.1:8000/predict") }
        )
    }
}
