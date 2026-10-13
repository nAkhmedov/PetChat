package uz.petchat.domain.error

import kotlin.coroutines.cancellation.CancellationException

class OfflineException : Exception("No connection to the server")

inline fun <T> runCatchingCancellable(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
