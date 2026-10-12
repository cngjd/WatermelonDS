package me.magnum.melonds.common.opengl

import me.magnum.melonds.domain.model.Rect
import me.magnum.melonds.domain.model.SCREEN_HEIGHT
import me.magnum.melonds.domain.model.WideMelon
import me.magnum.melonds.domain.model.consoleAspectRatio
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * UVs for sampling the DS screens out of the frame texture (top screen, 2 gap lines, bottom screen; vertically flipped).
 *
 * With WideMelon the texture is wider than 256 DS pixels: the top screen uses the full width (3D wings included), while
 * the bottom screen and other fixed 4:3 slots sample only the native 256 px in the middle.
 */
object ScreenTextureUVs {
    private val lineRelativeSize = 1f / (SCREEN_HEIGHT * 2 + 2).toFloat()

    fun topUvs(): FloatArray = topUvs(0f, 1f)

    /** Top screen restricted to the native center, for slots that must stay 4:3 (e.g. hybrid layouts). */
    fun topNativeUvs(widescreenViewWidth: Int): FloatArray {
        val (left, right) = nativeHorizontalRange(widescreenViewWidth)
        return topUvs(left, right)
    }

    fun bottomUvs(widescreenViewWidth: Int): FloatArray {
        val (left, right) = nativeHorizontalRange(widescreenViewWidth)
        return floatArrayOf(
            left, 1f,
            left, 0.5f + lineRelativeSize,
            right, 0.5f + lineRelativeSize,
            left, 1f,
            right, 0.5f + lineRelativeSize,
            right, 1f,
        )
    }

    /**
     * Grows the top screen's layout rect so the extra WideMelon columns show at the same scale as the native image: the
     * width is multiplied by width/256 around the rect's center. If that no longer fits in the surface, a 4:3 rect is scaled
     * down uniformly (keeping its center) and a deliberately stretched rect is clamped to the surface width. Layout rects
     * themselves are never modified.
     */
    fun widenTopScreenRect(rect: Rect, widescreenViewWidth: Int, surfaceWidth: Int, surfaceHeight: Int): Rect {
        val width = WideMelon.normalizeWidth(widescreenViewWidth)
        if (width == WideMelon.NATIVE_WIDTH || rect.width <= 0 || rect.height <= 0) {
            return rect
        }

        var newWidth = rect.width * width / WideMelon.NATIVE_WIDTH.toFloat()
        var newHeight = rect.height.toFloat()
        if (surfaceWidth > 0 && newWidth > surfaceWidth) {
            val rectAspectRatio = rect.width.toFloat() / rect.height
            val keepsConsoleAspect = abs(rectAspectRatio / consoleAspectRatio - 1f) < 0.02f
            if (keepsConsoleAspect) {
                // Aspect-correct layout: shrink uniformly so the wide image keeps its proportions
                newHeight *= surfaceWidth / newWidth
            }
            // Otherwise the layout stretches the screen on purpose (fill width/height), so keep filling the height
            newWidth = surfaceWidth.toFloat()
        }

        val centerX = rect.x + rect.width / 2f
        val centerY = rect.y + rect.height / 2f
        var x = (centerX - newWidth / 2f).roundToInt()
        val y = (centerY - newHeight / 2f).roundToInt()
        if (surfaceWidth > 0) {
            x = x.coerceIn(0, (surfaceWidth - newWidth.roundToInt()).coerceAtLeast(0))
        }
        return Rect(x, y, newWidth.roundToInt(), newHeight.roundToInt())
    }

    private fun topUvs(left: Float, right: Float): FloatArray {
        return floatArrayOf(
            left, 0.5f - lineRelativeSize,
            left, 0f,
            right, 0f,
            left, 0.5f - lineRelativeSize,
            right, 0f,
            right, 0.5f - lineRelativeSize,
        )
    }

    private fun nativeHorizontalRange(widescreenViewWidth: Int): Pair<Float, Float> {
        val width = WideMelon.normalizeWidth(widescreenViewWidth).toFloat()
        val pad = (width - WideMelon.NATIVE_WIDTH) / 2f / width
        return pad to 1f - pad
    }
}
