package com.renhui.opengles20study.paint;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 画笔点渲染器
 */
public class PaintPointRenderer implements GLSurfaceView.Renderer {
    public static float width = 0;
    public static float height = 0;

    private int mProgram;
    private String mVertexShaderSource;
    private String mFragmentShaderSource;
    private PPgles iCZ;

    public PaintPointRenderer(PPgles icZgles) {
        iCZ = icZgles;
        mVertexShaderSource = iCZ.getVertexShader();
        mFragmentShaderSource = iCZ.getFragmentShader();
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        mProgram = GLES20.glCreateProgram();
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, mVertexShaderSource);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, mFragmentShaderSource);
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);
        GLES20.glUseProgram(mProgram);
        iCZ.init(mProgram, vertexShader, fragmentShader);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int i, int i1) {
        GLES20.glViewport(0, 0, i, i1);
        width = i;
        height = i1;
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        iCZ.draw();
    }

    private int loadShader(int type, String source) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        return shader;
    }
}