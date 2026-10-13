package uz.petchat.feature.chatlist

import uz.petchat.core.mvi.Reducer

object ChatListReducer : Reducer<ChatListState, ChatListAction> {

    override fun reduce(state: ChatListState, action: ChatListAction): ChatListState =
        when (action) {
            is ChatListAction.ChatsLoaded -> state.copy(chats = action.chats, isLoaded = true)
            is ChatListAction.ConnectionChanged -> state.copy(connection = action.status)
            ChatListAction.Init, is ChatListAction.ChatClicked -> state
        }
}
