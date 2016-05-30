package com.nemustech.study.sysinfo;

import android.content.Context;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Created by cheolgyoon on 2016. 5. 12..
 */
public class SystemInfoProvider extends InfoProvider {
    @SuppressWarnings("unused")
    private static final String TAG = SystemInfoProvider.class.getSimpleName();

    private static ArrayList<InfoItem> sSystemContent;

    SystemInfoProvider(Context context) {
        super(context);
    }

    private String formatSeparator(char ch) {
        if (Character.isWhitespace(ch)) {
            return String.format("(0x%1$02X)", (int)ch);
        } else {
            return String.format("%1$c (0x%2$02X)", ch, (int)ch);
        }
    }
    private String formatSeparators(String str) {
        StringBuffer sb = new StringBuffer();
        if (null != str && 0 < str.length()) {
            sb.append(formatSeparator(str.charAt(0)));
            for (int idx = 1; idx < str.length(); ++idx) {
                sb.append('\n').append(formatSeparator(str.charAt(0)));
            }
        } else {
            sb.append(getString(R.string.none));
        }
        return sb.toString();
    }

    @Override
    ArrayList<InfoItem> getItems() {
        if (null == sSystemContent) {
            sSystemContent = new ArrayList<>();
            sSystemContent.add(new InfoItem(getString(R.string.system_processors), String.valueOf(Runtime.getRuntime().availableProcessors())));

            //  Add system properties
            Properties prop = System.getProperties();
            Enumeration<?> names = prop.propertyNames();
            while (names.hasMoreElements()) {
                String name = (String) names.nextElement();
                if (name.contains("separator")) {
                    sSystemContent.add(new InfoItem(name, formatSeparators(prop.getProperty(name))));
                } else {
                    sSystemContent.add(new InfoItem(name, prop.getProperty(name)));
                }
            }

            //  Add environment variables
            Map<String, String> env = System.getenv();
            Set<String> keys = env.keySet();
            for (String key: keys) {
                sSystemContent.add(new InfoItem(key, env.get(key)));
            }
        }
        return sSystemContent;
    }

    @Override
    protected Object[] getInfoParams() {
        return null;
    }
    @Override
    protected InfoItem getItem(int infoId, Object... params) {
        return null;
    }
}
