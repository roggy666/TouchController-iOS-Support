package top.fifthlight.touchcontroller.ext

import net.minecraft.client.renderer.Tessellator
import top.fifthlight.combine.paint.Color

// В 1.7.10 нет BufferBuilder, используется Tessellator
// Этот файл нужно полностью переписать под Tessellator

// Если вам нужен аналог BufferBuilder.color(), используйте:
fun Tessellator.setColorRGBA(color: Color): Tessellator {
    this.setColorRGBA(color.r, color.g, color.b, color.a)
    return this
}

// Пример использования в 1.7.10:
// val tessellator = Tessellator.instance
// tessellator.startDrawingQuads()
// tessellator.setColorRGBA(color)
// tessellator.addVertex(x, y, z)
// tessellator.draw()