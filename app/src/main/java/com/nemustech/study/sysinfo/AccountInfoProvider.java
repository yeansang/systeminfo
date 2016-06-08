package com.nemustech.study.sysinfo;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cheolgyoon on 2016. 6. 7..
 *
 */
public class AccountInfoProvider extends InfoProvider {
    @SuppressWarnings("unused")
    private static final String TAG = AccountInfoProvider.class.getSimpleName();
    private static ArrayList<InfoItem> sAccountItems;
    AccountInfoProvider(Context context) {
        super(context);
    }

    private InfoItem getAuthenticatorItem(AuthenticatorDescription ad, PackageManager pm) {
        String name;
        try {
            Resources r = pm.getResourcesForApplication(ad.packageName);
            name = r.getString(ad.labelId);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            name = getString(R.string.unknown);
        }
        StringBuffer sb = new StringBuffer();
        sb.append("Type: ").append(ad.type);
        sb.append("\nPackage: ").append(ad.packageName);
        return new InfoItem("Authenticator: " + name, sb.toString());
    }
    private InfoItem getAdminItem(List<ComponentName> admins) {
        StringBuffer sb = new StringBuffer();
        sb.append(admins.get(0));
        for (int idx = 1; idx < admins.size(); ++idx) {
            sb.append('\n').append(admins.get(idx));
        }
        return new InfoItem(getString(R.string.account_admin), sb.toString());
    }

    @Override
    ArrayList<InfoItem> getItems() {
        if (null == sAccountItems) {
            sAccountItems = new ArrayList<>();
            AccountManager am = (AccountManager)mContext.getSystemService(Context.ACCOUNT_SERVICE);
            Account[] accounts = am.getAccounts();
            if (null == accounts || 0 == accounts.length) {
                sAccountItems.add(new InfoItem(getString(R.string.item_account), getString(R.string.account_none)));
            } else {
                for (Account account: accounts) {
                    sAccountItems.add(new InfoItem("Account: " + account.name, "type: " + account.type));
                }
            }
            AuthenticatorDescription[] ads = am.getAuthenticatorTypes();
            if (null == ads || 0 == ads.length) {
                sAccountItems.add(new InfoItem(getString(R.string.account_auth), getString(R.string.account_auth_none)));
            } else {
                PackageManager pm = mContext.getPackageManager();
                for (AuthenticatorDescription ad: ads) {
                    sAccountItems.add(getAuthenticatorItem(ad, pm));
                }
            }

            //  TODO: Separate and enhance policy informations.
            DevicePolicyManager dpm = (DevicePolicyManager)mContext.getSystemService(Context.DEVICE_POLICY_SERVICE);
            List<ComponentName> admins = dpm.getActiveAdmins();
            if (null == admins || 0 == admins.size()) {
                sAccountItems.add(new InfoItem(getString(R.string.account_admin), getString(R.string.account_admin_none)));
            } else {
                sAccountItems.add(getAdminItem(admins));
            }
        }
        return sAccountItems;
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
