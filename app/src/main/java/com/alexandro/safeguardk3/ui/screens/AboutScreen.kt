package com.alexandro.safeguardk3.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Engineering
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alexandro.safeguardk3.ui.theme.*

/**
 * Layar Tentang Aplikasi dengan konsep visual "Safety Field Log":
 * - Logo shield sederhana warna navy dalam lingkaran amber muda
 * - Nama aplikasi: SafeGuard K3 Inspector
 * - Alur 3 item horizontal: Foto Lapangan -> Deteksi APD -> Laporan Inspeksi
 * - Spesifikasi ringkas dan penafian keselamatan kerja
 * - Bersih dari URL backend dan ADB
 */
@Composable
fun AboutScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ==========================================
        // 1. KARTU IDENTITAS SISTEM (LOGO SHIELD)
        // ==========================================
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = CardPutih,
            border = BorderStroke(1.dp, BorderAbu)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo shield sederhana
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(64.dp)
                        .background(SafetyAmberLight, CircleShape)
                        .border(1.dp, BorderAbu, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Shield,
                        contentDescription = "Logo SafeGuard K3",
                        tint = NavyK3,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "SafeGuard K3 Inspector",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyK3
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Versi 2.0.0 (Safety Field Log)",
                    fontSize = 11.sp,
                    color = AbuSekunder
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Klasifikasi Kepatuhan Penggunaan APD Pekerja Berbasis Android Menggunakan YOLOv8",
                    fontSize = 12.sp,
                    color = NavyK3,
                    textAlign = TextAlign.Center,
                    lineHeight = 17.sp
                )
            }
        }

        // ==========================================
        // 2. ALUR TIGA ITEM HORIZONTAL
        // ==========================================
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = CardPutih,
            border = BorderStroke(1.dp, BorderAbu)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ALUR INSPEKSI",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AbuSekunder,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Langkah 1: Foto Lapangan
                    WorkflowStepItem(
                        icon = Icons.Outlined.PhotoCamera,
                        title = "Foto Lapangan",
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = null,
                        tint = AbuSekunder,
                        modifier = Modifier.size(16.dp)
                    )

                    // Langkah 2: Deteksi APD
                    WorkflowStepItem(
                        icon = Icons.Outlined.Engineering,
                        title = "Deteksi APD",
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = null,
                        tint = AbuSekunder,
                        modifier = Modifier.size(16.dp)
                    )

                    // Langkah 3: Laporan Inspeksi
                    WorkflowStepItem(
                        icon = Icons.Outlined.Assignment,
                        title = "Laporan Inspeksi",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // ==========================================
        // 3. SPESIFIKASI TEKNOLOGI
        // ==========================================
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = CardPutih,
            border = BorderStroke(1.dp, BorderAbu)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "SPESIFIKASI SISTEM",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AbuSekunder,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                SpecRow("Arsitektur Mobile", "Jetpack Compose & Material 3")
                SpecRow("Model Penglihatan", "YOLOv8 Object Detection (6 Kelas)")
                SpecRow("Penyimpanan Log", "SQLite Offline Local Storage")
                SpecRow("Metode Evaluasi", "Pencocokan Spasial Helm & Rompi")
            }
        }

        // ==========================================
        // 4. DISCLAIMER RESMI K3
        // ==========================================
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFFF1F2ED),
            border = BorderStroke(1.dp, BorderAbu)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = NavyK3,
                    modifier = Modifier.size(20.dp)
                )

                Column {
                    Text(
                        text = "Penafian (Disclaimer) K3",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyK3
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "SafeGuard K3 Inspector berfungsi sebagai alat bantu pencatatan dan monitoring keselamatan kerja berbasis kecerdasan buatan. Keputusan akhir kepatuhan di lapangan tetap mengacu pada verifikasi inspektur dan pengawas K3.",
                        fontSize = 11.sp,
                        color = AbuSekunder,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // Ruang ekstra bawah agar tidak tertutup floating bottom dock
        Spacer(modifier = Modifier.height(84.dp))
    }
}

@Composable
private fun WorkflowStepItem(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFFF1F2ED), CircleShape)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = NavyK3,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = NavyK3,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp
        )
    }
}

@Composable
private fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, color = AbuSekunder)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = NavyK3)
    }
}
