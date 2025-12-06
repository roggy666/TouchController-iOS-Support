package top.fifthlight.combine.platform

import net.minecraft.util.ChatComponentText
import net.minecraft.util.ChatStyle
import top.fifthlight.combine.data.TextBuilder
import top.fifthlight.combine.data.Text as CombineText

class TextBuilderImpl(
    private val text: StringBuilder = StringBuilder(),
    private val style: ChatStyle = ChatStyle(),
) : TextBuilder {

    override fun bold(bold: Boolean, block: TextBuilder.() -> Unit) {
        val origBold = style.bold
        style.bold = bold
        append("§r")
        append(style.formattingCode)
        block(
            TextBuilderImpl(
                text = text,
                style = style,
            )
        )
        style.bold = origBold
        append("§r")
        append(style.formattingCode)
    }

    override fun underline(underline: Boolean, block: TextBuilder.() -> Unit) {
        val origUnderline = style.underlined
        style.underlined = underline
        append("§r")
        append(style.formattingCode)
        block(
            TextBuilderImpl(
                text = text,
                style = style,
            )
        )
        style.underlined = origUnderline
        append("§r")
        append(style.formattingCode)
    }

    override fun italic(italic: Boolean, block: TextBuilder.() -> Unit) {
        val origItalic = style.italic
        style.italic = italic
        append("§r")
        append(style.formattingCode)
        block(
            TextBuilderImpl(
                text = text,
                style = style,
            )
        )
        style.italic = origItalic
        append("§r")
        append(style.formattingCode)
    }

    override fun append(string: String) {
        this.text.append(string)
    }

    override fun appendWithoutStyle(text: CombineText) {
        this.text.append(text.toMinecraft().unformattedText)
    }

    fun build(): CombineText = TextImpl(ChatComponentText(text.toString()))
}
