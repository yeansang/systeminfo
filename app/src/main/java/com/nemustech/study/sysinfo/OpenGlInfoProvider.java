package com.nemustech.study.sysinfo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.opengl.EGL14;
import android.opengl.GLES10;
import android.os.Build;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGL10;

/**
 * Created by cheolgyoon on 2016. 6. 3..
 *
 */
public class OpenGlInfoProvider extends InfoProvider {
    @SuppressWarnings("unused")
    private static final String TAG = OpenGlInfoProvider.class.getSimpleName();
    private static ArrayList<InfoItem> sOpenGlItem;

    interface GLHelper {
        void onCreate();
        void onDestroy();
    }

    OpenGlInfoProvider(Context context) {
        super(context);
    }

    @SuppressLint("NewApi")
    private class GLHelper_14 implements GLHelper {
        private android.opengl.EGLDisplay display;
        private android.opengl.EGLSurface surface;
        private android.opengl.EGLContext context;
        @Override
        public void onCreate() {
            //  Display
            display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
            int[] ver = new int[2];
            EGL14.eglInitialize(display, ver, 0, ver, 1);

            // Config
            int[] configAttr = {
                    EGL14.EGL_COLOR_BUFFER_TYPE, EGL14.EGL_RGB_BUFFER,
                    EGL14.EGL_LEVEL, 0,
                    EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                    EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT,
                    EGL14.EGL_NONE
            };
            android.opengl.EGLConfig[] configs = new android.opengl.EGLConfig[1];
            int[] numConfig = new int[1];
            EGL14.eglChooseConfig(display, configAttr, 0, configs, 0, 1, numConfig, 0);

            if (numConfig[0] == 0) {
                // TROUBLE! No config found.
            }
            android.opengl.EGLConfig config = configs[0];

            //  Surface
            int[] surfAttr = {
                    EGL14.EGL_WIDTH, 64,
                    EGL14.EGL_HEIGHT, 64,
                    EGL14.EGL_NONE
            };
            surface = EGL14.eglCreatePbufferSurface(display, config, surfAttr, 0);

            //  Context
            int[] ctxAttrib = {
                    EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                    EGL14.EGL_NONE
            };
            context = EGL14.eglCreateContext(display, config, EGL14.EGL_NO_CONTEXT, ctxAttrib, 0);

            EGL14.eglMakeCurrent(display, surface, surface, context);
        }
        @Override
        public void onDestroy() {
            EGL14.eglMakeCurrent(display, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
            EGL14.eglDestroySurface(display, surface);
            EGL14.eglDestroyContext(display, context);
            EGL14.eglTerminate(display);
        }
    }

    private class GLHelper_10 implements GLHelper {
        javax.microedition.khronos.egl.EGLDisplay display;
        javax.microedition.khronos.egl.EGLSurface surface;
        javax.microedition.khronos.egl.EGLContext context;

        @Override
        public void onCreate() {
            EGL10 egl = (EGL10)javax.microedition.khronos.egl.EGLContext.getEGL();

            display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
            int[] vers = new int[2];
            egl.eglInitialize(display, vers);

            int[] configAttr = {
                    EGL10.EGL_COLOR_BUFFER_TYPE, EGL10.EGL_RGB_BUFFER,
                    EGL10.EGL_LEVEL, 0,
                    EGL10.EGL_SURFACE_TYPE, EGL10.EGL_PBUFFER_BIT,
                    EGL10.EGL_NONE
            };
            javax.microedition.khronos.egl.EGLConfig[] configs = new javax.microedition.khronos.egl.EGLConfig[1];
            int[] numConfig = new int[1];
            egl.eglChooseConfig(display, configAttr, configs, 1, numConfig);
            if (numConfig[0] == 0) {
                // TROUBLE! No config found.
            }
            javax.microedition.khronos.egl.EGLConfig config = configs[0];

            int[] surfAttr = {
                    EGL10.EGL_WIDTH, 64,
                    EGL10.EGL_HEIGHT, 64,
                    EGL10.EGL_NONE
            };
            surface = egl.eglCreatePbufferSurface(display, config, surfAttr);
            final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;  // missing in EGL10
            int[] ctxAttrib = {
                    EGL_CONTEXT_CLIENT_VERSION, 1,
                    EGL10.EGL_NONE
            };
            context = egl.eglCreateContext(display, config, EGL10.EGL_NO_CONTEXT, ctxAttrib);
            egl.eglMakeCurrent(display, surface, surface, context);
        }
        @Override
        public void onDestroy() {
            EGL10 egl = (EGL10)javax.microedition.khronos.egl.EGLContext.getEGL();

            egl.eglMakeCurrent(display, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
            egl.eglDestroySurface(display, surface);
            egl.eglDestroyContext(display, context);
            egl.eglTerminate(display);
        }
    }

    GLHelper getGlHelper() {
        if (Build.VERSION_CODES.JELLY_BEAN_MR1 <= Build.VERSION.SDK_INT) {
            return new GLHelper_14();
        } else {
            return new GLHelper_10();
        }
    }

    private String formatOpenGLExtensions(String src) {
        StringBuffer sb = new StringBuffer();
        if (null == src) {
            sb.append(getString(R.string.invalid_item));
        } else {
            final int len = src.length();
            for (int idx = 0; idx < len; ++idx) {
                char ch = src.charAt(idx);
                if (Character.isWhitespace(ch)) {
                    sb.append('\n');
                } else {
                    sb.append(ch);
                }
            }
            if (sb.charAt(sb.length() - 1) == '\n') {
                sb.deleteCharAt(sb.length() - 1);
            }
        }
        return sb.toString();
    }
    private String formatSize(int[] size) {
        return String.format("%d x %d", size[0], size[1]);
    }

    private int mGLValues[] = new int[2];
    private static final int MAGIC_NUMBER = -7151;
    private InfoItem getOpenGLItem(int id) {
        String value = null;
        boolean idValid = false;
        switch (id) {
            case R.string.opengl_max_elements_indices: {
                mGLValues[0] = MAGIC_NUMBER;
                GLES10.glGetIntegerv(GLES10.GL_MAX_ELEMENTS_INDICES, mGLValues, 0);
                if (mGLValues[0] != MAGIC_NUMBER) {
                    idValid = true;
                    value = String.valueOf(mGLValues[0]);
                }
                break;
            }
            case R.string.opengl_max_elements_vertices: {
                mGLValues[0] = MAGIC_NUMBER;
                GLES10.glGetIntegerv(GLES10.GL_MAX_ELEMENTS_VERTICES, mGLValues, 0);
                if (mGLValues[0] != MAGIC_NUMBER) {
                    idValid = true;
                    value = String.valueOf(mGLValues[0]);
                }
                break;
            }
            case R.string.opengl_max_lights: {
                mGLValues[0] = MAGIC_NUMBER;
                GLES10.glGetIntegerv(GLES10.GL_MAX_LIGHTS, mGLValues, 0);
                if (mGLValues[0] != MAGIC_NUMBER) {
                    idValid = true;
                    value = String.valueOf(mGLValues[0]);
                }
                break;
            }
            case R.string.opengl_max_modelview_stack_depth: {
                mGLValues[0] = MAGIC_NUMBER;
                GLES10.glGetIntegerv(GLES10.GL_MAX_MODELVIEW_STACK_DEPTH, mGLValues, 0);
                if (mGLValues[0] != MAGIC_NUMBER) {
                    idValid = true;
                    value = String.valueOf(mGLValues[0]);
                }
                break;
            }
            case R.string.opengl_max_projection_stack_depth: {
                mGLValues[0] = MAGIC_NUMBER;
                GLES10.glGetIntegerv(GLES10.GL_MAX_PROJECTION_STACK_DEPTH, mGLValues, 0);
                if (mGLValues[0] != MAGIC_NUMBER) {
                    idValid = true;
                    value = String.valueOf(mGLValues[0]);
                }
                break;
            }
            case R.string.opengl_max_texture_size: {
                mGLValues[0] = MAGIC_NUMBER;
                GLES10.glGetIntegerv(GLES10.GL_MAX_TEXTURE_SIZE, mGLValues, 0);
                if (mGLValues[0] != MAGIC_NUMBER) {
                    idValid = true;
                    value = String.valueOf(mGLValues[0]);
                }
                break;
            }
            case R.string.opengl_max_texture_stack_depth: {
                mGLValues[0] = MAGIC_NUMBER;
                GLES10.glGetIntegerv(GLES10.GL_MAX_TEXTURE_STACK_DEPTH, mGLValues, 0);
                if (mGLValues[0] != MAGIC_NUMBER) {
                    idValid = true;
                    value = String.valueOf(mGLValues[0]);
                }
                break;
            }
            case R.string.opengl_max_texture_units: {
                mGLValues[0] = MAGIC_NUMBER;
                GLES10.glGetIntegerv(GLES10.GL_MAX_TEXTURE_UNITS, mGLValues, 0);
                if (mGLValues[0] != MAGIC_NUMBER) {
                    idValid = true;
                    value = String.valueOf(mGLValues[0]);
                }
                break;
            }
            case R.string.opengl_max_viewport_dims: {
                mGLValues[0] = MAGIC_NUMBER;
                GLES10.glGetIntegerv(GLES10.GL_MAX_VIEWPORT_DIMS, mGLValues, 0);
                if (mGLValues[0] != MAGIC_NUMBER) {
                    idValid = true;
                    value = formatSize(mGLValues);
                }
                break;
            }
        }
        if (null != value) {
            return new InfoItem(getString(id), value);
        }
        if (idValid) {
            return new InfoItem(getString(id), getString(R.string.unsupported));
        }
        return new InfoItem(getString(id), getString(R.string.invalid_item));
    }

    @Override
    ArrayList<InfoItem> getItems() {
        if (null == sOpenGlItem) {
            sOpenGlItem = new ArrayList<>();
            sOpenGlItem.add(new InfoItem(getString(R.string.opengl_version), GLES10.glGetString(GLES10.GL_VERSION)));
            sOpenGlItem.add(new InfoItem(getString(R.string.opengl_vendor), GLES10.glGetString(GLES10.GL_VENDOR)));
            sOpenGlItem.add(new InfoItem(getString(R.string.opengl_renderer), GLES10.glGetString(GLES10.GL_RENDERER)));
//            sOpenGlItem.add(getOpenGLItem(R.string.opengl_max_elements_indices));
//            sOpenGlItem.add(getOpenGLItem(R.string.opengl_max_elements_vertices));
//            sOpenGlItem.add(getOpenGLItem(R.string.opengl_max_lights));
//            sOpenGlItem.add(getOpenGLItem(R.string.opengl_max_modelview_stack_depth));
//            sOpenGlItem.add(getOpenGLItem(R.string.opengl_max_projection_stack_depth));
            sOpenGlItem.add(getOpenGLItem(R.string.opengl_max_texture_size));
//            sOpenGlItem.add(getOpenGLItem(R.string.opengl_max_texture_stack_depth));
//            sOpenGlItem.add(getOpenGLItem(R.string.opengl_max_texture_units));
            sOpenGlItem.add(getOpenGLItem(R.string.opengl_max_viewport_dims));
            sOpenGlItem.add(new InfoItem(getString(R.string.opengl_extensions), formatOpenGLExtensions(GLES10.glGetString(GLES10.GL_EXTENSIONS))));

        }
        return sOpenGlItem;
    }

    @Override
    protected Object[] getInfoParams() {
        return new Object[0];
    }

    @Override
    protected InfoItem getItem(int infoId, Object... params) {
        return null;
    }
}
