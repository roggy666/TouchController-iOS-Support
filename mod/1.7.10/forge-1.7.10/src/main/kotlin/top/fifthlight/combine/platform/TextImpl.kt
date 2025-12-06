package top.fifthlight.combine.platform

import net.minecraft.util.ChatComponentText
import net.minecraft.util.ChatStyle
import net.minecraft.util.IChatComponent
import top.fifthlight.combine.data.Text as CombineText

@JvmInline
value class TextImpl(
    val inner: IChatComponent,
) : CombineText {
    override val string: String
        get() = inner.unformattedText

    override fun bold(): CombineText = TextImpl(ChatComponentText(STYLE_BOLD.formattingCode + string))

    override fun underline(): CombineText = TextImpl(ChatComponentText(STYLE_UNDERLINE.formattingCode + string))

    override fun italic(): CombineText = TextImpl(ChatComponentText(STYLE_ITALIC.formattingCode + string))

    override fun copy(): CombineText = TextImpl(inner.createCopy())

    override fun plus(other: CombineText): CombineText = TextImpl(inner.createCopy().appendSibling(other.toMinecraft()))

    companion object {
        val EMPTY = TextImpl(ChatComponentText(""))
        private val STYLE_BOLD = ChatStyle().apply { bold = true }
        private val STYLE_UNDERLINE = ChatStyle().apply { underlined = true }
        private val STYLE_ITALIC = ChatStyle().apply { italic = true }
    }
}
