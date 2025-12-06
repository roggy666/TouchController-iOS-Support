package top.fifthlight.touchcontroller.helper

import top.fifthlight.touchcontroller.common.event.RenderEvents

object MinecraftHelper {
    @JvmStatic
    fun onRenderStart() {
        RenderEvents.onRenderStart()
    }
}