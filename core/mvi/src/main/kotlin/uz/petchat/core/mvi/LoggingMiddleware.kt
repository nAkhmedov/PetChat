package uz.petchat.core.mvi

class LoggingMiddleware<S, A, E>(
    private val tag: String,
    private val logger: MviLogger,
) : Middleware<S, A, E> {

    override fun process(context: MiddlewareContext<S, A, E>, action: A, next: Next<A>) {
        if (!logger.isEnabled) {
            next(action)
            return
        }
        val start = System.nanoTime()
        next(action)
        val micros = (System.nanoTime() - start) / 1_000
        logger.log(tag, "$action (${micros}µs)")
    }
}

interface MviLogger {
    val isEnabled: Boolean

    fun log(tag: String, message: String)

    object None : MviLogger {
        override val isEnabled: Boolean = false

        override fun log(tag: String, message: String) = Unit
    }
}
