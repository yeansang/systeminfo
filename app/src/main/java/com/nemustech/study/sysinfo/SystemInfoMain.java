
package com.nemustech.study.sysinfo;

import android.Manifest;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;


public class SystemInfoMain extends Activity {
    @SuppressWarnings("unused")
    private static final String TAG = SystemInfoMain.class.getSimpleName();

    LinearLayout mItemList;
    ArrayAdapter<InfoItem> mAdapter;
    ListView mContentList;

    final static int REQUEST_PHONE = 1;
    final static int REQUEST_LOCATION = 2;
    final static int REQUEST_CAMERA = 3;

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
    private PolicyInfoProvider mPolicyProvider;
    private CodecInfoProvider mCodecProvider;
    private SecurityInfoProvider mSecurityProvider;
    private LocaleInfoProvider mLocaleProvider;
    private AboutInfoProvider mAboutProvider;

    int permissionCheck=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.l_main);
        mItemList = (LinearLayout)findViewById(R.id.l_items);
        mAdapter = new InfoListView.InfoItemAdapter(this, 0, new ArrayList<InfoItem>());
        mContentList = (ListView)findViewById(R.id.l_content);
        mContentList.setAdapter(mAdapter);
        TextView emptyView = (TextView)findViewById(R.id.no_data);
        mContentList.setEmptyView(emptyView);

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
        mPolicyProvider = new PolicyInfoProvider(this);
        mCodecProvider = new CodecInfoProvider(this);
        mSecurityProvider = new SecurityInfoProvider(this);
        mLocaleProvider = new LocaleInfoProvider(this);
        mAboutProvider = new AboutInfoProvider(this);

        setItemSelected(R.id.item_android);

        mGLHelper = mOpenGlProvider.getGlHelper();
        mGLHelper.onCreate();

        /*if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE, Manifest.permission.CAMERA}, 10);
        }*/
    }

    @Override
    protected void onDestroy() {
        mGLHelper.onDestroy();
        super.onDestroy();
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
                permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                        items = mTelephoneProvider.getItems();
                    } else {
                        requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_PHONE);
                        //items = new ArrayList<InfoItem>();
                    }
                }else{
                    items = mTelephoneProvider.getItems();
                }
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
                permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                        items = mLocationProvider.getItems();
                    } else {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
                        //items = new ArrayList<InfoItem>();
                    }
                }else{
                    items = mLocationProvider.getItems();
                }
                break;
            case R.id.item_camera:
                permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                        items = mCameraProvider.getItems();
                    } else {
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
                        //items = new ArrayList<InfoItem>();
                    }
                }else{
                    items = mCameraProvider.getItems();
                }
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
            case R.id.item_policy:
                items = mPolicyProvider.getItems();
                break;
            case R.id.item_codec:
                items = mCodecProvider.getItems();
                break;
            case R.id.item_security:
                items = mSecurityProvider.getItems();
                break;
            case R.id.item_locale:
                items = mLocaleProvider.getItems();
                break;
            case R.id.item_about:
                items = mAboutProvider.getItems();
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

    @Override
    public void onRequestPermissionsResult (int requestCode, String[] permissions, int[] grantResults) {
        ArrayList<InfoItem> items = null;
        if (requestCode == REQUEST_PHONE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                items = mTelephoneProvider.getItems();
            } else {
                items = new ArrayList<InfoItem>();
            }
        }else if(requestCode == REQUEST_LOCATION){
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                items = mLocationProvider.getItems();
            } else {
                items = new ArrayList<InfoItem>();
            }
        }else if(requestCode == REQUEST_CAMERA){
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                items = mCameraProvider.getItems();
            } else {
                items = new ArrayList<InfoItem>();
            }
        }else{
            Log.d("request code err",requestCode+"");
            items = new ArrayList<InfoItem>();
        }
        updateContent(items);
    }

    public void itemToXML(ArrayList<InfoItem> items){

    }
}
