package uz.petchat.feature.conversation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import uz.petchat.core.ui.component.Avatar
import uz.petchat.core.ui.theme.PetChatTheme
import uz.petchat.core.ui.ObserveAsEvents
import uz.petchat.core.ui.connectionStatusText
import uz.petchat.domain.model.ConnectionStatus
import uz.petchat.feature.conversation.ChatHeaderUi
import uz.petchat.feature.conversation.ConversationAction
import uz.petchat.feature.conversation.ConversationEffect
import uz.petchat.feature.conversation.ConversationState
import uz.petchat.feature.conversation.ConversationViewModel
import uz.petchat.feature.conversation.DateSeparatorUi
import uz.petchat.feature.conversation.MessageUi
import uz.petchat.feature.conversation.R

@Composable
internal fun ConversationRoute(
    onBack: () -> Unit,
    viewModel: ConversationViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    ObserveAsEvents(viewModel.effects) { effect ->
        when (effect) {
            ConversationEffect.ScrollToBottom -> scope.launch { listState.animateScrollToItem(0) }
            is ConversationEffect.ShowMessage -> scope.launch {
                snackbarHostState.showSnackbar(context.getString(effect.messageRes))
            }
        }
    }

    val onAction = remember(viewModel) { viewModel::dispatch }
    ConversationScreen(
        state = state,
        listState = listState,
        snackbarHostState = snackbarHostState,
        onAction = onAction,
        onBack = onBack,
    )
}

@Composable
internal fun ConversationScreen(
    state: ConversationState,
    listState: LazyListState,
    snackbarHostState: SnackbarHostState,
    onAction: (ConversationAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = { ConversationTopBar(header = state.header, connection = state.connection, onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(PetChatTheme.colors.chatBackground)
                .imePadding(),
        ) {
            Box(Modifier.weight(1f)) {
                if (state.isInitialLoading) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                } else {
                    MessageList(state = state, listState = listState, onAction = onAction)
                }
            }
            MessageInput(onSend = { text -> onAction(ConversationAction.Send(text)) })
        }
    }
}

@Composable
private fun MessageList(
    state: ConversationState,
    listState: LazyListState,
    onAction: (ConversationAction) -> Unit,
) {
    val scope = rememberCoroutineScope()

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            reverseLayout = true,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(
                items = state.items,
                key = { it.key },
                contentType = { it.contentType },
            ) { item ->
                when (item) {
                    is MessageUi -> MessageBubble(
                        message = item,
                        onRetry = { onAction(ConversationAction.Retry(item.key)) },
                    )
                    is DateSeparatorUi -> DateSeparator(item)
                }
            }

            when {
                state.isLoadingOlder -> item(key = KEY_LOADING, contentType = KEY_LOADING) {
                    Box(Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(strokeWidth = 2.dp)
                    }
                }
                state.pageError -> item(key = KEY_ERROR, contentType = KEY_ERROR) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        TextButton(onClick = { onAction(ConversationAction.LoadOlder) }) {
                            Text(stringResource(R.string.conversation_load_failed_retry))
                        }
                    }
                }
                else -> Unit
            }
        }

        LoadOlderEffect(
            listState = listState,
            messageCount = state.items.size,
            onLoadOlder = { onAction(ConversationAction.LoadOlder) },
        )
        StickToBottomEffect(listState = listState, newestKey = state.items.firstOrNull()?.key)

        val showJumpToBottom by remember(listState) {
            derivedStateOf { listState.firstVisibleItemIndex > JUMP_BUTTON_THRESHOLD }
        }
        AnimatedVisibility(
            visible = showJumpToBottom,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        ) {
            SmallFloatingActionButton(
                onClick = { scope.launch { listState.animateScrollToItem(0) } },
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = stringResource(R.string.conversation_jump_to_bottom),
                )
            }
        }
    }
}

@Composable
private fun LoadOlderEffect(
    listState: LazyListState,
    messageCount: Int,
    onLoadOlder: () -> Unit,
) {
    val currentOnLoadOlder by rememberUpdatedState(onLoadOlder)
    val currentCount by rememberUpdatedState(messageCount)
    LaunchedEffect(listState) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            val nearTop = currentCount > 0 && lastVisible >= layoutInfo.totalItemsCount - PREFETCH_DISTANCE
            nearTop to currentCount
        }
            .distinctUntilChanged()
            .filter { (nearTop, _) -> nearTop }
            .collect { currentOnLoadOlder() }
    }
}

@Composable
private fun StickToBottomEffect(listState: LazyListState, newestKey: String?) {
    LaunchedEffect(newestKey) {
        if (newestKey != null && listState.firstVisibleItemIndex <= 1) {
            listState.animateScrollToItem(0)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConversationTopBar(
    header: ChatHeaderUi?,
    connection: ConnectionStatus,
    onBack: () -> Unit,
) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.conversation_back),
                )
            }
        },
        title = {
            if (header != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Avatar(
                        initials = header.initials,
                        colorIndex = header.avatarColorIndex,
                        size = 40.dp,
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = header.title,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = subtitle(header, connection),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (header.isOnline && connection == ConnectionStatus.CONNECTED) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            maxLines = 1,
                        )
                    }
                }
            }
        },
    )
}

@Composable
private fun subtitle(header: ChatHeaderUi, connection: ConnectionStatus): String =
    connectionStatusText(connection) ?: when {
        header.isGroup -> stringResource(R.string.conversation_subtitle_group)
        header.isOnline -> stringResource(R.string.conversation_subtitle_online)
        else -> stringResource(R.string.conversation_subtitle_last_seen)
    }

private const val PREFETCH_DISTANCE = 10
private const val JUMP_BUTTON_THRESHOLD = 3
private const val KEY_LOADING = "loading_older"
private const val KEY_ERROR = "load_older_error"
