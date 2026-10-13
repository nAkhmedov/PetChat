package uz.petchat.data.repository

import java.io.IOException
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import uz.petchat.data.di.DataScope
import uz.petchat.data.mock.MockData
import uz.petchat.data.remote.RemoteMessage
import uz.petchat.data.remote.toDomain
import uz.petchat.domain.error.OfflineException
import uz.petchat.domain.error.runCatchingCancellable
import uz.petchat.domain.model.Chat
import uz.petchat.domain.model.ConnectionStatus
import uz.petchat.domain.model.Message
import uz.petchat.domain.model.MessageStatus
import uz.petchat.domain.repository.ChatRepository
import uz.petchat.domain.repository.ConnectionRepository
import uz.petchat.domain.repository.MessageRepository

@Singleton
internal class MockChatRepository @Inject constructor(
    private val connection: ConnectionRepository,
    @DataScope private val scope: CoroutineScope,
) : ChatRepository, MessageRepository {

    private val server = MockData.content

    private val chats = MutableStateFlow(server.chats.associateBy { it.id })

    private val messages = MutableStateFlow<Map<String, List<Message>>>(emptyMap())

    private val sendLock = Mutex()
    private val idSequence = AtomicLong()

    init {
        simulateIncomingMessages()
    }

    override fun observeChats(): Flow<List<Chat>> =
        combine(chats, messages) { chatMap, messageMap ->
            chatMap.values.map { chat ->
                chat.copy(lastMessage = messageMap[chat.id]?.firstOrNull() ?: chat.lastMessage)
            }
        }.distinctUntilChanged()

    override fun observeChat(chatId: String): Flow<Chat?> =
        observeChats()
            .map { list -> list.firstOrNull { it.id == chatId } }
            .distinctUntilChanged()

    override suspend fun markAsRead(chatId: String) {
        updateChat(chatId) { it.copy(unreadCount = 0) }
    }

    override fun observeMessages(chatId: String): Flow<List<Message>> =
        messages
            .map { it[chatId].orEmpty() }
            .distinctUntilChanged()

    override suspend fun loadOlder(chatId: String): Boolean {
        if (connection.status.value != ConnectionStatus.CONNECTED) throw OfflineException()
        delay(LATENCY_MS)
        maybeFail()

        val cursor = messages.value[chatId].orEmpty()
            .filter { it.serverId != null }
            .minOfOrNull { it.createdAt }
        val older = server.history[chatId].orEmpty()
            .filter { cursor == null || it.sentAt < cursor }
        val page = older.take(PAGE_SIZE).map { it.toDomain() }

        messages.update { map -> map + (chatId to map[chatId].orEmpty() + page) }
        return older.size > page.size
    }

    override suspend fun sendMessage(chatId: String, text: String) {
        val message = Message(
            localId = UUID.randomUUID().toString(),
            serverId = null,
            chatId = chatId,
            senderName = null,
            text = text,
            createdAt = System.currentTimeMillis(),
            isOutgoing = true,
            status = MessageStatus.PENDING,
        )
        messages.update { map -> map + (chatId to listOf(message) + map[chatId].orEmpty()) }
        deliver(message)
    }

    override suspend fun retryMessage(localId: String) {
        val message = findMessage(localId) ?: return
        updateMessage(message) { it.copy(status = MessageStatus.PENDING) }
        deliver(message)
    }

    private fun deliver(message: Message) {
        scope.launch {
            val serverId = sendLock.withLock {
                connection.status.first { it == ConnectionStatus.CONNECTED }
                runCatchingCancellable {
                    delay(SEND_LATENCY_MS)
                    maybeFail()
                    "out_${idSequence.incrementAndGet()}"
                }.getOrNull()
            }
            if (serverId == null) {
                updateMessage(message) { it.copy(status = MessageStatus.FAILED) }
                return@launch
            }
            updateMessage(message) { it.copy(serverId = serverId, status = MessageStatus.SENT) }
            delay(800)
            updateMessage(message) { it.copy(status = MessageStatus.DELIVERED) }
            delay(1_500)
            updateMessage(message) { it.copy(status = MessageStatus.READ) }
            delay(1_200)
            receive(message.chatId, MockData.replies.random())
        }
    }

    private fun simulateIncomingMessages() {
        scope.launch {
            connection.status.collectLatest { status ->
                if (status != ConnectionStatus.CONNECTED) return@collectLatest
                while (true) {
                    delay(Random.nextLong(15_000, 30_000))
                    receive(server.chats.random().id, MockData.phrases.random())
                }
            }
        }
    }

    private fun receive(chatId: String, text: String) {
        val chat = chats.value[chatId] ?: return
        val serverId = "in_${idSequence.incrementAndGet()}"
        val message = RemoteMessage(
            id = serverId,
            chatId = chatId,
            senderName = if (chat.isGroup) MockData.groupMembers.random() else null,
            text = text,
            sentAt = System.currentTimeMillis(),
            isMine = false,
            status = "read",
        ).toDomain()
        messages.update { map -> map + (chatId to listOf(message) + map[chatId].orEmpty()) }
        updateChat(chatId) { it.copy(unreadCount = it.unreadCount + 1) }
    }

    private fun maybeFail() {
        if (Random.nextFloat() < FAILURE_RATE) throw IOException("Simulated server error")
    }

    private fun updateChat(chatId: String, transform: (Chat) -> Chat) {
        chats.update { map ->
            val chat = map[chatId] ?: return@update map
            map + (chatId to transform(chat))
        }
    }

    private fun updateMessage(message: Message, transform: (Message) -> Message) {
        messages.update { map ->
            val list = map[message.chatId] ?: return@update map
            map + (message.chatId to list.map { if (it.localId == message.localId) transform(it) else it })
        }
    }

    private fun findMessage(localId: String): Message? =
        messages.value.values.firstNotNullOfOrNull { list -> list.firstOrNull { it.localId == localId } }

    private companion object {
        const val PAGE_SIZE = 30
        const val LATENCY_MS = 700L
        const val SEND_LATENCY_MS = 400L
        const val FAILURE_RATE = 0.1f
    }
}
