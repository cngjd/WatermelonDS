package me.magnum.melonds.domain.model

/**
 * WideMelon widescreen 3D view (adapted from manijose1919/widemelon-android and pruefsumme/widemelon, GPL-3.0).
 *
 * The emulator renders the top screen's 3D into a framebuffer that is [width] DS pixels wide instead of 256, while 2D,
 * menus and the bottom screen stay native in the middle. Only the OpenGL and Compute renderers support it.
 */
object WideMelon {
    const val NATIVE_WIDTH = SCREEN_WIDTH
    const val MAX_WIDTH = 768

    fun normalizeWidth(width: Int): Int {
        return width.coerceIn(NATIVE_WIDTH, MAX_WIDTH).let { if (it % 2 != 0) it - 1 else it }
    }

    fun isSupportedBy(renderer: VideoRenderer): Boolean {
        return renderer == VideoRenderer.OPENGL || renderer == VideoRenderer.COMPUTE
    }

    /** Aspect ratio of the top screen when rendered at [width]. */
    fun topScreenAspectRatio(width: Int): Float {
        return normalizeWidth(width).toFloat() / SCREEN_HEIGHT
    }
}
