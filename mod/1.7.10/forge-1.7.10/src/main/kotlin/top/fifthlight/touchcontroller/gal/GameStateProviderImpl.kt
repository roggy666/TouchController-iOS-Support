package top.fifthlight.touchcontroller.gal

import net.minecraft.client.Minecraft
import top.fifthlight.touchcontroller.common.gal.CameraPerspective
import top.fifthlight.touchcontroller.common.gal.GameState
import top.fifthlight.touchcontroller.common.gal.GameStateProvider

object GameStateProviderImpl : GameStateProvider {
    private val client = Minecraft.getMinecraft()

    override fun currentState(): GameState = GameState(
        inGame = client.thePlayer != null,
        inGui = client.currentScreen != null,
        perspective = when (client.gameSettings.thirdPersonView) {
            0 -> CameraPerspective.FIRST_PERSON
            1 -> CameraPerspective.THIRD_PERSON_BACK
            2 -> CameraPerspective.THIRD_PERSON_FRONT
            // Unknown perspective
            else -> CameraPerspective.FIRST_PERSON
        },
    )
}