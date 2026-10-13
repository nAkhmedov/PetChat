package uz.petchat.core.mvi

fun interface Reducer<S, A> {
    fun reduce(state: S, action: A): S
}
