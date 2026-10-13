package uz.petchat.feature.chatlist.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import uz.petchat.core.ui.component.Avatar
import uz.petchat.core.ui.component.UnreadBadge
import uz.petchat.core.ui.MessageStatusIcon
import uz.petchat.feature.chatlist.ChatItemUi
import uz.petchat.feature.chatlist.R

@Composable
internal fun ChatListItem(
    chat: ChatItemUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Avatar(
            initials = chat.initials,
            colorIndex = chat.avatarColorIndex,
            isOnline = chat.isOnline,
            size = 54.dp,
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = chat.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (chat.lastMessageStatus != null) {
                    MessageStatusIcon(
                        status = chat.lastMessageStatus,
                        modifier = Modifier.padding(end = 4.dp),
                    )
                }
                Text(
                    text = chat.time,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                PreviewText(
                    chat = chat,
                    modifier = Modifier.weight(1f),
                )
                if (chat.unreadBadge != null) {
                    Spacer(Modifier.width(8.dp))
                    UnreadBadge(text = chat.unreadBadge, isMuted = chat.isMuted)
                }
            }
        }
    }
}

@Composable
private fun PreviewText(chat: ChatItemUi, modifier: Modifier = Modifier) {
    val author = when {
        chat.isLastMessageOutgoing -> stringResource(R.string.chat_list_you)
        else -> chat.previewAuthor
    }
    val authorColor = MaterialTheme.colorScheme.primary
    val text = remember(author, chat.preview, authorColor) {
        buildPreview(author, chat.preview, authorColor)
    }
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
}

private fun buildPreview(author: String?, preview: String, authorColor: Color): AnnotatedString =
    buildAnnotatedString {
        if (author != null) {
            withStyle(SpanStyle(color = authorColor)) { append("$author: ") }
        }
        append(preview)
    }
