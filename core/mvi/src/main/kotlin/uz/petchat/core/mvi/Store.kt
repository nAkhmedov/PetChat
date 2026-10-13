package uz.petchat.core.mvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class Store<S : Any, A : Any, E : Any>(
    initialState: S,
    private val reducer: Reducer<S, A>,
    middlewares: List<Middleware<S, A, E>> = emptyList(),
    private val scope: CoroutineScope,
) {
    private val mutableState = MutableStateFlow(initialState)

    val state: StateFlow<S> = mutableState.asStateFlow()

    private val effectChannel = Channel<E>(Channel.BUFFERED)

    val effects: Flow<E> = effectChannel.receiveAsFlow()

    private val actions = Channel<A>(Channel.UNLIMITED)
    private val keyedJobs = HashMap<String, Job>()

    private val context = object : MiddlewareContext<S, A, E> {
        override val state: S
            get() = mutableState.value

        override fun dispatch(action: A) = this@Store.dispatch(action)

        override fun emit(effect: E) {
            effectChannel.trySend(effect)
        }

        override fun launch(key: String?, block: suspend CoroutineScope.() -> Unit): Job {
            val job = scope.launch(start = CoroutineStart.LAZY, block = block)
            if (key != null) {
                synchronized(keyedJobs) { keyedJobs.put(key, job) }?.cancel()
                job.invokeOnCompletion {
                    synchronized(keyedJobs) {
                        if (keyedJobs[key] === job) keyedJobs.remove(key)
                    }
                }
            }
            job.start()
            return job
        }
    }

    private val chain: Next<A> = middlewares.foldRight(
        Next<A> { action -> mutableState.value = reducer.reduce(mutableState.value, action) },
    ) { middleware, next ->
        Next { action -> middleware.process(context, action, next) }
    }

    init {
        scope.launch {
            for (action in actions) chain(action)
        }
    }

    fun dispatch(action: A) {
        actions.trySend(action)
    }
}
