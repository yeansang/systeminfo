package com.nemustech.study.sysinfo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;

/**
 * Created by cheolgyoon on 2016. 6. 8..
 *
 */
public class LocaleInfoProvider extends InfoProvider {
    @SuppressWarnings("unused")
    private static final String TAG = LocaleInfoProvider.class.getSimpleName();
    private static ArrayList<InfoItem> sLocaleItems;

    LocaleInfoProvider(Context context) {
        super(context);
    }

    private String formatLocaleName(Locale locale) {
        String name = locale.getDisplayName();
        String lname = locale.getDisplayName(locale);
        if (null != name && 0 < name.length()) {
            return name + ": " + lname;
        }
        return "";
    }

    @SuppressLint("NewApi")
    private void appendUnicodeLocaleInfo(StringBuilder sb, Locale locale) {
        if (Build.VERSION_CODES.LOLLIPOP <= Build.VERSION.SDK_INT) {
            Set<String> attribs = locale.getUnicodeLocaleAttributes();
            if (null != attribs && 0 < attribs.size()) {
                sb.append("\nUnicode attributes");
                for (String attr : attribs) {
                    sb.append("\n\t").append(attr);
                }
            }
            Set<String> keys = locale.getUnicodeLocaleKeys();
            if (null != keys && 0 < keys.size()) {
                sb.append("\nUnicode extension");
                for (String key : keys) {
                    sb.append("\n\t").append(key).append(": ");
                    sb.append(locale.getUnicodeLocaleType(key));
                }
            }
        }
    }

    @SuppressLint("NewApi")
    private void appendLocaleScript(StringBuilder sb, Locale locale) {
        if (Build.VERSION_CODES.LOLLIPOP <= Build.VERSION.SDK_INT) {
            String scr = locale.getScript();
            if (null != scr && 0 < scr.length()) {
                sb.append(" - ");
                String ds = locale.getDisplayScript();
                String lds = locale.getDisplayScript(locale);
                sb.append(ds).append(" (").append(lds).append(')');
            }
        }
    }

    @SuppressLint("NewApi")
    private String formatLocaleCodes(Locale locale) {
        String lang = locale.getLanguage();
        String cc = locale.getCountry();
        String var = locale.getVariant();
        String scr = null;
        String lang3 = null;
        String cc3 = null;
        try {
            lang3 = locale.getISO3Language();
            cc3 = locale.getISO3Country();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        StringBuilder sb = new StringBuilder();
        sb.append(lang);
        if (Build.VERSION_CODES.LOLLIPOP <= Build.VERSION.SDK_INT) {
            scr = locale.getScript();
            if (null != scr && 0 < scr.length()) {
                sb.append('-').append(scr);
            }
        }
        if (null != cc && 0 < cc.length()) {
            sb.append('-').append(cc);
        }
        if (null != var && 0 < var.length()) {
            sb.append(", ").append(var);
        }
        String tag = sb.toString();
        sb.append(" (");
        if (null == lang3 || 0 == lang3.length()) {
            sb.append(getString(R.string.no_iso3));
        } else {
            sb.append(lang3);
            if (null != scr && 0 < scr.length()) {
                sb.append('-').append(scr);
            }
            if (null != cc3 && 0 < cc3.length()) {
                sb.append('-').append(cc3);
            }
        }
        sb.append(')');
        if (Build.VERSION_CODES.LOLLIPOP <= Build.VERSION.SDK_INT) {
            String tag21 = locale.toLanguageTag();
            if (null != tag21 && 0 < tag21.length() && !tag.equals(tag21)) {
                sb.append(" tag ").append(tag21);
            }
        }
        return sb.toString();
    }

    private InfoItem getLocaleItem(Locale locale) {
        StringBuilder sb = new StringBuilder();
//        appendLocaleScript(sb, locale);  //  this information is redundant
        appendUnicodeLocaleInfo(sb, locale);

        return new InfoItem(formatLocaleName(locale), formatLocaleCodes(locale) + sb.toString());
    }

    private void addSpecificLocale(ArrayList<InfoItem> list, String name, Locale locale) {
        list.add(new InfoItem(name, formatLocaleCodes(locale) + " - " + formatLocaleName(locale)));
    }

    @Override
    ArrayList<InfoItem> getItems() {
        if (null == sLocaleItems) {
            sLocaleItems = new ArrayList<>();
            addSpecificLocale(sLocaleItems, getString(R.string.locale_current), mContext.getResources().getConfiguration().locale);
            addSpecificLocale(sLocaleItems, getString(R.string.locale_default), Locale.getDefault());
            Locale[] locales = Locale.getAvailableLocales();
            for (Locale locale: locales) {
                sLocaleItems.add(getLocaleItem(locale));
            }
        }
        return sLocaleItems;
    }

    @Override
    protected Object[] getInfoParams() {
        return new Object[0];
    }

    @Override
    protected InfoItem getItem(int infoId, Object... params) {
        return null;
    }
}
