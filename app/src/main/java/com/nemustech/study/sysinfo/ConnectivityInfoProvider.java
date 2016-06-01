package com.nemustech.study.sysinfo;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cheolgyoon on 2016. 6. 1..
 *
 */
public class ConnectivityInfoProvider extends InfoProvider {
    @SuppressWarnings("unused")
    private static final String TAG = ConnectivityInfoProvider.class.getSimpleName();

    private static ArrayList<InfoItem> sConnectivityItems;

    private Object[] mParams;
    ConnectivityInfoProvider(Context context) {
        super(context);
    }

    @SuppressWarnings("deprecation")
    private List<NetworkInfo> collectNetworkInfo(ConnectivityManager cm) {
        ArrayList<NetworkInfo> list = new ArrayList<>();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            NetworkInfo[] infos = cm.getAllNetworkInfo();
            for (NetworkInfo info: infos) {
                list.add(info);
            }
        } else {
            Network[] networks = cm.getAllNetworks();
            for (Network network: networks) {
                list.add(cm.getNetworkInfo(network));
            }
        }
        return list;
    }

    @Override
    protected Object[] getInfoParams() {
        return mParams;
    }

    private String formatName(NetworkInfo ni) {
        String subtype = ni.getSubtypeName();
        if (null == subtype || 0 == subtype.length()) {
            return ni.getTypeName();
        }
        return String.format("%s (%s)", ni.getTypeName(), subtype);
    }

    private String formatNetworkState(NetworkInfo ni) {
        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.connectivity_state))
                .append(": ").append(ni.getState())
                .append(" (").append(ni.getDetailedState()).append(')');
        if (!ni.isAvailable()) {
            sb.append("\n\tUnavailable");
        }
        if (ni.isRoaming()) {
            sb.append("\n\tRoaming");
        }
        String ei = ni.getExtraInfo();
        if (null != ei && 0 < ei.length()) {
            sb.append("\n\t\textra: ").append(ei);
        }
        return sb.toString();
    }

    @Override
    protected InfoItem getItem(int infoId, Object... params) {
        String name = null;
        String value = null;
        if (params[0] instanceof NetworkInfo) {
            NetworkInfo ni = (NetworkInfo)params[0];
            name = formatName(ni);

            switch (infoId) {
                case R.string.connectivity_state:
                    value = formatNetworkState(ni);
                    break;
                default:
                    break;
            }
        }
        if (null != value && 0 < value.length()) {
            return new InfoItem(name, value);
        }
        return null;
    }

    private static final InfoSpec[] sConnectivitySpecs = {
        new InfoSpec(R.string.connectivity_state, 1)
    };

    @Override
    ArrayList<InfoItem> getItems() {
        if (null == sConnectivityItems) {
            sConnectivityItems = new ArrayList<>();

            ConnectivityManager cm = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            List<NetworkInfo> nis = collectNetworkInfo(cm);
            if (null == nis || 0 == nis.size()) {
                sConnectivityItems.add(new InfoItem(getString(R.string.item_connectivity), getString(R.string.connectivity_none)));
            } else {
                mParams = new Object[1];
                for (NetworkInfo info: nis) {
                    mParams[0] = info;
                    addItems(sConnectivityItems, sConnectivitySpecs);
                }
            }
        }
        return sConnectivityItems;
    }
}
