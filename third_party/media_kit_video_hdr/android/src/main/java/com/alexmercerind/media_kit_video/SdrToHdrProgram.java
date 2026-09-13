package com.alexmercerind.media_kit_video;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

final class SdrToHdrProgram {
    private static final float[] POSITIONS = {-1f, -1f, 1f, -1f, -1f, 1f, 1f, 1f};
    private static final float[] TEX_COORDS = {0f, 0f, 1f, 0f, 0f, 1f, 1f, 1f};

    private static final String VERTEX =
            "uniform mat4 uTexMatrix;\n" +
            "attribute vec4 aPosition;\n" +
            "attribute vec4 aTextureCoord;\n" +
            "varying vec2 vTextureCoord;\n" +
            "void main(){ gl_Position=aPosition; vTextureCoord=(uTexMatrix*aTextureCoord).xy; }\n";

    // Recovered from the v2.0.0-hdr.0 release APK, then kept readable here so future upstream syncs
    // do not depend on an untracked local media-kit checkout again.
    private static final String FRAGMENT =
            "#extension GL_OES_EGL_image_external : require\n" +
            "#ifdef GL_FRAGMENT_PRECISION_HIGH\nprecision highp float;\n#else\nprecision mediump float;\n#endif\n" +
            "uniform samplerExternalOES sTexture;\n" +
            "uniform float uTargetPeakNits; uniform float uStrength; uniform float uSaturation;\n" +
            "uniform float uHighlightBoost; uniform float uPreDarken; uniform float uHighlightProtect; uniform float uFrameIndex;\n" +
            "varying vec2 vTextureCoord;\n" +
            "vec3 bt709ToBt2020(vec3 rgb){ mat3 m=mat3(0.6274,0.0691,0.0164, 0.3293,0.9195,0.0880, 0.0433,0.0114,0.8956); return m*rgb; }\n" +
            "float pqOetf(float L){ float m1=0.1593017578125; float m2=78.84375; float c1=0.8359375; float c2=18.8515625; float c3=18.6875; float Lm1=pow(max(L,0.0),m1); return pow((c1+c2*Lm1)/(1.0+c3*Lm1),m2); }\n" +
            "float softKneeNits(float v,float knee,float shoulder){ v=max(v,0.0); if(v<=knee)return v; float over=v-knee; float span=max(shoulder-knee,1.0); return knee+(over*span)/(over+span); }\n" +
            "void main(){\n" +
            " vec3 rgb=texture2D(sTexture,vTextureCoord).rgb; vec3 linear=pow(rgb,vec3(2.2));\n" +
            " linear*=1.0-clamp(uPreDarken,0.0,0.8); vec3 wide=bt709ToBt2020(linear);\n" +
            " float luma=dot(wide,vec3(0.2627,0.6780,0.0593)); vec3 chroma=wide-vec3(luma);\n" +
            " vec3 sat=vec3(luma)+chroma*clamp(uSaturation,0.0,1.2); float peak=max(uTargetPeakNits,100.0);\n" +
            " vec3 sdrNits=sat*100.0; float toneStrength=clamp(uStrength,0.0,1.0); float protect=clamp(uHighlightProtect,0.0,1.0);\n" +
            " float whiteAnchorNits=0.55*peak; vec3 anchorNits=sdrNits*(whiteAnchorNits/100.0); vec3 mappedNits=mix(sdrNits,anchorNits,toneStrength);\n" +
            " float lumaMapped=dot(mappedNits,vec3(0.2627,0.6780,0.0593)); float t=pow(protect,0.65); float x=max(lumaMapped/max(whiteAnchorNits,1.0),1.0e-5);\n" +
            " float shadowLift=1.0+0.05*t*(1.0-smoothstep(0.25,0.65,x)); float brightLift=smoothstep(0.55,1.20,x); float highlightExpand=1.0+2.20*t*brightLift;\n" +
            " float lumaExpanded=lumaMapped*shadowLift*highlightExpand; float highlightMask=smoothstep(0.75*whiteAnchorNits,1.05*whiteAnchorNits,lumaExpanded);\n" +
            " float boost=mix(1.0,clamp(uHighlightBoost,0.5,4.0),highlightMask); float boostedLuma=lumaExpanded+max(lumaExpanded-whiteAnchorNits,0.0)*(boost-1.0)*highlightMask;\n" +
            " float knee=mix(0.90*peak,0.68*peak,t); float shoulder=mix(1.8*peak,4.2*peak,t); float lumaSoft=softKneeNits(boostedLuma,knee,shoulder);\n" +
            " float lumaOut=max(lumaSoft,lumaMapped); float scale=min(lumaOut/max(lumaMapped,1.0e-4),12.0); vec3 softNits=mappedNits*scale;\n" +
            " vec3 pq=vec3(pqOetf(clamp(softNits.r/10000.0,0.0,1.0)),pqOetf(clamp(softNits.g/10000.0,0.0,1.0)),pqOetf(clamp(softNits.b/10000.0,0.0,1.0)));\n" +
            " vec3 outColor=pq; if(toneStrength>0.05){ float luma01=clamp(dot(pq,vec3(0.2627,0.6780,0.0593)),0.0,1.0); float noise=fract(sin(dot(gl_FragCoord.xy,vec2(12.9898,78.233))+uFrameIndex)*43758.5453); float d=(1.0/4096.0)*(1.0-smoothstep(0.6,1.0,luma01))*toneStrength; outColor=pq+vec3((noise-0.5)*d);}\n" +
            " gl_FragColor=vec4(clamp(outColor,0.0,1.0),1.0);\n" +
            "}\n";

    private final int program;
    private final int aPosition;
    private final int aTextureCoord;
    private final int uTexMatrix;
    private final int uPeak;
    private final int uStrength;
    private final int uSaturation;
    private final int uHighlight;
    private final int uPreDarken;
    private final int uHighlightProtect;
    private final int uFrameIndex;
    private final FloatBuffer positions;
    private final FloatBuffer texCoords;

    SdrToHdrProgram() {
        final int vs = compile(GLES20.GL_VERTEX_SHADER, VERTEX);
        final int fs = compile(GLES20.GL_FRAGMENT_SHADER, FRAGMENT);
        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vs);
        GLES20.glAttachShader(program, fs);
        GLES20.glLinkProgram(program);
        final int[] linked = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linked, 0);
        if (linked[0] != GLES20.GL_TRUE) {
            throw new RuntimeException("Could not link SDR2HDR program: " + GLES20.glGetProgramInfoLog(program));
        }
        GLES20.glDeleteShader(vs);
        GLES20.glDeleteShader(fs);
        aPosition = GLES20.glGetAttribLocation(program, "aPosition");
        aTextureCoord = GLES20.glGetAttribLocation(program, "aTextureCoord");
        uTexMatrix = GLES20.glGetUniformLocation(program, "uTexMatrix");
        uPeak = GLES20.glGetUniformLocation(program, "uTargetPeakNits");
        uStrength = GLES20.glGetUniformLocation(program, "uStrength");
        uSaturation = GLES20.glGetUniformLocation(program, "uSaturation");
        uHighlight = GLES20.glGetUniformLocation(program, "uHighlightBoost");
        uPreDarken = GLES20.glGetUniformLocation(program, "uPreDarken");
        uHighlightProtect = GLES20.glGetUniformLocation(program, "uHighlightProtect");
        uFrameIndex = GLES20.glGetUniformLocation(program, "uFrameIndex");
        positions = buffer(POSITIONS);
        texCoords = buffer(TEX_COORDS);
    }

    private static FloatBuffer buffer(float[] values) {
        final FloatBuffer b = ByteBuffer.allocateDirect(values.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        b.put(values).position(0);
        return b;
    }

    private static int compile(int type, String source) {
        final int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        final int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] != GLES20.GL_TRUE) {
            final String log = GLES20.glGetShaderInfoLog(shader);
            GLES20.glDeleteShader(shader);
            throw new RuntimeException("Could not compile shader: " + log);
        }
        return shader;
    }

    void draw(int textureId, float[] texMatrix, float peak, float strength, float saturation, float highlight, float preDarken, float highlightProtect, int frameIndex) {
        GLES20.glUseProgram(program);
        positions.position(0);
        texCoords.position(0);
        GLES20.glEnableVertexAttribArray(aPosition);
        GLES20.glVertexAttribPointer(aPosition, 2, GLES20.GL_FLOAT, false, 0, positions);
        GLES20.glEnableVertexAttribArray(aTextureCoord);
        GLES20.glVertexAttribPointer(aTextureCoord, 2, GLES20.GL_FLOAT, false, 0, texCoords);
        GLES20.glUniformMatrix4fv(uTexMatrix, 1, false, texMatrix, 0);
        GLES20.glUniform1f(uPeak, peak);
        GLES20.glUniform1f(uStrength, strength);
        GLES20.glUniform1f(uSaturation, saturation);
        GLES20.glUniform1f(uHighlight, highlight);
        GLES20.glUniform1f(uPreDarken, preDarken);
        GLES20.glUniform1f(uHighlightProtect, highlightProtect);
        GLES20.glUniform1f(uFrameIndex, frameIndex);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        GLES20.glDisableVertexAttribArray(aPosition);
        GLES20.glDisableVertexAttribArray(aTextureCoord);
    }

    void release() {
        GLES20.glDeleteProgram(program);
    }
}
