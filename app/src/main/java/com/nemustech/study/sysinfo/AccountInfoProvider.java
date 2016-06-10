package com.nemustech.study.sysinfo;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;

import java.util.ArrayList;

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
