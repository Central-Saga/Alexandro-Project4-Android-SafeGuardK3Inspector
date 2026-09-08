package com.alexandro.safeguardk3.ui.screens

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Engineering
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Shield
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
import com.alexandro.safeguardk3.data.local.InspectionRepository
import com.alexandro.safeguardk3.data.model.InspectionRecord
import com.alexandro.safeguardk3.ui.components.MetricCard
import com.alexandro.safeguardk3.ui.components.WorkerCard
import com.alexandro.safeguardk3.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File

/**
 * Layar Detail Riwayat Inspeksi bergaya "Safety Field Log":
 * - Header minimal dengan tombol kembali dan hapus
 * - Preview foto 4:3 tersimpan di penyimpanan internal
 * - Card laporan ringkas dengan garis aksen vertikal kiri
 * - Grid metrik objek inti
 * - Rincian individual setiap pekerja
 */
@Composable
fun HistoryDetailScreen(
    inspectionId: Long,
    onBack: () -> Unit,
    onDeleted: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { InspectionRepository(context) }

    var record by remember { mutableStateOf<InspectionRecord?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(inspectionId) {
        isLoading = true
        record = repository.getInspectionById(inspectionId)
        isLoading = false
    }

    val currentRecord = record

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ==========================================
        // HEADER DETAIL DENGAN TOMBOL KEMBALI
        // ==========================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Kembali",
                        tint = NavyK3,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Detail Inspeksi",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyK3
                    )
                    Text(
                        text = currentRecord?.formattedTime ?: "",
                        fontSize = 11.sp,
                        color = AbuSekunder
                    )
                }
            }

            IconButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = "Hapus",
                    tint = MerahRisiko,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NavyK3, strokeWidth = 2.5.dp)
            }
        } else if (currentRecord != null) {
            val (statusColor, statusBg) = when (currentRecord.classification) {
                "APD_LENGKAP" -> Pair(HijauAman, HijauAmanLight)
                "APD_TIDAK_LENGKAP" -> Pair(MerahRisiko, MerahRisikoLight)
                "PERLU_PEMERIKSAAN" -> Pair(OranyePeriksa, OranyePeriksaLight)
                else -> Pair(StatusSlate, StatusSlateLight)
            }

            // ==========================================
            // PREVIEW FOTO AREA KERJA (4:3)
            // ==========================================
            val imgFile = File(currentRecord.imagePath)
            if (imgFile.exists()) {
                val bmp = BitmapFactory.decodeFile(imgFile.absolutePath)
                if (bmp != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(4f / 3f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(CardPutih)
                            .border(1.dp, BorderAbu, RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "Foto Arsip Inspeksi",
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
                                text = "DOKUMEN LOG",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyK3,
                                letterSpacing = 0.8.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // ==========================================
            // CARD STATUS LAPORAN (STRIP VERTIKAL KIRI)
            // ==========================================
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = CardPutih,
                border = BorderStroke(1.dp, BorderAbu)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "RINGKASAN TEMUAN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AbuSekunder,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = statusBg.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(5.dp)
                                    .background(statusColor)
                            )

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 12.dp, horizontal = 14.dp)
                            ) {
                                Text(
                                    text = "STATUS INSPEKSI",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AbuSekunder,
                                    letterSpacing = 0.8.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = currentRecord.classificationLabel,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = currentRecord.reason,
                                    fontSize = 12.sp,
                                    color = NavyK3,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // GRID OBJEK TERDETEKSI INTI (3 KOLOM)
            // ==========================================
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricCard(
                        title = "Pekerja",
                        count = currentRecord.personCount,
                        icon = Icons.Outlined.Person,
                        color = ColorPerson,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Helm",
                        count = currentRecord.hardHatCount,
                        icon = Icons.Outlined.Engineering,
                        color = ColorHardHat,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Rompi",
                        count = currentRecord.vestCount,
                        icon = Icons.Outlined.Shield,
                        color = ColorVest,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ==========================================
            // RINCIAN PEKERJA (JIKA ADA DATA TERSIMPAN)
            // ==========================================
            val workers = remember(currentRecord.workersJson) {
                currentRecord.getWorkers()
            }

            if (workers.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = CardPutih,
                    border = BorderStroke(1.dp, BorderAbu)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "PEKERJA TERPERIKSA (${workers.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AbuSekunder,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            workers.forEach { worker ->
                                WorkerCard(worker = worker)
                            }
                        }
                    }
                }
            }

            // Catatan Penafian K3 Ringkas
            Text(
                text = "Catatan: Dokumen log disimpan secara lokal di perangkat. Verifikasi final dilakukan oleh petugas K3.",
                fontSize = 11.sp,
                color = AbuSekunder,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            )

            Spacer(modifier = Modifier.height(84.dp))
        }
    }

    // Dialog Konfirmasi Hapus
    if (showDeleteDialog && currentRecord != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus Catatan Log?", fontWeight = FontWeight.Bold, color = NavyK3) },
            text = { Text("Apakah Anda yakin ingin menghapus catatan inspeksi ini secara permanen?", color = AbuSekunder) },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            repository.deleteInspection(currentRecord.id)
                            showDeleteDialog = false
                            onDeleted()
                            snackbarHostState.showSnackbar("Catatan inspeksi berhasil dihapus")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MerahRisiko)
                ) {
                    Text("Hapus", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal", color = NavyK3)
                }
            }
        )
    }
}
