package top.fifthlight.combine.platform

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GLAllocation
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.entity.RenderItem
import net.minecraft.util.ResourceLocation
import org.joml.Matrix4f
import org.lwjgl.opengl.GL11
import top.fifthlight.combine.data.BackgroundTexture
import top.fifthlight.combine.data.Identifier
import top.fifthlight.combine.data.ItemStack
import top.fifthlight.combine.data.Texture
import top.fifthlight.combine.paint.Canvas
import top.fifthlight.combine.paint.ClipStack
import top.fifthlight.combine.paint.Color
import top.fifthlight.combine.paint.Colors
import top.fifthlight.data.*
import top.fifthlight.touchcontroller.assets.Textures
import top.fifthlight.combine.data.Text as CombineText

class CanvasImpl : Canvas, Gui() {
    companion object {
        private val IDENTIFIER_ATLAS = ResourceLocation("touchcontroller", "textures/gui/atlas.png")
    }

    private val client = Minecraft.getMinecraft()
    private val fontRenderer = client.fontRenderer
    override val textLineHeight: Int = fontRenderer.FONT_HEIGHT
    private val scaledResolution by lazy { ScaledResolution(client, client.displayWidth, client.displayHeight) }
    private val itemRenderer = RenderItem.getInstance()
    private val matrixBuffer = GLAllocation.createDirectFloatBuffer(16)
    private val matrixStack = arrayListOf<Matrix4f>(run {
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, matrixBuffer)
        Matrix4f().apply { set(matrixBuffer) }
    })

    private fun applyMatrix(matrix: Matrix4f) {
        matrix.get(matrixBuffer)
        GL11.glLoadMatrix(matrixBuffer)
    }

    override fun pushState() {
        val matrix = Matrix4f(matrixStack.last())
        matrixStack.add(matrix)
    }

    override fun popState() {
        matrixStack.removeLast<Matrix4f>()
        applyMatrix(matrixStack.last())
    }

    override fun translate(x: Int, y: Int) {
        matrixStack.last().apply {
            translate(x.toFloat(), y.toFloat(), 0f)
            applyMatrix(this)
        }
    }

    override fun translate(x: Float, y: Float) {
        matrixStack.last().apply {
            translate(x, y, 0f)
            applyMatrix(this)
        }
    }

    override fun rotate(degrees: Float) {
        matrixStack.last().apply {
            rotate(degrees, 0f, 0f, 1f)
            applyMatrix(this)
        }
    }

    override fun scale(x: Float, y: Float) {
        matrixStack.last().apply {
            scale(x, y, 1f)
            applyMatrix(this)
        }
    }

    override fun fillRect(offset: IntOffset, size: IntSize, color: Color) {
        drawRect(offset.x, offset.y, offset.x + size.width, offset.y + size.height, color.value)
        GL11.glEnable(GL11.GL_BLEND)
    }

    override fun fillGradientRect(
        offset: Offset,
        size: Size,
        leftTopColor: Color,
        leftBottomColor: Color,
        rightTopColor: Color,
        rightBottomColor: Color
    ) {
        val tessellator = Tessellator.instance
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_ALPHA_TEST)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glShadeModel(GL11.GL_SMOOTH)
        tessellator.startDrawingQuads()
        val dstRect = Rect(offset, size)
        tessellator.setColorRGBA_F(leftTopColor.r / 255f, leftTopColor.g / 255f, leftTopColor.b / 255f, leftTopColor.a / 255f)
        tessellator.addVertex(dstRect.left.toDouble(), dstRect.top.toDouble(), 0.0)
        tessellator.setColorRGBA_F(leftBottomColor.r / 255f, leftBottomColor.g / 255f, leftBottomColor.b / 255f, leftBottomColor.a / 255f)
        tessellator.addVertex(dstRect.left.toDouble(), dstRect.bottom.toDouble(), 0.0)
        tessellator.setColorRGBA_F(rightBottomColor.r / 255f, rightBottomColor.g / 255f, rightBottomColor.b / 255f, rightBottomColor.a / 255f)
        tessellator.addVertex(dstRect.right.toDouble(), dstRect.bottom.toDouble(), 0.0)
        tessellator.setColorRGBA_F(rightTopColor.r / 255f, rightTopColor.g / 255f, rightTopColor.b / 255f, rightTopColor.a / 255f)
        tessellator.addVertex(dstRect.right.toDouble(), dstRect.top.toDouble(), 0.0)
        tessellator.draw()
        GL11.glShadeModel(GL11.GL_FLAT)
        GL11.glEnable(GL11.GL_ALPHA_TEST)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
    }

    override fun drawRect(offset: IntOffset, size: IntSize, color: Color) {
        //  1 -> 2  |
        //  |    |  |
        //  4 -> 3 \|/

        val strokeSize = 1

        // 1 to 2
        fillRect(
            offset = offset,
            size = IntSize(
                width = size.width - strokeSize,
                height = strokeSize,
            ),
            color = color,
        )

        // 2 to 3
        fillRect(
            offset = IntOffset(
                x = offset.x + size.width - strokeSize,
                y = offset.y,
            ),
            size = IntSize(
                width = strokeSize,
                height = size.height - strokeSize,
            ),
            color = color,
        )

        // 4 to 3
        fillRect(
            offset = IntOffset(
                x = offset.x + strokeSize,
                y = offset.y + size.height - strokeSize,
            ),
            size = IntSize(
                width = size.width - strokeSize,
                height = strokeSize,
            ),
            color = color,
        )

        // 4 to 1
        fillRect(
            offset = IntOffset(
                x = offset.x,
                y = offset.y + strokeSize,
            ),
            size = IntSize(
                width = strokeSize,
                height = size.height - strokeSize,
            ),
            color = color,
        )
    }

    override fun drawText(offset: IntOffset, text: String, color: Color) {
        val x = offset.x
        var y = offset.y
        for (line in text.lineSequence()) {
            fontRenderer.drawString(line, x, y, color.value)
            y += fontRenderer.FONT_HEIGHT
        }
    }

    override fun drawText(offset: IntOffset, width: Int, text: String, color: Color) {
        if (width >= 16) {
            fontRenderer.drawSplitString(text, offset.x, offset.y, width, color.value)
        }
    }

    override fun drawText(offset: IntOffset, text: CombineText, color: Color) =
        drawText(offset, text.toMinecraft().formattedText, color)

    override fun drawText(offset: IntOffset, width: Int, text: CombineText, color: Color) {
        if (width > 0) {
            drawText(offset, width, text.toMinecraft().formattedText, color)
        }
    }

    private fun drawTexture(
        identifier: ResourceLocation,
        dstRect: Rect,
        uvRect: Rect,
        tint: Color = Colors.WHITE,
    ) {
        this.client.textureManager.bindTexture(identifier)
        GL11.glColor4f(tint.r / 256f, tint.g / 256f, tint.b / 256f, tint.a / 256f)
        val tessellator = Tessellator.instance
        tessellator.startDrawingQuads()
        tessellator.addVertexWithUV(dstRect.left.toDouble(), dstRect.top.toDouble(), 0.0, uvRect.left.toDouble(), uvRect.top.toDouble())
        tessellator.addVertexWithUV(dstRect.left.toDouble(), dstRect.bottom.toDouble(), 0.0, uvRect.left.toDouble(), uvRect.bottom.toDouble())
        tessellator.addVertexWithUV(dstRect.right.toDouble(), dstRect.bottom.toDouble(), 0.0, uvRect.right.toDouble(), uvRect.bottom.toDouble())
        tessellator.addVertexWithUV(dstRect.right.toDouble(), dstRect.top.toDouble(), 0.0, uvRect.right.toDouble(), uvRect.top.toDouble())
        tessellator.draw()
    }

    override fun drawTexture(
        identifier: Identifier,
        dstRect: Rect,
        uvRect: Rect,
        tint: Color,
    ) = drawTexture(
        identifier = identifier.toMinecraft(),
        dstRect = dstRect,
        uvRect = uvRect,
        tint = tint,
    )

    override fun drawTexture(
        texture: Texture,
        dstRect: Rect,
        srcRect: IntRect,
        tint: Color,
    ) = drawTexture(
        identifier = IDENTIFIER_ATLAS,
        dstRect = dstRect,
        uvRect = Rect(
            offset = (texture.atlasOffset + srcRect.offset).toOffset() / Textures.atlasSize.toSize(),
            size = srcRect.size.toSize() / Textures.atlasSize.toSize(),
        ),
        tint = tint,
    )

    override fun drawBackgroundTexture(
        texture: BackgroundTexture,
        scale: Float,
        dstRect: Rect,
        tint: Color,
    ) = drawTexture(
        identifier = texture.identifier.toMinecraft(),
        dstRect = dstRect,
        uvRect = Rect(
            offset = Offset.ZERO,
            size = dstRect.size / texture.size.toSize() / scale,
        ),
        tint = tint,
    )

    override fun drawItemStack(offset: IntOffset, size: IntSize, stack: ItemStack) {
        val minecraftStack = ((stack as? ItemStackImpl) ?: return).inner
        scale(size.width.toFloat() / 16f, size.height.toFloat() / 16f)
        GL11.glPushMatrix()
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        RenderHelper.enableGUIStandardItemLighting()
        itemRenderer.renderItemAndEffectIntoGUI(client.fontRenderer, client.textureManager, minecraftStack, offset.x, offset.y)
        RenderHelper.disableStandardItemLighting()
        GL11.glDisable(GL11.GL_DEPTH_TEST)
        GL11.glPopMatrix()
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glEnable(GL11.GL_ALPHA_TEST)
    }

    private val clipStack = ClipStack()

    override fun pushClip(absoluteArea: IntRect, relativeArea: IntRect) {
        val scaleFactor = scaledResolution.scaleFactor
        val rect = IntRect(
            offset = absoluteArea.offset * scaleFactor,
            size = absoluteArea.size * scaleFactor,
        )
        val clipRect = clipStack.pushClip(rect)
        GL11.glEnable(GL11.GL_SCISSOR_TEST)
        GL11.glScissor(clipRect.left, client.displayHeight - clipRect.bottom, clipRect.size.width, clipRect.size.height)
    }

    override fun popClip() {
        val clipRect = clipStack.popClip()
        if (clipRect != null) {
            GL11.glScissor(clipRect.left, client.displayHeight - clipRect.bottom, clipRect.size.width, clipRect.size.height)
        } else {
            GL11.glDisable(GL11.GL_SCISSOR_TEST)
        }
    }
}