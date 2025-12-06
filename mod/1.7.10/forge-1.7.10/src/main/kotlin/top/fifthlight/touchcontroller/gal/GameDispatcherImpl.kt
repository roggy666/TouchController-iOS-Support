package top.fifthlight.touchcontroller.gal

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import net.minecraft.client.Minecraft
import top.fifthlight.touchcontroller.common.gal.GameDispatcher
import kotlin.coroutines.CoroutineContext

object GameDispatcherImpl : GameDispatcher() {
    private val client = Minecraft.getMinecraft()

    // В 1.7.10 нет isCallingFromMinecraftThread, проверяем по имени потока
    private fun isMinecraftThread() = Thread.currentThread().name == "Client thread"

    override fun isDispatchNeeded(context: CoroutineContext) = !isMinecraftThread()

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (isMinecraftThread()) {
            Dispatchers.Unconfined.dispatch(context, block)
        } else {
            // В 1.7.10 нет addScheduledTask, выполняем блок напрямую
            // Это не идеально, но для большинства случаев должно работать
            block.run()
        }
    }
}