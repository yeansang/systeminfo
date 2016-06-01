package com.nemustech.study.sysinfo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cheolgyoon on 2016. 5. 31..
 *
 */
public class InputInfoProvider extends InfoProvider {
    @SuppressWarnings("unused")
    private static final String TAG = InputInfoProvider.class.getSimpleName();
    private static ArrayList<InfoItem> sInfoItems;
    InputInfoProvider(Context context) {
        super(context);
    }

    private boolean bitsSet(int opl, int opr) {
        return (opl & opr) == opr;
    }

    private String formatSource(int source) {
        StringBuilder sb = new StringBuilder();

        if (bitsSet(source, InputDevice.SOURCE_BLUETOOTH_STYLUS)) {
            sb.append("\n\tBluetooth Stylus");
        }
        if (bitsSet(source, InputDevice.SOURCE_DPAD)) {
            sb.append("\n\tDpad");
        }
        if (bitsSet(source, InputDevice.SOURCE_GAMEPAD)) {
            sb.append("\n\tGame pad");
        }
        if (bitsSet(source, InputDevice.SOURCE_HDMI)) {
            sb.append("\n\tHDMI");
        }
        if (bitsSet(source, InputDevice.SOURCE_JOYSTICK)) {
            sb.append("\n\tJoystick");
        }
        if (bitsSet(source, InputDevice.SOURCE_KEYBOARD)) {
            sb.append("\n\tKeyboard");
        }
        if (bitsSet(source, InputDevice.SOURCE_MOUSE)) {
            sb.append("\n\tMouse");
        }
        if (bitsSet(source, InputDevice.SOURCE_STYLUS)) {
            sb.append("\n\tStylus");
        }
        if (bitsSet(source, InputDevice.SOURCE_TOUCH_NAVIGATION)) {
            sb.append("\n\tTouch navigation");
        }
        if (bitsSet(source, InputDevice.SOURCE_TOUCHPAD)) {
            sb.append("\n\tTouchpad");
        }
        if (bitsSet(source, InputDevice.SOURCE_TOUCHSCREEN)) {
            sb.append("\n\tTouch screen");
        }
        if (bitsSet(source, InputDevice.SOURCE_TRACKBALL)) {
            sb.append("\n\tTrackball");
        }
        if (0 == sb.length()) {
            sb.append("\n\t").append(getString(R.string.unknown));
        }
        return sb.toString();
    }
    private void appendSourceClass(StringBuilder sb, int source) {
        int sourceClass = source & InputDevice.SOURCE_CLASS_MASK;
        if (bitsSet(sourceClass, InputDevice.SOURCE_CLASS_BUTTON)) {
            sb.append("\t- Button class");
        }
        if (bitsSet(sourceClass, InputDevice.SOURCE_CLASS_JOYSTICK)) {
            sb.append("\t- Joystick class");
        }
        if (bitsSet(sourceClass, InputDevice.SOURCE_CLASS_POINTER)) {
            sb.append("\t- Pointer class");
        }
        if (bitsSet(sourceClass, InputDevice.SOURCE_CLASS_POSITION)) {
            sb.append("\t- Position class");
        }
        if (bitsSet(sourceClass, InputDevice.SOURCE_CLASS_TRACKBALL)) {
            sb.append("\t- Trackball class");
        }
        if (sourceClass == InputDevice.SOURCE_CLASS_NONE) {
            sb.append("\t- No class");
        }
    }
    private String formatSources(int sources) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("x%08x", sources));
        sb.append(String.format(" (class x%02x)\n", (sources & InputDevice.SOURCE_CLASS_MASK)));
        appendSourceClass(sb, sources);
        sb.append(formatSource(sources));
        return sb.toString();
    }

    private String formatAxis(int axis) {
        String name;
        switch (axis) {
            case MotionEvent.AXIS_BRAKE: name = "brake"; break;
            case MotionEvent.AXIS_DISTANCE: name = "distance"; break;
            case MotionEvent.AXIS_GAS: name = "gas"; break;
            case MotionEvent.AXIS_GENERIC_1: name = "generic 1"; break;
            case MotionEvent.AXIS_HAT_X: name = "hat X"; break;
            case MotionEvent.AXIS_HAT_Y: name = "hat Y"; break;
            case MotionEvent.AXIS_HSCROLL: name = "horizontal scroll"; break;
            case MotionEvent.AXIS_LTRIGGER: name = "left trigger"; break;
            case MotionEvent.AXIS_ORIENTATION: name = "orientation"; break;
            case MotionEvent.AXIS_PRESSURE: name = "pressure"; break;
            case MotionEvent.AXIS_RTRIGGER: name = "right trigger"; break;
            case MotionEvent.AXIS_RUDDER: name = "rudder"; break;
            case MotionEvent.AXIS_RX: name = "rotation X"; break;
            case MotionEvent.AXIS_RY: name = "rotation Y"; break;
            case MotionEvent.AXIS_RZ: name = "rotation Z"; break;
            case MotionEvent.AXIS_SIZE: name = "size"; break;
            case MotionEvent.AXIS_THROTTLE: name = "throttle"; break;
            case MotionEvent.AXIS_TILT: name = "tilt"; break;
            case MotionEvent.AXIS_TOOL_MAJOR: name = "major tool"; break;
            case MotionEvent.AXIS_TOOL_MINOR: name = "minor tool"; break;
            case MotionEvent.AXIS_TOUCH_MAJOR: name = "major touch"; break;
            case MotionEvent.AXIS_TOUCH_MINOR: name = "minor touch"; break;
            case MotionEvent.AXIS_VSCROLL: name = "vertical scroll"; break;
            case MotionEvent.AXIS_WHEEL: name = "wheel"; break;
            case MotionEvent.AXIS_X: name = "X"; break;
            case MotionEvent.AXIS_Y: name = "Y"; break;
            case MotionEvent.AXIS_Z: name = "Z"; break;
            default: name = getString(R.string.unknown);
        }

        return String.format("%s (%d)", name, axis);
    }

    @SuppressLint("NewApi")
    private void appendMotionRange(StringBuilder sb, InputDevice.MotionRange range) {
        if (Build.VERSION_CODES.HONEYCOMB_MR1 <= Build.VERSION.SDK_INT) {
            int axis = range.getAxis();
            sb.append("\t\tAxis: ").append(formatAxis(axis)).append('\n');
        }
        float flat = range.getFlat();
        float fuzz = range.getFuzz();
        float min = range.getMin();
        float max = range.getMax();
        sb.append(String.format("\t\tvalue %f ~ %f\n\t\tflat %f fuzz %f\n", min, max, flat, fuzz));
        if (Build.VERSION_CODES.JELLY_BEAN_MR2 <= Build.VERSION.SDK_INT) {
            float resolution = range.getResolution();
            sb.append(String.format("\t\tResolution %f\n", resolution));
        }
    }

    private String formatMotionRanges(List<InputDevice.MotionRange> ranges) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("total %d\n", ranges.size()));
        for (int idx = 0; idx < ranges.size(); ++idx) {
            sb.append(String.format("\tMotion range %d\n", idx));
            InputDevice.MotionRange range = ranges.get(idx);
            appendMotionRange(sb, range);
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    @SuppressLint("NewApi")
    private boolean hasMicrophone(InputDevice device) {
        if (Build.VERSION_CODES.M <= Build.VERSION.SDK_INT) {
            return device.hasMicrophone();
        }
        return false;
    }
    @SuppressLint("NewApi")
    private boolean hasVibrator(InputDevice device) {
        if (Build.VERSION_CODES.JELLY_BEAN <= Build.VERSION.SDK_INT) {
            return device.getVibrator().hasVibrator();
        }
        return false;
    }
    @SuppressLint("NewApi")
    private boolean isVirtual(InputDevice device) {
        if (Build.VERSION_CODES.JELLY_BEAN <= Build.VERSION.SDK_INT) {
            return device.isVirtual();
        }
        return false;
    }

    private String getFeaturesString(InputDevice device) {
        StringBuilder sb = new StringBuilder();
        if (hasVibrator(device)) {
            sb.append("\n\tVibrator");
        }
        if (hasMicrophone(device)) {
            sb.append("\n\tMicrophone");
        }
        if (isVirtual(device)) {
            sb.append("\n\t(virtual device)");
        }
        if (0 < sb.length()) {
            return sb.toString();
        }
        return null;
    }

    @SuppressLint("NewApi")
    private void appendInputProperty(StringBuilder sb, int id, InputDevice device) {
        String title = getString(id);
        String value = null;
        try {
            switch (id) {
                case R.string.input_id:
                    value = String.valueOf(device.getId());
                    break;
                case R.string.input_source:
                    value = formatSources(device.getSources());
                    break;
                case R.string.input_descriptor:
                    value = device.getDescriptor();
                    break;
                case R.string.input_product_id:
                    value = String.valueOf(device.getProductId());
                    break;
                case R.string.input_vendor_id:
                    value = String.valueOf(device.getVendorId());
                    break;
                case R.string.input_controller_number: {
                    int number = device.getControllerNumber();
                    if (0 != number) {
                        value = String.valueOf(number);
                    }
                    break;
                }
                case R.string.input_motion_ranges: {
                    List<InputDevice.MotionRange> ranges = device.getMotionRanges();
                    if (null != ranges && 0 < ranges.size()) {
                        value = formatMotionRanges(device.getMotionRanges());
                    }
                    break;
                }
                case R.string.input_other_features: value = getFeaturesString(device); break;
                default: value = getString(R.string.invalid_item);
            }
        } catch (Error e) {
            Log.e(TAG, e.toString());
            value = getString(R.string.unsupported);
        }
        if (null != value) {
            sb.append(title).append(": ").append(value).append('\n');
        }
    }

    private static final InfoSpec[] sInputSpecs = {
            new InfoSpec(R.string.input_id, 9),
            new InfoSpec(R.string.input_descriptor, 16),
            new InfoSpec(R.string.input_vendor_id, 19),
            new InfoSpec(R.string.input_product_id, 19),
            new InfoSpec(R.string.input_source, 9),
            new InfoSpec(R.string.input_controller_number, 19),
            new InfoSpec(R.string.input_motion_ranges, 12),
            new InfoSpec(R.string.input_other_features, 9),
    };

    private InfoItem getInputItem(int deviceId) {
        InputDevice device = InputDevice.getDevice(deviceId);
        String name;
        StringBuilder sb = new StringBuilder();
        if (null == device) {
            name = String.format("ID: %d", deviceId);
            sb.append(getString(R.string.unknown));
        } else {
            name = device.getName();
            for (InfoSpec spec: sInputSpecs) {
                if (spec.minSdk <= Build.VERSION.SDK_INT) {
                    appendInputProperty(sb, spec.titleId, device);
                }
            }
            sb.deleteCharAt(sb.length() - 1);
        }
        return new InfoItem(name, sb.toString());
    }

    @Override
    ArrayList<InfoItem> getItems() {
        if (null == sInfoItems) {
            sInfoItems = new ArrayList<>();

            int[] ids = InputDevice.getDeviceIds();
            if (null == ids || 0 == ids.length) {
                sInfoItems.add(new InfoItem(getString(R.string.item_input), getString(R.string.input_none)));
            } else {
                for (int id : ids) {
                    sInfoItems.add(getInputItem(id));
                }
            }
        }
        return sInfoItems;
    }

    //  These methods are unused here
    @Override
    protected Object[] getInfoParams() {
        return new Object[0];
    }

    @Override
    protected InfoItem getItem(int infoId, Object... params) {
        return null;
    }
}
