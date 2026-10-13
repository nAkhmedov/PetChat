package uz.petchat.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class PetChatColors(
    val chatBackground: Color,
    val incomingBubble: Color,
    val outgoingBubble: Color,
    val onIncomingBubble: Color,
    val onOutgoingBubble: Color,
    val incomingMeta: Color,
    val outgoingMeta: Color,
    val inputBackground: Color,
    val online: Color,
    val unreadBadge: Color,
    val mutedBadge: Color,
    val readCheck: Color,
)

private val LightChatColors = PetChatColors(
    chatBackground = Color(0xFFD9E4EC),
    incomingBubble = Color(0xFFFFFFFF),
    outgoingBubble = Color(0xFFE3F8C9),
    onIncomingBubble = Color(0xFF000000),
    onOutgoingBubble = Color(0xFF000000),
    incomingMeta = Color(0xFF8C9AA6),
    outgoingMeta = Color(0xFF5BA145),
    inputBackground = Color(0xFFF1F3F5),
    online = Color(0xFF4DCB5A),
    unreadBadge = Color(0xFF3390EC),
    mutedBadge = Color(0xFFA8B3BC),
    readCheck = Color(0xFF4FAE4E),
)

private val DarkChatColors = PetChatColors(
    chatBackground = Color(0xFF0E1621),
    incomingBubble = Color(0xFF182533),
    outgoingBubble = Color(0xFF2B5278),
    onIncomingBubble = Color(0xFFF5F5F5),
    onOutgoingBubble = Color(0xFFF5F5F5),
    incomingMeta = Color(0xFF6D7F8F),
    outgoingMeta = Color(0xFF7DA8D3),
    inputBackground = Color(0xFF242F3D),
    online = Color(0xFF4DCB5A),
    unreadBadge = Color(0xFF5288C1),
    mutedBadge = Color(0xFF3E546A),
    readCheck = Color(0xFF9CCBF2),
)

private val LightScheme = lightColorScheme(
    primary = Color(0xFF3390EC),
    onPrimary = Color.White,
    surface = Color.White,
    onSurface = Color(0xFF000000),
    onSurfaceVariant = Color(0xFF707579),
    background = Color.White,
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFF5288C1),
    onPrimary = Color.White,
    surface = Color(0xFF17212B),
    onSurface = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF7F91A4),
    background = Color(0xFF17212B),
)

private val LocalPetChatColors = staticCompositionLocalOf { LightChatColors }

@Composable
fun PetChatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val chatColors = if (darkTheme) DarkChatColors else LightChatColors
    CompositionLocalProvider(LocalPetChatColors provides chatColors) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkScheme else LightScheme,
            content = content,
        )
    }
}

object PetChatTheme {
    val colors: PetChatColors
        @Composable
        @ReadOnlyComposable
        get() = LocalPetChatColors.current
}
