package top.fifthlight.touchcontroller.gal

import net.minecraft.client.Minecraft
import net.minecraft.client.settings.KeyBinding
import net.minecraft.util.ChatComponentTranslation
import top.fifthlight.combine.data.Text
import top.fifthlight.combine.platform.TextImpl
import top.fifthlight.touchcontroller.common.gal.DefaultKeyBindingType
import top.fifthlight.touchcontroller.common.gal.KeyBindingHandler
import top.fifthlight.touchcontroller.common.gal.KeyBindingState

class KeyBindingStateImpl(
    val keyBinding: KeyBinding,
) : KeyBindingState() {
    override val id: String
        get() = keyBinding.keyDescription

    override val name: Text
        get() = TextImpl(ChatComponentTranslation(keyBinding.keyDescription))

    override val categoryId: String
        get() = keyBinding.keyCategory

    override val categoryName: Text
        get() = TextImpl(ChatComponentTranslation(keyBinding.keyCategory))

    override fun click() {
        super.click()
        // pressTime приватное в 1.7.10, используем рефлексию
        try {
            val field = KeyBinding::class.java.getDeclaredField("pressTime")
            field.isAccessible = true
            field.setInt(keyBinding, field.getInt(keyBinding) + 1)
        } catch (e: Exception) {
            // Игнорируем ошибки рефлексии
        }
    }

    override fun haveClickCount(): Boolean {
        return try {
            val field = KeyBinding::class.java.getDeclaredField("pressTime")
            field.isAccessible = true
            field.getInt(keyBinding) > 0
        } catch (e: Exception) {
            false
        }
    }
}

object KeyBindingHandlerImpl : KeyBindingHandler() {
    private val client = Minecraft.getMinecraft()
    private val options = client.gameSettings
    private val state = mutableMapOf<KeyBinding, KeyBindingStateImpl>()

    private fun DefaultKeyBindingType.toMinecraft() = when (this) {
        DefaultKeyBindingType.ATTACK -> options.keyBindAttack
        DefaultKeyBindingType.USE -> options.keyBindUseItem
        DefaultKeyBindingType.INVENTORY -> options.keyBindInventory
        // В 1.7.10 нет второй руки, используем keyBindDrop как ближайший аналог
        DefaultKeyBindingType.SWAP_HANDS -> options.keyBindDrop
        DefaultKeyBindingType.SNEAK -> options.keyBindSneak
        DefaultKeyBindingType.SPRINT -> options.keyBindSprint
        DefaultKeyBindingType.JUMP -> options.keyBindJump
        DefaultKeyBindingType.PLAYER_LIST -> options.keyBindPlayerList
        DefaultKeyBindingType.LEFT -> options.keyBindLeft
        DefaultKeyBindingType.RIGHT -> options.keyBindRight
        DefaultKeyBindingType.UP -> options.keyBindForward
        DefaultKeyBindingType.DOWN -> options.keyBindBack
    }

    fun isDown(key: KeyBinding) = state[key]?.let { it.clicked || it.locked } == true

    private fun getState(key: KeyBinding) = state.getOrPut(key) {
        KeyBindingStateImpl(key)
    }

    override fun getState(type: DefaultKeyBindingType): KeyBindingState {
        return getState(type.toMinecraft())
    }

    override fun getState(id: String): KeyBindingState? {
        // В 1.7.10 нужно получить доступ к hash через рефлексию
        return try {
            val field = KeyBinding::class.java.getDeclaredField("hash")
            field.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val hash = field.get(null) as java.util.Map<String, KeyBinding>
            hash[id]?.let { getState(it) }
        } catch (e: Exception) {
            null
        }
    }

    override fun getAllStates(): Map<String, KeyBindingState> {
        return try {
            val field = KeyBinding::class.java.getDeclaredField("hash")
            field.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val hash = field.get(null) as java.util.Map<String, KeyBinding>
            // java.util.Map использует entrySet(), не entries
            hash.entrySet().associate { entry -> entry.key to getState(entry.value) }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    override fun getExistingStates(): Collection<KeyBindingState> = state.values
}
