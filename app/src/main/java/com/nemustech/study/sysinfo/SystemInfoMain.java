
package com.nemustech.study.sysinfo;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.drm.DrmManagerClient;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.location.Criteria;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.opengl.EGL14;
import android.opengl.GLES10;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.microedition.khronos.egl.EGL10;

public class SystemInfoMain extends Activity {
    @SuppressWarnings("unused")
    private static final String TAG = SystemInfoMain.class.getSimpleName();

    LinearLayout mItemList;
    ArrayAdapter<InfoItem> mAdapter;
    ListView mContentList;

    private interface GLHelper {
        void onCreate();
        void onDestroy();
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

    private GLHelper mGLHelper;

    private AndroidInfoProvider mAndroidProvider;
    private SystemInfoProvider mSystemProvider;
    private ScreenInfoProvider mScreenProvider;
    private MemoryInfoProvider mMemoryProvider;
    private StorageInfoProvider mStorageProvider;
    private TelephoneInfoProvider mTelephoneProvider;
    private SensorInfoProvider mSensorProvider;
    private InputInfoProvider mInputProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.l_main);
        mItemList = (LinearLayout)findViewById(R.id.l_items);
        mAdapter = new InfoListView.InfoItemAdapter(this, 0, new ArrayList<InfoItem>());
        mContentList = (ListView)findViewById(R.id.l_content);
        mContentList.setAdapter(mAdapter);

        mAndroidProvider = new AndroidInfoProvider(this);
        mSystemProvider = new SystemInfoProvider(this);
        mScreenProvider = new ScreenInfoProvider(this);
        mMemoryProvider = new MemoryInfoProvider(this);
        mStorageProvider = new StorageInfoProvider(this);
        mTelephoneProvider = new TelephoneInfoProvider(this);
        mSensorProvider = new SensorInfoProvider(this);
        mInputProvider = new InputInfoProvider(this);

        setItemSelected(R.id.item_android);
        if (Build.VERSION_CODES.JELLY_BEAN_MR1 <= Build.VERSION.SDK_INT) {
            mGLHelper = new GLHelper_14();
        } else {
            mGLHelper = new GLHelper_10();
        }
        mGLHelper.onCreate();
    }

    @Override
    protected void onDestroy() {
        mGLHelper.onDestroy();
        super.onDestroy();
    }

    ArrayList<InfoItem> mConnectivityContent;
    ArrayList<InfoItem> mNetworkContent;
    ArrayList<InfoItem> mLocationContent;
    ArrayList<InfoItem> mCameraContent;
    ArrayList<InfoItem> mOpenGLContent;
    ArrayList<InfoItem> mDrmContent;
    ArrayList<InfoItem> mAccountContent;
    ArrayList<InfoItem> mLocaleContent;
    ArrayList<InfoItem> mCodecContent;
    ArrayList<InfoItem> mSecurityContent;

    private static final String[] sUNIT = {
        "", "K", "M", "G", "T", "P", "E", "*"
    };

    private void addConnectivityItems(ArrayList<InfoItem> list, NetworkInfo network) {
        String subtype = network.getSubtypeName();
        String name;
        if (null == subtype || 0 == subtype.length()) {
            name = network.getTypeName();
        } else {
            name = String.format("%s (%s)", network.getTypeName(), network.getSubtypeName());
        }
        StringBuffer sb = new StringBuffer();
        sb.append(getString(R.string.connectivity_state)).append(network.getState()).append('(').append(network.getDetailedState()).append(')');
        String extra = network.getExtraInfo();
        if (null != extra && 0 < extra.length()) {
            sb.append('\n').append(extra);
        }
        if (!network.isAvailable()) {
            sb.append('\n').append(getString(R.string.connectivity_unavailable));
        }
        if (network.isRoaming()) {
            sb.append('\n').append(getString(R.string.connectivity_roaming));
        }
        list.add(new InfoItem(name, sb.toString()));
    }
    private ArrayList<InfoItem> getConnectivityContent() {
        if (null == mConnectivityContent) {
            mConnectivityContent = new ArrayList<InfoItem>();

            ConnectivityManager cm = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo[] networks = cm.getAllNetworkInfo();

            if (null == networks || 0 == networks.length) {
                mConnectivityContent.add(new InfoItem(getString(R.string.item_connectivity), getString(R.string.connectivity_none)));
            } else {
                for (NetworkInfo network: networks) {
                    addConnectivityItems(mConnectivityContent, network);
                }
            }
        }
        return mConnectivityContent;
    }

    private String formatHardwareAddress(byte[] address) {
        if (null == address || 0 == address.length) {
            return getString(R.string.unknown);
        }
        StringBuffer sb = new StringBuffer();
        sb.append(String.format("%02x", address[0]));
        for (int idx = 1; idx < address.length; ++idx) {
            sb.append('-').append(String.format("%02x", address[idx]));
        }
        return sb.toString();
    }
    private void addNetworkItems(ArrayList<InfoItem> list, NetworkInterface network) {
        String name = network.getDisplayName();
        StringBuffer sb = new StringBuffer();
        sb.append(getString(R.string.network_name)).append(network.getName());
        sb.append('\n').append(getString(R.string.network_hardware_address));
        try {
            sb.append(formatHardwareAddress(network.getHardwareAddress()));
        } catch (SocketException e1) {
            e1.printStackTrace();
            sb.append(formatHardwareAddress(null));
        }
        try {
            sb.append('\n').append(getString(R.string.network_mtu)).append(String.valueOf(network.getMTU()));
        } catch (SocketException e1) {
            e1.printStackTrace();
        }
        try {
            sb.append('\n').append(getString(network.isUp()? R.string.network_up: R.string.network_down));
        } catch (SocketException e) {
            e.printStackTrace();
        }
        ArrayList<InetAddress> aList = Collections.list(network.getInetAddresses());
        for (InetAddress ia: aList) {
            String hostName = ia.getHostName();
            String hostAddress = ia.getHostAddress();
            sb.append('\n').append(hostName);
            if (!hostName.equals(hostAddress)) {
                sb.append('(').append(ia.getHostAddress()).append(')');
            }
        }
        ArrayList<NetworkInterface> nList = Collections.list(network.getSubInterfaces());
        if (0 < nList.size()) {
            sb.append('\n').append(getString(R.string.network_subinterface)).append(String.valueOf(nList.size()));
        }
        list.add(new InfoItem(name, sb.toString()));
        for (NetworkInterface subnet: nList) {
            addNetworkItems(list, subnet);
        }
    }
    private ArrayList<InfoItem> getNetworkContent() {
        if (null == mNetworkContent) {
            mNetworkContent = new ArrayList<InfoItem>();

            ArrayList<NetworkInterface> nList = null;
            try {
                nList = Collections.list(NetworkInterface.getNetworkInterfaces());
            } catch (SocketException e) {
                e.printStackTrace();
            }

            if (null == nList || 0 == nList.size()) {
                mNetworkContent.add(new InfoItem(getString(R.string.item_network), getString(R.string.network_none)));
            } else {
                final ArrayList<NetworkInterface> fList = nList;
                new Thread(new Runnable() {
                    public void run() {
                        for (NetworkInterface network: fList) {
                            addNetworkItems(mNetworkContent, network);
                        }
                        runOnUiThread(new Runnable() {
                            public void run() {
                                updateContent(mNetworkContent);
                            }
                        });
                    }
                }).start();
            }
        }
        return mNetworkContent;
    }

    private String formatLocationAccuracy(int acc) {
        String value = null;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            switch (acc) {
                case Criteria.ACCURACY_COARSE: value = "Coarse"; break;
                case Criteria.ACCURACY_FINE: value = "Fine"; break;
            }
        } else {
            switch (acc) {
                case Criteria.ACCURACY_LOW: value = "Low"; break;
                case Criteria.ACCURACY_MEDIUM: value = "Medium"; break;
                case Criteria.ACCURACY_HIGH: value = "high"; break;
            }
        }
        if (null == value) {
            value = getString(R.string.unknown);
        }
        return String.format("%s (%d)", value, acc);
    }
    private String formatLocationPowerReq(int req) {
        String value;
        switch (req) {
            case Criteria.NO_REQUIREMENT: value = "No requirement"; break;
            case Criteria.POWER_LOW: value = "Low"; break;
            case Criteria.POWER_MEDIUM: value = "Medium"; break;
            case Criteria.POWER_HIGH: value = "High"; break;
            default: value = getString(R.string.unknown);
        }
        return String.format("%s (%d)", value, req);
    }
    private void addLocationItems(ArrayList<InfoItem> list, LocationProvider lp, boolean enabled) {
        String name = lp.getName();
        StringBuffer sb = new StringBuffer();
        sb.append(getString(R.string.location_accuracy)).append(formatLocationAccuracy(lp.getAccuracy()));
        sb.append('\n').append(getString(R.string.location_power_req)).append(formatLocationPowerReq(lp.getPowerRequirement()));
        sb.append('\n').append(getString(R.string.location_cost)).append(getString(lp.hasMonetaryCost()? R.string.location_may_charge: R.string.location_free));
        boolean rCel = lp.requiresCell();
        boolean rNet = lp.requiresNetwork();
        boolean rSat = lp.requiresSatellite();
        if (rCel | rNet | rSat) {
            sb.append("\n(").append(getString(R.string.location_requires));
            if (rCel) {
                sb.append(getString(R.string.location_req_cell));
            }
            if (rNet) {
                sb.append(getString(R.string.location_req_network));
            }
            if (rSat) {
                sb.append(getString(R.string.location_req_satellite));
            }
            sb.append(')');
        }
        boolean sAlt = lp.supportsAltitude();
        boolean sBng = lp.supportsBearing();
        boolean sSpd = lp.supportsSpeed();
        if (sAlt | sBng | sSpd) {
            sb.append("\n(").append(getString(R.string.location_supports));
            if (sAlt) {
                sb.append(getString(R.string.location_sup_altitude));
            }
            if (sBng) {
                sb.append(getString(R.string.location_sup_bearing));
            }
            if (sSpd) {
                sb.append(getString(R.string.location_sup_speed));
            }
            sb.append(')');
        }
        list.add(new InfoItem(name, sb.toString()));
    }
    private ArrayList<InfoItem> getLocationContent() {
        if (null == mLocationContent) {
            mLocationContent = new ArrayList<InfoItem>();

            LocationManager lm = (LocationManager)getSystemService(LOCATION_SERVICE);
            List<String> lpList = lm.getAllProviders();

            if (null == lpList || 0 == lpList.size()) {
                mLocationContent.add(new InfoItem(getString(R.string.item_location), getString(R.string.location_none)));
            } else {
                for (String name: lpList) {
                    LocationProvider lp = lm.getProvider(name);
                    if (null == lp) {
                        mLocationContent.add(new InfoItem(name, getString(R.string.invalid_item)));
                    } else {
                        boolean enabled = lm.isProviderEnabled(name);
                        addLocationItems(mLocationContent, lp, enabled);
                    }
                }
            }
        }
        return mLocationContent;
    }

    private void addCameraInfoItems(ArrayList<InfoItem> list, CameraInfo info, String prefix) {
        list.add(new InfoItem(prefix + getString(R.string.camera_facing),
                getString((info.facing == CameraInfo.CAMERA_FACING_BACK)? R.string.camera_facing_back: R.string.camera_facing_front)));
        list.add(new InfoItem(prefix + getString(R.string.camera_orientation), String.valueOf(info.orientation)));
        if (Build.VERSION_CODES.JELLY_BEAN_MR1 <= Build.VERSION.SDK_INT) {
            list.add(new InfoItem(prefix + getString(R.string.camera_mute_shutter_sound), 
                    getString(info.canDisableShutterSound? R.string.camera_allowed: R.string.camera_not_allowed)));
        }
    }
    private String formatZoomRatios(List<Integer> ratios) {
        StringBuffer sb = new StringBuffer();
        if (null != ratios) {
            for (Integer ratio: ratios) {
                int i = ratio / 100;
                int f = ratio % 100;
                sb.append(i).append('.').append(f).append('\n');
            }
        }
        if (0 < sb.length()) {
            sb.deleteCharAt(sb.length() - 1);
        } else {
            sb.append(getString(R.string.unsupported));
        }
        return sb.toString();
    }
    private String formatStringList(List<String> strings) {
        StringBuffer sb = new StringBuffer();
        if (null != strings) {
            for (String str: strings) {
                sb.append(str).append('\n');
            }
        }
        if (0 < sb.length()) {
            sb.deleteCharAt(sb.length() - 1);
        } else {
            sb.append(getString(R.string.unsupported));
        }
        return sb.toString();
    }
    private String formatImageFormats(List<Integer> formats) {
        StringBuffer sb = new StringBuffer();
        if (null != formats) {
            for (Integer format: formats) {
                String name;
                switch (format) {
                    case ImageFormat.JPEG: name = "JPEG"; break;
                    case ImageFormat.NV16: name = "NV16"; break;
                    case ImageFormat.NV21: name = "NV21"; break;
                    case ImageFormat.RGB_565: name = "RGB 565"; break;
                    case ImageFormat.YUV_420_888: name = "Generic YCbCr"; break;
                    case ImageFormat.YUY2: name = "YUY2"; break;
                    case ImageFormat.YV12: name = "YUV"; break;
    
                    case ImageFormat.UNKNOWN:
                    default:
                        name = getString(R.string.unknown); break;
                }
                sb.append(name).append(" (").append(format).append(")\n");
            }
        }
        if (0 < sb.length()) {
            sb.deleteCharAt(sb.length() - 1);
        } else {
            sb.append(getString(R.string.unsupported));
        }
        return sb.toString();
    }
    private String formatPixelSize(long size) {
        int kidx = 0;
        long tmp = size;
        long div = 1;
        while (tmp > 1024) {
            ++kidx;
            tmp /= 1024;
            div *= 1024;
        }
        float v = (float)size / (float)div;
        if (sUNIT.length <= kidx) {
            kidx = sUNIT.length - 1;
        }
        return String.format("%.1f %s pixels", v, sUNIT[kidx]);
    }
    private String formatSizes(List<Camera.Size> sizes, boolean showPixels) {
        StringBuffer sb = new StringBuffer();
        if (null != sizes) {
            for (Camera.Size size: sizes) {
                if (0 < size.width && 0 < size.height) {  //  ignore zero-size for thumbnail sizes
                    sb.append(size.width).append(" x ").append(size.height);
                    if (showPixels) {
                        sb.append(" (").append(formatPixelSize((long)size.width * (long)size.height)).append(')');
                    }
                    sb.append('\n');
                }
            }
        }
        if (0 < sb.length()) {
            sb.deleteCharAt(sb.length() - 1);
        } else {
            sb.append(getString(R.string.unsupported));
        }
        return sb.toString();
    }
    private String formatFpsRangeList(List<int[]> ranges) {
        StringBuffer sb = new StringBuffer();
        if (null != ranges) {
            for (int[] range: ranges) {
                float min = range[Camera.Parameters.PREVIEW_FPS_MIN_INDEX] / 1000f;
                float max = range[Camera.Parameters.PREVIEW_FPS_MAX_INDEX] / 1000f;
                if (min == max) {
                    sb.append(min);
                } else {
                    sb.append(min).append(" - ").append(max);
                }
                sb.append(" fps\n");
            }
        }
        if (0 < sb.length()) {
            sb.deleteCharAt(sb.length() - 1);
        } else {
            sb.append(getString(R.string.unsupported));
        }
        return sb.toString();
    }
    private void addCameraFeatureItem(ArrayList<InfoItem> list, Camera.Parameters cp, String prefix) {
        StringBuffer sb = new StringBuffer();
        if (cp.isAutoExposureLockSupported()) {
            sb.append(getString(R.string.camera_auto_exposure_lock)).append('\n');
        }
        if (cp.isAutoWhiteBalanceLockSupported()) {
            sb.append(getString(R.string.camera_auto_white_balance_lock)).append('\n');
        }
        if (cp.isZoomSupported()) {
            sb.append(getString(R.string.camera_zoom));
            if (cp.isSmoothZoomSupported()) {
                sb.append(" (").append(getString(R.string.camera_smooth_zoom)).append(')');
            }
            sb.append('\n');
        }
        if (cp.isVideoSnapshotSupported()) {
            sb.append(getString(R.string.camera_video_snapshot)).append('\n');
        }
        if (cp.isVideoStabilizationSupported()) {
            sb.append(getString(R.string.camera_video_stabilization)).append('\n');
        }
        if (0 < sb.length()) {
            sb.deleteCharAt(sb.length() - 1);
            list.add(new InfoItem(prefix + getString(R.string.camera_features), sb.toString()));
        }
    }
    private void addCameraParameterItems(ArrayList<InfoItem> list, Camera.Parameters cp, String prefix) {
        list.add(new InfoItem(prefix + getString(R.string.camera_supported_antibanding), formatStringList(cp.getSupportedAntibanding())));
        list.add(new InfoItem(prefix + getString(R.string.camera_supported_color_effects), formatStringList(cp.getSupportedColorEffects())));
        list.add(new InfoItem(prefix + getString(R.string.camera_supported_flash_modes), formatStringList(cp.getSupportedFlashModes())));
        list.add(new InfoItem(prefix + getString(R.string.camera_supported_focus_modes), formatStringList(cp.getSupportedFocusModes())));
        list.add(new InfoItem(prefix + getString(R.string.camera_supported_scene_modes), formatStringList(cp.getSupportedSceneModes())));
        list.add(new InfoItem(prefix + getString(R.string.camera_supported_white_balance), formatStringList(cp.getSupportedWhiteBalance())));
        list.add(new InfoItem(prefix + getString(R.string.camera_supported_picture_formats), formatImageFormats(cp.getSupportedPictureFormats())));
        list.add(new InfoItem(prefix + getString(R.string.camera_supported_picture_sizes), formatSizes(cp.getSupportedPictureSizes(), true)));
        list.add(new InfoItem(prefix + getString(R.string.camera_supported_thumbnail_sizes), formatSizes(cp.getSupportedJpegThumbnailSizes(), false)));
        list.add(new InfoItem(prefix + getString(R.string.camera_supported_preview_formats), formatImageFormats(cp.getSupportedPreviewFormats())));
        list.add(new InfoItem(prefix + getString(R.string.camera_supported_preview_fps), formatFpsRangeList(cp.getSupportedPreviewFpsRange())));
        list.add(new InfoItem(prefix + getString(R.string.camera_supported_preview_sizes), formatSizes(cp.getSupportedPreviewSizes(), false)));
        list.add(new InfoItem(prefix + getString(R.string.camera_supported_video_sizes), formatSizes(cp.getSupportedVideoSizes(), true)));
        addCameraFeatureItem(list, cp, prefix);
        if (cp.isZoomSupported()) {
            list.add(new InfoItem(prefix + getString(R.string.camera_zoom_ratios), formatZoomRatios(cp.getZoomRatios())));
        }
    }
    private void addCameraItems(ArrayList<InfoItem> list, int idx) {
        Camera cam = null;
        try {
            cam = Camera.open(idx);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        String name = getString(R.string.camera_name, idx);
        if (null == cam) {
            list.add(new InfoItem(name, getString(R.string.unsupported)));
        } else {
            String prefix = name + ": ";
            CameraInfo ci = new CameraInfo();
            ci.facing = MAGIC_NUMBER;
            Camera.getCameraInfo(idx, ci);
            if (MAGIC_NUMBER != ci.facing) {
                addCameraInfoItems(list, ci, prefix);
            }
            Camera.Parameters cp = cam.getParameters();
            addCameraParameterItems(list, cp, prefix);
            cam.release();
        }
    }
    private ArrayList<InfoItem> getCameraContent() {
        if (null == mCameraContent) {
            mCameraContent = new ArrayList<InfoItem>();
            int num = Camera.getNumberOfCameras();
            if (0 == num) {
                mCameraContent.add(new InfoItem(getString(R.string.item_camera), getString(R.string.camera_none)));
            } else {
                for (int idx = 0; idx < num; ++idx) {
                    addCameraItems(mCameraContent, idx);
                }
            }
        }
        return mCameraContent;
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
    private ArrayList<InfoItem> getOpenGLContent() {
        if (null == mOpenGLContent) {
            mOpenGLContent = new ArrayList<InfoItem>();

            mOpenGLContent.add(new InfoItem(getString(R.string.opengl_version), GLES10.glGetString(GLES10.GL_VERSION)));
            mOpenGLContent.add(new InfoItem(getString(R.string.opengl_vendor), GLES10.glGetString(GLES10.GL_VENDOR)));
            mOpenGLContent.add(new InfoItem(getString(R.string.opengl_renderer), GLES10.glGetString(GLES10.GL_RENDERER)));
//            mOpenGLContent.add(getOpenGLItem(R.string.opengl_max_elements_indices));
//            mOpenGLContent.add(getOpenGLItem(R.string.opengl_max_elements_vertices));
//            mOpenGLContent.add(getOpenGLItem(R.string.opengl_max_lights));
//            mOpenGLContent.add(getOpenGLItem(R.string.opengl_max_modelview_stack_depth));
//            mOpenGLContent.add(getOpenGLItem(R.string.opengl_max_projection_stack_depth));
            mOpenGLContent.add(getOpenGLItem(R.string.opengl_max_texture_size));
//            mOpenGLContent.add(getOpenGLItem(R.string.opengl_max_texture_stack_depth));
//            mOpenGLContent.add(getOpenGLItem(R.string.opengl_max_texture_units));
            mOpenGLContent.add(getOpenGLItem(R.string.opengl_max_viewport_dims));
            mOpenGLContent.add(new InfoItem(getString(R.string.opengl_extensions), formatOpenGLExtensions(GLES10.glGetString(GLES10.GL_EXTENSIONS))));
        }
        return mOpenGLContent;
    }

    private ArrayList<InfoItem> getDrmContent() {
        if (null == mDrmContent) {
            mDrmContent = new ArrayList<InfoItem>();

            if (VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                mDrmContent.add(new InfoItem(getString(R.string.item_drm), getString(R.string.sdk_version_required, Build.VERSION_CODES.HONEYCOMB)));
            } else {
                DrmManagerClient dcm = new DrmManagerClient(this);
                String[] engines = dcm.getAvailableDrmEngines();
                if (null == engines || 0 == engines.length) {
                    mDrmContent.add(new InfoItem(getString(R.string.item_drm), getString(R.string.drm_none)));
                } else {
                    StringBuffer sb = new StringBuffer();
                    sb.append("- ").append(engines[0]);
                    for (int idx = 1; idx < engines.length; ++idx) {
                        sb.append("\n- ").append(engines[idx]);
                    }
                    mDrmContent.add(new InfoItem(getString(R.string.item_drm), sb.toString()));
                }
                if (Build.VERSION_CODES.JELLY_BEAN <= VERSION.SDK_INT) {
                    dcm.release();
                }
            }
        }
        return mDrmContent;
    }

    private InfoItem getAuthenticatorItem(AuthenticatorDescription ad, PackageManager pm) {
        String name;
        try {
            Resources r = pm.getResourcesForApplication(ad.packageName);
            name = r.getString(ad.labelId);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            name = getString(R.string.unknown);
        }
        StringBuffer sb = new StringBuffer();
        sb.append("Type: ").append(ad.type);
        sb.append("\nPackage: ").append(ad.packageName);
        return new InfoItem("Authenticator: " + name, sb.toString());
    }
    private InfoItem getAdminItem(List<ComponentName> admins) {
        StringBuffer sb = new StringBuffer();
        sb.append(admins.get(0));
        for (int idx = 1; idx < admins.size(); ++idx) {
            sb.append('\n').append(admins.get(idx));
        }
        return new InfoItem(getString(R.string.account_admin), sb.toString());
    }
    private ArrayList<InfoItem> getAccountContent() {
        if (null == mAccountContent) {
            mAccountContent = new ArrayList<InfoItem>();
            AccountManager am = (AccountManager)getSystemService(ACCOUNT_SERVICE);
            Account[] accounts = am.getAccounts();
            if (null == accounts || 0 == accounts.length) {
                mAccountContent.add(new InfoItem(getString(R.string.item_account), getString(R.string.account_none)));
            } else {
                for (Account account: accounts) {
                    mAccountContent.add(new InfoItem("Account: " + account.name, "type: " + account.type));
                }
            }
            AuthenticatorDescription[] ads = am.getAuthenticatorTypes();
            if (null == ads || 0 == ads.length) {
                mAccountContent.add(new InfoItem(getString(R.string.account_auth), getString(R.string.account_auth_none)));
            } else {
                PackageManager pm = getPackageManager();
                for (AuthenticatorDescription ad: ads) {
                    mAccountContent.add(getAuthenticatorItem(ad, pm));
                }
            }
            DevicePolicyManager dpm = (DevicePolicyManager)getSystemService(DEVICE_POLICY_SERVICE);
            List<ComponentName> admins = dpm.getActiveAdmins();
            if (null == admins || 0 == admins.size()) {
                mAccountContent.add(new InfoItem(getString(R.string.account_admin), getString(R.string.account_admin_none)));
            } else {
                mAccountContent.add(getAdminItem(admins));
            }
        }
        return mAccountContent;
    }

    private String formatLocaleName(Locale locale) {
        return locale.getDisplayName() + ": " + locale.getDisplayName(locale);
    }
    private String formatLocaleCodes(Locale locale) {
        String lang = locale.getLanguage();
        String cc = locale.getCountry();
        String var = locale.getVariant();
        String lang3 = null;
        String cc3 = null;
        try {
            lang3 = locale.getISO3Language();
            cc3 = locale.getISO3Country();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        StringBuffer sb = new StringBuffer();
        sb.append(lang);
        if (null != cc && 0 < cc.length()) {
            sb.append('_').append(cc);
        }
        if (null != var && 0 < var.length()) {
            sb.append(", ").append(var);
        }
        sb.append(" (");
        if (null == lang3 || 0 == lang3.length()) {
            sb.append(getString(R.string.no_iso3));
        } else {
            sb.append(lang3);
            if (null != cc3 && 0 < cc3.length()) {
                sb.append('_').append(cc3);
            }
        }
        sb.append(')');
        return sb.toString();
    }
    private InfoItem getLocaleItem(Locale locale) {
        return new InfoItem(formatLocaleName(locale), formatLocaleCodes(locale));
    }
    private void addSpecificLocale(ArrayList<InfoItem> list, String name, Locale locale) {
        list.add(new InfoItem(name, formatLocaleCodes(locale) + " - " + formatLocaleName(locale)));
    }
    private ArrayList<InfoItem> getLocaleContent() {
        if (null == mLocaleContent) {
            mLocaleContent = new ArrayList<InfoItem>();
            addSpecificLocale(mLocaleContent, getString(R.string.locale_current), getResources().getConfiguration().locale);
            addSpecificLocale(mLocaleContent, getString(R.string.locale_default), Locale.getDefault());
            Locale[] locales = Locale.getAvailableLocales();
            for (Locale locale: locales) {
                mLocaleContent.add(getLocaleItem(locale));
            }
        }
        return mLocaleContent;
    }

    private InfoItem getCodecInfoItem(MediaCodecInfo info) {
        String[] types = info.getSupportedTypes();
        StringBuffer sb = new StringBuffer();
        if (null == types || 0 == types.length) {
            sb.append(getString(R.string.unsupported));
        } else {
            sb.append(types[0]);
            for (int idx = 1; idx < types.length; ++idx) {
                sb.append("\n").append(types[idx]);
            }
            if (info.isEncoder()) {
                sb.append("\n").append(getString(R.string.codec_is_encoder));
            }
        }
        return new InfoItem(info.getName(), sb.toString());
    }
    private ArrayList<InfoItem> getCodecContent() {
        if (null == mCodecContent) {
            mCodecContent = new ArrayList<InfoItem>();
            if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                mCodecContent.add(new InfoItem(getString(R.string.codec_info), getString(R.string.sdk_version_required, Build.VERSION_CODES.JELLY_BEAN)));
            } else if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                int count = MediaCodecList.getCodecCount();
                for (int idx = 0; idx < count; ++idx) {
                    MediaCodecInfo info = MediaCodecList.getCodecInfoAt(idx);
                    mCodecContent.add(getCodecInfoItem(info));
                }
            } else {
                MediaCodecList mcl = new MediaCodecList(MediaCodecList.ALL_CODECS);
                MediaCodecInfo[] infos = mcl.getCodecInfos();
                if (null == infos || 0 == infos.length) {
                    mCodecContent.add(new InfoItem(getString(R.string.codec_info), getString(R.string.codec_none)));
                } else {
                    for (MediaCodecInfo info: infos) {
                        mCodecContent.add(getCodecInfoItem(info));
                    }
                }
            }
        }
        return mCodecContent;
    }

    private void addSecurityContent(ArrayList<InfoItem> list, Provider provider) {
        String name = provider.getName() + ": " + provider.getInfo();
        Set<Provider.Service> services = provider.getServices();
        StringBuffer sb = new StringBuffer();
        if (null == services || 0 == services.size()) {
            sb.append(getString(R.string.security_no_service));
        } else {
            Package pPkg = null;
            for (Provider.Service ps: services) {
                Class<?> cls = null;
                Package pkg = null;
                try {
                    cls = Class.forName(ps.getClassName());
                    pkg = cls.getPackage();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                if (null != pkg && !pkg.equals(pPkg)) {
                    sb.append("Package ").append(pkg.getName()).append('\n');
                    pPkg = pkg;
                }
                sb.append(ps.getAlgorithm());
                sb.append(" (").append(ps.getType()).append(")\n");
//                String className;
//                if (null != pkg) {
//                    className = ps.getClassName().substring(pkg.getName().length() + 1);
//                } else {
//                    String fullName = ps.getClassName();
//                    className = fullName.substring(fullName.lastIndexOf('.') + 1);
//                }
//                sb.append(" (").append(className).append(")\n");
            }
            sb.deleteCharAt(sb.length() - 1);
        }
        list.add(new InfoItem(name, sb.toString()));
    }
    private ArrayList<InfoItem> getSecurityContent() {
        if (null == mSecurityContent) {
            mSecurityContent = new ArrayList<InfoItem>();
            Provider[] providers = Security.getProviders();
            if (null == providers || 0 == providers.length) {
                mSecurityContent.add(new InfoItem(getString(R.string.item_security), getString(R.string.security_none)));
            } else {
                for (Provider p: providers) {
                    addSecurityContent(mSecurityContent, p);
                }
            }
        }
        return mSecurityContent;
    }
    private void updateContent(ArrayList<InfoItem> items) {
        mAdapter.clear();
        for (InfoItem item: items) {
            mAdapter.add(item);
        }
        mAdapter.notifyDataSetChanged();
        mContentList.setSelection(0);
    }

    private void onItemSelected(View view) {
        ArrayList<InfoItem> items = null;
        switch (view.getId()) {
            case R.id.item_android:
                items = mAndroidProvider.getItems();
                break;
            case R.id.item_system:
                items = mSystemProvider.getItems();
                break;
            case R.id.item_screen:
                items = mScreenProvider.getItems();
                break;
            case R.id.item_memory:
                items = mMemoryProvider.getItems();
                break;
            case R.id.item_storage:
                items = mStorageProvider.getItems();
                break;
            case R.id.item_phone:
                items = mTelephoneProvider.getItems();
                break;
            case R.id.item_sensor:
                items = mSensorProvider.getItems();
                break;
            case R.id.item_input:
                items = mInputProvider.getItems();
                break;
            case R.id.item_connectivity:
                items = getConnectivityContent();
                break;
            case R.id.item_network:
                items = getNetworkContent();
                break;
            case R.id.item_location:
                items = getLocationContent();
                break;
            case R.id.item_camera:
                items = getCameraContent();
                break;
            case R.id.item_opengl:
                items = getOpenGLContent();
                break;
            case R.id.item_drm:
                items = getDrmContent();
                break;
            case R.id.item_account:
                items = getAccountContent();
                break;
            case R.id.item_codec:
                items = getCodecContent();
                break;
            case R.id.item_security:
                items = getSecurityContent();
                break;
            case R.id.item_locale:
                items = getLocaleContent();
                break;
        }
        if (null != items) {
            updateContent(items);
        }
    }

    private void setItemSelected(int selectedId) {
        final int count = mItemList.getChildCount();
        for (int idx = 0; idx < count; ++idx) {
            View child = mItemList.getChildAt(idx);
            if (child.getId() == selectedId) {
                if (!child.isSelected()) {
                    onItemSelected(child);
                }
                child.setSelected(true);
            } else {
                child.setSelected(false);
            }
        }
    }

    public void onItemClick(View view) {
        Log.i(TAG, "CDSS onItemClick()");
        setItemSelected(view.getId());
    }
}
