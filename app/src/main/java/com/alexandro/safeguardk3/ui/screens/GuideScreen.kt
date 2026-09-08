package com.alexandro.safeguardk3.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alexandro.safeguardk3.ui.theme.*

/**
 * Layar Panduan APD bergaya checklist minimal "Safety Field Log":
 * - Judul "Panduan APD"
 * - Urutan 01–05: Helm Pengaman, Rompi Safety, Masker, Sarung Tangan, Sepatu Safety
 * - Setiap item berbentuk card kecil yang bisa dibuka (expandable)
 * - Warna nomor memakai amber (#E9A826), teks memakai navy (#17324D)
 * - Bersih dari informasi backend teknis
 */
@Composable
fun GuideScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ==========================================
        // HEADER PANDUAN APD
        // ==========================================
        Column(modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)) {
            Text(
                text = "Panduan APD",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = NavyK3
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Standar keselamatan Alat Pelindung Diri pada area kerja konstruksi dan industri",
                fontSize = 11.sp,
                color = AbuSekunder
            )
        }

        // ==========================================
        // CARD CAKUPAN DETEKSI MODEL (6 KELAS)
        // ==========================================
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = CardPutih,
            border = BorderStroke(1.dp, BorderAbu)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(34.dp)
                        .background(SafetyAmberLight, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Shield,
                        contentDescription = null,
                        tint = NavyK3,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Cakupan Deteksi YOLOv8 (6 Kelas)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyK3
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Sistem mengidentifikasi 6 kelas objek: Pekerja, Helm Pengaman, Rompi Safety, Masker, Sarung Tangan, dan Sepatu Safety. Evaluasi kepatuhan inti wajib didasarkan pada helm dan rompi secara spasial per pekerja.",
                        fontSize = 11.sp,
                        color = AbuSekunder,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // ==========================================
        // CHECKLIST 01 - 05 EXPANDABLE CARDS
        // ==========================================
        Text(
            text = "DAFTAR STANDAR APD",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = AbuSekunder,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(top = 4.dp)
        )

        // 01: Helm Pengaman
        ExpandableGuideCard(
            number = "01",
            title = "Helm Pengaman",
            subtitle = "Standar SNI / ANSI Z89.1",
            icon = Icons.Outlined.Engineering,
            initiallyExpanded = true,
            details = listOf(
                "Melindungi kepala dari benturan, material jatuh, dan bahaya sengatan listrik.",
                "Wajib terpasang dengan tali dagu (chinstrap) terkunci rapat saat di area proyek.",
                "Dipetakan pada area kepala (35% bagian atas postur tubuh pekerja)."
            )
        )

        // 02: Rompi Safety
        ExpandableGuideCard(
            number = "02",
            title = "Rompi Safety",
            subtitle = "Standar ANSI/ISEA 107",
            icon = Icons.Outlined.Shield,
            initiallyExpanded = true,
            details = listOf(
                "Memastikan visibilitas tinggi pekerja baik di siang hari maupun area minim penerangan.",
                "Menggunakan bahan warna fluoresen cerah dengan pita pemantul cahaya (retroreflective).",
                "Dipetakan pada area torso (20% - 75% tinggi tubuh pekerja)."
            )
        )

        // 03: Masker
        ExpandableGuideCard(
            number = "03",
            title = "Masker",
            subtitle = "Standar SNI / NIOSH N95",
            icon = Icons.Outlined.Masks,
            initiallyExpanded = false,
            details = listOf(
                "Melindungi saluran pernapasan dari partikel debu semen, serbuk kayu, dan aerosol kimia.",
                "Wajib menutupi hidung dan mulut secara rapat tanpa celah.",
                "Direkomendasikan di area berdebu, pengelasan, atau pencampuran material."
            )
        )

        // 04: Sarung Tangan
        ExpandableGuideCard(
            number = "04",
            title = "Sarung Tangan",
            subtitle = "Standar EN 388",
            icon = Icons.Outlined.PanTool,
            initiallyExpanded = false,
            details = listOf(
                "Melindungi tangan dari bahaya abrasi, sayatan tajam, tusukan, atau paparan panas.",
                "Dipilih sesuai tipe pekerjaan (kulit untuk konstruksi berat, nitril untuk cairan/kimia).",
                "Meningkatkan daya cengkeram alat kerja agar tidak mudah tergelincir."
            )
        )

        // 05: Sepatu Safety
        ExpandableGuideCard(
            number = "05",
            title = "Sepatu Safety",
            subtitle = "Standar SNI 7079 / ASTM F2413",
            icon = Icons.Outlined.Hiking,
            initiallyExpanded = false,
            details = listOf(
                "Dilengkapi pelindung jari berbahan baja (steel toe) untuk menahan kejatuhan benda berat.",
                "Sol bawah dilengkapi pelat anti-paku dan tekstur anti-selip pada permukaan licin/basah.",
                "Memberikan isolasi aman dari kebocoran arus listrik statis di lantai kerja."
            )
        )

        // Ruang ekstra bawah agar tidak tertutup floating bottom dock
        Spacer(modifier = Modifier.height(84.dp))
    }
}

/**
 * Komponen Card Panduan APD yang dapat dibuka/tutup:
 * - Nomor urut berwarna Safety Amber (#E9A826)
 * - Judul dalam warna Navy K3 (#17324D)
 * - Ikon outline dan chevron expand/collapse
 */
@Composable
private fun ExpandableGuideCard(
    number: String,
    title: String,
    subtitle: String,
    icon: ImageVector,
    details: List<String>,
    initiallyExpanded: Boolean = false
) {
    var isExpanded by remember { mutableStateOf(initiallyExpanded) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(14.dp),
        color = CardPutih,
        border = BorderStroke(1.dp, BorderAbu)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Nomor Urut Amber
                    Text(
                        text = number,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SafetyAmber
                    )

                    // Ikon Outline
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFFF1F2ED), CircleShape)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = NavyK3,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Column {
                        Text(
                            text = title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyK3
                        )
                        Text(
                            text = subtitle,
                            fontSize = 10.sp,
                            color = AbuSekunder
                        )
                    }
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = null,
                    tint = NavyK3,
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 12.dp, start = 36.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    details.forEach { detail ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 6.dp)
                                    .size(5.dp)
                                    .background(SafetyAmber, CircleShape)
                            )
                            Text(
                                text = detail,
                                fontSize = 11.sp,
                                color = NavyK3,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
