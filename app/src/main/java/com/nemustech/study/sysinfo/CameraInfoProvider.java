package com.nemustech.study.sysinfo;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cheolgyoon on 2016. 6. 2..
 *
 */
public class CameraInfoProvider extends InfoProvider {
    @SuppressWarnings("unused")
    private static final String TAG = CameraInfoProvider.class.getSimpleName();
    private static final int MAGIC_NUMBER = -7151;
    private static ArrayList<InfoItem> sCameraItems;
    private Object[] mParams = new Object[2];

    CameraInfoProvider(Context context) {
        super(context);
    }

    private void addCameraInfoItems(ArrayList<InfoItem> list, Camera.CameraInfo info, String prefix) {
        list.add(new InfoItem(prefix + getString(R.string.camera_facing),
                getString((info.facing == Camera.CameraInfo.CAMERA_FACING_BACK)? R.string.camera_facing_back: R.string.camera_facing_front)));
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
            Camera.CameraInfo ci = new Camera.CameraInfo();
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

    @Override
    protected Object[] getInfoParams() {
        return mParams;
    }

    @Override
    protected InfoItem getItem(int infoId, Object... params) {
        return null;
    }

    @Override
    ArrayList<InfoItem> getItems() {
        if (null == sCameraItems) {
            sCameraItems = new ArrayList<>();
            int num = Camera.getNumberOfCameras();
            if (0 == num) {
                sCameraItems.add(new InfoItem(getString(R.string.item_camera), getString(R.string.camera_none)));
            } else {
                for (int idx = 0; idx < num; ++idx) {
                    addCameraItems(sCameraItems, idx);
                }
            }
        }
        return sCameraItems;
    }
}
