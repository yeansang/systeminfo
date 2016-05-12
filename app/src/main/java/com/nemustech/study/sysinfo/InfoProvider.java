package com.nemustech.study.sysinfo;

import android.content.Context;
import android.os.Build;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by cheolgyoon on 2016. 5. 11..
 */
public abstract class InfoProvider {
    @SuppressWarnings("unused")
    private static final String TAG = InfoProvider.class.getSimpleName();

    private static SimpleDateFormat mSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm.ss");
    String formatTime(long time) {
        return mSdf.format(new Date(time));
    }
    Context mContext;

    abstract ArrayList<InfoItem> getItems();
    abstract protected InfoItem getItem(int infoId);

    protected final InfoItem getItem(InfoSpec spec) {
        if (Build.VERSION.SDK_INT < spec.minSdk) {
            return null;
            //new InfoItem(getString(spec.titleId), mContext.getString(R.string.sdk_version_required, spec.minSdk));
        }
        InfoItem item = getItem(spec.titleId);
        if (null == item) {
            item = new InfoItem(getString(spec.titleId), getString(R.string.unsupported));
        }
        return item;
    }

    InfoProvider(Context context) {
        mContext = context.getApplicationContext();
    }

//    String getString(int id, Object... args) {
//        return mContext.getString(id, args);
//    }
    String getString(int id) {
        return mContext.getString(id);
    }

    String formatStringArray(String... array) {
        StringBuffer sb = new StringBuffer();
        if (null != array && 0 < array.length) {
            sb.append(array[0]);
            for (int idx = 1; idx < array.length; ++idx) {
                sb.append('\n').append(array[idx]);
            }
        } else {
            sb.append(getString(R.string.none));
        }
        return sb.toString();
    }

    String formatFloatArray(float[] array) {
        StringBuffer sb = new StringBuffer();
        if (null != array && 0 < array.length) {
            sb.append(String.valueOf(array[0]));
            for (int idx = 1; idx < array.length; ++idx) {
                sb.append('\n').append(String.valueOf(array[idx]));
            }
        } else {
            sb.append(getString(R.string.none));
        }
        return sb.toString();
    }

    static protected class InfoSpec {
        int titleId;
        int minSdk;
        InfoSpec(int titleId, int minSdk) {
            this.titleId = titleId;
            this.minSdk = minSdk;
        }
    }
}
