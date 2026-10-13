package uz.petchat.feature.conversation.middleware

import javax.inject.Inject
import uz.petchat.core.mvi.Middleware
import uz.petchat.core.mvi.MiddlewareContext
import uz.petchat.core.mvi.Next
import uz.petchat.domain.error.runCatchingCancellable
import uz.petchat.domain.model.ConnectionStatus
import uz.petchat.domain.usecase.LoadOlderMessagesUseCase
import uz.petchat.feature.conversation.ConversationAction
import uz.petchat.feature.conversation.ConversationEffect
import uz.petchat.feature.conversation.ConversationState

class PaginationMiddleware @Inject constructor(
    private val loadOlderMessages: LoadOlderMessagesUseCase,
) : Middleware<ConversationState, ConversationAction, ConversationEffect> {

    override fun process(
        context: MiddlewareContext<ConversationState, ConversationAction, ConversationEffect>,
        action: ConversationAction,
        next: Next<ConversationAction>,
    ) {
        when (action) {
            ConversationAction.Init -> {
                next(action)
                loadPage(context)
            }

            ConversationAction.LoadOlder -> {
                val state = context.state
                if (state.isLoadingOlder || state.endReached) return
                next(action)
                loadPage(context)
            }

            is ConversationAction.ConnectionChanged -> {
                val hadError = context.state.pageError
                next(action)
                if (hadError && action.status == ConnectionStatus.CONNECTED) {
                    context.dispatch(ConversationAction.LoadOlder)
                }
            }

            else -> next(action)
        }
    }

    private fun loadPage(context: MiddlewareContext<ConversationState, ConversationAction, ConversationEffect>) {
        val chatId = context.state.chatId
        context.launch(key = KEY) {
            runCatchingCancellable { loadOlderMessages(chatId) }
                .onSuccess { hasMore -> context.dispatch(ConversationAction.OlderLoaded(endReached = !hasMore)) }
                .onFailure { context.dispatch(ConversationAction.OlderLoadFailed) }
        }
    }

    private companion object {
        const val KEY = "pagination"
    }
}
