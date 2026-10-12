package me.magnum.melonds.common.opengl

import me.magnum.melonds.domain.model.VideoFiltering
import me.magnum.melonds.domain.model.WideMelon

object VideoFilterShaderProvider {
    /**
     * @param textureWidth Width of the frame texture in DS pixels (256, or the WideMelon width). Filters that sample
     * neighbouring texels need it to compute texel sizes.
     */
    fun getShaderSource(filtering: VideoFiltering, textureWidth: Int = WideMelon.NATIVE_WIDTH): ShaderProgramSource {
        val width = WideMelon.normalizeWidth(textureWidth)
        return when (filtering) {
            VideoFiltering.NONE -> ShaderProgramSource.NoFilterShader
            VideoFiltering.LINEAR -> ShaderProgramSource.LinearShader
            VideoFiltering.XBR2 -> ShaderProgramSource.createXbrShader(width)
            VideoFiltering.HQ2X -> ShaderProgramSource.createHq2xShader(width)
            VideoFiltering.HQ4X -> ShaderProgramSource.createHq4xShader(width)
            VideoFiltering.QUILEZ -> ShaderProgramSource.createQuilezShader(width)
            VideoFiltering.LCD -> ShaderProgramSource.createLcdShader(width)
            VideoFiltering.SCANLINES -> ShaderProgramSource.createScanlinesShader(width)
            VideoFiltering.RETROARCH -> ShaderProgramSource.RetroArchPassthroughShader
        }
    }
}
