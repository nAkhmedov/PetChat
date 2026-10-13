package uz.petchat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import uz.petchat.core.ui.theme.PetChatTheme
import uz.petchat.feature.chatlist.navigation.ChatListDestination
import uz.petchat.feature.chatlist.navigation.chatListScreen
import uz.petchat.feature.conversation.navigation.conversationScreen
import uz.petchat.feature.conversation.navigation.navigateToConversation

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            PetChatTheme {
                PetChatNavHost()
            }
        }
    }
}

@Composable
private fun PetChatNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = ChatListDestination) {
        chatListScreen(onChatClick = navController::navigateToConversation)
        conversationScreen(onBack = { navController.popBackStack() })
    }
}
