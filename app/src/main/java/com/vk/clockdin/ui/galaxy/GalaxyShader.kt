// Location: app/src/main/java/com/vk/clockdin/ui/galaxy/GalaxyShader.kt
package com.vk.clockdin.ui.galaxy

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush

/**
 * AGSL RuntimeShader for rendering neon/pastel radial glow falloffs
 * around mood beads and orbs without expensive multi-pass Gaussian blurs.
 */
object GalaxyShader {

    private const val GLOW_SHADER_SRC = """
        uniform float2 uCenter;
        uniform float uRadius;
        uniform float4 uColor;
        uniform float uIntensity;

        half4 main(float2 fragCoord) {
            float dist = length(fragCoord - uCenter);
            float normDist = dist / uRadius;
            if (normDist >= 1.0) {
                return half4(0.0, 0.0, 0.0, 0.0);
            }
            // Smooth cubic falloff for a physical glowing halo
            float falloff = (1.0 - normDist);
            float halo = falloff * falloff * falloff * uIntensity;
            return half4(uColor.rgb * halo, uColor.a * halo);
        }
    """

    private val runtimeShader by lazy {
        RuntimeShader(GLOW_SHADER_SRC)
    }

    /**
     * Creates a reusable AGSL ShaderBrush configured with target center, radius, color, and intensity.
     */
    fun createGlowBrush(
        centerX: Float,
        centerY: Float,
        radius: Float,
        color: Color,
        intensity: Float = 1.0f
    ): ShaderBrush {
        runtimeShader.setFloatUniform("uCenter", centerX, centerY)
        runtimeShader.setFloatUniform("uRadius", radius.coerceAtLeast(1f))
        runtimeShader.setFloatUniform(
            "uColor",
            color.red,
            color.green,
            color.blue,
            color.alpha
        )
        runtimeShader.setFloatUniform("uIntensity", intensity)
        return ShaderBrush(runtimeShader)
    }
}
