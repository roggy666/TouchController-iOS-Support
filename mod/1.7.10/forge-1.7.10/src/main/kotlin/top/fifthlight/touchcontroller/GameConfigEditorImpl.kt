package top.fifthlight.touchcontroller

import net.minecraft.client.Minecraft
import net.minecraft.client.settings.GameSettings
import top.fifthlight.touchcontroller.common.config.GameConfigEditor
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

object GameConfigEditorImpl : GameConfigEditor {
    private val pendingCallbackLock = ReentrantLock()
    private var pendingCallbacks: MutableList<GameConfigEditor.Callback>? = mutableListOf()

    private class EditorImpl(val options: GameSettings) : GameConfigEditor.Editor {
        override var autoJump: Boolean
            get() {
                // Ищем autoJump через рефлексию, так как в документации не указано
                return try {
                    val field = GameSettings::class.java.getDeclaredField("autoJump")
                    field.isAccessible = true
                    field.getBoolean(options)
                } catch (e: Exception) {
                    false
                }
            }
            set(value) {
                try {
                    val field = GameSettings::class.java.getDeclaredField("autoJump")
                    field.isAccessible = true
                    field.setBoolean(options, value)
                } catch (e: Exception) {
                    // Игнорируем если поле не найдено
                }
            }
    }

    fun executePendingCallback() {
        pendingCallbackLock.withLock {
            val callbacks = pendingCallbacks
            if (callbacks == null) {
                return
            }
            pendingCallbacks = null
            if (callbacks.isNotEmpty()) {
                with(EditorImpl(Minecraft.getMinecraft().gameSettings)) {
                    callbacks.forEach { callback ->
                        callback.invoke(this)
                    }
                    options.saveOptions()
                }
            }
        }
    }

    override fun submit(callback: GameConfigEditor.Callback) {
        pendingCallbackLock.withLock {
            pendingCallbacks?.add(callback) ?: run {
                with(EditorImpl(Minecraft.getMinecraft().gameSettings)) {
                    callback.invoke(this)
                    options.saveOptions()
                }
            }
        }
    }
}
