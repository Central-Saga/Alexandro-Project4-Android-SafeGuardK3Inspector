package com.alexandro.safeguardk3.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alexandro.safeguardk3.ui.theme.*

/**
 * Kartu metrik objek terdeteksi untuk grid 3 kolom x 2 baris bergaya Safety Field Log.
 * - Card putih dengan border abu tipis (#E6E7E1)
 * - Ikon outline dengan latar lingkaran lembut
 * - Jumlah deteksi (angka 0 ditampilkan netral abu-abu, bukan merah)
 * - Label singkat dan ringkas tanpa memakan tinggi berlebih
 */
@Composable
fun MetricCard(
    title: String,
    count: Int,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    val isZero = count == 0
    val iconTint = if (isZero) AbuSekunder else color
    val iconBg = if (isZero) Color(0xFFF1F2ED) else color.copy(alpha = 0.12f)
    val countColor = if (isZero) AbuSekunder else NavyK3

    Surface(
        modifier = modifier,
        color = CardPutih,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, BorderAbu)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(30.dp)
                    .background(iconBg, CircleShape)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = count.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = countColor
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = AbuSekunder,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
