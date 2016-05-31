package com.nemustech.study.sysinfo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;

/**
 * Created by cheolgyoon on 2016. 5. 12..
 *
 */
public class ScreenInfoProvider extends InfoProvider {
    @SuppressWarnings("unused")
    private static final String TAG = ScreenInfoProvider.class.getSimpleName();

    private ArrayList<InfoItem> mScreenContent;
    private Object[] mParams;

    ScreenInfoProvider(Context context) {
        super(context);
    }

    private String formatSize(int w, int h) {
        return String.format("%d x %d", w, h);
    }
    private String formatSize(float w, float h) {
        return String.format("%f x %f", w, h);
    }
    private String formatSize(Point p) {
        return String.format("%d x %d", p.x, p.y);
    }

    private String formatDpi(int dpi) {
        String vn;
        switch (dpi) {
            case DisplayMetrics.DENSITY_LOW:
                vn = "ldpi";
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                vn = "mdpi";
                break;
            case DisplayMetrics.DENSITY_HIGH:
                vn = "hdpi";
                break;
            case DisplayMetrics.DENSITY_XHIGH:
                vn = "xhdpi";
                break;
            case DisplayMetrics.DENSITY_XXHIGH:
                vn = "xxhdpi";
                break;
            case DisplayMetrics.DENSITY_XXXHIGH:
                vn = "xxxhdpi";
                break;
            case DisplayMetrics.DENSITY_TV:
                vn = "tvdpi";
                break;
            default:
                vn = getString(R.string.unknown);
        }
        return String.format("%d dpi (%s)", dpi, vn);
    }

    @SuppressWarnings("deprecation")
    private String formatPixelFormat(int format) {
        String name;
        switch (format) {
            case PixelFormat.A_8:
                name = "A_8";
                break;
            case PixelFormat.LA_88:
                name = "LA_88";
                break;
            case PixelFormat.L_8:
                name = "L_8";
                break;
            case PixelFormat.RGBA_4444:
                name = "RGBA_4444";
                break;
            case PixelFormat.RGBA_5551:
                name = "RGBA_5551";
                break;
            case PixelFormat.RGBA_8888:
                name = "RGBA_8888";
                break;
            case PixelFormat.RGBX_8888:
                name = "RGBX_8888";
                break;
            case PixelFormat.RGB_332:
                name = "RGB_332";
                break;
            case PixelFormat.OPAQUE:
                name = "OPAQUE";
                break;
            case PixelFormat.RGB_565:
                name = "RGB_565";
                break;
            case PixelFormat.RGB_888:
                name = "RGB_888";
                break;
            case PixelFormat.TRANSLUCENT:
                name = "TRANSLUCENT";
                break;
            case PixelFormat.TRANSPARENT:
                name = "TRANSPARENT";
                break;
            default:
                name = getString(R.string.unknown);
                break;
        }
        return String.format("%s (%d)", name, format);
    }
    private Point mTmpPoint = new Point();
    private String formatSizeDp(Display disp, DisplayMetrics dm) {
        getDisplaySize(mTmpPoint, disp);
        final float w = mTmpPoint.x / dm.density;
        final float h = mTmpPoint.y / dm.density;
        return String.format("%.2f x %.2f dp", w, h);
    }
    private String formatSizeInch(Display disp, DisplayMetrics dm) {
        getDisplaySize(mTmpPoint, disp);
        final float w = mTmpPoint.x / dm.xdpi;
        final float h = mTmpPoint.y / dm.ydpi;
        final double d = Math.sqrt(w * w + h * h);
        return String.format("%.2f x %.2f (%.2f inch)", w, h, d);
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    private void getDisplaySize(Point outPoint, Display disp) {
        try {
            if (Build.VERSION_CODES.JELLY_BEAN_MR1 <= Build.VERSION.SDK_INT) {
                disp.getRealSize(outPoint);
            } else {
                disp.getSize(outPoint);
            }
        } catch (Error e) {
            Log.i(TAG, e.toString());
            outPoint.x = disp.getWidth();
            outPoint.y = disp.getHeight();
        }
    }

    private String formatDisplayState(int state) {
        switch (state) {
            case Display.STATE_ON: return String.format("On (%d)", state);
            case Display.STATE_OFF: return String.format("Off (%d)", state);
            case Display.STATE_DOZE: return String.format("Doze (%d)", state);
            case Display.STATE_DOZE_SUSPEND: return String.format("Doze suspend (%d)", state);
            case Display.STATE_UNKNOWN: return String.format("Unknown (%d)", state);
        }
        return getString(R.string.unknown_int_value, state);
    }

    @SuppressLint("NewApi")
    private String getSizeRange(Display disp) {
        Point largest = new Point();
        disp.getCurrentSizeRange(mTmpPoint, largest);
        return String.format("%4d x%5d - %4d x%5d", mTmpPoint.x, mTmpPoint.y, largest.x, largest.y);
    }

    @SuppressLint("InlinedApi")
    private String formatFlags(int flags) {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("Feature flags: 0x%02X", flags));
        if ((flags & Display.FLAG_SUPPORTS_PROTECTED_BUFFERS) != 0) {
            sb.append("\n\t").append(String.format("0x%02X: Supports protected buffer", Display.FLAG_SUPPORTS_PROTECTED_BUFFERS));
            flags &= ~Display.FLAG_SUPPORTS_PROTECTED_BUFFERS;
        }
        if ((flags & Display.FLAG_SECURE) != 0) {
            sb.append("\n\t").append(String.format("0x%02X: Secure", Display.FLAG_SECURE));
            flags &= ~Display.FLAG_SECURE;
        }
        if ((flags & Display.FLAG_PRIVATE) != 0) {
            sb.append("\n\t").append(String.format("0x%02X: Private", Display.FLAG_PRIVATE));
            flags &= ~Display.FLAG_PRIVATE;
        }
        if ((flags & Display.FLAG_PRESENTATION) != 0) {
            sb.append("\n\t").append(String.format("0x%02X: Presentation", Display.FLAG_PRESENTATION));
            flags &= ~Display.FLAG_PRESENTATION;
        }
        if ((flags & Display.FLAG_ROUND) != 0) {
            sb.append("\n\t").append(String.format("0x%02X: Round shaped", Display.FLAG_ROUND));
            flags &= ~Display.FLAG_ROUND;
        }
        if (0 != flags) {
            sb.append("\n\t").append(String.format("0x%02X: Unknown", flags));
        }

        return sb.toString();
    }

    private Display[] getDisplays() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            return  new Display[] { wm.getDefaultDisplay() };
        } else {
            DisplayManager dm = (DisplayManager) mContext.getSystemService(Context.DISPLAY_SERVICE);
            return dm.getDisplays();
        }
    }

    private String formatLogicalSize(int layout) {
        int layoutSize = layout & Configuration.SCREENLAYOUT_SIZE_MASK;
        switch (layoutSize) {
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                return getString(R.string.screen_size_small, layoutSize);
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                return getString(R.string.screen_size_normal, layoutSize);
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                return getString(R.string.screen_size_large, layoutSize);
            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                return getString(R.string.screen_size_xlarge, layoutSize);
            case Configuration.SCREENLAYOUT_SIZE_UNDEFINED:
                return getString(R.string.screen_size_undefined, layoutSize);
            default:
        }
        return mContext.getString(R.string.unknown_int_value, layoutSize);
    }

    private String formatUiMode(int uiMode) {
        StringBuilder sb = new StringBuilder();
        int modeType = uiMode & Configuration.UI_MODE_TYPE_MASK;

        switch (modeType) {
            case Configuration.UI_MODE_TYPE_APPLIANCE:
                sb.append(getString(R.string.screen_mode_type_appliance, modeType)); break;
            case Configuration.UI_MODE_TYPE_CAR:
                sb.append(getString(R.string.screen_mode_type_car, modeType)); break;
            case Configuration.UI_MODE_TYPE_DESK:
                sb.append(getString(R.string.screen_mode_type_desk, modeType)); break;
            case Configuration.UI_MODE_TYPE_NORMAL:
                sb.append(getString(R.string.screen_mode_type_normal, modeType)); break;
            case Configuration.UI_MODE_TYPE_TELEVISION:
                sb.append(getString(R.string.screen_mode_type_television, modeType)); break;
            case Configuration.UI_MODE_TYPE_WATCH:
                sb.append(getString(R.string.screen_mode_type_watch, modeType)); break;
            case Configuration.UI_MODE_TYPE_UNDEFINED:
                sb.append(getString(R.string.screen_mode_type_undefined, modeType)); break;
            default:
                sb.append(getString(R.string.unknown_int_value, modeType)); break;
        }
        sb.append('\n');
        int night = uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (night) {
            case Configuration.UI_MODE_NIGHT_NO:
                sb.append(getString(R.string.screen_night_no, night)); break;
            case Configuration.UI_MODE_NIGHT_YES:
                sb.append(getString(R.string.screen_night_yes, night)); break;
            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                sb.append(getString(R.string.screen_night_undefined, night)); break;
            default:
                sb.append(getString(R.string.unknown_int_value, modeType)); break;
        }
        return sb.toString();
    }

    private String formatLayoutDirection(int direction) {
        switch (direction) {
            case View.LAYOUT_DIRECTION_LTR:
                return getString(R.string.screen_layout_ltr, direction);
            case View.LAYOUT_DIRECTION_RTL:
                return getString(R.string.screen_layout_rtl, direction);
            case View.LAYOUT_DIRECTION_LOCALE:
            case View.LAYOUT_DIRECTION_INHERIT:
                //  These two values are not expected here.
        }
        return getString(R.string.unknown_int_value, direction);
    }

    @SuppressLint("NewApi")
    private void setParamsForDisplay(Display display) {
        DisplayMetrics metrics = new DisplayMetrics();
        try {
            display.getRealMetrics(metrics);
        } catch (Error e) {
            Log.i(TAG, e.toString());
            display.getMetrics(metrics);
        }
        mParams = new Object[] { display, metrics };
    }

    @Override
    protected Object[] getInfoParams() {
        return mParams;
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    @Override
    protected InfoItem getItem(int titleId, Object... params) {
        Configuration config = mContext.getResources().getConfiguration();
        String value;
        Display display = null;
        DisplayMetrics metrics;

        try {
            switch (titleId) {
                case R.string.screen_logical_size: value = formatLogicalSize(config.screenLayout); break;
                case R.string.screen_smallest_width_dp: value = String.format("%d", config.smallestScreenWidthDp); break;
                case R.string.screen_ui_mode: value = formatUiMode(config.uiMode); break;
                case R.string.screen_layout_direction: value = formatLayoutDirection(config.getLayoutDirection()); break;
                default:
                    display = (params[0] instanceof Display)? (Display)params[0]: null;
                    assert display != null;
                    metrics = (params[1] instanceof DisplayMetrics)? (DisplayMetrics)params[1]: null;
                    assert metrics != null;
                    switch (titleId) {
                        case R.string.screen_dpi: value = formatDpi(metrics.densityDpi); break;
                        case R.string.screen_available_size: display.getSize(mTmpPoint); value = formatSize(mTmpPoint); break;
                        case R.string.screen_real_size: getDisplaySize(mTmpPoint, display); value = formatSize(mTmpPoint); break;
                        case R.string.screen_logical_size_in_dp: value = formatSizeDp(display, metrics); break;
                        case R.string.screen_physical_size_in_inch: value = formatSizeInch(display, metrics); break;
                        case R.string.screen_pixel_format: value = formatPixelFormat(display.getPixelFormat()); break;
                        case R.string.screen_physical_dpi: value = formatSize(metrics.xdpi, metrics.ydpi); break;
                        case R.string.screen_logical_density: value = String.valueOf(metrics.density); break;
                        case R.string.screen_scaled_density: value = String.valueOf(metrics.scaledDensity); break;
                        case R.string.screen_id: value = String.valueOf(display.getDisplayId()); break;
                        case R.string.screen_name: value = display.getName(); break;
                        case R.string.screen_rotation: value = String.valueOf(display.getRotation()); break;
                        case R.string.screen_refresh_rate: value = String.valueOf(display.getRefreshRate()); break;
                        case R.string.screen_supported_refresh_rates: value = formatFloatArray(display.getSupportedRefreshRates()); break;
                        case R.string.screen_app_vsync_offset: value = String.valueOf(display.getAppVsyncOffsetNanos()); break;
                        case R.string.screen_presentation_deadline: value = String.format("%,d", display.getPresentationDeadlineNanos()); break;
                        case R.string.screen_state: value = formatDisplayState(display.getState()); break;
                        case R.string.screen_size_range: value = getSizeRange(display); break;
                        case R.string.screen_other_features: value = formatFlags(display.getFlags()); break;
                        default: value = getString(R.string.invalid_item);
                    }
            }
        } catch (Error e) {
            Log.e(TAG, e.toString());
            value = getString(R.string.unsupported);
        }

        String title = (null == display)? getString(titleId): String.format("Screen %d: %s", display.getDisplayId(), getString(titleId));
        return new InfoItem(title, value);
    }

    private static final InfoSpec[] sConfigSpecs = {
            new InfoSpec(R.string.screen_logical_size, 4),
            new InfoSpec(R.string.screen_smallest_width_dp, 13),
            new InfoSpec(R.string.screen_ui_mode, 8),
            new InfoSpec(R.string.screen_layout_direction, 17),
    };

    private static final InfoSpec[] sDisplaySpecs = {
            new InfoSpec(R.string.screen_id, 1),
            new InfoSpec(R.string.screen_dpi, 4),
            new InfoSpec(R.string.screen_available_size, 13),
            new InfoSpec(R.string.screen_real_size, 17),
            new InfoSpec(R.string.screen_size_range, 16),
            new InfoSpec(R.string.screen_logical_size_in_dp, 1),
            new InfoSpec(R.string.screen_physical_size_in_inch, 1),
            new InfoSpec(R.string.screen_pixel_format, 1),
            new InfoSpec(R.string.screen_physical_dpi, 1),
            new InfoSpec(R.string.screen_logical_density, 1),
            new InfoSpec(R.string.screen_scaled_density, 1),
            new InfoSpec(R.string.screen_name, 17),
            new InfoSpec(R.string.screen_rotation, 8),
            new InfoSpec(R.string.screen_refresh_rate, 1),
            new InfoSpec(R.string.screen_supported_refresh_rates, 21),
            new InfoSpec(R.string.screen_app_vsync_offset, 21),
            new InfoSpec(R.string.screen_presentation_deadline, 17),
            new InfoSpec(R.string.screen_state, 20),
            new InfoSpec(R.string.screen_other_features, 17),
    };

    @Override
    ArrayList<InfoItem> getItems() {
        if (null == mScreenContent) {
            mScreenContent = new ArrayList<>();
        }

        mScreenContent.clear();

        addItems(mScreenContent, sConfigSpecs);

        Display[] displays = getDisplays();
        mScreenContent.add(new InfoItem(getString(R.string.screen_count), String.valueOf(displays.length)));

        for (Display display: displays) {
            setParamsForDisplay(display);
            addItems(mScreenContent, sDisplaySpecs);
        }

        return mScreenContent;
    }
}
