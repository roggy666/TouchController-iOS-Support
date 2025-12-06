package top.fifthlight.combine.platform

import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.ChatComponentText
import top.fifthlight.combine.data.Identifier
import top.fifthlight.combine.data.MetadataItemStack
import top.fifthlight.combine.data.Text

@JvmInline
value class ItemStackImpl(
    val inner: ItemStack
) : MetadataItemStack {
    override val amount: Int
        get() = inner.stackSize

    override val id: Identifier
        get() {
            // В 1.7.10 getNameForObject возвращает String, а не ResourceLocation
            val name = Item.itemRegistry.getNameForObject(inner.item) as? String ?: "minecraft:air"
            return net.minecraft.util.ResourceLocation(name).toCombine()
        }

    override val metadata: Int
        get() = inner.getMetadata()

    override val item: ItemImpl
        get() = ItemImpl(
            inner = inner.item,
            metadata = inner.getMetadata(),
        )

    override val isEmpty: Boolean
        get() = inner.stackSize == 0

    override val name: Text
        get() = TextImpl(ChatComponentText(inner.displayName))

    override fun withAmount(amount: Int) = ItemStackImpl(inner.copy().apply { stackSize = amount })
}