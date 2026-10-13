package uz.petchat.feature.chatlist.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import uz.petchat.feature.chatlist.ui.ChatListRoute

@Serializable
data object ChatListDestination

fun NavGraphBuilder.chatListScreen(onChatClick: (chatId: String) -> Unit) {
    composable<ChatListDestination> {
        ChatListRoute(onChatClick = onChatClick)
    }
}
