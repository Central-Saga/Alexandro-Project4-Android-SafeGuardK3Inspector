package com.alexandro.safeguardk3.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Engineering
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alexandro.safeguardk3.data.model.WorkerItem
import com.alexandro.safeguardk3.ui.theme.*

/**
 * Komponen baris rincian pekerja bergaya task-list pada Safety Field Log.
 * - Card pendek dan hemat ruang
 * - Nomor identifikasi pekerja
 * - Status Helm dan Rompi dengan ikon centang/silang
 * - Badge status ringkas Lengkap atau Perlu APD
 */
@Composable
fun WorkerCard(
    worker: WorkerItem,
    modifier: Modifier = Modifier
) {
    val isComplete = worker.status == "APD_LENGKAP"

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = CardPutih,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, BorderAbu)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Label Pekerja
                Text(
                    text = "Pekerja #${worker.workerId}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = NavyK3
                )

                // Indikator Helm & Rompi
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status Helm
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Engineering,
                            contentDescription = "Helm",
                            tint = if (worker.hasHardHat) HijauAman else MerahRisiko,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Helm",
                            fontSize = 11.sp,
                            color = if (worker.hasHardHat) NavyK3 else MerahRisiko,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(
                            imageVector = if (worker.hasHardHat) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null,
                            tint = if (worker.hasHardHat) HijauAman else MerahRisiko,
                            modifier = Modifier.size(12.dp)
                        )
                    }

                    // Status Rompi
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Shield,
                            contentDescription = "Rompi",
                            tint = if (worker.hasVest) HijauAman else MerahRisiko,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Rompi",
                            fontSize = 11.sp,
                            color = if (worker.hasVest) NavyK3 else MerahRisiko,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(
                            imageVector = if (worker.hasVest) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null,
                            tint = if (worker.hasVest) HijauAman else MerahRisiko,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                // Badge Ringkas
                Surface(
                    color = if (isComplete) HijauAmanLight else MerahRisikoLight,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isComplete) "Lengkap" else "Perlu APD",
                        color = if (isComplete) HijauAman else MerahRisiko,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            if (worker.reason.isNotBlank() && !isComplete) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = worker.reason,
                    fontSize = 10.sp,
                    color = AbuSekunder
                )
            }
        }
    }
}

/**
 * Chip status APD Tambahan bergaya compact card Safety Field Log.
 * Menampilkan label APD dan teks Terdeteksi atau Tidak terdeteksi.
 */
@Composable
fun ApdStatusChip(
    label: String,
    isDetected: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = CardPutih,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, BorderAbu)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = NavyK3
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (isDetected) "Terdeteksi" else "Tidak terdeteksi",
                fontSize = 10.sp,
                fontWeight = if (isDetected) FontWeight.Bold else FontWeight.Normal,
                color = if (isDetected) HijauAman else AbuSekunder
            )
        }
    }
}
