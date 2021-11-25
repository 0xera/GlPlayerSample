precision mediump float;

varying vec2 vTextureCoord;

uniform sampler2D uFrameTexture;
uniform sampler2D uOverlayTexture;
uniform vec2 uResolution;

vec4 texture2DBlur(sampler2D texture, vec2 coord, vec2 resolution, int radius) {
    vec2 texelSize = 1.0 / resolution;
    int matrixLength = ((radius + 1) * 2) + 1;

    vec4 sumColor;
    for (int ix = 0; ix < matrixLength; ix += (radius + 1)) {
        for (int iy = 0; iy < matrixLength; iy += (radius + 1)) {
            vec2 offset = vec2(ivec2(ix, iy) - (radius + 1)) * texelSize;
            vec4 color = texture2D(texture, coord + offset);

            if (color.rgb == vec3(0.0)) {
                return texture2D(texture, coord);
            }

            sumColor += color;
        }
    }

    return sumColor / 9.0;
}

vec4 texture2DByMax(sampler2D texture, vec2 coord, vec2 resolution, int radius) {
    vec2 texelSize = 1.0 / resolution;

    vec3 rgbNW = texture2D(texture, coord + vec2(-1.0, 1.0) * float(radius) * texelSize).rgb;
    vec3 rgbNE = texture2D(texture, coord + vec2(1.0, -1.0) * float(radius) * texelSize).rgb;
    vec3 rgbSW = texture2D(texture, coord + vec2(-1.0, 1.0) * float(radius) * texelSize).rgb;
    vec3 rgbSE = texture2D(texture, coord + vec2(1.0, 1.0) * float(radius) * texelSize).rgb;
    vec3 texRgb = texture2D(texture, coord).rgb;

    vec3 colorByMax;
    colorByMax.r = max(max(max(rgbNW.r, rgbNE.r), max(rgbSW.r, rgbSE.r)), texRgb.r);
    colorByMax.g = max(max(max(rgbNW.g, rgbNE.g), max(rgbSW.g, rgbSE.g)), texRgb.g);
    colorByMax.b = max(max(max(rgbNW.b, rgbNE.b), max(rgbSW.b, rgbSE.b)), texRgb.b);

    return vec4(colorByMax, 1.0);
}

void main() {
    highp float offset = 1.0 / 3.0;
    vec2 vTextureCoordContent = vec2(vTextureCoord.x * offset + offset, vTextureCoord.y);
    vec2 vTextureCoordMask = vec2(vTextureCoord.x * offset, vTextureCoord.y);
    vec2 vTextureCoordOverlayMap = vec2(vTextureCoord.x * offset + (offset * 2.0), vTextureCoord.y);

    vec4 maskColor = texture2D(uFrameTexture, vTextureCoordMask);
    vec4 maskColorByMax = texture2DByMax(uFrameTexture, vTextureCoordMask, uResolution, 1);

    vec4 contentColor = texture2D(uFrameTexture, vTextureCoordContent);
    contentColor.a = maskColor.r;

    vec2 blurOverlayMapColor = texture2DBlur(uFrameTexture, vTextureCoordOverlayMap, uResolution, 12).xy;

    vec4 overlayColor = texture2D(uOverlayTexture, blurOverlayMapColor.rg);

    gl_FragColor = mix(contentColor, overlayColor, maskColorByMax.g * overlayColor.a);
}