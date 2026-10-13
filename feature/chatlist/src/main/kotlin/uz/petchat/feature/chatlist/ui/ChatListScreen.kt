package uz.petchat.feature.chatlist.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.persistentListOf
import uz.petchat.core.ui.theme.PetChatTheme
import uz.petchat.core.ui.ObserveAsEvents
import uz.petchat.core.ui.connectionStatusText
import uz.petchat.domain.model.ConnectionStatus
import uz.petchat.domain.model.MessageStatus
import uz.petchat.feature.chatlist.ChatItemUi
import uz.petchat.feature.chatlist.ChatListAction
import uz.petchat.feature.chatlist.ChatListEffect
import uz.petchat.feature.chatlist.ChatListState
import uz.petchat.feature.chatlist.ChatListViewModel
import uz.petchat.feature.chatlist.R

@Composable
internal fun ChatListRoute(
    onChatClick: (chatId: String) -> Unit,
    viewModel: ChatListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ObserveAsEvents(viewModel.effects) { effect ->
        when (effect) {
            is ChatListEffect.OpenConversation -> onChatClick(effect.chatId)
        }
    }
    val onAction = remember(viewModel) { viewModel::dispatch }
    ChatListScreen(state = state, onAction = onAction)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChatListScreen(
    state: ChatListState,
    onAction: (ChatListAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { ChatListTitle(state.connection) })
        },
    ) { padding ->
        if (state.chats.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                if (state.isLoaded && state.connection == ConnectionStatus.CONNECTED) {
                    Text(stringResource(R.string.chat_list_empty))
                } else {
                    CircularProgressIndicator()
                }
            }
        } else {
            LazyColumn(
                contentPadding = padding,
                modifier = Modifier.fillMaxSize(),
            ) {
                items(
                    items = state.chats,
                    key = { it.id },
                    contentType = { CONTENT_TYPE_CHAT },
                ) { chat ->
                    Column(Modifier.animateItem()) {
                        ChatListItem(
                            chat = chat,
                            onClick = { onAction(ChatListAction.ChatClicked(chat.id)) },
                        )
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(start = 78.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatListTitle(connection: ConnectionStatus) {
    val status = connectionStatusText(connection)
    if (status == null) {
        Text(stringResource(R.string.chat_list_title))
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            CircularProgressIndicator(
                strokeWidth = 2.dp,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(12.dp))
            Text(status, style = MaterialTheme.typography.titleMedium)
        }
    }
}

private const val CONTENT_TYPE_CHAT = "chat"

@Preview
@Composable
private fun ChatListScreenPreview() {
    PetChatTheme {
        ChatListScreen(
            state = ChatListState(
                chats = persistentListOf(
                    previewChat("1", "Aziz Karimov", unread = "3"),
                    previewChat("2", "Android Uzbekistan", author = "Jasur"),
                    previewChat("3", "Dilnoza", status = MessageStatus.READ),
                ),
                isLoaded = true,
                connection = ConnectionStatus.CONNECTED,
            ),
            onAction = {},
        )
    }
}

private fun previewChat(
    id: String,
    title: String,
    unread: String? = null,
    author: String? = null,
    status: MessageStatus? = null,
) = ChatItemUi(
    id = id,
    title = title,
    initials = title.take(1),
    avatarColorIndex = id.toInt(),
    isOnline = id == "1",
    isPinned = false,
    isMuted = false,
    time = "14:05",
    preview = "Can you review my PR when you have a minute?",
    previewAuthor = author,
    isLastMessageOutgoing = status != null,
    lastMessageStatus = status,
    unreadBadge = unread,
)
