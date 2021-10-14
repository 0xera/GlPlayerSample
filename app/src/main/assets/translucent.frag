precision mediump float;

uniform sampler2D uFrameTexture;
varying vec2 vTextureCoord;

void main() {
    vec2 vTextureCoordContent = vec2(vTextureCoord.x * 0.5 + 0.5, vTextureCoord.y);
    vec2 vTextureCoordMask = vec2(vTextureCoord.x * 0.5, vTextureCoord.y);

    vec4 contentColor = texture2D(uFrameTexture, vTextureCoordContent);
    vec4 maskColor = texture2D(uFrameTexture, vTextureCoordMask);

    gl_FragColor = vec4(contentColor.rgb, maskColor.r);
}
