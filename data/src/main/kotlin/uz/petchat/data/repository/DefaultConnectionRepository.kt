package uz.petchat.data.repository

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import uz.petchat.data.connectivity.NetworkMonitor
import uz.petchat.data.di.DataScope
import uz.petchat.domain.model.ConnectionStatus
import uz.petchat.domain.repository.ConnectionRepository

@Singleton
internal class DefaultConnectionRepository @Inject constructor(
    networkMonitor: NetworkMonitor,
    @DataScope scope: CoroutineScope,
) : ConnectionRepository {

    override val status: StateFlow<ConnectionStatus> = networkMonitor.isOnline
        .flatMapLatest { online ->
            if (!online) {
                flowOf(ConnectionStatus.WAITING_FOR_NETWORK)
            } else {
                flow {
                    emit(ConnectionStatus.CONNECTING)
                    delay(HANDSHAKE_MS)
                    emit(ConnectionStatus.CONNECTED)
                }
            }
        }
        .stateIn(scope, SharingStarted.Eagerly, ConnectionStatus.CONNECTING)

    private companion object {
        const val HANDSHAKE_MS = 1_200L
    }
}
