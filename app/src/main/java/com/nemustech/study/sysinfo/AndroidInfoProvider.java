package com.nemustech.study.sysinfo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by cheolgyoon on 2016. 5. 11..
 */
public class AndroidInfoProvider extends InfoProvider {
    @SuppressWarnings("unused")
    private static final String TAG = AndroidInfoProvider.class.getSimpleName();
    static ArrayList<InfoItem> sAndroidContent;

    public AndroidInfoProvider(Context context) {
        super(context);
    }

    private static final String[] SDK_NAMES = {
            "* Unknown",
            "Base",
            "Base_1_1",
            "Cupcake",
            "Donut",
            "Eclair",
            "Eclair_0_1",
            "Eclair MR1",
            "Froyo",
            "Gingerbread",
            "Gingerbread MR1",
            "Honeycomb",
            "Honeycomb_MR1",
            "Honeycomb_MR2",
            "Ice Cream Sandwich",
            "Ice Cream Sandwich MR1",
            "Jelly Bean",
            "Jelly Bean MR1",
            "Jelly Bean MR2",
            "KitKat",
            "KitKat for Wearables",
            "Lollipop",
            "Lollipop MR1",
            "M"
    };

    private String getSdkName(int sdkInt) {
        int idx = (sdkInt < 0 || SDK_NAMES.length <= sdkInt)? 0: sdkInt;
        return SDK_NAMES[idx];
    }

    private String formatSdk(int sdkInt) {
        return String.format("%d (%s)", sdkInt, getSdkName(sdkInt));
    }
    private String formatPreviewSdk(int sdkInt) {
        String previewName = (0 == sdkInt)? getString(R.string.android_tags): getSdkName(sdkInt);
        return String.format("%d (%s)", sdkInt, previewName);
    }

    @Override
    protected InfoItem getItem(int titleId) {
        String title = getString(titleId);
        String value;
        try {
            switch (titleId) {
                case R.string.android_release: value = Build.VERSION.RELEASE; break;
                case R.string.android_sdk: value = formatSdk(Build.VERSION.SDK_INT); break;
                case R.string.android_time: value = formatTime(Build.TIME); break;
                case R.string.android_tags: value = Build.TAGS; break;
                case R.string.android_type: value = Build.TYPE; break;
                case R.string.android_codename: value = Build.VERSION.CODENAME; break;
                case R.string.android_display: value = Build.DISPLAY; break;
                case R.string.android_fingerprint: value = Build.FINGERPRINT; break;
                case R.string.android_manufacturer: value = Build.MANUFACTURER; break;
                case R.string.android_brand: value = Build.BRAND; break;
                case R.string.android_product: value = Build.PRODUCT; break;
                case R.string.android_device: value = Build.DEVICE; break;
                case R.string.android_model: value = Build.MODEL; break;
                case R.string.android_hardware: value = Build.HARDWARE; break;
                case R.string.android_serial: value = Build.SERIAL; break;
                case R.string.android_board: value = Build.BOARD; break;
                case R.string.android_bootloader: value = Build.BOOTLOADER; break;
                case R.string.android_radio: {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                        value = Build.RADIO;
                    } else {
                        value = Build.getRadioVersion();
                    }
                } break;
                case R.string.android_incremental: value = Build.VERSION.INCREMENTAL; break;
                case R.string.android_host: value = Build.HOST; break;
                case R.string.android_id: value = Build.ID; break;
                case R.string.android_secure_id: value = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID); break;
                case R.string.android_supported_abis: {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
                        value = Build.CPU_ABI;
                    } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        value = formatStringArray(Build.CPU_ABI, Build.CPU_ABI2);
                    } else {
                        value = formatStringArray(Build.SUPPORTED_ABIS);
                    }
                } break;
                case R.string.android_supported_32bit_abis: value = formatStringArray(Build.SUPPORTED_32_BIT_ABIS); break;
                case R.string.android_supported_64bit_abis: value = formatStringArray(Build.SUPPORTED_64_BIT_ABIS); break;
                case R.string.android_base_os: value = Build.VERSION.BASE_OS; break;
                case R.string.android_preview_sdk: value = formatPreviewSdk(Build.VERSION.PREVIEW_SDK_INT); break;

                default: value = getString(R.string.invalid_item);
            }
        } catch (Error e) {
            Log.e(TAG, e.toString());
            value = getString(R.string.unsupported);
        }
        return new InfoItem(title, value);
    }

    private static final InfoSpec[] itemSpecs = {
        new InfoSpec(R.string.android_release, 1),
        new InfoSpec(R.string.android_sdk, 4),
        new InfoSpec(R.string.android_time, 1),
        new InfoSpec(R.string.android_tags, 1),
        new InfoSpec(R.string.android_type, 1),
        new InfoSpec(R.string.android_codename, 4),
        new InfoSpec(R.string.android_display, 3),
        new InfoSpec(R.string.android_id, 1),
        new InfoSpec(R.string.android_fingerprint, 1),
        new InfoSpec(R.string.android_manufacturer, 4),
        new InfoSpec(R.string.android_brand, 1),
        new InfoSpec(R.string.android_product, 1),
        new InfoSpec(R.string.android_device, 1),
        new InfoSpec(R.string.android_model, 1),
        new InfoSpec(R.string.android_hardware, 8),
        new InfoSpec(R.string.android_serial, 9),
        new InfoSpec(R.string.android_board, 1),
        new InfoSpec(R.string.android_bootloader, 8),
        new InfoSpec(R.string.android_radio, 8),
        new InfoSpec(R.string.android_incremental, 1),
        new InfoSpec(R.string.android_supported_abis, 4),
        new InfoSpec(R.string.android_supported_32bit_abis, 1),
        new InfoSpec(R.string.android_supported_64bit_abis, 1),
        new InfoSpec(R.string.android_host, 1),
        new InfoSpec(R.string.android_secure_id, 3),
        new InfoSpec(R.string.android_base_os, 23),
        new InfoSpec(R.string.android_preview_sdk, 23),
    };

    @Override
    ArrayList<InfoItem> getItems() {
        if (null == sAndroidContent) {
            sAndroidContent = new ArrayList<>();
            for (int idx = 0; idx < itemSpecs.length; ++idx) {
                InfoItem item = getItem(itemSpecs[idx]);
                if (null != item) {
                    sAndroidContent.add(item);
                }
            }
//            sAndroidContent.add(getItem(R.string.android_release));
//            sAndroidContent.add(getItem(R.string.android_sdk));
//            sAndroidContent.add(getItem(R.string.android_time));
//            sAndroidContent.add(getItem(R.string.android_tags));
//            sAndroidContent.add(getItem(R.string.android_type));
//            sAndroidContent.add(getItem(R.string.android_codename));
//            sAndroidContent.add(getItem(R.string.android_display));
//            sAndroidContent.add(getItem(R.string.android_id));
//            sAndroidContent.add(getItem(R.string.android_fingerprint));
//            sAndroidContent.add(getItem(R.string.android_manufacturer));
//            sAndroidContent.add(getItem(R.string.android_brand));
//            sAndroidContent.add(getItem(R.string.android_product));
//            sAndroidContent.add(getItem(R.string.android_device));
//            sAndroidContent.add(getItem(R.string.android_model));
//            sAndroidContent.add(getItem(R.string.android_hardware));
//            sAndroidContent.add(getItem(R.string.android_serial));
//            sAndroidContent.add(getItem(R.string.android_board));
//            sAndroidContent.add(getItem(R.string.android_bootloader));
//            sAndroidContent.add(getItem(R.string.android_radio));
//            sAndroidContent.add(getItem(R.string.android_incremental));
//            if (Build.VERSION_CODES.LOLLIPOP <= Build.VERSION.SDK_INT) {
//                sAndroidContent.add(getItem(R.string.android_supported_abis));
//                sAndroidContent.add(getItem(R.string.android_supported_32bit_abis));
//                sAndroidContent.add(getItem(R.string.android_supported_64bit_abis));
//            }
//            sAndroidContent.add(getItem(R.string.android_host));
//            sAndroidContent.add(getItem(R.string.android_secure_id));
        }
        return sAndroidContent;
    }
}
