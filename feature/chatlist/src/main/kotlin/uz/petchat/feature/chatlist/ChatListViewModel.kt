package uz.petchat.feature.chatlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import uz.petchat.core.mvi.LoggingMiddleware
import uz.petchat.core.mvi.MviLogger
import uz.petchat.core.mvi.Store

@HiltViewModel
class ChatListViewModel @Inject constructor(
    chatListMiddleware: ChatListMiddleware,
    logger: MviLogger,
) : ViewModel() {

    private val store = Store<ChatListState, ChatListAction, ChatListEffect>(
        initialState = ChatListState(),
        reducer = ChatListReducer,
        middlewares = listOf(
            LoggingMiddleware("ChatList", logger),
            chatListMiddleware,
        ),
        scope = viewModelScope,
    )

    val state: StateFlow<ChatListState> = store.state
    val effects: Flow<ChatListEffect> = store.effects

    init {
        store.dispatch(ChatListAction.Init)
    }

    fun dispatch(action: ChatListAction) = store.dispatch(action)
}
