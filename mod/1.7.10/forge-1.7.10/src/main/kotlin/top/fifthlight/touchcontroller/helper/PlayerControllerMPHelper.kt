@file:Suppress("unused")

package top.fifthlight.touchcontroller.helper

import net.minecraft.client.multiplayer.PlayerControllerMP
import net.minecraft.entity.player.EntityPlayer
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import top.fifthlight.combine.platform.toCombine
import top.fifthlight.touchcontroller.common.config.GlobalConfigHolder
import top.fifthlight.touchcontroller.common.helper.CrosshairTargetHelper

object PlayerControllerMPHelper : KoinComponent {
    private val globalConfigHolder: GlobalConfigHolder by inject()

    private var prevYaw: Float = 0f
    private var prevPitch: Float = 0f
    private var resetPlayerLookTarget = false

    @JvmStatic
    fun beforeUsingItem(controller: PlayerControllerMP, player: EntityPlayer) {
        // В 1.7.10 нет EnumHand, используем getHeldItem() без параметров
        val itemStack = player.getHeldItem()
        if (itemStack == null) {
            return
        }
        val crosshairAimingItems = globalConfigHolder.config.value.item.crosshairAimingItems
        if (itemStack.item.toCombine() !in crosshairAimingItems) {
            return
        }

        prevYaw = player.rotationYaw
        prevPitch = player.rotationPitch

        val (yaw, pitch) = CrosshairTargetHelper.calculatePlayerRotation(CrosshairTargetHelper.lastCrosshairDirection)
        player.rotationYaw = yaw
        player.rotationPitch = pitch

        // netClientHandler приватное поле, используем рефлексию
        try {
            val field = PlayerControllerMP::class.java.getDeclaredField("netClientHandler")
            field.isAccessible = true
            val handler = field.get(controller) as net.minecraft.client.network.NetHandlerPlayClient
            handler.addToSendQueue(PacketHelper.createPlayerLookPacket(yaw, pitch, player.onGround))
        } catch (e: Exception) {
            // Игнорируем ошибки рефлексии
        }

        resetPlayerLookTarget = true
    }

    @JvmStatic
    fun afterUsingItem(controller: PlayerControllerMP, player: EntityPlayer) {
        if (!resetPlayerLookTarget) {
            return
        }
        resetPlayerLookTarget = false
        player.rotationYaw = prevYaw
        player.rotationPitch = prevPitch

        // netClientHandler приватное поле, используем рефлексию
        try {
            val field = PlayerControllerMP::class.java.getDeclaredField("netClientHandler")
            field.isAccessible = true
            val handler = field.get(controller) as net.minecraft.client.network.NetHandlerPlayClient
            handler.addToSendQueue(PacketHelper.createPlayerLookPacket(player.rotationYaw, player.rotationPitch, player.onGround))
        } catch (e: Exception) {
            // Игнорируем ошибки рефлексии
        }
    }
}