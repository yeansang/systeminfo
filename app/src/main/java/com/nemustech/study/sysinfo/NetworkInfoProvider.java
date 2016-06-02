package com.nemustech.study.sysinfo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by cheolgyoon on 2016. 6. 1..
 *
 */
public class NetworkInfoProvider extends InfoProvider {
    @SuppressWarnings("unused")
    private static final String TAG = NetworkInfoProvider.class.getSimpleName();

    private static ArrayList<InfoItem> sNetworkItems;

    NetworkInfoProvider(Context context) {
        super(context);
    }

    private String formatHardwareAddress(byte[] address) {
        if (null == address || 0 == address.length) {
            return getString(R.string.unknown);
        }
        StringBuffer sb = new StringBuffer();
        sb.append(String.format("%02x", address[0]));
        for (int idx = 1; idx < address.length; ++idx) {
            sb.append('-').append(String.format("%02x", address[idx]));
        }
        return sb.toString();
    }

    @SuppressLint("NewApi")
    private void addNetworkItems(ArrayList<InfoItem> list, NetworkInterface network) {
        String name = network.getDisplayName();
        StringBuffer sb = new StringBuffer();
        sb.append(getString(R.string.network_name)).append(network.getName());
        sb.append('\n').append(getString(R.string.network_hardware_address));
        try {
            sb.append(formatHardwareAddress(network.getHardwareAddress()));
        } catch (SocketException e1) {
            e1.printStackTrace();
            sb.append(formatHardwareAddress(null));
        }
        try {
            sb.append('\n').append(getString(R.string.network_mtu)).append(String.valueOf(network.getMTU()));
        } catch (SocketException e1) {
            e1.printStackTrace();
        }
        if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
            int index = network.getIndex();
            sb.append(String.format("\nNetwork index: %d", index));
            if (index < 0) {
                sb.append(getString(R.string.unknown));
            }
        }
        try {
            sb.append('\n').append(getString(network.isUp()? R.string.network_up: R.string.network_down));
        } catch (SocketException e) {
            e.printStackTrace();
        }
        ArrayList<InetAddress> aList = Collections.list(network.getInetAddresses());
        if (0 < aList.size()) {
            sb.append('\n').append("iNetAddresses");
            for (InetAddress ia : aList) {
                String hostName = ia.getCanonicalHostName();
                String hostAddress = ia.getHostAddress();
                sb.append("\n\t").append(hostName);
                if (!hostName.equals(hostAddress)) {
                    sb.append('(').append(hostAddress).append(')');
                }
            }
        }
        ArrayList<NetworkInterface> nList = Collections.list(network.getSubInterfaces());
        if (0 < nList.size()) {
            sb.append('\n').append(getString(R.string.network_subinterface)).append(String.valueOf(nList.size()));
        }
        list.add(new InfoItem(name, sb.toString()));
        for (NetworkInterface subnet: nList) {
            addNetworkItems(list, subnet);
        }
    }

    @Override
    protected InfoItem getItem(int infoId, Object... params) {
        return null;
    }

    @Override
    protected Object[] getInfoParams() {
        return new Object[0];
    }

    @Override
    ArrayList<InfoItem> getItems() {
        if (null == sNetworkItems) {
            sNetworkItems = new ArrayList<>();

            ArrayList<NetworkInterface> nList = null;
            try {
                nList = Collections.list(NetworkInterface.getNetworkInterfaces());
            } catch (SocketException e) {
                e.printStackTrace();
            }

            if (null == nList || 0 == nList.size()) {
                sNetworkItems.add(new InfoItem(getString(R.string.item_network), getString(R.string.network_none)));
            } else {
                final ArrayList<NetworkInterface> fList = nList;
                for (NetworkInterface network: fList) {
                    addNetworkItems(sNetworkItems, network);
                }
            }

        }
        return sNetworkItems;
    }
}
