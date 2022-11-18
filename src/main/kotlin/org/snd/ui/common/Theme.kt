package org.snd.ui.common

import androidx.compose.material.darkColors
import androidx.compose.ui.graphics.Color

object AppTheme {
    val colors: Colors = Colors()

    class Colors(
        val backgroundDark: Color = Color(0xFF2B2B2B),
        val backgroundDarker: Color = Color(0XFF313335),
        val backgroundMedium: Color = Color(0xFF3C3F41),
        val backgroundLight: Color = Color(0xFF4E5254),
        val backgroundLighter: Color = Color(0xFF717476),
        val highlight: Color = Color(0XFFf2efd0),
        val buttonActive: Color = Color(0XFF34EB8F),
        val announcementMessageColor: Color = Color(0XFFF7A52A),
        val green: Color = Color(0XFF4FD172),

        val guest: Color = Color(0XFF888888),
        val regularUser: Color = Color(0XFFC8C8C8),
        val moderator: Color = Color(0XFF00AA00),
        val channelAdmin: Color = Color(0XFFFF9900),
        val siteAdmin: Color = Color(0XFFCC0000),

        val material: androidx.compose.material.Colors = darkColors(
            background = backgroundDark,
            surface = backgroundMedium,
            primary = Color.White
        ),
    )
}