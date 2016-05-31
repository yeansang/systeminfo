package com.nemustech.study.sysinfo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cheolgyoon on 2016. 5. 31..
 *
 */
public class SensorInfoProvider extends InfoProvider {
    @SuppressWarnings("unused")
    private static final String TAG = SensorInfoProvider.class.getSimpleName();

    private static ArrayList<InfoItem> sSensorContent;

    SensorInfoProvider(Context context) {
        super(context);
    }

    @SuppressWarnings("deprecation")
    private String formatSensorType(int type) {
        String name;
        switch (type) {
            case Sensor.TYPE_ACCELEROMETER: name = "Accelerometer"; break;
            case Sensor.TYPE_ALL: name = "All"; break;
            case Sensor.TYPE_AMBIENT_TEMPERATURE: name = "Ambient temperature"; break;
            case Sensor.TYPE_GAME_ROTATION_VECTOR: name = "Rotation vector"; break;
            case Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR: name = "Geomagnetic rotation vector"; break;
            case Sensor.TYPE_GRAVITY: name = "Gravity"; break;
            case Sensor.TYPE_GYROSCOPE: name = "Gyroscope"; break;
            case Sensor.TYPE_GYROSCOPE_UNCALIBRATED: name = "Gyroscope (uncalibrated)"; break;
            case Sensor.TYPE_HEART_RATE: name = "Heart rate"; break;
            case Sensor.TYPE_LIGHT: name = "Light"; break;
            case Sensor.TYPE_LINEAR_ACCELERATION: name = "Linear acceleration"; break;
            case Sensor.TYPE_MAGNETIC_FIELD: name = "Magnetic field"; break;
            case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED: name = "Magnetic field (uncalibrated)"; break;
            case Sensor.TYPE_ORIENTATION: name = "Orientation"; break;
            case Sensor.TYPE_PRESSURE: name = "Pressure"; break;
            case Sensor.TYPE_PROXIMITY: name = "Proximity"; break;
            case Sensor.TYPE_RELATIVE_HUMIDITY: name = "Relative humidity"; break;
            case Sensor.TYPE_ROTATION_VECTOR: name = "Rotation vector"; break;
            case Sensor.TYPE_SIGNIFICANT_MOTION: name = "Significant motion"; break;
            case Sensor.TYPE_STEP_COUNTER: name = "Step counter"; break;
            case Sensor.TYPE_STEP_DETECTOR: name = "Step detector"; break;
            case Sensor.TYPE_TEMPERATURE: name = "Temperature"; break;
            default:  name = getString(R.string.unknown);
        }
        return String.format("%s (%d)", name, type);
    }

    private String formatReportingMode(int mode) {
        String strMode = null;
        switch (mode) {
            case Sensor.REPORTING_MODE_CONTINUOUS: strMode = "Continuous"; break;
            case Sensor.REPORTING_MODE_ON_CHANGE: strMode = "On change"; break;
            case Sensor.REPORTING_MODE_ONE_SHOT: strMode = "One shot"; break;
            case Sensor.REPORTING_MODE_SPECIAL_TRIGGER: strMode = "Special trigger"; break;
            default: break;
        }
        if (null == strMode) {
            return getString(R.string.unknown_int_value, mode);
        }
        return String.format("%s (%d)", strMode, mode);
    }

    @SuppressLint("NewApi")
    private void appendSensorProperty(StringBuilder sb, int id, Sensor sensor) {
        String title = getString(id);
        String value;
        try {
            switch (id) {
                case R.string.sensor_type:
                    value = formatSensorType(sensor.getType());
                    if (Build.VERSION_CODES.KITKAT_WATCH <= Build.VERSION.SDK_INT) {
                        value += "\n\t" + sensor.getStringType();
                    }
                    break;
                case R.string.sensor_vendor: value = sensor.getVendor(); break;
                case R.string.sensor_version: value = String.valueOf(sensor.getVersion()); break;
                case R.string.sensor_power: value = String.valueOf(sensor.getPower()); break;
                case R.string.sensor_resolution: value = String.valueOf(sensor.getResolution()); break;
                case R.string.sensor_max_range: value = String.valueOf(sensor.getMaximumRange()); break;
                case R.string.sensor_delay: {
                    int max = (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)? -1: sensor.getMaxDelay();
                    if (max <= 0) {
                        value = String.format("minimum %,d", sensor.getMinDelay());
                    } else {
                        value = String.format("%,d ~ %,d", sensor.getMinDelay(), max);
                    }
                    break;
                }
                case R.string.sensor_batch_fifo_size: {
                    int max = sensor.getFifoMaxEventCount();
                    if (0 == max) {
                        value = getString(R.string.sensor_batch_not_supported);
                    } else {
                        value = String.format("%,d ~ %,d", sensor.getFifoReservedEventCount(), max);
                    }
                    break;
                }
                case R.string.sensor_reporting_mode: value = formatReportingMode(sensor.getReportingMode()); break;
                default: value = getString(R.string.invalid_item);
            }
        } catch (Error e) {
            Log.e(TAG, e.toString());
            value = getString(R.string.unsupported);
        }
        sb.append(title).append(": ").append(value).append('\n');
    }

    private static final InfoSpec[] sSensorSpecs = {
            new InfoSpec(R.string.sensor_type, 3),
            new InfoSpec(R.string.sensor_vendor, 3),
            new InfoSpec(R.string.sensor_version, 3),
            new InfoSpec(R.string.sensor_power, 3),
            new InfoSpec(R.string.sensor_resolution, 3),
            new InfoSpec(R.string.sensor_max_range, 3),
            new InfoSpec(R.string.sensor_delay, 9),
            new InfoSpec(R.string.sensor_batch_fifo_size, 19),
            new InfoSpec(R.string.sensor_reporting_mode, 21),
    };

    private InfoItem getSensorItem(Sensor sensor) {
        String name = sensor.getName();
        StringBuilder sb = new StringBuilder();
        for (InfoSpec spec: sSensorSpecs) {
            if (spec.minSdk <= Build.VERSION.SDK_INT) {
                appendSensorProperty(sb, spec.titleId, sensor);
            }
        }
        sb.deleteCharAt(sb.length() - 1);
        return new InfoItem(name, sb.toString());
    }

    @Override
    ArrayList<InfoItem> getItems() {
        if (null == sSensorContent) {
            sSensorContent = new ArrayList<>();
            SensorManager sm = (SensorManager)mContext.getSystemService(Context.SENSOR_SERVICE);
            List<Sensor> sensors = sm.getSensorList(Sensor.TYPE_ALL);
            if (null == sensors || 0 == sensors.size()) {
                sSensorContent.add(new InfoItem(getString(R.string.item_sensor), getString(R.string.sensor_none)));
            } else {
                for (Sensor sensor: sensors) {
                    sSensorContent.add(getSensorItem(sensor));
                }
            }
        }

        return sSensorContent;
    }

    //  These methods are not used here

    @Override
    protected Object[] getInfoParams() {
        return null;
    }
    @Override
    protected InfoItem getItem(int infoId, Object... params) {
        return null;
    }
}
