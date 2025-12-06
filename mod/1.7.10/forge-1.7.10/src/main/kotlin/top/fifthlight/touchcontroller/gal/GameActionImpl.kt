package top.fifthlight.touchcontroller.gal

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiChat
import net.minecraft.util.ScreenShotHelper
import top.fifthlight.combine.data.Text
import top.fifthlight.combine.platform.toMinecraft
import top.fifthlight.touchcontroller.common.gal.GameAction

object GameActionImpl : GameAction {
    private val client = Minecraft.getMinecraft()

    override fun openChatScreen() {
        client.displayGuiScreen(GuiChat())
    }

    override fun openGameMenu() {
        client.displayInGameMenu()
    }

    override fun sendMessage(text: Text) {
        client.ingameGUI.chatGUI.printChatMessage(text.toMinecraft())
    }

    override fun nextPerspective() {
        client.gameSettings.thirdPersonView++
        if (client.gameSettings.thirdPersonView > 2) {
            client.gameSettings.thirdPersonView = 0
        }

        // В 1.7.10 нет loadEntityShader и setDisplayListEntitiesDirty
        // Переключение перспективы обрабатывается автоматически
    }

    override fun takeScreenshot() {
        ScreenShotHelper.saveScreenshot(
            client.mcDataDir,
            client.displayWidth,
            client.displayHeight,
            client.framebuffer
        ).let { message ->
            client.ingameGUI.chatGUI.printChatMessage(message)
        }
    }

    override var hudHidden: Boolean
        get() = client.gameSettings.hideGUI
        set(value) {
            client.gameSettings.hideGUI = value
        }
}