package uz.petchat.core.mvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

fun interface Middleware<S, A, E> {
    fun process(context: MiddlewareContext<S, A, E>, action: A, next: Next<A>)
}

fun interface Next<A> {
    operator fun invoke(action: A)
}

interface MiddlewareContext<S, A, E> {

    val state: S

    fun dispatch(action: A)

    fun emit(effect: E)

    fun launch(key: String? = null, block: suspend CoroutineScope.() -> Unit): Job
}
