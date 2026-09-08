package com.alexandro.safeguardk3.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alexandro.safeguardk3.data.model.PredictionResponse
import com.alexandro.safeguardk3.ui.theme.*

/**
 * Komponen hasil inspeksi bergaya Safety Field Log:
 * - Ringkasan temuan dengan garis aksen vertikal kiri sesuai status
 * - Pekerja terperiksa (task-list style, dapat dilipat bila banyak)
 * - APD tambahan (Masker, Sarung Tangan, Sepatu Safety)
 * - Tombol outline navy Simpan Laporan
 */
@Composable
fun ResultCard(
    result: PredictionResponse,
    onSaveToHistory: (() -> Unit)? = null,
    isSaved: Boolean = false,
    modifier: Modifier = Modifier
) {
    val (statusColor, statusBg) = when (result.classification) {
         "APD_LENGKAP" -> Pair(HijauAman, HijauAmanLight)
        "APD_TIDAK_LENGKAP" -> Pair(MerahRisiko, MerahRisikoLight)
        "PERLU_PEMERIKSAAN" -> Pair(OranyePeriksa, OranyePeriksaLight)
        else -> Pair(StatusSlate, StatusSlateLight)
    }

    var isWorkersExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ==========================================
        // 1. RINGKASAN TEMUAN (DENGAN STRIP VERTIKAL)
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

                // Card Laporan Ringkas dengan Garis Aksen Vertikal Kiri
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
                        // Garis aksen vertikal kiri
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
                                text = result.classificationLabel,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = result.reason,
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
        // 2. PEKERJA TERPERIKSA (JIKA ADA)
        // ==========================================
        if (result.workers.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = CardPutih,
                border = BorderStroke(1.dp, BorderAbu)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PEKERJA TERPERIKSA (${result.workers.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AbuSekunder,
                            letterSpacing = 1.sp
                        )

                        if (result.workers.size > 3) {
                            TextButton(
                                onClick = { isWorkersExpanded = !isWorkersExpanded },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = if (isWorkersExpanded) "Tutup" else "Lihat Semua",
                                    fontSize = 11.sp,
                                    color = NavyK3,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Icon(
                                    imageVector = if (isWorkersExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                                    contentDescription = null,
                                    tint = NavyK3,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val displayWorkers = if (isWorkersExpanded || result.workers.size <= 3) {
                        result.workers
                    } else {
                        result.workers.take(3)
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        displayWorkers.forEach { worker ->
                            WorkerCard(worker = worker)
                        }
                    }
                }
            }
        }

        // ==========================================
        // 3. APD TAMBAHAN
        // ==========================================
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = CardPutih,
            border = BorderStroke(1.dp, BorderAbu)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "APD TAMBAHAN",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AbuSekunder,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ApdStatusChip(
                        label = "Masker",
                        isDetected = result.summary.maskCount > 0,
                        modifier = Modifier.weight(1f)
                    )
                    ApdStatusChip(
                        label = "Sarung Tangan",
                        isDetected = result.summary.glovesCount > 0,
                        modifier = Modifier.weight(1f)
                    )
                    ApdStatusChip(
                        label = "Sepatu Safety",
                        isDetected = result.summary.safetyBootsCount > 0,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Kepatuhan inti dievaluasi dari helm dan rompi. APD tambahan ditampilkan dari hasil deteksi model.",
                    fontSize = 11.sp,
                    color = AbuSekunder,
                    lineHeight = 15.sp
                )
            }
        }

        // ==========================================
        // 4. TOMBOL SIMPAN LAPORAN (OUTLINE NAVY)
        // ==========================================
        if (onSaveToHistory != null) {
            OutlinedButton(
                onClick = onSaveToHistory,
                enabled = !isSaved,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, if (isSaved) BorderAbu else NavyK3),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (isSaved) AbuSekunder else NavyK3
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(
                    imageVector = if (isSaved) Icons.Default.Check else Icons.Outlined.BookmarkBorder,
                    contentDescription = null,
                    tint = if (isSaved) HijauAman else NavyK3,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSaved) "Laporan Tersimpan" else "Simpan Laporan",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
