package com.nemustech.study.sysinfo;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by cheolgyoon on 2016. 5. 16..
 *
 */
public class StorageInfoProvider extends InfoProvider {
    @SuppressWarnings("unused")
    private static final String TAG = StorageInfoProvider.class.getSimpleName();
    private static ArrayList<InfoItem> sStorageContent;

    StorageInfoProvider(Context context) {
        super(context);
    }

    @Override
    protected InfoItem getItem(int infoId) {
        return null;
    }

    private InfoItem getFileItem(int titleId, File file, String name) {
        String value;
        try {
            switch (titleId) {
                case R.string.storage_absolute_path: value = file.getAbsolutePath(); break;
                case R.string.storage_total_space: value = formatStorageSize(file.getTotalSpace()); break;
                case R.string.storage_free_space: value = formatStorageSize(file.getFreeSpace()); break;
                case R.string.storage_usable_space: value = formatStorageSize(file.getUsableSpace()); break;
                case R.string.storage_state: {
                    if (Build.VERSION_CODES.LOLLIPOP <= Build.VERSION.SDK_INT) {
                        value = Environment.getExternalStorageState(file);
                    } else {
                        value = Environment.getStorageState(file);
                    }
                } break;
                default: value = getString(R.string.invalid_item);
            }
        } catch (Error e) {
            Log.e(TAG, e.toString());
            value = getString(R.string.unsupported);
        }
        return new InfoItem(String.format("%s: %s", name, getString(titleId)), value);
    }

    private static final InfoSpec[] sStorageSpecs = {
            new InfoSpec(R.string.storage_absolute_path, 1),
            new InfoSpec(R.string.storage_total_space, 9),
            new InfoSpec(R.string.storage_free_space, 9),
            new InfoSpec(R.string.storage_usable_space, 9),
            new InfoSpec(R.string.storage_state, 19),
    };

    private void addFileItems(ArrayList<InfoItem> items, File file, String name) {
        for (int idx = 0; idx < sStorageSpecs.length; ++idx) {
            InfoSpec spec = sStorageSpecs[idx];
            if (spec.minSdk <= Build.VERSION.SDK_INT) {
                InfoItem item = getFileItem(sStorageSpecs[idx].titleId, file, name);
                if (null != item) {
                    items.add(item);
                }
            }
        }
    }

    private String formatStorageEnvironment() {
        boolean em;
        try {
            em = Environment.isExternalStorageEmulated();
        } catch (Error e) {
            Log.i(TAG, e.toString());
            em = false;
        }
        boolean rm = Environment.isExternalStorageRemovable();
        StringBuffer sb = new StringBuffer();
        sb.append(Environment.getExternalStorageState()).append(", ");
        if (rm) {
            sb.append(getString(R.string.storage_removable));
        } else {
            sb.append(getString(R.string.storage_non_removable));
        }
        if (em) {
            sb.append(getString(R.string.storage_emulated));
        }
        return sb.toString();
    }
    private boolean isExternalStorageAccessible() {
        if (!Environment.isExternalStorageRemovable()) {
            return true;
        }
        final String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    @Override
    ArrayList<InfoItem> getItems() {
        if (null == sStorageContent) {
            sStorageContent = new ArrayList<>();
            addFileItems(sStorageContent, Environment.getRootDirectory(), "System");
            addFileItems(sStorageContent, Environment.getDataDirectory(), "Data");
            addFileItems(sStorageContent, Environment.getDownloadCacheDirectory(), "Cache");
            sStorageContent.add(new InfoItem(getString(R.string.storage_external_description), formatStorageEnvironment()));
            if (isExternalStorageAccessible()) {
                addFileItems(sStorageContent, Environment.getExternalStorageDirectory(), "External");
            }
        }
        return sStorageContent;
    }
}
