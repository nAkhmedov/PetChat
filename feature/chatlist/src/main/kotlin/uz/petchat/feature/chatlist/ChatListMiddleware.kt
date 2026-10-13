package uz.petchat.feature.chatlist

import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import uz.petchat.core.mvi.Middleware
import uz.petchat.core.mvi.MiddlewareContext
import uz.petchat.core.mvi.Next
import uz.petchat.domain.usecase.ObserveChatsUseCase
import uz.petchat.domain.usecase.ObserveConnectionUseCase

class ChatListMiddleware @Inject constructor(
    private val observeChats: ObserveChatsUseCase,
    private val observeConnection: ObserveConnectionUseCase,
    private val mapper: ChatItemUiMapper,
) : Middleware<ChatListState, ChatListAction, ChatListEffect> {

    override fun process(
        context: MiddlewareContext<ChatListState, ChatListAction, ChatListEffect>,
        action: ChatListAction,
        next: Next<ChatListAction>,
    ) {
        next(action)
        when (action) {
            ChatListAction.Init -> {
                context.launch(key = KEY_CHATS) {
                    observeChats()
                        .map { chats -> mapper.map(chats) }
                        .flowOn(Dispatchers.Default)
                        .collect { context.dispatch(ChatListAction.ChatsLoaded(it)) }
                }
                context.launch(key = KEY_CONNECTION) {
                    observeConnection().collect { context.dispatch(ChatListAction.ConnectionChanged(it)) }
                }
            }
            is ChatListAction.ChatClicked -> context.emit(ChatListEffect.OpenConversation(action.chatId))
            is ChatListAction.ChatsLoaded, is ChatListAction.ConnectionChanged -> Unit
        }
    }

    private companion object {
        const val KEY_CHATS = "chats"
        const val KEY_CONNECTION = "connection"
    }
}
