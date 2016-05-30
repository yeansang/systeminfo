package com.nemustech.study.sysinfo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by cheolgyoon on 2016. 5. 30..
 *
 */
public class TelephoneInfoProvider extends InfoProvider {
    @SuppressWarnings("unused")
    private static final String TAG = TelephoneInfoProvider.class.getSimpleName();

    private static ArrayList<InfoItem> sPhoneContent;

    TelephoneInfoProvider(Context context) {
        super(context);
    }

    private TelephonyManager[] mParams;

    private String formatPhoneType(int phoneType) {
        String name;
        switch (phoneType) {
            case TelephonyManager.PHONE_TYPE_CDMA:
                name = "CDMA";
                break;
            case TelephonyManager.PHONE_TYPE_GSM:
                name = "GSM";
                break;
            case TelephonyManager.PHONE_TYPE_SIP:
                name = "SIP";
                break;
            case TelephonyManager.PHONE_TYPE_NONE:
            default:
                name = getString(R.string.unknown);
                break;
        }
        return String.format("%s (%d)", name, phoneType);
    }
    private String formatNetworkType(int networkType) {
        String name;
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                name = "1xRTT";
                break;
            case TelephonyManager.NETWORK_TYPE_CDMA:
                name = "CDMA";
                break;
            case TelephonyManager.NETWORK_TYPE_EDGE:
                name = "EDGE";
                break;
            case TelephonyManager.NETWORK_TYPE_EHRPD:
                name = "EHRPD";
                break;
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                name = "EVDO_0";
                break;
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                name = "EVDO_A";
                break;
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                name = "EVDO_B";
                break;
            case TelephonyManager.NETWORK_TYPE_GPRS:
                name = "GPRS";
                break;
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                name = "HSDPA";
                break;
            case TelephonyManager.NETWORK_TYPE_HSPA:
                name = "HSPA";
                break;
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                name = "HSPAP";
                break;
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                name = "HSUPA";
                break;
            case TelephonyManager.NETWORK_TYPE_IDEN:
                name = "IDEN";
                break;
            case TelephonyManager.NETWORK_TYPE_LTE:
                name = "LTE";
                break;
            case TelephonyManager.NETWORK_TYPE_UMTS:
                name = "UMTS";
                break;
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
            default:
                name = getString(R.string.unknown);
                break;
        }
        return String.format("%s (%d)", name, networkType);
    }
    private String formatSimState(int simState) {
        String name;
        switch (simState) {
            case TelephonyManager.SIM_STATE_ABSENT:
                name = "Absent";
                break;
            case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                name = "Network locked";
                break;
            case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                name = "PIN required";
                break;
            case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                name = "PUK required";
                break;
            case TelephonyManager.SIM_STATE_READY:
                name = "Ready";
                break;
            case TelephonyManager.SIM_STATE_UNKNOWN:
            default:
                name = getString(R.string.unknown);
                break;
        }
        return String.format("%s (%d)", name, simState);
    }

    @SuppressLint("NewApi")
    private String getFeatures(TelephonyManager tm) {
        StringBuilder sb = new StringBuilder();
        if (tm.isNetworkRoaming()) {
            sb.append("Roaming");
        } else {
            sb.append("NOT Roaming");
        }

        if (tm.hasIccCard()) {
            sb.append("\nICC card");
        }
        if (Build.VERSION_CODES.LOLLIPOP <= Build.VERSION.SDK_INT) {
            if (tm.isSmsCapable()) {
                sb.append("\nSMS");
            }
        }

        if (Build.VERSION_CODES.LOLLIPOP_MR1 <= Build.VERSION.SDK_INT) {
            if (tm.isVoiceCapable()) {
                sb.append("\nVoice call");
            }
            if (tm.hasCarrierPrivileges()) {
                sb.append("\nCarrier Privileges");
            }
        }

        if (Build.VERSION_CODES.M <= Build.VERSION.SDK_INT) {
            if (tm.isHearingAidCompatibilitySupported()) {
                sb.append("\nHearing Aid Compatibility support");
            }
            if (tm.isTtyModeSupported()) {
                sb.append("\nTTY(Teletypewriter) support");
            }
            if (tm.isWorldPhone()) {
                sb.append("\nWorld phone");
            }
        }
        return sb.toString();
    }

    @Override
    protected Object[] getInfoParams() {
        return mParams;
    }

    @SuppressLint("NewApi")
    @Override
    protected InfoItem getItem(int titleId, Object... objs) {
        if (objs.length == 1 && objs[0] instanceof TelephonyManager) {
            TelephonyManager tm = (TelephonyManager)(objs[0]);
            String title = getString(titleId);
            String value;
            try {
                switch (titleId) {
                    case R.string.phone_phone_count: value = String.valueOf(tm.getPhoneCount()); break;
                    case R.string.phone_phone_type: value = formatPhoneType(tm.getPhoneType()); break;
                    case R.string.phone_device_id: value = tm.getDeviceId(); break;
                    case R.string.phone_subscriber_id: value = tm.getSubscriberId(); break;
                    case R.string.phone_device_software_version: value = tm.getDeviceSoftwareVersion(); break;
                    case R.string.phone_group_id_l1: value = tm.getGroupIdLevel1(); break;
                    case R.string.phone_line1_number: value = tm.getLine1Number(); break;
                    case R.string.phone_mms_ua: value = tm.getMmsUserAgent(); break;
                    case R.string.phone_mms_ua_prof_url: value = tm.getMmsUAProfUrl(); break;
                    case R.string.phone_network_country_iso: value = tm.getNetworkCountryIso(); break;
                    case R.string.phone_network_operator: value = tm.getNetworkOperator(); break;
                    case R.string.phone_network_operator_name: value = tm.getNetworkOperatorName(); break;
                    case R.string.phone_network_type: value = formatNetworkType(tm.getNetworkType()); break;
                    case R.string.phone_sim_state: value = formatSimState(tm.getSimState()); break;
                    case R.string.phone_sim_country_iso: value = tm.getSimCountryIso(); break;
                    case R.string.phone_sim_operator: value = tm.getSimOperator(); break;
                    case R.string.phone_sim_operator_name: value = tm.getSimOperatorName(); break;
                    case R.string.phone_sim_serial: value = tm.getSimSerialNumber(); break;
                    case R.string.phone_voice_mail_tag: value = tm.getVoiceMailAlphaTag(); break;
                    case R.string.phone_voice_mail_number: value = tm.getVoiceMailNumber(); break;
                    case R.string.phone_features: value = getFeatures(tm); break;
                    default: value = getString(R.string.invalid_item);
                }
            } catch (Error e) {
                Log.e(TAG, e.toString());
                value = getString(R.string.unsupported);
            }
            return new InfoItem(title, value);
        }

        return null;
    }

    private static final InfoSpec[] sPhoneSpecs = {
        new InfoSpec(R.string.phone_phone_count, 23),
        new InfoSpec(R.string.phone_phone_type, 1),
        new InfoSpec(R.string.phone_device_id, 1),
        new InfoSpec(R.string.phone_subscriber_id, 1),
        new InfoSpec(R.string.phone_device_software_version, 1),
        new InfoSpec(R.string.phone_line1_number, 1),
        new InfoSpec(R.string.phone_mms_ua, 19),
        new InfoSpec(R.string.phone_mms_ua_prof_url, 19),
        new InfoSpec(R.string.phone_network_country_iso, 1),
        new InfoSpec(R.string.phone_network_operator, 1),
        new InfoSpec(R.string.phone_network_operator_name, 1),
        new InfoSpec(R.string.phone_network_type, 1),
        new InfoSpec(R.string.phone_sim_state, 1),
        new InfoSpec(R.string.phone_voice_mail_tag, 1),
        new InfoSpec(R.string.phone_voice_mail_number, 1),
        new InfoSpec(R.string.phone_features, 1),
    };

    private static final InfoSpec[] sGsmSpecs = {
        new InfoSpec(R.string.phone_group_id_l1, 18),
    };

    private static final InfoSpec[] sSimSpecs = {
        new InfoSpec(R.string.phone_sim_country_iso, 1),
        new InfoSpec(R.string.phone_sim_operator, 1),
        new InfoSpec(R.string.phone_sim_operator_name, 1),
        new InfoSpec(R.string.phone_sim_serial, 1),
    };

    @Override
    ArrayList<InfoItem> getItems() {
        if (null == sPhoneContent) {
            sPhoneContent = new ArrayList<>();

            TelephonyManager tm = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
            if (null == tm || tm.getPhoneType() == TelephonyManager.PHONE_TYPE_NONE) {
                sPhoneContent.add(new InfoItem(getString(R.string.phone_phone_type), getString(R.string.phone_none)));
            } else {
                mParams = new TelephonyManager[] { tm };
                addItems(sPhoneContent, sPhoneSpecs);
                if (tm.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM) {
                    addItems(sPhoneContent, sGsmSpecs);
                }
                if (tm.getSimState() == TelephonyManager.SIM_STATE_READY) {
                    addItems(sPhoneContent, sSimSpecs);
                }
            }
        }
        return sPhoneContent;
    }
}
