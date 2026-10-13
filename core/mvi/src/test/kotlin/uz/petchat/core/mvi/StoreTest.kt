package uz.petchat.core.mvi

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class StoreTest {

    private data class CounterState(val count: Int = 0)

    private sealed interface CounterAction {
        data object Increment : CounterAction
        data object IncrementLater : CounterAction
        data object Blocked : CounterAction
    }

    private val reducer = Reducer<CounterState, CounterAction> { state, action ->
        when (action) {
            CounterAction.Increment -> state.copy(count = state.count + 1)
            CounterAction.IncrementLater, CounterAction.Blocked -> state
        }
    }

    @Test
    fun `actions are reduced in dispatch order`() = runTest {
        val store = Store<CounterState, CounterAction, Nothing>(CounterState(), reducer, scope = backgroundScope)

        repeat(5) { store.dispatch(CounterAction.Increment) }
        runCurrent()

        assertEquals(5, store.state.value.count)
    }

    @Test
    fun `middleware can swallow an action so the reducer never sees it`() = runTest {
        val guard = Middleware<CounterState, CounterAction, Nothing> { _, action, next ->
            if (action != CounterAction.Blocked) next(action)
        }
        val countingReducer = Reducer<CounterState, CounterAction> { state, _ -> state.copy(count = state.count + 1) }
        val store = Store(CounterState(), countingReducer, listOf(guard), backgroundScope)

        store.dispatch(CounterAction.Blocked)
        store.dispatch(CounterAction.Increment)
        runCurrent()

        assertEquals(1, store.state.value.count)
    }

    @Test
    fun `launch with the same key cancels the previous job`() = runTest {
        val debounce = Middleware<CounterState, CounterAction, Nothing> { context, action, next ->
            if (action == CounterAction.IncrementLater) {
                context.launch(key = "later") {
                    delay(100)
                    context.dispatch(CounterAction.Increment)
                }
            } else {
                next(action)
            }
        }
        val store = Store(CounterState(), reducer, listOf(debounce), backgroundScope)

        store.dispatch(CounterAction.IncrementLater)
        store.dispatch(CounterAction.IncrementLater)
        advanceUntilIdle()

        assertEquals(1, store.state.value.count)
    }

    @Test
    fun `effects are delivered to the collector`() = runTest {
        val effects = Middleware<CounterState, CounterAction, String> { context, action, next ->
            next(action)
            context.emit("count=${context.state.count}")
        }
        val store = Store(CounterState(), reducer, listOf(effects), backgroundScope)

        store.dispatch(CounterAction.Increment)
        runCurrent()

        assertEquals("count=1", store.effects.first())
    }
}
