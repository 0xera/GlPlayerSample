#extension GL_OES_EGL_image_external : require

precision mediump float;

uniform samplerExternalOES uFrameTexture;
varying vec2 vTextureCoord;

#define threshold 0.55
#define padding 0.05

void main() {
    vec4 frameColor = texture2D(uFrameTexture, vTextureCoord);

    vec4 greenScreen = vec4(0.,1.,0.,1.);

    vec3 diff = frameColor.rgb - greenScreen.rgb;
    float fac = smoothstep(threshold - padding, threshold + padding, dot(diff, diff));

    gl_FragColor = vec4(frameColor.rgb, fac);
}
