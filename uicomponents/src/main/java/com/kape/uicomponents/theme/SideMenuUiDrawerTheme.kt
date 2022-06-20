package com.kape.uicomponents.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

//<editor-fold desc="# Public: Side menu theme #">
@Immutable
object CommonSideMenuColors {
    val grey20: Color = Color(color = 0xFF_323642)
    val grey92: Color = Color(color = 0xFF_EEEEEE)

    val pia_text_dark_87_percent: Color = Color(color = 0xDE_001B31)
    val pia_text_light_white_87_percent: Color = Color(color = 0xDE_FFFFFF)

    val greendark20 = Color(color = 0xFF_4CB649)

    val md_light_dividers: Color = Color(color = 0x1F_000000)
    val md_dark_dividers: Color = Color(color = 0x1F_FFFFFF)

    val connecting_orange: Color = Color(color = 0xFF_FFA500)
}

@Immutable
data class SideMenuColors(
        val sideMenuBackground: Color,

        val grey20_white: Color,

        @Suppress("SpellCheckingInspection") val pia_sidemenu_divider: Color,

        val textColorPrimary: Color,

        val textColorPrimaryDark: Color,

        val sideMenuItemRippleColor: Color
)

@Immutable
class PiaTextStyle(
        val textSize: TextUnit,
        val fontFamily: FontFamily,
        val textStyle: TextStyle,
        val textColor: Color
)

interface ISideMenuUiTheme {
    val isLightTheme: Boolean

    val colors: SideMenuColors
        @Composable
        @ReadOnlyComposable
        get

    @Suppress(names = ["PropertyName"])
    val PiaTextTitle: PiaTextStyle
        @Composable
        @ReadOnlyComposable
        get

    @Suppress(names = ["PropertyName"])
    val PiaTextBody2: PiaTextStyle
        @Composable
        @ReadOnlyComposable
        get

    @Suppress(names = ["PropertyName"])
    val PiaExpirationTitle: PiaTextStyle
        @Composable
        @ReadOnlyComposable
        get

    @Suppress(names = ["PropertyName"])
    val PiaExpirationDescription: PiaTextStyle
        @Composable
        @ReadOnlyComposable
        get
}

object SideMenuRippleTheme : RippleTheme {
    @Composable
    override fun defaultColor(): Color = SideMenuUiTheme.colors.sideMenuItemRippleColor

    @Composable
    override fun rippleAlpha(): RippleAlpha = RippleAlpha(
            pressedAlpha = 1f,
            focusedAlpha = 1f,
            draggedAlpha = 1f,
            hoveredAlpha = 0f
    )
}

val SideMenuUiTheme: ISideMenuUiTheme
    @Composable
    @ReadOnlyComposable
    get() = if (MaterialTheme.colors.isLight) SideMenuUiThemeLight else SideMenuUiThemeDark
//</editor-fold>

//<editor-fold desc="# Private: Theme api #">
private object SideMenuUiThemeLight : ISideMenuUiTheme {
    override val isLightTheme: Boolean = true

    override val colors: SideMenuColors
        @Composable
        @ReadOnlyComposable
        get() = SideMenuColors(
                sideMenuBackground = CommonSideMenuColors.grey92,
                grey20_white = CommonSideMenuColors.grey20,
                pia_sidemenu_divider = CommonSideMenuColors.md_light_dividers,
                textColorPrimary = CommonSideMenuColors.grey20,
                textColorPrimaryDark = CommonSideMenuColors.pia_text_light_white_87_percent,
                sideMenuItemRippleColor = CommonSideMenuColors.greendark20
        )

    override val PiaTextTitle: PiaTextStyle
        @Composable
        @ReadOnlyComposable
        get() = PiaTextStyle(
                textSize = 18.sp,
                fontFamily = FontFamily.SansSerif,
                textStyle = TextStyle(),
                textColor = CommonSideMenuColors.pia_text_dark_87_percent
        )

    override val PiaTextBody2: PiaTextStyle
        @Composable
        @ReadOnlyComposable
        get() = PiaTextStyle(
                textSize = 14.sp,
                fontFamily = FontFamily.Default,
                textStyle = TextStyle(fontWeight = FontWeight.Bold),
                textColor = CommonSideMenuColors.pia_text_dark_87_percent
        )

    @Suppress(names = ["PropertyName"])
    override val PiaExpirationTitle: PiaTextStyle
        @Composable
        @ReadOnlyComposable
        get() = PiaTextStyle(
                textSize = 15.sp,
                fontFamily = FontFamily.Default,
                textStyle = TextStyle(fontWeight = FontWeight.Bold),
                textColor = colors.textColorPrimary
        )

    @Suppress(names = ["PropertyName"])
    override val PiaExpirationDescription: PiaTextStyle
        @Composable
        @ReadOnlyComposable
        get() = PiaTextStyle(
                textSize = 14.sp,
                fontFamily = FontFamily.Default,
                textStyle = TextStyle(fontWeight = FontWeight.Normal),
                textColor = colors.textColorPrimaryDark
        )
}

private object SideMenuUiThemeDark : ISideMenuUiTheme {
    override val isLightTheme: Boolean = false

    override val colors: SideMenuColors
        @Composable
        @ReadOnlyComposable
        get() = SideMenuColors(
                sideMenuBackground = Color(value = 0xFF_323642UL),
                grey20_white = Color.White,
                pia_sidemenu_divider = CommonSideMenuColors.md_dark_dividers,
                textColorPrimary = Color.White,
                textColorPrimaryDark = CommonSideMenuColors.pia_text_light_white_87_percent,
                sideMenuItemRippleColor = CommonSideMenuColors.greendark20
        )

    override val PiaTextTitle: PiaTextStyle
        @Composable
        @ReadOnlyComposable
        get() = PiaTextStyle(
                textSize = 18.sp,
                fontFamily = FontFamily.SansSerif,
                textStyle = TextStyle(),
                textColor = CommonSideMenuColors.pia_text_light_white_87_percent
        )

    override val PiaTextBody2: PiaTextStyle
        @Composable
        @ReadOnlyComposable
        get() = PiaTextStyle(
                textSize = 14.sp,
                fontFamily = FontFamily.Default,
                textStyle = TextStyle(fontWeight = FontWeight.Bold),
                textColor = CommonSideMenuColors.pia_text_light_white_87_percent
        )

    @Suppress(names = ["PropertyName"])
    override val PiaExpirationTitle: PiaTextStyle
        @Composable
        @ReadOnlyComposable
        get() = PiaTextStyle(
                textSize = 15.sp,
                fontFamily = FontFamily.Default,
                textStyle = TextStyle(fontWeight = FontWeight.Bold),
                textColor = colors.textColorPrimary
        )

    @Suppress(names = ["PropertyName"])
    override val PiaExpirationDescription: PiaTextStyle
        @Composable
        @ReadOnlyComposable
        get() = PiaTextStyle(
                textSize = 14.sp,
                fontFamily = FontFamily.Default,
                textStyle = TextStyle(fontWeight = FontWeight.Normal),
                textColor = colors.textColorPrimaryDark
        )
}
//</editor-fold>
