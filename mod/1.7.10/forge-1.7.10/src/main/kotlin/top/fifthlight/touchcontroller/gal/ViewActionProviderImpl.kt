package top.fifthlight.touchcontroller.gal

import net.minecraft.client.Minecraft
import net.minecraft.util.MovingObjectPosition
import top.fifthlight.touchcontroller.common.gal.CrosshairTarget
import top.fifthlight.touchcontroller.common.gal.ViewActionProvider

object ViewActionProviderImpl : ViewActionProvider {
    private val client = Minecraft.getMinecraft()

    override fun getCrosshairTarget(): CrosshairTarget? {
        val target = client.objectMouseOver ?: return null
        return when (target.typeOfHit) {
            MovingObjectPosition.MovingObjectType.ENTITY -> CrosshairTarget.ENTITY
            MovingObjectPosition.MovingObjectType.BLOCK -> CrosshairTarget.BLOCK
            MovingObjectPosition.MovingObjectType.MISS -> CrosshairTarget.MISS
            else -> return null
        }
    }

    override fun getCurrentBreakingProgress(): Float {
        // curBlockDamageMP приватное поле в 1.7.10, используем рефлексию
        return try {
            val field = client.playerController.javaClass.getDeclaredField("curBlockDamageMP")
            field.isAccessible = true
            field.getFloat(client.playerController)
        } catch (e: Exception) {
            0f
        }
    }
}