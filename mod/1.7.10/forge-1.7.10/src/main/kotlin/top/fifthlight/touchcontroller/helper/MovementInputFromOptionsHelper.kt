@file:Suppress("unused")

package top.fifthlight.touchcontroller.helper

import net.minecraft.client.Minecraft
import net.minecraft.util.MovementInput
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import top.fifthlight.touchcontroller.common.model.ControllerHudModel

object MovementInputFromOptionsHelper : KoinComponent {
    private val controllerHudModel: ControllerHudModel by inject()

    @JvmStatic
    fun onEndTick(input: MovementInput) {
        val client = Minecraft.getMinecraft()
        if (client.currentScreen != null) {
            return
        }

        val result = controllerHudModel.result

        input.moveForward += result.forward
        input.moveStrafe += result.left
        input.moveForward = input.moveForward.coerceIn(-1f, 1f)
        input.moveStrafe = input.moveStrafe.coerceIn(-1f, 1f)

        // В 1.7.10 используются другие поля (jump вместо forwardKeyDown и т.д.)
        // Используем рефлексию для доступа к приватным полям
        try {
            // field_78902_a - forward, field_78900_b - back, field_78901_c - left, field_78899_d - right
            val forwardField = MovementInput::class.java.getDeclaredField("field_78902_a")
            val backField = MovementInput::class.java.getDeclaredField("field_78900_b")
            val leftField = MovementInput::class.java.getDeclaredField("field_78901_c")
            val rightField = MovementInput::class.java.getDeclaredField("field_78899_d")

            forwardField.isAccessible = true
            backField.isAccessible = true
            leftField.isAccessible = true
            rightField.isAccessible = true

            forwardField.setBoolean(input, forwardField.getBoolean(input) || result.forward > 0.5f || (result.boatLeft && result.boatRight))
            backField.setBoolean(input, backField.getBoolean(input) || result.forward < -0.5f)
            leftField.setBoolean(input, leftField.getBoolean(input) || result.left > 0.5f || (!result.boatLeft && result.boatRight))
            rightField.setBoolean(input, rightField.getBoolean(input) || result.left < -0.5f || (result.boatLeft && !result.boatRight))
        } catch (e: Exception) {
            // Игнорируем ошибки рефлексии
        }
    }
}
