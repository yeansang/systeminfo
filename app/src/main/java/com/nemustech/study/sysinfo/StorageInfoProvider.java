package com.nemustech.study.sysinfo;

import android.annotation.SuppressLint;
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

    private Object[] mParams;

    StorageInfoProvider(Context context) {
        super(context);
    }

    private String formatStorageEnvironment() {
        boolean em;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            em = Environment.isExternalStorageEmulated();
        } else {
            em = false;
        }

        boolean rm = Environment.isExternalStorageRemovable();
        StringBuilder sb = new StringBuilder();
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

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    @Override
    protected InfoItem getItem(int titleId, Object... params) {
        String value;
        String name = "*";
        try {
            File file = (File)params[0];
            name = (String)params[1];

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

    @Override
    protected Object[] getInfoParams() {
        return mParams;
    }

    private void setupParams(File dir, String name) {
        mParams = new Object[] { dir, name };
    }

    private static final InfoSpec[] sStorageSpecs = {
            new InfoSpec(R.string.storage_absolute_path, 1),
            new InfoSpec(R.string.storage_total_space, 9),
            new InfoSpec(R.string.storage_free_space, 9),
            new InfoSpec(R.string.storage_usable_space, 9),
            new InfoSpec(R.string.storage_state, 19),
    };

    @Override
    ArrayList<InfoItem> getItems() {
        if (null == sStorageContent) {
            sStorageContent = new ArrayList<>();
            setupParams(Environment.getRootDirectory(), "System");
            addItems(sStorageContent, sStorageSpecs);

            setupParams(Environment.getDataDirectory(), "Data");
            addItems(sStorageContent, sStorageSpecs);

            setupParams(Environment.getDownloadCacheDirectory(), "Cache");
            addItems(sStorageContent, sStorageSpecs);

            sStorageContent.add(new InfoItem(getString(R.string.storage_external_description), formatStorageEnvironment()));
            if (isExternalStorageAccessible()) {
                setupParams(Environment.getExternalStorageDirectory(), "External");
                addItems(sStorageContent, sStorageSpecs);
            }
        }
        return sStorageContent;
    }
}
