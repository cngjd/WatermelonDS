package me.magnum.melonds.domain.model

internal fun resolveThreadedRendering(renderer: VideoRenderer, requested: Boolean): Boolean = when (renderer) {
    VideoRenderer.SOFTWARE -> requested
    VideoRenderer.VULKAN -> true
    VideoRenderer.OPENGL, VideoRenderer.COMPUTE -> false
}

data class RendererConfiguration(
    val renderer: VideoRenderer,
    val videoFiltering: VideoFiltering,
    val threadedRendering: Boolean,
    private val internalResolutionScaling: Int,
    val rendererDebugToolsEnabled: Boolean,
    val rendererDebugBgObjEnabled: Boolean,
    val rendererDebugLatchTraceEnabled: Boolean,
    val conservativeCoverageEnabled: Boolean,
    val conservativeCoveragePx: Float,
    val conservativeCoverageDepthBias: Float,
    val conservativeCoverageApplyRepeat: Boolean,
    val conservativeCoverageApplyClamp: Boolean,
    val debug3dClearMagenta: Boolean,
    val retroArchShader: RetroArchShaderConfiguration,
    /** Requested WideMelon 3D view width (even, 256..768). Use [effectiveWidescreenViewWidth] for the applied value. */
    val widescreenViewWidth: Int = WideMelon.NATIVE_WIDTH,
) {

    /** WideMelon width actually applied: the requested width for OpenGL/Compute, native 256 for every other renderer. */
    val effectiveWidescreenViewWidth get() = if (WideMelon.isSupportedBy(renderer)) {
        WideMelon.normalizeWidth(widescreenViewWidth)
    } else {
        WideMelon.NATIVE_WIDTH
    }

    val resolutionScaling get() = when (renderer) {
        VideoRenderer.SOFTWARE -> 1
        VideoRenderer.OPENGL -> internalResolutionScaling
        VideoRenderer.VULKAN -> internalResolutionScaling
        VideoRenderer.COMPUTE -> internalResolutionScaling
    }
}
