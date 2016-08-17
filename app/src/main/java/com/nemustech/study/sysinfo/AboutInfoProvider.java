package com.nemustech.study.sysinfo;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by cheolgyoon on 2016. 6. 8..
 *
 */
public class AboutInfoProvider extends InfoProvider {
    @SuppressWarnings("unused")
    private static final String TAG = AboutInfoProvider.class.getSimpleName();
    private static ArrayList<InfoItem> sAboutItems;

    AboutInfoProvider(Context context) {
        super(context);
    }

    @Override
    protected Object[] getInfoParams() {
        return new Object[0];
    }

    @Override
    protected InfoItem getItem(int infoId, Object... params) {
        String value;
        try {
            switch (infoId) {
                case R.string.about_app_name: {
                    value = mContext.getPackageManager().getApplicationLabel(mContext.getApplicationInfo()).toString();
                    break;
                }
                case R.string.about_packagename: {
                    value = mContext.getPackageName();
                    break;
                }
                case R.string.about_version: {
                    PackageInfo pi = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
                    value = String.format("%s (%d)", pi.versionName, pi.versionCode);
                    break;
                }
                case R.string.about_supported_latest_android: {
                    value = AndroidInfoProvider.formatSdk(mContext.getApplicationInfo().targetSdkVersion);
                    break;
                }
                case R.string.about_developer: value = "cheolgyoon.yoo@nemustech.com, ysan1991@gmail.com"; break;
                case R.string.about_thanks: value = "Nemustech (http://www.nemustech.com)"; break;
                case R.string.about_source: value = "https://github.com/yeansang/systeminfo"; break;
                case R.string.about_license: value = getString(R.string.about_license_gpl2); break;
                default:
                    value = getString(R.string.invalid_item);
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            value = getString(R.string.unsupported);
        }
        return new InfoItem(getString(infoId), value);
    }

    private static final InfoSpec[] sAboutSpecs = {
            new InfoSpec(R.string.about_app_name, 1),
            new InfoSpec(R.string.about_packagename, 1),
            new InfoSpec(R.string.about_version, 1),
            new InfoSpec(R.string.about_supported_latest_android, 1),
            new InfoSpec(R.string.about_developer, 1),
            new InfoSpec(R.string.about_thanks, 1),
            new InfoSpec(R.string.about_source, 1),
            new InfoSpec(R.string.about_license, 1),
    };

    @Override
    ArrayList<InfoItem> getItems() {
        if (null == sAboutItems) {
            sAboutItems = new ArrayList<>();
            addItems(sAboutItems, sAboutSpecs);
        }
        return sAboutItems;
    }
}
