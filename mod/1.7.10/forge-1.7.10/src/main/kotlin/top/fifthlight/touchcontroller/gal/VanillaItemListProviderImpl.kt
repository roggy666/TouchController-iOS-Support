package top.fifthlight.touchcontroller.gal

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.ChatComponentTranslation
import top.fifthlight.combine.platform.TextImpl
import top.fifthlight.combine.platform.toCombine
import top.fifthlight.touchcontroller.common.gal.PlayerHandle
import top.fifthlight.touchcontroller.common.gal.VanillaItemListProvider

object VanillaItemListProviderImpl : VanillaItemListProvider {
    class CreativeTabImpl(group: CreativeTabs, items: PersistentList<ItemStack>) :
        VanillaItemListProvider.CreativeTab {
        override val type = when (group.tabIndex) {
            // В 1.7.10 нет INVENTORY и SEARCH табов, используем индексы
            0 -> VanillaItemListProvider.CreativeTab.Type.CATEGORY // tabBlock
            1 -> VanillaItemListProvider.CreativeTab.Type.CATEGORY // tabDecorations
            else -> VanillaItemListProvider.CreativeTab.Type.CATEGORY
        }

        override val name = TextImpl(ChatComponentTranslation(group.tabLabel))

        override val icon = group.iconItemStack.toCombine()

        override val items = items.map(ItemStack::toCombine).toPersistentList()
    }

    private val creativeTabs by lazy {
        // В 1.7.10 CreativeTabs - это массив, доступный через поле creativeTabArray
        CreativeTabs.creativeTabArray
            .filterNotNull()
            .map {
                val list = mutableListOf<ItemStack>()
                Item.itemRegistry.forEach { item ->
                    if (item is Item) {
                        item.getSubItems(item, it, list)
                    }
                }
                CreativeTabImpl(it, list.toPersistentList())
            }
            .toPersistentList()
    }

    override fun getCreativeTabs(player: PlayerHandle): PersistentList<VanillaItemListProvider.CreativeTab> = creativeTabs
}