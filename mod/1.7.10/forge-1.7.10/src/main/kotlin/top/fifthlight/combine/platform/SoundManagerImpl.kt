package top.fifthlight.combine.platform

import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.client.audio.SoundHandler
import net.minecraft.util.ResourceLocation
import top.fifthlight.combine.sound.SoundKind
import top.fifthlight.combine.sound.SoundManager as CombineSoundManager

class SoundManagerImpl(
    private val soundManager: SoundHandler
) : CombineSoundManager {
    override fun play(kind: SoundKind, pitch: Float) {
        val soundName = when (kind) {
            SoundKind.BUTTON_PRESS -> "gui.button.press"
        }
        soundManager.playSound(PositionedSoundRecord.func_147674_a(ResourceLocation(soundName), pitch))
    }
}