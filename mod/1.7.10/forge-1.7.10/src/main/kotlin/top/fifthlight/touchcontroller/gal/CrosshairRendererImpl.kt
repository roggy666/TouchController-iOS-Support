package top.fifthlight.touchcontroller.gal

import net.minecraft.client.renderer.Tessellator
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL14
import top.fifthlight.combine.paint.Canvas
import top.fifthlight.data.Offset
import top.fifthlight.touchcontroller.common.config.TouchRingConfig
import top.fifthlight.touchcontroller.common.gal.CrosshairRenderer
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private const val CROSSHAIR_CIRCLE_PARTS = 24
private const val CROSSHAIR_CIRCLE_ANGLE = 2 * PI.toFloat() / CROSSHAIR_CIRCLE_PARTS

private fun point(angle: Float, radius: Float) = Offset(
    x = cos(angle) * radius,
    y = sin(angle) * radius
)

object CrosshairRendererImpl : CrosshairRenderer {
    override fun renderOuter(canvas: Canvas, config: TouchRingConfig) {
        val tessellator = Tessellator.instance
        tessellator.startDrawingQuads()
        val innerRadius = config.radius.toFloat()
        val outerRadius = (config.radius + config.outerRadius).toFloat()
        var angle = -PI.toFloat() / 2f
        for (i in 0 until CROSSHAIR_CIRCLE_PARTS) {
            val endAngle = angle + CROSSHAIR_CIRCLE_ANGLE
            val point0 = point(angle, outerRadius)
            val point1 = point(endAngle, outerRadius)
            val point2 = point(angle, innerRadius)
            val point3 = point(endAngle, innerRadius)
            angle = endAngle

            tessellator.addVertex(point0.x.toDouble(), point0.y.toDouble(), 0.0)
            tessellator.addVertex(point2.x.toDouble(), point2.y.toDouble(), 0.0)
            tessellator.addVertex(point3.x.toDouble(), point3.y.toDouble(), 0.0)
            tessellator.addVertex(point1.x.toDouble(), point1.y.toDouble(), 0.0)
        }

        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glColor4f(1f, 1f, 1f, 1f)
        GL11.glEnable(GL11.GL_BLEND)
        GL14.glBlendFuncSeparate(
            GL11.GL_ONE_MINUS_DST_COLOR,
            GL11.GL_ONE_MINUS_SRC_COLOR,
            GL11.GL_ONE,
            GL11.GL_ZERO
        )
        tessellator.draw()
        GL14.glBlendFuncSeparate(
            GL11.GL_SRC_ALPHA,
            GL11.GL_ONE_MINUS_SRC_ALPHA,
            GL11.GL_ONE,
            GL11.GL_ZERO
        )
        GL11.glEnable(GL11.GL_TEXTURE_2D)
    }

    override fun renderInner(canvas: Canvas, config: TouchRingConfig, progress: Float) {
        val tessellator = Tessellator.instance
        tessellator.startDrawing(GL11.GL_TRIANGLE_FAN)
        tessellator.addVertex(0.0, 0.0, 0.0)

        var angle = 0f
        for (i in 0..CROSSHAIR_CIRCLE_PARTS) {
            val point = point(angle, config.radius * progress)
            angle -= CROSSHAIR_CIRCLE_ANGLE

            tessellator.addVertex(point.x.toDouble(), point.y.toDouble(), 0.0)
        }

        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glColor4f(1f, 1f, 1f, 1f)
        GL11.glEnable(GL11.GL_BLEND)
        GL14.glBlendFuncSeparate(
            GL11.GL_ONE_MINUS_DST_COLOR,
            GL11.GL_ONE_MINUS_SRC_COLOR,
            GL11.GL_ONE,
            GL11.GL_ZERO
        )
        tessellator.draw()
        GL14.glBlendFuncSeparate(
            GL11.GL_SRC_ALPHA,
            GL11.GL_ONE_MINUS_SRC_ALPHA,
            GL11.GL_ONE,
            GL11.GL_ZERO
        )
        GL11.glEnable(GL11.GL_TEXTURE_2D)
    }
}