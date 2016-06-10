package com.nemustech.study.sysinfo;

import android.annotation.SuppressLint;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cheolgyoon on 2016. 6. 9..
 *
 */
public class PolicyInfoProvider extends InfoProvider {
    @SuppressWarnings("unused")
    private static final String TAG = PolicyInfoProvider.class.getSimpleName();
    private static ArrayList<InfoItem> sPolicyItems;
    private Object[] mParams;

    PolicyInfoProvider(Context context) {
        super(context);
    }

    @SuppressLint("NewApi")
    private String formatAdministrators(DevicePolicyManager dpm) {
        List<ComponentName> admins = dpm.getActiveAdmins();
        if (null == admins || 0 == admins.size()) {
            return getString(R.string.policy_admin_none);
        }
        StringBuilder sb = new StringBuilder();
        for (ComponentName admin: admins) {
            sb.append(admin.toShortString());
            if (Build.VERSION_CODES.JELLY_BEAN_MR2 <= Build.VERSION.SDK_INT) {
                if (dpm.isDeviceOwnerApp(admin.getPackageName())) {
                    sb.append("\n\tDevice owner");
                }
                if (dpm.isProfileOwnerApp(admin.getPackageName())) {
                    sb.append("\n\tProfile owner");
                }
            }
            sb.append('\n');
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    @SuppressLint("NewApi")
    private String formatRestrictions(DevicePolicyManager dpm) {
        StringBuilder sb = new StringBuilder();

        try {
            if (!dpm.isActivePasswordSufficient()) {
                sb.append("Insufficient password quality\n");
            }
        } catch (SecurityException se) {
            Log.e(TAG, se.toString());
        }
        if (Build.VERSION_CODES.HONEYCOMB <= Build.VERSION.SDK_INT) {
            if (dpm.getStorageEncryption(null)) {
                sb.append("Storage encryption required\n");
            }
        }
        if (Build.VERSION_CODES.ICE_CREAM_SANDWICH <= Build.VERSION.SDK_INT) {
            if (dpm.getCameraDisabled(null)) {
                sb.append("Camera disabled\n");
            }
        }
        if (Build.VERSION_CODES.LOLLIPOP <= Build.VERSION.SDK_INT) {
            if (dpm.getAutoTimeRequired()) {
                sb.append("Auto time required\n");
            }
            if (dpm.getScreenCaptureDisabled(null)) {
                sb.append("Screen capture disabled\n");
            }
        }
        if (0 == sb.length()) {
            sb.append("No specific restrictions");
        }

        return sb.toString();
    }

    private boolean flagsSet(int value, int flag) {
        return (value & flag) == flag;
    }

    @SuppressLint("NewApi")
    private String formatKeyguardDisabledFeatures(DevicePolicyManager dpm) {
        StringBuilder sb = new StringBuilder();
        int kdf = dpm.getKeyguardDisabledFeatures(null);
        sb.append(String.format("0x%08X", kdf));
        if (0 == kdf) {
            sb.append(" (none)");
        }
        if (0x7FFFFFFF == kdf) {
            sb.append(" (all)");
        }

        if (flagsSet(kdf, DevicePolicyManager.KEYGUARD_DISABLE_FINGERPRINT)) {
            sb.append("\n\tFingerprint").append(String.format("%0x08X", DevicePolicyManager.KEYGUARD_DISABLE_FINGERPRINT));
            kdf &= ~DevicePolicyManager.KEYGUARD_DISABLE_FINGERPRINT;
        }
        if (flagsSet(kdf, DevicePolicyManager.KEYGUARD_DISABLE_SECURE_CAMERA)) {
            sb.append("\n\tCamera").append(String.format("%0x08X", DevicePolicyManager.KEYGUARD_DISABLE_SECURE_CAMERA));
            kdf &= ~DevicePolicyManager.KEYGUARD_DISABLE_SECURE_CAMERA;
        }
        if (flagsSet(kdf, DevicePolicyManager.KEYGUARD_DISABLE_SECURE_NOTIFICATIONS)) {
            sb.append("\n\tNotifications").append(String.format("%0x08X", DevicePolicyManager.KEYGUARD_DISABLE_SECURE_NOTIFICATIONS));
            kdf &= ~DevicePolicyManager.KEYGUARD_DISABLE_SECURE_NOTIFICATIONS;
        }
        if (flagsSet(kdf, DevicePolicyManager.KEYGUARD_DISABLE_TRUST_AGENTS)) {
            sb.append("\n\tTrust agent").append(String.format("%0x08X", DevicePolicyManager.KEYGUARD_DISABLE_TRUST_AGENTS));
            kdf &= ~DevicePolicyManager.KEYGUARD_DISABLE_TRUST_AGENTS;
        }
        if (flagsSet(kdf, DevicePolicyManager.KEYGUARD_DISABLE_UNREDACTED_NOTIFICATIONS)) {
            sb.append("\n\tUnredacted notifications").append(String.format("%0x08X", DevicePolicyManager.KEYGUARD_DISABLE_UNREDACTED_NOTIFICATIONS));
            kdf &= ~DevicePolicyManager.KEYGUARD_DISABLE_UNREDACTED_NOTIFICATIONS;
        }
        if (flagsSet(kdf, DevicePolicyManager.KEYGUARD_DISABLE_WIDGETS_ALL)) {
            sb.append("\n\tAll widgets").append(String.format("%0x08X", DevicePolicyManager.KEYGUARD_DISABLE_WIDGETS_ALL));
            kdf &= ~DevicePolicyManager.KEYGUARD_DISABLE_WIDGETS_ALL;
        }
        if (0 != kdf) {
            sb.append("\n\tUnrecognized flags: ").append(String.format("0x%08X", kdf));
        }
        return sb.toString();
    }

    private String formatPasswordQuality(DevicePolicyManager dpm) {
        StringBuilder sb = new StringBuilder();
        int pq = dpm.getPasswordQuality(null);
        switch (pq) {
            case DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED:
                sb.append("Unspecified"); break;
            case DevicePolicyManager.PASSWORD_QUALITY_SOMETHING:
                sb.append("Any password"); break;
            case DevicePolicyManager.PASSWORD_QUALITY_NUMERIC:
                sb.append("At least numeric"); break;
            case DevicePolicyManager.PASSWORD_QUALITY_NUMERIC_COMPLEX:
                sb.append("At least complex numeric"); break;
            case DevicePolicyManager.PASSWORD_QUALITY_COMPLEX:
                sb.append("Complex"); break;
            case DevicePolicyManager.PASSWORD_QUALITY_BIOMETRIC_WEAK:
                sb.append("Weak biometric"); break;
            case DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC:
                sb.append("Alphabetic"); break;
            case DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC:
                sb.append("Alphanumeric"); break;
            default:
                sb.append(getString(R.string.unknown));
        }
        sb.append(String.format(" (0x%08X)", pq));
        sb.append(" Length ").append(dpm.getPasswordMinimumLength(null));
        sb.append(" ~ ").append(dpm.getPasswordMaximumLength(pq));

        return sb.toString();
    }

    @SuppressLint("NewApi")
    private String formatPasswordQualityDetail(DevicePolicyManager dpm) {
        StringBuilder sb = new StringBuilder();
        int min  = dpm.getPasswordMinimumLength(null);
        sb.append("Minimum length ").append(min);

        int minT = dpm.getPasswordMinimumLetters(null);
        int minL = dpm.getPasswordMinimumLowerCase(null);
        int minU = dpm.getPasswordMinimumUpperCase(null);
        int minN = dpm.getPasswordMinimumNumeric(null);
        int minX = dpm.getPasswordMinimumNonLetter(null);
        int minS = dpm.getPasswordMinimumSymbols(null);
        if (0 < minT) {
            sb.append("\n\tminimum letters: ").append(minT);
        }
        if (0 < minL) {
            sb.append("\n\tminimum lowercases: ").append(minL);
        }
        if (0 < minU) {
            sb.append("\n\tminimum uppercases: ").append(minU);
        }
        if (0 < minN) {
            sb.append("\n\tminimum numerics: ").append(minN);
        }
        if (0 < minX) {
            sb.append("\n\tminimum non-letters: ").append(minX);
        }
        if (0 < minS) {
            sb.append("\n\tminimum symbols: ").append(minS);
        }
        return sb.toString();
    }

    @SuppressLint("NewApi")
    private String formatStorageEncryptionStatus(DevicePolicyManager dpm) {
        StringBuilder sb = new StringBuilder();
        int ses = dpm.getStorageEncryptionStatus();
        switch (ses) {
            case DevicePolicyManager.ENCRYPTION_STATUS_UNSUPPORTED:
                sb.append("Storage encryption not supported"); break;
            case DevicePolicyManager.ENCRYPTION_STATUS_INACTIVE:
                sb.append("Inactive"); break;
            case DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE_DEFAULT_KEY:
                sb.append("Encrypted with default key"); break;
            case DevicePolicyManager.ENCRYPTION_STATUS_ACTIVATING:
                sb.append("Encryption in progress"); break;
            case DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE:
                sb.append("Encrypted"); break;
            default: sb.append(getString(R.string.unknown));
        }
        sb.append(" (").append(ses).append(')');
        if (dpm.getStorageEncryption(null)) {
            sb.append("\nEncryption required");
        }
        return sb.toString();
    }

    @SuppressLint("NewApi")
    @Override
    protected InfoItem getItem(int infoId, Object... params) {
        String value = null;
        if (params[0] instanceof DevicePolicyManager) {
            DevicePolicyManager dpm = (DevicePolicyManager)params[0];
            switch (infoId) {
                case R.string.policy_admin: value = formatAdministrators(dpm); break;
                case R.string.policy_management_disabled_types: value = formatStringArray(dpm.getAccountTypesWithManagementDisabled()); break;
                case R.string.policy_restrictions: value = formatRestrictions(dpm); break;
                case R.string.policy_keyguard_disabled_features: value = formatKeyguardDisabledFeatures(dpm); break;
                case R.string.policy_failed_password_attempt: {
                    try {
                        value = String.valueOf(dpm.getCurrentFailedPasswordAttempts());
                    } catch (SecurityException se) {
                        value = getString(R.string.policy_not_watching_login);
                        Log.e(TAG, se.toString());
                    }
                    break;
                }
                case R.string.policy_max_failed_password_for_wipe: value = String.valueOf(dpm.getMaximumFailedPasswordsForWipe(null)); break;
                case R.string.policy_max_time_to_lock: value = String.valueOf(dpm.getMaximumTimeToLock(null)); break;
                case R.string.policy_password_expiration: value = String.valueOf(dpm.getPasswordExpiration(null)); break;
                case R.string.policy_password_expiration_timeout: value = String.valueOf(dpm.getPasswordExpirationTimeout(null)); break;
                case R.string.policy_password_history_len: value = String.valueOf(dpm.getPasswordHistoryLength(null)); break;
                case R.string.policy_password_quality: value = formatPasswordQuality(dpm); break;
                case R.string.policy_password_quality_detail: value = formatPasswordQualityDetail(dpm); break;
                case R.string.policy_storage_encryption_status: value = formatStorageEncryptionStatus(dpm); break;
                case R.string.policy_system_update_policy:
                default:
                    value = getString(R.string.invalid_item);
            }
        }
        if (null == value) {
            return null;
        }
        return new InfoItem(getString(infoId), value);
    }

    private static final InfoSpec[] sPolicySpecs = {
            new InfoSpec(R.string.policy_admin, 8),
            new InfoSpec(R.string.policy_management_disabled_types, 21),
            new InfoSpec(R.string.policy_restrictions, 8),
            new InfoSpec(R.string.policy_keyguard_disabled_features, 17),
            new InfoSpec(R.string.policy_failed_password_attempt, 8),
            new InfoSpec(R.string.policy_max_failed_password_for_wipe, 8),
            new InfoSpec(R.string.policy_max_time_to_lock, 8),
            new InfoSpec(R.string.policy_password_expiration, 11),
            new InfoSpec(R.string.policy_password_expiration_timeout, 11),
            new InfoSpec(R.string.policy_password_history_len, 11),
            new InfoSpec(R.string.policy_password_quality, 8),
            new InfoSpec(R.string.policy_password_quality_detail, 11),
            new InfoSpec(R.string.policy_storage_encryption_status, 11),
            new InfoSpec(R.string.policy_system_update_policy, 23),
    };

    @Override
    ArrayList<InfoItem> getItems() {
        if (null == sPolicyItems) {
            sPolicyItems = new ArrayList<>();

            DevicePolicyManager dpm = (DevicePolicyManager)mContext.getSystemService(Context.DEVICE_POLICY_SERVICE);
            mParams = new Object[] { dpm };
            addItems(sPolicyItems, sPolicySpecs);
        }
        return sPolicyItems;
    }

    @Override
    protected Object[] getInfoParams() {
        return mParams;
    }
}
