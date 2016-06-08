package com.nemustech.study.sysinfo;

import android.content.Context;

import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by cheolgyoon on 2016. 6. 8..
 *
 */
public class SecurityInfoProvider extends InfoProvider {
    @SuppressWarnings("unused")
    private static final String TAG = SecurityInfoProvider.class.getSimpleName();

    private static ArrayList<InfoItem> sSecurityItems;
    private static HashSet<String> sTypes;

    SecurityInfoProvider(Context context) {
        super(context);
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
                Class<?> cls;
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
                String type = ps.getType();
                sb.append(ps.getAlgorithm());
                sb.append(" (").append(type).append(")\n");
                sTypes.add(type);
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

    private void addServiceItem(ArrayList<InfoItem> list, String serviceType) {
        Set<String> algs = Security.getAlgorithms(serviceType);
        if (null != algs && 0 < algs.size()) {
            StringBuilder sb = new StringBuilder();
            for (String alg: algs) {
                sb.append(alg).append('\n');
            }
            sb.deleteCharAt(sb.length() - 1);
            list.add(new InfoItem(serviceType + " algorithms", sb.toString()));
        }
    }

    @Override
    ArrayList<InfoItem> getItems() {
        if (null == sSecurityItems) {
            sSecurityItems = new ArrayList<>();
            sTypes = new HashSet<>();
            Provider[] providers = Security.getProviders();
            if (null == providers || 0 == providers.length) {
                sSecurityItems.add(new InfoItem(getString(R.string.item_security), getString(R.string.security_none)));
            } else {
                for (Provider p: providers) {
                    addSecurityContent(sSecurityItems, p);
                }
                for (String type: sTypes) {
                    addServiceItem(sSecurityItems, type);
                }
            }
        }
        return sSecurityItems;
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
