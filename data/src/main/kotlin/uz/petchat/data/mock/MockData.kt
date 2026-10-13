package uz.petchat.data.mock

import kotlin.random.Random
import uz.petchat.domain.model.Chat
import uz.petchat.data.remote.RemoteMessage
import uz.petchat.data.remote.toDomain

internal object MockData {

    private const val SEED = 2026
    private const val MINUTE = 60_000L

    private data class ChatSeed(val title: String, val isGroup: Boolean)

    private val chatSeeds = listOf(
        ChatSeed("Aziz Karimov", false),
        ChatSeed("Android Uzbekistan", true),
        ChatSeed("Dilnoza Rahimova", false),
        ChatSeed("Kotlin Developers", true),
        ChatSeed("Jasur Tursunov", false),
        ChatSeed("Team Standup", true),
        ChatSeed("Malika Sobirova", false),
        ChatSeed("Bekzod Aliyev", false),
        ChatSeed("Design Review", true),
        ChatSeed("Sardor Umarov", false),
        ChatSeed("Nilufar Yusupova", false),
        ChatSeed("Otabek Ergashev", false),
        ChatSeed("Family", true),
        ChatSeed("Kamola Ismoilova", false),
        ChatSeed("Rustam Nazarov", false),
        ChatSeed("Book Club", true),
        ChatSeed("Shahzod Qodirov", false),
        ChatSeed("Gulnora Akbarova", false),
        ChatSeed("QA Team", true),
        ChatSeed("Timur Saidov", false),
        ChatSeed("Laylo Hamidova", false),
        ChatSeed("Ulugbek Mirzaev", false),
        ChatSeed("Zarina Olimova", false),
        ChatSeed("Football on Saturday", true),
    )

    val groupMembers = listOf("Aziz", "Dilnoza", "Jasur", "Malika", "Bekzod", "Sardor", "Nilufar")

    val phrases = listOf(
        "Salom! Qalaysan?",
        "Hi! How are you?",
        "Did you see the new release notes?",
        "Let's sync after lunch",
        "Yaxshi, rahmat 🙂",
        "The build is green now ✅",
        "Can you review my PR when you have a minute?",
        "I pushed a fix for the crash on Android 14",
        "Ertaga uchrashamizmi?",
        "Sounds good 👍",
        "What do you think about moving the chat list to Paging 3?",
        "Compose recomposition count dropped from 40 to 3 after the stability fix",
        "Ok, I'll check and get back to you",
        "Kechirasiz, hozir bandman, keyinroq yozaman",
        "Haha 😄",
        "Meeting moved to 15:00",
        "Have a great weekend!",
        "Coffee? ☕",
        "Here is the link to the design file",
        "Thanks a lot!",
        "Did the interview go well?",
        "I think we should cache it in Room and observe it as a Flow",
        "Yes, exactly",
        "Let me double-check the logs",
        "On my way 🚗",
        "Zo'r! Davom etamiz",
        "Who is joining the call?",
        "Remember to update the changelog",
        "Nice catch, that was a race condition",
        "See you tomorrow",
    )

    val replies = listOf(
        "Got it 👍",
        "Ha, albatta",
        "Interesting, tell me more",
        "Ok!",
        "Let me think about it",
        "😂",
        "Sure, let's do it",
        "Rahmat!",
    )

    val content: Content by lazy { generate(System.currentTimeMillis()) }

    class Content(
        val chats: List<Chat>,
        val history: Map<String, List<RemoteMessage>>,
    )

    private fun generate(now: Long): Content {
        val random = Random(SEED)
        val history = HashMap<String, List<RemoteMessage>>(chatSeeds.size)
        val chats = chatSeeds.mapIndexed { index, seed ->
            val chatId = "chat_$index"
            val size = random.nextInt(40, 320)
            var time = now - index * 37 * MINUTE - random.nextLong(0, 30 * MINUTE)
            val messages = List(size) { position ->
                val isOutgoing = random.nextInt(100) < 40
                val serverId = "${chatId}_$position"
                val message = RemoteMessage(
                    id = serverId,
                    chatId = chatId,
                    senderName = if (seed.isGroup && !isOutgoing) groupMembers.random(random) else null,
                    text = phrases.random(random),
                    sentAt = time,
                    isMine = isOutgoing,
                    status = "read",
                )
                time -= random.nextLong(1, 240) * MINUTE
                message
            }
            history[chatId] = messages
            Chat(
                id = chatId,
                title = seed.title,
                isGroup = seed.isGroup,
                isOnline = !seed.isGroup && random.nextBoolean(),
                isPinned = index < 2,
                isMuted = seed.isGroup && random.nextInt(100) < 40,
                unreadCount = if (random.nextInt(100) < 35) random.nextInt(1, 12) else 0,
                lastMessage = messages.first().toDomain(),
            )
        }
        return Content(chats, history)
    }
}
