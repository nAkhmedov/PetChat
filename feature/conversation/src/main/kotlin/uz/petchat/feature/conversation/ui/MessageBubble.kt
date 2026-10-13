package uz.petchat.feature.conversation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uz.petchat.core.ui.time.RelativeDay
import uz.petchat.core.ui.theme.PetChatTheme
import uz.petchat.core.ui.MessageStatusIcon
import uz.petchat.domain.model.MessageStatus
import uz.petchat.feature.conversation.DateSeparatorUi
import uz.petchat.feature.conversation.MessageUi
import uz.petchat.feature.conversation.R

private val IncomingTailShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp)
private val OutgoingTailShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 4.dp, bottomStart = 16.dp)
private val PlainShape = RoundedCornerShape(16.dp)
private val SeparatorShape = RoundedCornerShape(percent = 50)

@Composable
internal fun MessageBubble(
    message: MessageUi,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = PetChatTheme.colors
    val shape = when {
        !message.showTail -> PlainShape
        message.isOutgoing -> OutgoingTailShape
        else -> IncomingTailShape
    }
    val isFailed = message.status == MessageStatus.FAILED

    Row(
        horizontalArrangement = if (message.isOutgoing) Arrangement.End else Arrangement.Start,
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = if (message.showTail) 6.dp else 2.dp),
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .background(
                    color = if (message.isOutgoing) colors.outgoingBubble else colors.incomingBubble,
                    shape = shape,
                )
                .then(if (isFailed) Modifier.clickable(onClick = onRetry) else Modifier)
                .padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            if (message.senderName != null) {
                Text(
                    text = message.senderName,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyLarge,
                color = if (message.isOutgoing) colors.onOutgoingBubble else colors.onIncomingBubble,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(
                    text = message.time,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (message.isOutgoing) colors.outgoingMeta else colors.incomingMeta,
                )
                if (message.isOutgoing) {
                    Spacer(Modifier.width(3.dp))
                    MessageStatusIcon(status = message.status, size = 14.dp)
                }
            }
        }
    }
}

@Composable
internal fun DateSeparator(item: DateSeparatorUi, modifier: Modifier = Modifier) {
    val label = when (item.relativeDay) {
        RelativeDay.TODAY -> stringResource(R.string.conversation_today)
        RelativeDay.YESTERDAY -> stringResource(R.string.conversation_yesterday)
        RelativeDay.OTHER -> item.label
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .background(PetChatTheme.colors.mutedBadge, SeparatorShape)
                .padding(horizontal = 10.dp, vertical = 3.dp),
        )
    }
}

@Preview
@Composable
private fun MessageBubblePreview() {
    PetChatTheme {
        Column(
            Modifier
                .background(PetChatTheme.colors.chatBackground)
                .padding(8.dp),
        ) {
            MessageBubble(
                message = MessageUi(
                    key = "1",
                    text = "Did you see the new release notes?",
                    time = "14:02",
                    isOutgoing = false,
                    status = MessageStatus.READ,
                    senderName = "Jasur",
                    showTail = true,
                ),
                onRetry = {},
            )
            MessageBubble(
                message = MessageUi(
                    key = "2",
                    text = "Yes! Compose recompositions dropped a lot after the stability fix",
                    time = "14:05",
                    isOutgoing = true,
                    status = MessageStatus.READ,
                    senderName = null,
                    showTail = true,
                ),
                onRetry = {},
            )
        }
    }
}
