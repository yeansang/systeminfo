
package com.nemustech.study.sysinfo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;


public class SystemInfoMain extends Activity {
    @SuppressWarnings("unused")
    private static final String TAG = SystemInfoMain.class.getSimpleName();

    final static String VER = "1.3";

    String PATH;
    File dir = null;

    LinearLayout mItemList;
    ArrayAdapter<InfoItem> mAdapter;
    ListView mContentList;

    final static int REQUEST_PHONE = 1;
    final static int REQUEST_LOCATION = 2;
    final static int REQUEST_CAMERA = 3;
    final static int REQUEST_ALL = 4;

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

    private ArrayList<InfoItem> networkItems;

    private boolean outCome = false;

    int permissionCheck=0;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        MenuItem refresh = menu.getItem(0);
        MenuItem share = menu.getItem(1);
        refresh.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    String[] requstPermission = new String[]{"","",""};
                    permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE);
                    if(permissionCheck == PackageManager.PERMISSION_DENIED){
                        requstPermission[0]=Manifest.permission.READ_PHONE_STATE;
                    }
                    permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
                    if(permissionCheck == PackageManager.PERMISSION_DENIED){
                        requstPermission[1]=Manifest.permission.ACCESS_FINE_LOCATION;
                    }
                    permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA);
                    if(permissionCheck == PackageManager.PERMISSION_DENIED){
                        requstPermission[2]=Manifest.permission.CAMERA;
                    }
                    requestPermissions(requstPermission,REQUEST_ALL);
                }else {
                    itemToXML();
                }
                Log.d("onCreateMenu","refresh");
                return true;
            }
        });
        share.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                Log.d("onCreateMenu","share");
                File fs = new File(PATH + "/device.xml");
                Uri fUri = Uri.fromFile(fs);

                Intent mail = new Intent(Intent.ACTION_SEND);

                Log.d("permission",checkUriPermission(fUri,null,null, Binder.getCallingPid(),Binder.getCallingUid(),Intent.FLAG_GRANT_READ_URI_PERMISSION)+"");
                mail.setType("plain/text");
                mail.putExtra(Intent.EXTRA_EMAIL, new String[]{""});

                mail.putExtra(Intent.EXTRA_STREAM, fUri);
                mail.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Log.d("share",fUri.toString());
                startActivity(Intent.createChooser(mail, "mail"));
                return true;
            }
        });
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PATH = getExternalFilesDir(null).getPath();
        dir =  new File(getExternalFilesDir(null),"device");
        if(!dir.exists()){
            dir.mkdir();
        }

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
        mNetworkProvider.getItemsAsync(mReceiver);
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

        if(!outCome) {
            File fs = new File(PATH + "/device.xml");
            if (!fs.exists()) {
                itemToXML();
            }
            FileInputStream xmlInput;
            try {
                xmlInput = new FileInputStream(fs);
            }catch (FileNotFoundException e){
                e.printStackTrace();
            }
        }

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
                    //updateContent(mReceivedItems);
                    networkItems = mReceivedItems;
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
                items = networkItems;
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
        }else if(requestCode == REQUEST_ALL){
            itemToXML();
        }
        else{
            Log.d("request code err",requestCode+"");
            items = new ArrayList<InfoItem>();
        }
        //updateContent(items);
    }

    public void itemToXML(){
        XmlSerializer serializer = Xml.newSerializer();

        File fs = new File(PATH+"/device.xml");
        FileOutputStream fos=null;

        StringWriter writer = new StringWriter();

        Toast toast = Toast.makeText(getApplicationContext(),"refreshing...",Toast.LENGTH_SHORT);
        toast.show();

        try {
             fos = new FileOutputStream(fs);
        }catch (FileNotFoundException e){
            e.printStackTrace();
            toast = Toast.makeText(getApplicationContext(),"refresh err:File not found",Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        ItemReceiver rec = new ItemReceiver();
        try {
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8",true);
            serializer.startTag("","deviceinfo");
            serializer.startTag("","info");
            serializer.attribute("","ver",VER);
            serializer.endTag("","info");

            xmlWriter("Android", new AndroidInfoProvider(this).getItems(), serializer);
            xmlWriter("Screen", new ScreenInfoProvider(this).getItems(), serializer);
            xmlWriter("System", new SystemInfoProvider(this).getItems(), serializer);
            xmlWriter("Memory", new MemoryInfoProvider(this).getItems(), serializer);
            xmlWriter("Storage", new StorageInfoProvider(this).getItems(), serializer);

            permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                    xmlWriter("Telephone", new TelephoneInfoProvider(this).getItems(), serializer);
                } else {
                    xmlWriter("Telephone", new ArrayList<InfoItem>(), serializer);
                }
            }else{
                xmlWriter("Telephone", new TelephoneInfoProvider(this).getItems(), serializer);
            }

            xmlWriter("Sensors", new SensorInfoProvider(this).getItems(), serializer);
            xmlWriter("InputDevices", new InputInfoProvider(this).getItems(), serializer);
            xmlWriter("Connectivity", new ConnectivityInfoProvider(this).getItems(), serializer);
            xmlWriter("Networks", networkItems, serializer);

            permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                    xmlWriter("LocationProvider", new LocationInfoProvider(this).getItems(), serializer);
                } else {
                    xmlWriter("LocationProvider", new ArrayList<InfoItem>(), serializer);
                }
            }else{
                xmlWriter("LocationProvider", new LocationInfoProvider(this).getItems(), serializer);
            }

            permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                    xmlWriter("Camera", new CameraInfoProvider(this).getItems(), serializer);
                } else {
                    xmlWriter("Camera", new ArrayList<InfoItem>(), serializer);
                }
            }else{
                xmlWriter("Camera", new CameraInfoProvider(this).getItems(), serializer);
            }

            xmlWriter("OpenGL", mOpenGlProvider.getItems(), serializer);
            xmlWriter("DRM engines", new DrmInfoProvider(this).getItems(), serializer);
            xmlWriter("Accounts", new AccountInfoProvider(this).getItems(), serializer);
            xmlWriter("DevicePolicy", new PolicyInfoProvider(this).getItems(), serializer);
            xmlWriter("Multimedia codec", new CodecInfoProvider(this).getItems(), serializer);
            xmlWriter("Security providers", new SecurityInfoProvider(this).getItems(), serializer);
            xmlWriter("SupportedLocales", new LocationInfoProvider(this).getItems(), serializer);
            xmlWriter("AboutApp", new AboutInfoProvider(this).getItems(), serializer);

            serializer.endDocument();
            serializer.flush();
            fos.write(writer.toString().getBytes());
            fos.close();
            toast = Toast.makeText(getApplicationContext(),"refreshed!",Toast.LENGTH_SHORT);
            toast.show();

        }catch (Exception e){
            toast = Toast.makeText(getApplicationContext(),"refresh err:XML err",Toast.LENGTH_SHORT);
            toast.show();
            e.printStackTrace();
        }

    }

    public void xmlWriter(String tagName, ArrayList<InfoItem> items, XmlSerializer serializer) throws IOException {
        serializer.startTag("",tagName.replaceAll(" ","_"));
        for(InfoItem i : items){
            serializer.startTag("","item");

            serializer.startTag("","name");
            serializer.text(i.name);
            serializer.endTag("","name");

            serializer.startTag("","value");
            serializer.text(i.value);
            serializer.endTag("","value");

            serializer.endTag("","item");
        }
        if(items.isEmpty()){
            serializer.startTag("","item");

            serializer.startTag("","name");
            serializer.text("permission err");
            serializer.endTag("","name");

            serializer.startTag("","value");
            serializer.text("permission err");
            serializer.endTag("","value");

            serializer.endTag("","item");
        }
        serializer.endTag("",tagName.replaceAll(" ","_"));
    }
}
