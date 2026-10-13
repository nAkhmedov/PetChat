package uz.petchat.feature.conversation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import uz.petchat.core.mvi.LoggingMiddleware
import uz.petchat.core.mvi.MviLogger
import uz.petchat.core.mvi.Store
import uz.petchat.feature.conversation.middleware.ConversationObserverMiddleware
import uz.petchat.feature.conversation.middleware.PaginationMiddleware
import uz.petchat.feature.conversation.middleware.SendMessageMiddleware
import uz.petchat.feature.conversation.navigation.ConversationDestination

@HiltViewModel
class ConversationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    paginationMiddleware: PaginationMiddleware,
    sendMessageMiddleware: SendMessageMiddleware,
    observerMiddleware: ConversationObserverMiddleware,
    logger: MviLogger,
) : ViewModel() {

    private val chatId = savedStateHandle.toRoute<ConversationDestination>().chatId

    private val store = Store<ConversationState, ConversationAction, ConversationEffect>(
        initialState = ConversationState(chatId = chatId),
        reducer = ConversationReducer,
        middlewares = listOf(
            LoggingMiddleware("Conversation", logger),
            paginationMiddleware,
            sendMessageMiddleware,
            observerMiddleware,
        ),
        scope = viewModelScope,
    )

    val state: StateFlow<ConversationState> = store.state
    val effects: Flow<ConversationEffect> = store.effects

    init {
        store.dispatch(ConversationAction.Init)
    }

    fun dispatch(action: ConversationAction) = store.dispatch(action)
}
