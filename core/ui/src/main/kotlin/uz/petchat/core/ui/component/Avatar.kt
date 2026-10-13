package uz.petchat.core.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.petchat.core.ui.theme.PetChatTheme

private val AvatarPalette = listOf(
    Color(0xFFE17076),
    Color(0xFFFAA774),
    Color(0xFFA695E7),
    Color(0xFF7BC862),
    Color(0xFF6EC9CB),
    Color(0xFF65AADD),
    Color(0xFFEE7AAE),
)

const val AVATAR_COLOR_COUNT = 7

@Composable
fun Avatar(
    initials: String,
    colorIndex: Int,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    isOnline: Boolean = false,
) {
    val background = AvatarPalette[colorIndex.mod(AvatarPalette.size)]
    val onlineColor = PetChatTheme.colors.online
    val ringColor = MaterialTheme.colorScheme.surface

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .drawWithContent {
                drawCircle(background)
                drawContent()
                if (isOnline) {
                    val dotRadius = this.size.minDimension * 0.12f
                    val center = Offset(
                        x = this.size.width - dotRadius,
                        y = this.size.height - dotRadius,
                    )
                    drawCircle(ringColor, radius = dotRadius + 2.dp.toPx(), center = center)
                    drawCircle(onlineColor, radius = dotRadius, center = center)
                }
            },
    ) {
        Text(
            text = initials,
            color = Color.White,
            fontSize = (size.value * 0.38f).sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
        )
    }
}

@Preview
@Composable
private fun AvatarPreview() {
    PetChatTheme {
        Avatar(initials = "AK", colorIndex = 3, isOnline = true, size = 54.dp)
    }
}
