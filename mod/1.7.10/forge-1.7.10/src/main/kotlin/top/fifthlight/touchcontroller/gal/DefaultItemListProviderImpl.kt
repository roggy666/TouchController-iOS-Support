package top.fifthlight.touchcontroller.gal

import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.Item
import top.fifthlight.combine.platform.ItemFactoryImpl
import top.fifthlight.combine.platform.ItemImpl
import top.fifthlight.touchcontroller.common.config.ItemList
import top.fifthlight.touchcontroller.common.gal.DefaultItemListProvider

object DefaultItemListProviderImpl : DefaultItemListProvider {
    override val usableItems = ItemList(
        whitelist = persistentListOf(
            ItemImpl(Items.bow),
            ItemImpl(Items.fishing_rod),
            ItemImpl(Items.map),
            // SHIELD, KNOWLEDGE_BOOK не существуют в 1.7.10
            ItemImpl(Items.writable_book),
            ItemImpl(Items.written_book),
            ItemImpl(Items.ender_eye),
            ItemImpl(Items.ender_pearl),
            ItemImpl(Items.potionitem),
            ItemImpl(Items.snowball),
            ItemImpl(Items.egg),
            // SPLASH_POTION, LINGERING_POTION не существуют в 1.7.10
            ItemImpl(Items.experience_bottle),
            ItemImpl(Items.milk_bucket),
        ),
        subclasses = persistentSetOf(
            ItemFactoryImpl.armorSubclass,
            ItemFactoryImpl.foodSubclass,
        ),
    )

    override val showCrosshairItems = ItemList(
        whitelist = persistentListOf(
            ItemImpl(Items.egg),
            ItemImpl(Items.snowball),
            ItemImpl(Items.bow),
            // SPLASH_POTION, LINGERING_POTION не существуют в 1.7.10
            ItemImpl(Items.experience_bottle),
            ItemImpl(Items.ender_eye),
        ),
    )

    override val crosshairAimingItems = ItemList(
        whitelist = persistentListOf(
            ItemImpl(Items.ender_eye),
            ItemImpl(Item.getItemFromBlock(Blocks.waterlily)),
        ),
        subclasses = persistentSetOf(
            ItemFactoryImpl.bucketSubclass,
            // universalBucketSubclass не существует в 1.7.10
            ItemFactoryImpl.boatSubclass,
            ItemFactoryImpl.monsterPlacerSubclass,
        ),
    )
}