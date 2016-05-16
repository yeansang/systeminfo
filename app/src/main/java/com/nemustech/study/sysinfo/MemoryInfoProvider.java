package com.nemustech.study.sysinfo;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by cheolgyoon on 2016. 5. 16..
 *
 */
public class MemoryInfoProvider extends InfoProvider {
    @SuppressWarnings("unused")
    private static final String TAG = MemoryInfoProvider.class.getSimpleName();

    MemoryInfoProvider(Context context) {
        super(context);
    }

    private static ArrayList<InfoItem> sMemoryContent;

    private InfoItem getMemoryItem(int titleId, ActivityManager.MemoryInfo mi, Runtime rt) {
        String title = "";
        String value;

        try {
            title = getString(titleId);
            switch (titleId) {
                case R.string.memory_available: value = formatStorageSize(mi.availMem); break;
                case R.string.memory_total: value = formatStorageSize(mi.totalMem); break;
                case R.string.memory_threshold: value = formatStorageSize(mi.threshold); break;
                case R.string.memory_runtime_free: value = formatStorageSize(rt.freeMemory()); break;
                case R.string.memory_runtime_max: value = formatStorageSize(rt.maxMemory()); break;
                case R.string.memory_runtime_total: value = formatStorageSize(rt.totalMemory()); break;
                default: value = getString(R.string.invalid_item);
            }
        } catch (Error e) {
            Log.e(TAG, e.toString());
            value = getString(R.string.unsupported);
        }
        return new InfoItem(title, value);
    }

    private static final InfoSpec[] sMemorySpecs = {
            new InfoSpec(R.string.memory_total, 16),
            new InfoSpec(R.string.memory_available, 1),
            new InfoSpec(R.string.memory_threshold, 1),
            new InfoSpec(R.string.memory_runtime_free, 1),
            new InfoSpec(R.string.memory_runtime_max, 1),
            new InfoSpec(R.string.memory_runtime_total, 1),
    };

    @Override
    ArrayList<InfoItem> getItems() {
        if (null == sMemoryContent) {
            sMemoryContent = new ArrayList<>();
        }
        ActivityManager.MemoryInfo aMeminfo = new ActivityManager.MemoryInfo();
        ((ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryInfo(aMeminfo);
        Runtime runtime = Runtime.getRuntime();
        sMemoryContent.clear();
        for (int idx = 0; idx < sMemorySpecs.length; ++idx) {
            InfoSpec spec = sMemorySpecs[idx];
            if (spec.minSdk <= Build.VERSION.SDK_INT) {
                InfoItem item = getMemoryItem(sMemorySpecs[idx].titleId, aMeminfo, runtime);
                if (null != item) {
                    sMemoryContent.add(item);
                }
            }
        }
        return sMemoryContent;
    }

    @Override
    protected InfoItem getItem(int infoId) {
        //  This method is not used here
        return null;
    }
}
