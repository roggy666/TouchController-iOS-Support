package top.fifthlight.touchcontroller.gal

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import net.minecraft.client.Minecraft
import org.apache.commons.lang3.StringUtils
import top.fifthlight.combine.platform.TextImpl
import top.fifthlight.touchcontroller.common.gal.ChatMessage
import top.fifthlight.touchcontroller.common.gal.ChatMessageProvider

object ChatMessageProviderImpl : ChatMessageProvider {
    private val client = Minecraft.getMinecraft()

    override fun getMessages(): PersistentList<ChatMessage> =
        // В 1.7.10 chatLines приватное, используем getSentMessages вместо этого
        client.ingameGUI.chatGUI.sentMessages
            .reversed()
            .map { ChatMessage(message = TextImpl(it as net.minecraft.util.IChatComponent)) }
            .toPersistentList()

    override fun sendMessage(message: String) {
        val message = StringUtils.normalizeSpace(message.trim())
        if (!message.isEmpty()) {
            client.ingameGUI.chatGUI.addToSentMessages(message)
            client.thePlayer!!.sendChatMessage(message)
        }
    }
}