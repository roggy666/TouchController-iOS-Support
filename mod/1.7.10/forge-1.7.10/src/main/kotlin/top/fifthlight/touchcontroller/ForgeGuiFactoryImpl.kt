package top.fifthlight.touchcontroller

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import cpw.mods.fml.client.IModGuiFactory
import cpw.mods.fml.client.IModGuiFactory.RuntimeOptionCategoryElement
import org.koin.core.component.KoinComponent
import top.fifthlight.touchcontroller.common.ui.screen.getConfigScreen

@Suppress("unused")
class ForgeGuiFactoryImpl : IModGuiFactory, KoinComponent {
    override fun initialize(minecraftInstance: Minecraft) {}

    override fun mainConfigGuiClass(): Class<out GuiScreen> = ConfigGuiWrapper::class.java

    override fun runtimeGuiCategories() = setOf<RuntimeOptionCategoryElement>()

    override fun getHandlerFor(element: RuntimeOptionCategoryElement): IModGuiFactory.RuntimeOptionGuiHandler? = null

    class ConfigGuiWrapper(private val parentScreen: GuiScreen) : GuiScreen() {
        private val actualScreen = getConfigScreen(parentScreen) as GuiScreen

        override fun initGui() {
            mc.displayGuiScreen(actualScreen)
        }
    }
}