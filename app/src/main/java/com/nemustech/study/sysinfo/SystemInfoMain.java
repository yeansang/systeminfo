
package com.nemustech.study.sysinfo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;


public class SystemInfoMain extends Activity {
    @SuppressWarnings("unused")
    private static final String TAG = SystemInfoMain.class.getSimpleName();

    LinearLayout mItemList;
    ArrayAdapter<InfoItem> mAdapter;
    ListView mContentList;

    private OpenGlInfoProvider.GLHelper mGLHelper;

    private AndroidInfoProvider mAndroidProvider;
    private SystemInfoProvider mSystemProvider;
    private ScreenInfoProvider mScreenProvider;
    private MemoryInfoProvider mMemoryProvider;
    private StorageInfoProvider mStorageProvider;
    private TelephoneInfoProvider mTelephoneProvider;
    private SensorInfoProvider mSensorProvider;
    private InputInfoProvider mInputProvider;
    private ConnectivityInfoProvider mConnectivityProvider;
    private NetworkInfoProvider mNetworkProvider;
    private LocationInfoProvider mLocationProvider;
    private CameraInfoProvider mCameraProvider;
    private OpenGlInfoProvider mOpenGlProvider;
    private DrmInfoProvider mDrmProvider;
    private AccountInfoProvider mAccountProvider;
    private CodecInfoProvider mCodecProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.l_main);
        mItemList = (LinearLayout)findViewById(R.id.l_items);
        mAdapter = new InfoListView.InfoItemAdapter(this, 0, new ArrayList<InfoItem>());
        mContentList = (ListView)findViewById(R.id.l_content);
        mContentList.setAdapter(mAdapter);

        mAndroidProvider = new AndroidInfoProvider(this);
        mSystemProvider = new SystemInfoProvider(this);
        mScreenProvider = new ScreenInfoProvider(this);
        mMemoryProvider = new MemoryInfoProvider(this);
        mStorageProvider = new StorageInfoProvider(this);
        mTelephoneProvider = new TelephoneInfoProvider(this);
        mSensorProvider = new SensorInfoProvider(this);
        mInputProvider = new InputInfoProvider(this);
        mConnectivityProvider = new ConnectivityInfoProvider(this);
        mNetworkProvider = new NetworkInfoProvider(this);
        mLocationProvider = new LocationInfoProvider(this);
        mCameraProvider = new CameraInfoProvider(this);
        mOpenGlProvider = new OpenGlInfoProvider(this);
        mDrmProvider = new DrmInfoProvider(this);
        mAccountProvider = new AccountInfoProvider(this);
        mCodecProvider = new CodecInfoProvider(this);

        setItemSelected(R.id.item_android);

        mGLHelper = mOpenGlProvider.getGlHelper();
        mGLHelper.onCreate();
    }

    @Override
    protected void onDestroy() {
        mGLHelper.onDestroy();
        super.onDestroy();
    }

    ArrayList<InfoItem> mLocaleContent;
    ArrayList<InfoItem> mSecurityContent;

    private String formatLocaleName(Locale locale) {
        return locale.getDisplayName() + ": " + locale.getDisplayName(locale);
    }
    private String formatLocaleCodes(Locale locale) {
        String lang = locale.getLanguage();
        String cc = locale.getCountry();
        String var = locale.getVariant();
        String lang3 = null;
        String cc3 = null;
        try {
            lang3 = locale.getISO3Language();
            cc3 = locale.getISO3Country();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        StringBuffer sb = new StringBuffer();
        sb.append(lang);
        if (null != cc && 0 < cc.length()) {
            sb.append('_').append(cc);
        }
        if (null != var && 0 < var.length()) {
            sb.append(", ").append(var);
        }
        sb.append(" (");
        if (null == lang3 || 0 == lang3.length()) {
            sb.append(getString(R.string.no_iso3));
        } else {
            sb.append(lang3);
            if (null != cc3 && 0 < cc3.length()) {
                sb.append('_').append(cc3);
            }
        }
        sb.append(')');
        return sb.toString();
    }
    private InfoItem getLocaleItem(Locale locale) {
        return new InfoItem(formatLocaleName(locale), formatLocaleCodes(locale));
    }
    private void addSpecificLocale(ArrayList<InfoItem> list, String name, Locale locale) {
        list.add(new InfoItem(name, formatLocaleCodes(locale) + " - " + formatLocaleName(locale)));
    }
    private ArrayList<InfoItem> getLocaleContent() {
        if (null == mLocaleContent) {
            mLocaleContent = new ArrayList<InfoItem>();
            addSpecificLocale(mLocaleContent, getString(R.string.locale_current), getResources().getConfiguration().locale);
            addSpecificLocale(mLocaleContent, getString(R.string.locale_default), Locale.getDefault());
            Locale[] locales = Locale.getAvailableLocales();
            for (Locale locale: locales) {
                mLocaleContent.add(getLocaleItem(locale));
            }
        }
        return mLocaleContent;
    }

    private void addSecurityContent(ArrayList<InfoItem> list, Provider provider) {
        String name = provider.getName() + ": " + provider.getInfo();
        Set<Provider.Service> services = provider.getServices();
        StringBuffer sb = new StringBuffer();
        if (null == services || 0 == services.size()) {
            sb.append(getString(R.string.security_no_service));
        } else {
            Package pPkg = null;
            for (Provider.Service ps: services) {
                Class<?> cls = null;
                Package pkg = null;
                try {
                    cls = Class.forName(ps.getClassName());
                    pkg = cls.getPackage();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                if (null != pkg && !pkg.equals(pPkg)) {
                    sb.append("Package ").append(pkg.getName()).append('\n');
                    pPkg = pkg;
                }
                sb.append(ps.getAlgorithm());
                sb.append(" (").append(ps.getType()).append(")\n");
//                String className;
//                if (null != pkg) {
//                    className = ps.getClassName().substring(pkg.getName().length() + 1);
//                } else {
//                    String fullName = ps.getClassName();
//                    className = fullName.substring(fullName.lastIndexOf('.') + 1);
//                }
//                sb.append(" (").append(className).append(")\n");
            }
            sb.deleteCharAt(sb.length() - 1);
        }
        list.add(new InfoItem(name, sb.toString()));
    }
    private ArrayList<InfoItem> getSecurityContent() {
        if (null == mSecurityContent) {
            mSecurityContent = new ArrayList<InfoItem>();
            Provider[] providers = Security.getProviders();
            if (null == providers || 0 == providers.length) {
                mSecurityContent.add(new InfoItem(getString(R.string.item_security), getString(R.string.security_none)));
            } else {
                for (Provider p: providers) {
                    addSecurityContent(mSecurityContent, p);
                }
            }
        }
        return mSecurityContent;
    }
    private void updateContent(ArrayList<InfoItem> items) {
        mAdapter.clear();
        for (InfoItem item: items) {
            mAdapter.add(item);
        }
        mAdapter.notifyDataSetChanged();
        mContentList.setSelection(0);
    }

    private class ItemReceiver implements InfoProvider.AsyncInfoReceiver {
        private ArrayList<InfoItem> mReceivedItems;
        private Runnable mUpdateCmd = new Runnable() {
            @Override
            public void run() {
                if (null != mReceivedItems) {
                    updateContent(mReceivedItems);
                }
            }
        };
        @Override
        public void onItemReceived(ArrayList<InfoItem> items) {
            mReceivedItems = items;
            runOnUiThread(mUpdateCmd);
        }
    }
    private ItemReceiver mReceiver = new ItemReceiver();

    private void onItemSelected(View view) {
        ArrayList<InfoItem> items = null;
        switch (view.getId()) {
            case R.id.item_android:
                items = mAndroidProvider.getItems();
                break;
            case R.id.item_system:
                items = mSystemProvider.getItems();
                break;
            case R.id.item_screen:
                items = mScreenProvider.getItems();
                break;
            case R.id.item_memory:
                items = mMemoryProvider.getItems();
                break;
            case R.id.item_storage:
                items = mStorageProvider.getItems();
                break;
            case R.id.item_phone:
                items = mTelephoneProvider.getItems();
                break;
            case R.id.item_sensor:
                items = mSensorProvider.getItems();
                break;
            case R.id.item_input:
                items = mInputProvider.getItems();
                break;
            case R.id.item_connectivity:
                items = mConnectivityProvider.getItems();
                break;
            case R.id.item_network:
                mNetworkProvider.getItemsAsync(mReceiver);
                break;
            case R.id.item_location:
                items = mLocationProvider.getItems();
                break;
            case R.id.item_camera:
                mCameraProvider.getItemsAsync(mReceiver);
                break;
            case R.id.item_opengl:
                items = mOpenGlProvider.getItems();
                break;
            case R.id.item_drm:
                items = mDrmProvider.getItems();
                break;
            case R.id.item_account:
                items = mAccountProvider.getItems();
                break;
            case R.id.item_codec:
                items = mCodecProvider.getItems();
                break;
            case R.id.item_security:
                items = getSecurityContent();
                break;
            case R.id.item_locale:
                items = getLocaleContent();
                break;
        }
        if (null != items) {
            updateContent(items);
        }
    }

    private void setItemSelected(int selectedId) {
        final int count = mItemList.getChildCount();
        for (int idx = 0; idx < count; ++idx) {
            View child = mItemList.getChildAt(idx);
            if (child.getId() == selectedId) {
                if (!child.isSelected()) {
                    onItemSelected(child);
                }
                child.setSelected(true);
            } else {
                child.setSelected(false);
            }
        }
    }

    public void onItemClick(View view) {
        Log.i(TAG, "CDSS onItemClick()");
        setItemSelected(view.getId());
    }
}
