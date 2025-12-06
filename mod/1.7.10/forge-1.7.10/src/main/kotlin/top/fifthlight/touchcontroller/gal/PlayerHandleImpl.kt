package top.fifthlight.touchcontroller.gal

import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import net.minecraft.block.material.Material
import net.minecraft.client.Minecraft
import net.minecraft.entity.item.EntityBoat
import net.minecraft.entity.item.EntityMinecart
import net.minecraft.entity.passive.*
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import top.fifthlight.combine.data.Item
import top.fifthlight.combine.platform.ItemStackImpl
import top.fifthlight.combine.platform.toCombine
import top.fifthlight.touchcontroller.common.config.ItemList
import top.fifthlight.touchcontroller.common.gal.PlayerHandle
import top.fifthlight.touchcontroller.common.gal.PlayerHandleFactory
import top.fifthlight.touchcontroller.common.gal.PlayerInventory
import top.fifthlight.touchcontroller.common.gal.RidingEntityType
import top.fifthlight.combine.data.ItemStack as CombineItemStack

@JvmInline
value class PlayerHandleImpl(val inner: EntityPlayer) : PlayerHandle {
    private val client: Minecraft
        get() = Minecraft.getMinecraft()

    override fun hasItemsOnHand(list: ItemList): Boolean {
        // В 1.7.10 нет двуручной системы, проверяем только основной предмет
        val heldItem = inner.heldItem ?: return false
        return heldItem.toCombine().item in list
    }

    override fun matchesItemOnHand(item: Item): Boolean {
        // В 1.7.10 нет двуручной системы, проверяем только основной предмет
        val heldItem = inner.heldItem ?: return false
        return item.matches(heldItem.toCombine().item)
    }

    override fun changeLookDirection(deltaYaw: Double, deltaPitch: Double) {
        // В 1.7.10 нет метода turn, управляем напрямую углами
        inner.rotationYaw += deltaYaw.toFloat()
        inner.rotationPitch = (inner.rotationPitch - deltaPitch.toFloat()).coerceIn(-90f, 90f)
    }

    override var currentSelectedSlot: Int
        get() = inner.inventory.currentItem
        set(value) {
            inner.inventory.currentItem = value
        }

    override fun dropSlot(index: Int) {
        if (index == currentSelectedSlot) {
            inner.dropOneItem(true)
            return
        }

        val originalSlot = currentSelectedSlot
        val interactionManager = client.playerController

        // Меняем слот, дропаем, возвращаем слот обратно
        currentSelectedSlot = index
        // В 1.7.10 нет syncCurrentPlayItem, он вызывается автоматически
        
        inner.dropOneItem(true)

        currentSelectedSlot = originalSlot
    }

    override fun getInventorySlot(index: Int): CombineItemStack {
        val stack = inner.inventory.getStackInSlot(index)
        return if (stack == null) {
            ItemStackImpl(ItemStack(null as net.minecraft.item.Item?, 0))
        } else {
            ItemStackImpl(stack)
        }
    }

    override fun getInventory() = PlayerInventory(
        main = inner.inventory.mainInventory.map { 
            if (it == null) ItemStackImpl(ItemStack(null as net.minecraft.item.Item?, 0))
            else it.toCombine()
        }.toPersistentList(),
        armor = inner.inventory.armorInventory.map { 
            if (it == null) ItemStackImpl(ItemStack(null as net.minecraft.item.Item?, 0))
            else it.toCombine()
        }.toPersistentList(),
        // В 1.7.10 нет offHand, возвращаем пустой ItemStack
        offHand = ItemStackImpl(ItemStack(null as net.minecraft.item.Item?, 0)),
    )

    override val isUsingItem: Boolean
        get() = inner.isUsingItem()

    override val onGround: Boolean
        get() = inner.onGround

    override var isFlying: Boolean
        get() = inner.capabilities.isFlying
        set(value) {
            inner.capabilities.isFlying = value
        }

    override val isSubmergedInWater: Boolean
        get() = inner.isInsideOfMaterial(Material.water)

    override val isTouchingWater: Boolean
        get() = inner.isInWater

    override var isSprinting: Boolean
        get() = inner.isSprinting
        set(value) {
            inner.isSprinting = value
        }

    override val isSneaking: Boolean
        get() = inner.isSneaking

    override val ridingEntityType: RidingEntityType?
        get() = when (inner.ridingEntity) {
            null -> null
            is EntityMinecart -> RidingEntityType.MINECART
            is EntityBoat -> RidingEntityType.BOAT
            is EntityPig -> RidingEntityType.PIG
            is EntityHorse -> RidingEntityType.HORSE
            // В 1.7.10 нет EntityDonkey, EntityMule, EntityLlama, EntityZombieHorse, EntitySkeletonHorse
            else -> RidingEntityType.OTHER
        }

    override val canFly: Boolean
        get() = inner.capabilities.allowFlying
}

object PlayerHandleFactoryImpl : PlayerHandleFactory {
    private val client = Minecraft.getMinecraft()

    // В 1.7.10 это thePlayer, а не player
    override fun getPlayerHandle(): PlayerHandle? = client.thePlayer?.let(::PlayerHandleImpl)
}