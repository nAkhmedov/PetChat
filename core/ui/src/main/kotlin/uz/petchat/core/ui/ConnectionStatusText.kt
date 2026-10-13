package uz.petchat.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import uz.petchat.domain.model.ConnectionStatus

@Composable
fun connectionStatusText(status: ConnectionStatus): String? = when (status) {
    ConnectionStatus.WAITING_FOR_NETWORK -> stringResource(R.string.core_ui_waiting_for_network)
    ConnectionStatus.CONNECTING -> stringResource(R.string.core_ui_connecting)
    ConnectionStatus.CONNECTED -> null
}
