@file:Suppress("unused")

package top.fifthlight.touchcontroller.helper

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.EntityRenderer
import net.minecraft.entity.Entity
import net.minecraft.util.MathHelper
import net.minecraft.util.MovingObjectPosition
import net.minecraft.util.Vec3
import org.joml.Matrix4f
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import top.fifthlight.touchcontroller.common.config.GlobalConfigHolder
import top.fifthlight.touchcontroller.common.helper.CrosshairTargetHelper.getCrosshairDirection
import top.fifthlight.touchcontroller.common.helper.CrosshairTargetHelper.lastCrosshairDirection
import top.fifthlight.touchcontroller.common.model.ControllerHudModel

object EntityRendererHelper : KoinComponent {
    private val globalConfigHolder: GlobalConfigHolder by inject()
    private val controllerHudModel: ControllerHudModel by inject()
    private val client = Minecraft.getMinecraft()

    @JvmStatic
    fun doDisableMouseDirection(): Boolean {
        var config = globalConfigHolder.config.value
        return config.regular.disableMouseMove
    }

    private fun getProjectionMatrix(farPlaneDistance: Float, fov: Float): Matrix4f {
        val aspect = client.displayWidth.toFloat() / client.displayHeight.toFloat()
        // В 1.7.10 MathHelper.sqrt_2 может быть обфусцированным полем, используем вычисление
        val sqrt2 = kotlin.math.sqrt(2.0).toFloat()
        return Matrix4f().setPerspective(Math.toRadians(fov.toDouble()).toFloat(), aspect, 0.05f, farPlaneDistance * sqrt2)
    }

    private fun getViewVector(fov: Float, farPlaneDistance: Float, entity: Entity, partialTicks: Float): Vec3 {
        val projectionMatrix = getProjectionMatrix(farPlaneDistance, fov)
        val cameraPitchDegrees = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks
        val cameraPitchRadians = Math.toRadians(cameraPitchDegrees.toDouble())
        val cameraYawDegrees = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks
        val cameraYawRadians = Math.toRadians(cameraYawDegrees.toDouble())
        val direction = getCrosshairDirection(projectionMatrix, cameraPitchRadians, cameraYawRadians)
        lastCrosshairDirection = direction

        return Vec3.createVectorHelper(direction.x.toDouble(), direction.y.toDouble(), direction.z.toDouble())
    }

    @JvmStatic
    fun getLook(entity: Entity, entityRenderer: EntityRenderer, partialTicks: Float): Vec3 {
        // getFOVModifier и farPlaneDistance приватные в 1.7.10, используем рефлексию
        val fov = try {
            val method = EntityRenderer::class.java.getDeclaredMethod("getFOVModifier", Float::class.javaPrimitiveType, Boolean::class.javaPrimitiveType)
            method.isAccessible = true
            method.invoke(entityRenderer, partialTicks, true) as Float
        } catch (e: Exception) {
            70f // fallback FOV
        }
        val farPlane = try {
            val field = EntityRenderer::class.java.getDeclaredField("farPlaneDistance")
            field.isAccessible = true
            field.getFloat(entityRenderer)
        } catch (e: Exception) {
            256f // fallback far plane
        }
        return getViewVector(fov, farPlane, entity, partialTicks)
    }

    @JvmStatic
    fun rayTrace(entity: Entity, blockReachDistance: Double, partialTicks: Float, entityRenderer: EntityRenderer): MovingObjectPosition? {
        val position = Vec3.createVectorHelper(
            entity.prevPosX + (entity.posX - entity.prevPosX) * partialTicks,
            entity.prevPosY + (entity.posY - entity.prevPosY) * partialTicks + 1.62,
            entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTicks
        )
        val fov = try {
            val method = EntityRenderer::class.java.getDeclaredMethod("getFOVModifier", Float::class.javaPrimitiveType, Boolean::class.javaPrimitiveType)
            method.isAccessible = true
            method.invoke(entityRenderer, partialTicks, true) as Float
        } catch (e: Exception) {
            70f
        }
        val farPlane = try {
            val field = EntityRenderer::class.java.getDeclaredField("farPlaneDistance")
            field.isAccessible = true
            field.getFloat(entityRenderer)
        } catch (e: Exception) {
            256f
        }
        val direction = getViewVector(fov, farPlane, entity, partialTicks)
        val endPosition = position.addVector(direction.xCoord * blockReachDistance, direction.yCoord * blockReachDistance, direction.zCoord * blockReachDistance)
        // В 1.7.10 rayTraceBlocks принимает только 3 параметра
        return entity.worldObj.rayTraceBlocks(position, endPosition, false)
    }

    @JvmStatic
    fun doDisableBlockOutline() = !controllerHudModel.result.showBlockOutline
}
