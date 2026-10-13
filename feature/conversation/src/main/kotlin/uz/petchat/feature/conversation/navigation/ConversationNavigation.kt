package uz.petchat.feature.conversation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import uz.petchat.feature.conversation.ui.ConversationRoute

@Serializable
data class ConversationDestination(val chatId: String)

fun NavController.navigateToConversation(chatId: String) {
    navigate(ConversationDestination(chatId)) {
        launchSingleTop = true
    }
}

fun NavGraphBuilder.conversationScreen(onBack: () -> Unit) {
    composable<ConversationDestination> {
        ConversationRoute(onBack = onBack)
    }
}
