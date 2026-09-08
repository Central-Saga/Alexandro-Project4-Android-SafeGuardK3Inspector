package com.alexandro.safeguardk3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.alexandro.safeguardk3.ui.components.FloatingBottomDock
import com.alexandro.safeguardk3.ui.navigation.NavItem
import com.alexandro.safeguardk3.ui.screens.*
import com.alexandro.safeguardk3.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SafeGuardK3Theme {
                SafeGuardK3App()
            }
        }
    }
}

/**
 * Root Komponen SafeGuard K3 Inspector dengan konsep visual "Safety Field Log":
 * - Latar utama krem sangat muda (#F7F7F2)
 * - Floating bottom dock mengambang dengan lingkaran aktif amber (#FFF1CE)
 * - Masing-masing layar memiliki header minimal terintegrasi
 */
@Composable
fun SafeGuardK3App() {
    var selectedTab by remember { mutableStateOf<NavItem>(NavItem.Inspection) }
    var selectedHistoryId by remember { mutableStateOf<Long?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Menangani tombol back perangkat saat membuka layar detail riwayat
    if (selectedHistoryId != null) {
        BackHandler {
            selectedHistoryId = null
        }
    }

    Scaffold(
        containerColor = LatarKrem,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Konten Layar Aktif
            val historyId = selectedHistoryId
            if (historyId != null) {
                HistoryDetailScreen(
                    inspectionId = historyId,
                    onBack = { selectedHistoryId = null },
                    onDeleted = { selectedHistoryId = null },
                    snackbarHostState = snackbarHostState
                )
            } else {
                when (selectedTab) {
                    NavItem.Inspection -> InspectionScreen(
                        snackbarHostState = snackbarHostState
                    )
                    NavItem.History -> HistoryScreen(
                        onSelectInspection = { id -> selectedHistoryId = id },
                        snackbarHostState = snackbarHostState
                    )
                    NavItem.Guide -> GuideScreen()
                    NavItem.About -> AboutScreen()
                }
            }

            // Floating Bottom Dock (hanya tampil jika tidak di layar detail riwayat)
            if (selectedHistoryId == null) {
                FloatingBottomDock(
                    currentTab = selectedTab,
                    onTabSelected = { tab ->
                        selectedTab = tab
                        selectedHistoryId = null
                    },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}
