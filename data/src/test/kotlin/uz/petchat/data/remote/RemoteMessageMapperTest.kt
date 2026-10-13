package uz.petchat.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import uz.petchat.domain.model.MessageStatus

class RemoteMessageMapperTest {

    private val remote = RemoteMessage(
        id = "m1",
        chatId = "chat_1",
        senderName = "Aziz",
        text = "Salom",
        sentAt = 1_000L,
        isMine = false,
        status = "read",
    )

    @Test
    fun `fields are mapped and the server id doubles as the local id`() {
        val message = remote.toDomain()

        assertEquals("m1", message.localId)
        assertEquals("m1", message.serverId)
        assertEquals("chat_1", message.chatId)
        assertEquals("Aziz", message.senderName)
        assertEquals("Salom", message.text)
        assertEquals(1_000L, message.createdAt)
        assertEquals(false, message.isOutgoing)
        assertEquals(MessageStatus.READ, message.status)
    }

    @Test
    fun `sender name stays null for direct chats`() {
        assertNull(remote.copy(senderName = null).toDomain().senderName)
    }

    @Test
    fun `known statuses are mapped case-insensitively`() {
        assertEquals(MessageStatus.DELIVERED, "Delivered".toMessageStatus())
        assertEquals(MessageStatus.READ, "READ".toMessageStatus())
    }

    @Test
    fun `an unknown status falls back to SENT`() {
        assertEquals(MessageStatus.SENT, "archived".toMessageStatus())
    }
}
