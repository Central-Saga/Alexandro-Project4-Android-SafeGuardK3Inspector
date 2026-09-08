package com.alexandro.safeguardk3.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 4 item navigasi menu bawah pada SafeGuard K3 Inspector:
 * 1. Inspeksi: Pemeriksaan foto area kerja & deteksi APD
 * 2. Riwayat: Catatan log inspeksi keselamatan tersimpan
 * 3. Panduan: Panduan 01-05 standar APD K3
 * 4. Tentang: Informasi identitas sistem & alur kerja
 */
sealed class NavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Inspection : NavItem(
        route = "inspection",
        title = "Inspeksi",
        icon = Icons.Outlined.Shield
    )

    data object History : NavItem(
        route = "history",
        title = "Riwayat",
        icon = Icons.Outlined.History
    )

    data object Guide : NavItem(
        route = "guide",
        title = "Panduan",
        icon = Icons.AutoMirrored.Outlined.MenuBook
    )

    data object About : NavItem(
        route = "about",
        title = "Tentang",
        icon = Icons.Outlined.Info
    )

    companion object {
        val items: List<NavItem>
            get() = listOf(
                Inspection,
                History,
                Guide,
                About
            )
    }
}
