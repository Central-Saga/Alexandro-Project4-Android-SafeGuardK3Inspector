package com.alexandro.safeguardk3.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.PhotoCamera
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alexandro.safeguardk3.data.local.InspectionRepository
import com.alexandro.safeguardk3.data.model.InspectionRecord
import com.alexandro.safeguardk3.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File

/**
 * Layar Riwayat Inspeksi bergaya "Safety Log":
 * - Header: "Riwayat Inspeksi"
 * - Card laporan ringkas dengan thumbnail kecil kiri
 * - Strip warna status pada sisi kiri card
 * - Di kanan: status, tanggal/waktu, jumlah pekerja
 * - Empty state minimal dengan ikon clipboard dan teks "Belum ada laporan inspeksi."
 */
@Composable
fun HistoryScreen(
    onSelectInspection: (Long) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { InspectionRepository(context) }
    var historyList by remember { mutableStateOf<List<InspectionRecord>>(emptyList()) }
    var isLoadingHistory by remember { mutableStateOf(true) }

    var itemToDelete by remember { mutableStateOf<InspectionRecord?>(null) }
    var showClearAllDialog by remember { mutableStateOf(false) }

    fun refreshHistory() {
        coroutineScope.launch {
            isLoadingHistory = true
            historyList = repository.getAllInspections()
            isLoadingHistory = false
        }
    }

    LaunchedEffect(Unit) {
        refreshHistory()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // ==========================================
        // HEADER RIWAYAT INSPEKSI
        // ==========================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Riwayat Inspeksi",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyK3
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${historyList.size} laporan inspeksi tercatat",
                    fontSize = 11.sp,
                    color = AbuSekunder
                )
            }

            if (historyList.isNotEmpty()) {
                TextButton(onClick = { showClearAllDialog = true }) {
                    Text(
                        text = "Hapus Semua",
                        color = MerahRisiko,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (isLoadingHistory) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = NavyK3, strokeWidth = 2.5.dp)
            }
        } else if (historyList.isEmpty()) {
            // ==========================================
            // EMPTY STATE MINIMAL BERGAYA SAFETY LOG
            // ==========================================
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = CardPutih,
                    border = BorderStroke(1.dp, BorderAbu)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color(0xFFF1F2ED), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Assignment,
                                contentDescription = null,
                                tint = NavyK3.copy(alpha = 0.7f),
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Belum ada laporan inspeksi.",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = NavyK3
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Ambil foto pada menu Inspeksi dan simpan hasil analisis untuk mencatat log kepatuhan APD.",
                            fontSize = 12.sp,
                            color = AbuSekunder,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        } else {
            // ==========================================
            // DAFTAR LOG LAPORAN RINGKAS
            // ==========================================
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 84.dp)
            ) {
                items(historyList, key = { it.id }) { item ->
                    val (statusColor, statusBg) = when (item.classification) {
                        "APD_LENGKAP" -> Pair(HijauAman, HijauAmanLight)
                        "APD_TIDAK_LENGKAP" -> Pair(MerahRisiko, MerahRisikoLight)
                        "PERLU_PEMERIKSAAN" -> Pair(OranyePeriksa, OranyePeriksaLight)
                        else -> Pair(StatusSlate, StatusSlateLight)
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectInspection(item.id) },
                        shape = RoundedCornerShape(14.dp),
                        color = CardPutih,
                        border = BorderStroke(1.dp, BorderAbu)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min)
                        ) {
                            // Strip warna status di sisi kiri
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(5.dp)
                                    .background(statusColor)
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Thumbnail foto
                                val imgFile = File(item.imagePath)
                                if (imgFile.exists()) {
                                    val thumb = BitmapFactory.decodeFile(imgFile.absolutePath)
                                    if (thumb != null) {
                                        Image(
                                            bitmap = thumb.asImageBitmap(),
                                            contentDescription = "Thumbnail",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(60.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .border(1.dp, BorderAbu, RoundedCornerShape(10.dp))
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                    }
                                } else {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFFF1F2ED))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.PhotoCamera,
                                            contentDescription = null,
                                            tint = AbuSekunder,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                }

                                // Bagian Kanan: status, tanggal/waktu, jumlah pekerja
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            color = statusBg,
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))
                                        ) {
                                            Text(
                                                text = item.classificationLabel,
                                                color = statusColor,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = { itemToDelete = item },
                                            modifier = Modifier.size(26.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.DeleteOutline,
                                                contentDescription = "Hapus item",
                                                tint = AbuSekunder,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = item.reason,
                                        fontSize = 11.sp,
                                        color = NavyK3,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Pekerja: ${item.personCount}  |  Helm: ${item.hardHatCount}  |  Rompi: ${item.vestCount}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = AbuSekunder
                                        )
                                        Text(
                                            text = item.formattedTime,
                                            fontSize = 9.sp,
                                            color = AbuSekunder
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog Konfirmasi Hapus 1 Item
    val toDelete = itemToDelete
    if (toDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Hapus Catatan Log?", fontWeight = FontWeight.Bold, color = NavyK3) },
            text = { Text("Hapus laporan inspeksi tanggal ${toDelete.formattedTime}?", color = AbuSekunder) },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            repository.deleteInspection(toDelete.id)
                            itemToDelete = null
                            refreshHistory()
                            snackbarHostState.showSnackbar("Catatan inspeksi dihapus")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MerahRisiko)
                ) {
                    Text("Hapus", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Batal", color = NavyK3)
                }
            }
        )
    }

    // Dialog Konfirmasi Hapus Semua
    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = { Text("Bersihkan Semua Riwayat?", fontWeight = FontWeight.Bold, color = NavyK3) },
            text = { Text("Seluruh riwayat inspeksi dan foto tersimpan akan dihapus permanen dari perangkat.", color = AbuSekunder) },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            repository.clearAllInspections()
                            showClearAllDialog = false
                            refreshHistory()
                            snackbarHostState.showSnackbar("Semua riwayat berhasil dibersihkan")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MerahRisiko)
                ) {
                    Text("Hapus Semua", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text("Batal", color = NavyK3)
                }
            }
        )
    }
}
