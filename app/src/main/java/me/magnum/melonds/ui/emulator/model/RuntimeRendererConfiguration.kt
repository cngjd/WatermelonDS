package me.magnum.melonds.ui.emulator.model

import me.magnum.melonds.domain.model.RetroArchShaderConfiguration
import me.magnum.melonds.domain.model.VideoFiltering
import me.magnum.melonds.domain.model.VideoRenderer

data class RuntimeRendererConfiguration(
    val renderer: VideoRenderer,
    val videoFiltering: VideoFiltering,
    val resolutionScaling: Int,
    val retroArchShader: RetroArchShaderConfiguration,
    /** Width of the top screen in the frame texture (256 native, wider with WideMelon). */
    val widescreenViewWidth: Int = 256,
)
