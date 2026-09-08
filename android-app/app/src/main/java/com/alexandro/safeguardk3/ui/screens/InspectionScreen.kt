package com.alexandro.safeguardk3.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.alexandro.safeguardk3.data.api.ApiService
import com.alexandro.safeguardk3.data.local.InspectionRepository
import com.alexandro.safeguardk3.data.model.InspectionRecord
import com.alexandro.safeguardk3.data.model.PredictionResponse
import com.alexandro.safeguardk3.ui.components.BoundingBoxOverlay
import com.alexandro.safeguardk3.ui.components.MetricCard
import com.alexandro.safeguardk3.ui.components.ResultCard
import com.alexandro.safeguardk3.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Layar Inspeksi K3 dengan konsep visual "Safety Field Log":
 * - Header minimal: Logo shield outline navy, SafeGuard, INSPEKSI APD, status dot, tombol refresh
 * - Kartu status inspeksi lapangan
 * - Bukti visual: rasio 4:3, sudut 20dp, line-art helm saat kosong, overlay label saat tersedia
 * - Tombol aksi foto: outline navy compact (Kamera & Galeri atau Ganti Foto & Hapus)
 * - Tombol analisis: lebar navy gelap "Mulai Inspeksi" dengan ikon amber
 * - Statistik objek: grid 3 kolom x 2 baris (nilai 0 netral)
 * - Ringkasan temuan, pekerja terperiksa, APD tambahan, dan tombol simpan laporan
 */
@Composable
fun InspectionScreen(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val apiService = remember { ApiService() }
    val repository = remember { InspectionRepository(context) }

    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var annotatedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var tempCameraFile by remember { mutableStateOf<File?>(null) }

    var isLoading by remember { mutableStateOf(false) }
    var serverOnline by remember { mutableStateOf<Boolean?>(null) }
    var inspectionResult by remember { mutableStateOf<PredictionResponse?>(null) }
    var isSavedToHistory by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showChangePhotoDialog by remember { mutableStateOf(false) }

    // Launcher Kamera
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraFile != null && tempCameraFile!!.exists()) {
            val bmp = BitmapFactory.decodeFile(tempCameraFile!!.absolutePath)
            if (bmp != null) {
                originalBitmap = bmp
                annotatedBitmap = null
                inspectionResult = null
                isSavedToHistory = false
                errorMessage = null
            }
        }
    }

    // Launcher Galeri Modern
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bmp = BitmapFactory.decodeStream(inputStream)
                if (bmp != null) {
                    originalBitmap = bmp
                    annotatedBitmap = null
                    inspectionResult = null
                    isSavedToHistory = false
                    errorMessage = null
                }
            } catch (e: Exception) {
                Log.e("SafeGuardK3", "Gagal memuat galeri: ${e.message}", e)
                errorMessage = "Gagal memuat gambar dari galeri perangkat."
            }
        }
    }

    fun launchCamera() {
        try {
            val storageDir = File(context.cacheDir, "inspection_photos")
            if (!storageDir.exists()) storageDir.mkdirs()
            val file = File.createTempFile("INSP_", ".jpg", storageDir)
            tempCameraFile = file
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            Log.e("SafeGuardK3", "Gagal membuka kamera: ${e.message}", e)
            errorMessage = "Gagal menginisialisasi kamera perangkat."
        }
    }

    fun launchGallery() {
        photoPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    // Fungsi cek kesehatan server
    fun verifyServerHealth() {
        coroutineScope.launch {
            val res = apiService.checkHealth()
            res.onSuccess {
                serverOnline = true
            }.onFailure {
                serverOnline = false
            }
        }
    }

    LaunchedEffect(Unit) {
        verifyServerHealth()
    }

    // Helper kompresi dan downscale foto
    suspend fun compressAndScaleBitmap(src: Bitmap): ByteArray = withContext(Dispatchers.IO) {
        val maxDimension = 1024
        val width = src.width
        val height = src.height
        val scaled = if (width > maxDimension || height > maxDimension) {
            val ratio = width.toFloat() / height.toFloat()
            val newWidth: Int
            val newHeight: Int
            if (ratio > 1) {
                newWidth = maxDimension
                newHeight = (maxDimension / ratio).toInt()
            } else {
                newHeight = maxDimension
                newWidth = (maxDimension * ratio).toInt()
            }
            Bitmap.createScaledBitmap(src, newWidth, newHeight, true)
        } else {
            src
        }

        val stream = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 85, stream)
        stream.toByteArray()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // =================================================================
        // 1. HEADER MINIMAL
        // =================================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sisi Kiri: Ikon shield outline navy + Teks SafeGuard + INSPEKSI APD
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = SafetyAmberLight.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, BorderAbu),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Shield,
                            contentDescription = "SafeGuard K3",
                            tint = NavyK3,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = "SafeGuard",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyK3
                    )
                    Text(
                        text = "INSPEKSI APD",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AbuSekunder,
                        letterSpacing = 1.2.sp
                    )
                }
            }

            // Sisi Kanan: Indikator bulat status sistem + Tombol refresh icon saja
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val statusDotColor = when (serverOnline) {
                    true -> HijauAman
                    false -> MerahRisiko
                    null -> AbuSekunder
                }

                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(statusDotColor, CircleShape)
                )

                IconButton(
                    onClick = { verifyServerHealth() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = "Refresh Status",
                        tint = NavyK3,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // =================================================================
        // 2. KARTU STATUS INSPEKSI HARI INI
        // =================================================================
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = CardPutih,
            border = BorderStroke(1.dp, BorderAbu)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFF1F2ED), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Assignment,
                            contentDescription = null,
                            tint = NavyK3,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Inspeksi Lapangan",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyK3
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Siap memeriksa kondisi APD pekerja",
                            fontSize = 11.sp,
                            color = AbuSekunder
                        )
                    }
                }

                val (badgeText, badgeColor, badgeBg) = when (serverOnline) {
                    true -> Triple("Sistem siap", HijauAman, HijauAmanLight)
                    false -> Triple("Sistem offline", MerahRisiko, MerahRisikoLight)
                    null -> Triple("Memeriksa...", AbuSekunder, Color(0xFFF1F2ED))
                }

                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = badgeText,
                        color = badgeColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // =================================================================
        // 3. AREA INPUT FOTO (BUKTI VISUAL)
        // =================================================================
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "BUKTI VISUAL",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = AbuSekunder,
                letterSpacing = 1.sp
            )

            val displayBitmap = annotatedBitmap ?: originalBitmap

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(CardPutih)
                    .border(1.dp, BorderAbu, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (displayBitmap != null) {
                    Image(
                        bitmap = displayBitmap.asImageBitmap(),
                        contentDescription = "Foto Bukti Visual",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )

                    Surface(
                        color = CardPutih.copy(alpha = 0.92f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, BorderAbu),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "FOTO INSPEKSI",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyK3,
                            letterSpacing = 0.8.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color(0xFFF1F2ED), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Engineering,
                                contentDescription = null,
                                tint = NavyK3.copy(alpha = 0.7f),
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Tambahkan foto area kerja",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = NavyK3
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Gunakan kamera atau pilih foto dari galeri",
                            fontSize = 11.sp,
                            color = AbuSekunder,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // =================================================================
        // 4. TOMBOL TINDAKAN FOTO (OUTLINE NAVY TIPIS)
        // =================================================================
        if (originalBitmap == null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { launchCamera() },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, NavyK3),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyK3),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PhotoCamera,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Kamera",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                OutlinedButton(
                    onClick = { launchGallery() },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, NavyK3),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyK3),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Galeri",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { showChangePhotoDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, NavyK3),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyK3),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Sync,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Ganti Foto",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                OutlinedButton(
                    onClick = {
                        originalBitmap = null
                        annotatedBitmap = null
                        inspectionResult = null
                        isSavedToHistory = false
                        errorMessage = null
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MerahRisiko),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MerahRisiko),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Hapus",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // =================================================================
        // 5. TOMBOL ANALISIS (LEBAR NAVY GELAP DENGAN IKON AMBER)
        // =================================================================
        Button(
            onClick = {
                val bmp = originalBitmap
                if (bmp != null) {
                    coroutineScope.launch {
                        isLoading = true
                        errorMessage = null
                        try {
                            val imageBytes = compressAndScaleBitmap(bmp)
                            val res = apiService.predict(imageBytes)
                            res.onSuccess { response ->
                                inspectionResult = response
                                isSavedToHistory = false
                                annotatedBitmap = BoundingBoxOverlay.draw(bmp, response.detections)
                            }.onFailure { err ->
                                errorMessage = "Gagal memproses inspeksi. Pastikan sistem siap dan coba kembali."
                                Log.e("SafeGuardK3", "Analisis error: ${err.message}", err)
                            }
                        } catch (e: Exception) {
                            errorMessage = "Terjadi kendala saat memproses gambar."
                            Log.e("SafeGuardK3", "Exception prediksi: ${e.message}", e)
                        } finally {
                            isLoading = false
                        }
                    }
                } else {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Silakan tambahkan foto area kerja terlebih dahulu.")
                    }
                }
            },
            enabled = originalBitmap != null && !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = NavyK3,
                disabledContainerColor = NavyK3.copy(alpha = 0.35f)
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = SafetyAmber,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Menganalisis APD pada foto…",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = SafetyAmber,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Mulai Inspeksi",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }

        // Pesan Error jika ada
        if (errorMessage != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MerahRisikoLight,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MerahRisiko.copy(alpha = 0.3f))
            ) {
                Text(
                    text = errorMessage!!,
                    color = MerahRisiko,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // =================================================================
        // HASIL SETELAH ANALISIS SELESAI
        // =================================================================
        val result = inspectionResult
        if (result != null) {
            // =============================================================
            // 7. STATISTIK OBJEK (GRID 3 KOLOM X 2 BARIS)
            // =============================================================
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "OBJEK TERDETEKSI",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AbuSekunder,
                    letterSpacing = 1.sp
                )

                // Baris 1: Pekerja, Helm, Rompi
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricCard(
                        title = "Pekerja",
                        count = result.summary.personCount,
                        icon = Icons.Outlined.Person,
                        color = ColorPerson,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Helm",
                        count = result.summary.hardHatCount,
                        icon = Icons.Outlined.Engineering,
                        color = ColorHardHat,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Rompi",
                        count = result.summary.vestCount,
                        icon = Icons.Outlined.Shield,
                        color = ColorVest,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Baris 2: Masker, Sarung Tangan, Sepatu Safety
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricCard(
                        title = "Masker",
                        count = result.summary.maskCount,
                        icon = Icons.Outlined.Masks,
                        color = ColorMask,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Sarung Tangan",
                        count = result.summary.glovesCount,
                        icon = Icons.Outlined.PanTool,
                        color = ColorGloves,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Sepatu Safety",
                        count = result.summary.safetyBootsCount,
                        icon = Icons.Outlined.Hiking,
                        color = ColorBoots,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // =============================================================
            // 6, 8, 9, 10. RINGKASAN TEMUAN, RINCIAN PEKERJA, APD TAMBAHAN, TOMBOL SIMPAN
            // =============================================================
            ResultCard(
                result = result,
                isSaved = isSavedToHistory,
                onSaveToHistory = {
                    coroutineScope.launch {
                        try {
                            val bitmapToSave = annotatedBitmap ?: originalBitmap
                            if (bitmapToSave != null) {
                                val savedImagePath = repository.saveInspectionImage(bitmapToSave)
                                val timeFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                                val formattedTime = timeFormat.format(Date())

                                val record = InspectionRecord(
                                    timestamp = System.currentTimeMillis(),
                                    formattedTime = formattedTime,
                                    imagePath = savedImagePath,
                                    classification = result.classification,
                                    classificationLabel = result.classificationLabel,
                                    reason = result.reason,
                                    personCount = result.summary.personCount,
                                    hardHatCount = result.summary.hardHatCount,
                                    vestCount = result.summary.vestCount,
                                    workersJson = InspectionRecord.workersToJson(result.workers)
                                )

                                repository.insertInspection(record)
                                isSavedToHistory = true
                                snackbarHostState.showSnackbar("Laporan inspeksi berhasil disimpan ke riwayat")
                            }
                        } catch (e: Exception) {
                            Log.e("SafeGuardK3", "Gagal menyimpan riwayat: ${e.message}", e)
                            snackbarHostState.showSnackbar("Gagal menyimpan ke riwayat.")
                        }
                    }
                }
            )
        }

        // Catatan Penafian K3 Ringkas
        Text(
            text = "Catatan: Hasil analisis otomatis berbasis AI. Verifikasi fisik tetap menjadi tanggung jawab pengawas keselamatan kerja.",
            fontSize = 11.sp,
            color = AbuSekunder,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        )

        // Ruang ekstra di bawah agar tidak tertutup floating bottom dock
        Spacer(modifier = Modifier.height(84.dp))
    }

    // Dialog Pilihan Ganti Foto
    if (showChangePhotoDialog) {
        AlertDialog(
            onDismissRequest = { showChangePhotoDialog = false },
            title = {
                Text(
                    text = "Ganti Foto Inspeksi",
                    fontWeight = FontWeight.Bold,
                    color = NavyK3
                )
            },
            text = {
                Text(
                    text = "Pilih sumber foto baru untuk area kerja.",
                    color = AbuSekunder
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showChangePhotoDialog = false
                    launchGallery()
                }) {
                    Text("Buka Galeri", color = NavyK3, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showChangePhotoDialog = false
                    launchCamera()
                }) {
                    Text("Buka Kamera", color = NavyK3, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
