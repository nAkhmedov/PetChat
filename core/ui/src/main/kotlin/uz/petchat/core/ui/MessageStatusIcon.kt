package uz.petchat.core.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import uz.petchat.core.ui.theme.PetChatTheme
import uz.petchat.domain.model.MessageStatus

@Composable
fun MessageStatusIcon(
    status: MessageStatus,
    modifier: Modifier = Modifier,
    size: Dp = 16.dp,
    tint: Color = PetChatTheme.colors.outgoingMeta,
) {
    val (icon, description) = when (status) {
        MessageStatus.PENDING -> R.drawable.ic_clock to R.string.core_ui_status_pending
        MessageStatus.SENT -> R.drawable.ic_check to R.string.core_ui_status_sent
        MessageStatus.DELIVERED -> R.drawable.ic_double_check to R.string.core_ui_status_delivered
        MessageStatus.READ -> R.drawable.ic_double_check to R.string.core_ui_status_read
        MessageStatus.FAILED -> R.drawable.ic_error to R.string.core_ui_status_failed
    }
    val iconTint = when (status) {
        MessageStatus.READ -> PetChatTheme.colors.readCheck
        MessageStatus.FAILED -> MaterialTheme.colorScheme.error
        else -> tint
    }
    Icon(
        painter = painterResource(icon),
        contentDescription = stringResource(description),
        tint = iconTint,
        modifier = modifier.size(size),
    )
}
