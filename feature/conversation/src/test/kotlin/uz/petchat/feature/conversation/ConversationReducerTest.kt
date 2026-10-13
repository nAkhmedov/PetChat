package uz.petchat.feature.conversation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class ConversationReducerTest {

    private val initial = ConversationState(chatId = "chat_1")

    @Test
    fun `load older starts loading and clears a previous error`() {
        val failed = initial.copy(pageError = true)

        val state = ConversationReducer.reduce(failed, ConversationAction.LoadOlder)

        assertTrue(state.isLoadingOlder)
        assertFalse(state.pageError)
    }

    @Test
    fun `older loaded stops loading and records the end`() {
        val loading = initial.copy(isLoadingOlder = true)

        val state = ConversationReducer.reduce(loading, ConversationAction.OlderLoaded(endReached = true))

        assertFalse(state.isLoadingOlder)
        assertTrue(state.endReached)
    }

    @Test
    fun `failure stops loading and shows the error`() {
        val loading = initial.copy(isLoadingOlder = true)

        val state = ConversationReducer.reduce(loading, ConversationAction.OlderLoadFailed)

        assertFalse(state.isLoadingOlder)
        assertTrue(state.pageError)
    }

    @Test
    fun `send is a side effect only`() {
        assertSame(initial, ConversationReducer.reduce(initial, ConversationAction.Send("hi")))
    }

    @Test
    fun `initial loading is shown only while there is nothing cached`() {
        assertTrue(initial.copy(isLoadingOlder = true).isInitialLoading)
        assertFalse(initial.isInitialLoading)
    }
}
