
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

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
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

    private NetworkInfoProvider mNetworkProvider;
    private ArrayList<InfoItem> tempNetworkItems= new ArrayList<>();
    private OpenGlInfoProvider mOpenGlProvider;

    private ArrayList<InfoItem> androidItems = new ArrayList<>();
    private ArrayList<InfoItem> systemItems= new ArrayList<>();
    private ArrayList<InfoItem> screenItems= new ArrayList<>();
    private ArrayList<InfoItem> memoryItems= new ArrayList<>();
    private ArrayList<InfoItem> storageItems= new ArrayList<>();
    private ArrayList<InfoItem> telephoneItems= new ArrayList<>();
    private ArrayList<InfoItem> sensorItems= new ArrayList<>();
    private ArrayList<InfoItem> inputItems= new ArrayList<>();
    private ArrayList<InfoItem> connectivityItems= new ArrayList<>();
    private ArrayList<InfoItem> networkItems= new ArrayList<>();
    private ArrayList<InfoItem> locationItems= new ArrayList<>();
    private ArrayList<InfoItem> cameraItems= new ArrayList<>();
    private ArrayList<InfoItem> openglItems= new ArrayList<>();
    private ArrayList<InfoItem> drmItems= new ArrayList<>();
    private ArrayList<InfoItem> accountItems= new ArrayList<>();
    private ArrayList<InfoItem> policyItems= new ArrayList<>();
    private ArrayList<InfoItem> codecItems= new ArrayList<>();
    private ArrayList<InfoItem> securityItems= new ArrayList<>();
    private ArrayList<InfoItem> localeItems= new ArrayList<>();
    private ArrayList<InfoItem> aboutItems= new ArrayList<>();

    private XmlPullParser xpp;

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

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        File fa = null;



        if(intent != null){
            Uri urivalue = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
            Log.d("text",intent.getData().toString());

           //String xml = intent.getStringExtra(Intent.EXTRA_TEXT);
            if(urivalue!=null) Log.d("text",urivalue.toString());
            /*if (xml != null) {
                String sendFile = intent.getData().getPath();
                fa = new File(sendFile);
                Log.d("onCreate", sendFile);
                outCome = true;
            }*/
        }

        setContentView(R.layout.l_main);
        mItemList = (LinearLayout)findViewById(R.id.l_items);
        mAdapter = new InfoListView.InfoItemAdapter(this, 0, new ArrayList<InfoItem>());
        mContentList = (ListView)findViewById(R.id.l_content);
        mContentList.setAdapter(mAdapter);
        TextView emptyView = (TextView)findViewById(R.id.no_data);
        emptyView.setText("Select list");
        mContentList.setEmptyView(emptyView);

        mNetworkProvider = new NetworkInfoProvider(this);

        mOpenGlProvider = new OpenGlInfoProvider(this);

        setItemSelected(R.id.item_android);

        mGLHelper = mOpenGlProvider.getGlHelper();
        mGLHelper.onCreate();

        if(!outCome) {
            File fs = new File(PATH + "/device.xml");
            if (!fs.exists()) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE}, REQUEST_ALL);
                }else{
                    itemToXML();
                }
            }
            xmlReader(fs);
        }else{
            if(fa != null) xmlReader(fa);
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
                    tempNetworkItems = mReceivedItems;
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
                items = androidItems;
                break;
            case R.id.item_system:
                items = systemItems;
                break;
            case R.id.item_screen:
                items = screenItems;
                break;
            case R.id.item_memory:
                items = memoryItems;
                break;
            case R.id.item_storage:
                items = storageItems;
                break;
            case R.id.item_phone:
                items = telephoneItems;
                break;
            case R.id.item_sensor:
                items = sensorItems;
                break;
            case R.id.item_input:
                items = inputItems;
                break;
            case R.id.item_connectivity:
                items = connectivityItems;
                break;
            case R.id.item_network:
                items = networkItems;
                break;
            case R.id.item_location:
                items = locationItems;
                break;
            case R.id.item_camera:
                items = cameraItems;
                break;
            case R.id.item_opengl:
                items = openglItems;
                break;
            case R.id.item_drm:
                items = drmItems;
                break;
            case R.id.item_account:
                items = accountItems;
                break;
            case R.id.item_policy:
                items = policyItems;
                break;
            case R.id.item_codec:
                items = codecItems;
                break;
            case R.id.item_security:
                items = securityItems;
                break;
            case R.id.item_locale:
                items = localeItems;
                break;
            case R.id.item_about:
                items = aboutItems;
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
                //items = mTelephoneProvider.getItems();
            } else {
                items = new ArrayList<InfoItem>();
            }
        }else if(requestCode == REQUEST_LOCATION){
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //items = mLocationProvider.getItems();
            } else {
                items = new ArrayList<InfoItem>();
            }
        }else if(requestCode == REQUEST_CAMERA){
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //items = mCameraProvider.getItems();
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

    private void itemToXML(){
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
            mNetworkProvider.getItemsAsync(mReceiver);
            xmlWriter("Networks", tempNetworkItems, serializer);

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
            xmlWriter("DRM_engines", new DrmInfoProvider(this).getItems(), serializer);
            xmlWriter("Accounts", new AccountInfoProvider(this).getItems(), serializer);
            xmlWriter("DevicePolicy", new PolicyInfoProvider(this).getItems(), serializer);
            xmlWriter("Multimedia_codec", new CodecInfoProvider(this).getItems(), serializer);
            xmlWriter("Security_providers", new SecurityInfoProvider(this).getItems(), serializer);
            xmlWriter("SupportedLocales", new LocaleInfoProvider(this).getItems(), serializer);
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
        //xmlReader(new File(PATH + "/device.xml"));
    }

    private void xmlWriter(String tagName, ArrayList<InfoItem> items, XmlSerializer serializer) throws IOException {
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

    private void xmlReader(File fs){
        FileInputStream xmlInput;
        XmlPullParserFactory factory;
        try {
            factory= XmlPullParserFactory.newInstance();
            xpp= factory.newPullParser();
            xmlInput = new FileInputStream(fs);
            xpp.setInput(new InputStreamReader(xmlInput));
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }catch (XmlPullParserException e){
            e.printStackTrace();
        }

        String catName;
        ArrayList<InfoItem> temp = null;
        String name = null;
        String value = null;
        try{
            while (xpp.getEventType() != XmlPullParser.END_DOCUMENT){
                switch (xpp.getEventType()){
                    case XmlPullParser.START_TAG:
                        catName = xpp.getName();
                        if(catName.equals("name")){
                            xpp.next();
                            name = xpp.getText();
                            Log.d("xmlReader1", name);
                        }else if(catName.equals("value")){
                            xpp.next();
                            value = xpp.getText();
                            if(value != null) Log.d("xmlReader2", value);
                        }else if(!catName.equals("item")){
                            if(catName.equals("Android")) temp = androidItems;
                            else if(catName.equals("Screen")) temp = screenItems;
                            else if(catName.equals("System")) temp = systemItems;
                            else if(catName.equals("Memory")) temp = memoryItems;
                            else if(catName.equals("Storage")) temp = storageItems;
                            else if(catName.equals("Telephone")) temp = telephoneItems;
                            else if(catName.equals("Sensors")) temp = sensorItems;
                            else if(catName.equals("InputDevices")) temp = inputItems;
                            else if(catName.equals("Connectivity")) temp = connectivityItems;
                            else if(catName.equals("Networks")) temp = networkItems;
                            else if(catName.equals("LocationProvider")) temp = locationItems;
                            else if(catName.equals("Camera")) temp = cameraItems;
                            else if(catName.equals("OpenGL")) temp = openglItems;
                            else if(catName.equals("DRM_engines")) temp = drmItems;
                            else if(catName.equals("Accounts")) temp = accountItems;
                            else if(catName.equals("DevicePolicy")) temp = policyItems;
                            else if(catName.equals("Multimedia_codec")) temp = codecItems;
                            else if(catName.equals("Security_providers")) temp = securityItems;
                            else if(catName.equals("SupportedLocales")) temp = localeItems;
                            else if(catName.equals("AboutApp")) temp = aboutItems;
                            Log.d("xmlReader menu",catName);
                            if(temp!=null)  temp.clear();
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if(xpp.getName().equals("item")){
                            temp.add(new InfoItem(name,value));
                        }
                        break;
                }
                xpp.next();
            }
        }catch (XmlPullParserException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
