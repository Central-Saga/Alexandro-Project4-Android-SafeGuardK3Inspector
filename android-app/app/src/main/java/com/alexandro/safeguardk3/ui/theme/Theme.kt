package com.alexandro.safeguardk3.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = NavyK3,
    onPrimary = Color.White,
    secondary = SafetyAmber,
    onSecondary = NavyK3,
    tertiary = HijauAman,
    background = LatarKrem,
    surface = CardPutih,
    onBackground = NavyK3,
    onSurface = NavyK3,
    outline = BorderAbu,
    surfaceVariant = Color(0xFFF1F2EC)
)

@Composable
fun SafeGuardK3Theme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = LatarKrem.toArgb()
                window.navigationBarColor = LatarKrem.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = true
                controller.isAppearanceLightNavigationBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
