package uz.petchat.feature.conversation.middleware

import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import uz.petchat.core.mvi.Store
import uz.petchat.domain.model.ConnectionStatus
import uz.petchat.domain.model.Message
import uz.petchat.domain.repository.MessageRepository
import uz.petchat.domain.usecase.LoadOlderMessagesUseCase
import uz.petchat.feature.conversation.ConversationAction
import uz.petchat.feature.conversation.ConversationEffect
import uz.petchat.feature.conversation.ConversationReducer
import uz.petchat.feature.conversation.ConversationState

class PaginationMiddlewareTest {

    private val repository = FakeMessageRepository()

    private fun TestScope.createStore() = Store<ConversationState, ConversationAction, ConversationEffect>(
        initialState = ConversationState(chatId = "chat_1"),
        reducer = ConversationReducer,
        middlewares = listOf(PaginationMiddleware(LoadOlderMessagesUseCase(repository))),
        scope = backgroundScope,
    )

    @Test
    fun `init loads the first page`() = runTest {
        val store = createStore()

        store.dispatch(ConversationAction.Init)
        runCurrent()

        assertEquals(1, repository.requests)
        assertFalse(store.state.value.isLoadingOlder)
    }

    @Test
    fun `duplicate load requests while loading are ignored`() = runTest {
        val gate = CompletableDeferred<Unit>()
        repository.gate = gate
        val store = createStore()

        store.dispatch(ConversationAction.Init)
        store.dispatch(ConversationAction.LoadOlder)
        store.dispatch(ConversationAction.LoadOlder)
        runCurrent()
        gate.complete(Unit)
        runCurrent()

        assertEquals(1, repository.requests)
    }

    @Test
    fun `load older requests the next page`() = runTest {
        val store = createStore()
        store.dispatch(ConversationAction.Init)
        runCurrent()

        store.dispatch(ConversationAction.LoadOlder)
        runCurrent()

        assertEquals(2, repository.requests)
    }

    @Test
    fun `end of history stops further requests`() = runTest {
        repository.hasMore = false
        val store = createStore()
        store.dispatch(ConversationAction.Init)
        runCurrent()

        store.dispatch(ConversationAction.LoadOlder)
        runCurrent()

        assertTrue(store.state.value.endReached)
        assertEquals(1, repository.requests)
    }

    @Test
    fun `failure shows an error and reconnect retries automatically`() = runTest {
        repository.error = IOException("offline")
        val store = createStore()
        store.dispatch(ConversationAction.Init)
        runCurrent()
        assertTrue(store.state.value.pageError)

        repository.error = null
        store.dispatch(ConversationAction.ConnectionChanged(ConnectionStatus.CONNECTED))
        runCurrent()

        assertFalse(store.state.value.pageError)
        assertEquals(2, repository.requests)
    }

    private class FakeMessageRepository : MessageRepository {
        var requests = 0
        var hasMore = true
        var error: Exception? = null
        var gate: CompletableDeferred<Unit>? = null

        override suspend fun loadOlder(chatId: String): Boolean {
            requests++
            gate?.await()
            error?.let { throw it }
            return hasMore
        }

        override fun observeMessages(chatId: String): Flow<List<Message>> = emptyFlow()
        override suspend fun sendMessage(chatId: String, text: String) = Unit
        override suspend fun retryMessage(localId: String) = Unit
    }
}
