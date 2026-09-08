package com.alexandro.safeguardk3.ui.theme

import androidx.compose.ui.graphics.Color

// =======================================================
// SafeGuard K3 Inspector - Safety Field Log Color Palette
// =======================================================

// Warna Inti Tema K3
val NavyK3 = Color(0xFF17324D)             // Teks utama, judul, outline utama, button utama
val SafetyAmber = Color(0xFFE9A826)        // Kuning/amber konstruksi, aksen inspeksi
val SafetyAmberLight = Color(0xFFFFF1CE)   // Lingkaran aktif dock navigasi bawah
val HijauAman = Color(0xFF198754)          // Status APD Lengkap
val HijauAmanLight = Color(0xFFE8F5E9)     // Background badge hijau
val MerahRisiko = Color(0xFFC63B3B)        // Status APD Tidak Lengkap
val MerahRisikoLight = Color(0xFFFDE8E8)   // Background badge merah
val OranyePeriksa = Color(0xFFD97706)      // Status Perlu Pemeriksaan
val OranyePeriksaLight = Color(0xFFFEF3C7) // Background badge oranye
val AbuSekunder = Color(0xFF6B7280)        // Teks sekunder, label netral
val LatarKrem = Color(0xFFF7F7F2)          // Latar utama aplikasi (krem sangat muda)
val CardPutih = Color(0xFFFFFFFF)          // Background card putih
val BorderAbu = Color(0xFFE6E7E1)          // Border abu tipis pada card

// Alias Kompatibilitas
val PrimaryBlue = NavyK3
val DarkBlue = NavyK3
val LightBackground = LatarKrem
val CardBackground = CardPutih
val TextDark = NavyK3
val TextMuted = AbuSekunder

// Status Kepatuhan K3
val StatusGreen = HijauAman
val StatusGreenLight = HijauAmanLight
val StatusRed = MerahRisiko
val StatusRedLight = MerahRisikoLight
val StatusOrange = OranyePeriksa
val StatusOrangeLight = OranyePeriksaLight
val StatusSlate = Color(0xFF4B5563)
val StatusSlateLight = Color(0xFFF3F4F6)

// 6 Kelas Objek YOLOv8 K3
val ColorPerson = NavyK3
val ColorPersonLight = Color(0xFFE7ECF0)

val ColorHardHat = HijauAman
val ColorHardHatLight = HijauAmanLight

val ColorVest = SafetyAmber
val ColorVestLight = SafetyAmberLight

val ColorMask = Color(0xFF0284C7)
val ColorMaskLight = Color(0xFFE0F2FE)

val ColorGloves = Color(0xFF0D9488)
val ColorGlovesLight = Color(0xFFCCFBF1)

val ColorBoots = Color(0xFF7C3AED)
val ColorBootsLight = Color(0xFFEDE9FE)
