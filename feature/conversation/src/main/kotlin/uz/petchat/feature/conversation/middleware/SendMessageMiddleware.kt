package uz.petchat.feature.conversation.middleware

import javax.inject.Inject
import uz.petchat.core.mvi.Middleware
import uz.petchat.core.mvi.MiddlewareContext
import uz.petchat.core.mvi.Next
import uz.petchat.domain.usecase.RetryMessageUseCase
import uz.petchat.domain.usecase.SendMessageResult
import uz.petchat.domain.usecase.SendMessageUseCase
import uz.petchat.feature.conversation.ConversationAction
import uz.petchat.feature.conversation.ConversationEffect
import uz.petchat.feature.conversation.ConversationState
import uz.petchat.feature.conversation.R

class SendMessageMiddleware @Inject constructor(
    private val sendMessage: SendMessageUseCase,
    private val retryMessage: RetryMessageUseCase,
) : Middleware<ConversationState, ConversationAction, ConversationEffect> {

    override fun process(
        context: MiddlewareContext<ConversationState, ConversationAction, ConversationEffect>,
        action: ConversationAction,
        next: Next<ConversationAction>,
    ) {
        next(action)
        when (action) {
            is ConversationAction.Send -> {
                val chatId = context.state.chatId
                context.launch {
                    when (sendMessage(chatId, action.text)) {
                        SendMessageResult.Queued -> context.emit(ConversationEffect.ScrollToBottom)
                        SendMessageResult.TooLong ->
                            context.emit(ConversationEffect.ShowMessage(R.string.conversation_error_too_long))
                        SendMessageResult.Empty -> Unit
                    }
                }
            }
            is ConversationAction.Retry -> context.launch { retryMessage(action.localId) }
            else -> Unit
        }
    }
}
