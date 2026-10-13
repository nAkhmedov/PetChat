package uz.petchat.feature.conversation

import uz.petchat.core.mvi.Reducer
import uz.petchat.feature.conversation.ConversationAction.ConnectionChanged
import uz.petchat.feature.conversation.ConversationAction.HeaderUpdated
import uz.petchat.feature.conversation.ConversationAction.Init
import uz.petchat.feature.conversation.ConversationAction.LoadOlder
import uz.petchat.feature.conversation.ConversationAction.MessagesUpdated
import uz.petchat.feature.conversation.ConversationAction.OlderLoadFailed
import uz.petchat.feature.conversation.ConversationAction.OlderLoaded
import uz.petchat.feature.conversation.ConversationAction.Retry
import uz.petchat.feature.conversation.ConversationAction.Send

object ConversationReducer : Reducer<ConversationState, ConversationAction> {

    override fun reduce(state: ConversationState, action: ConversationAction): ConversationState =
        when (action) {
            Init, LoadOlder -> state.copy(isLoadingOlder = true, pageError = false)
            is OlderLoaded -> state.copy(isLoadingOlder = false, endReached = action.endReached)
            OlderLoadFailed -> state.copy(isLoadingOlder = false, pageError = true)
            is HeaderUpdated -> state.copy(header = action.header)
            is MessagesUpdated -> state.copy(items = action.items)
            is ConnectionChanged -> state.copy(connection = action.status)
            is Send, is Retry -> state
        }
}
