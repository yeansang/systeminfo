package com.nemustech.study.sysinfo;

import android.content.Context;
import android.location.Criteria;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cheolgyoon on 2016. 6. 2..
 *
 */
public class LocationInfoProvider extends InfoProvider {
    @SuppressWarnings("unused")
    private static final String TAG = LocationInfoProvider.class.getSimpleName();

    private static ArrayList<InfoItem> sLocationItems;

    LocationInfoProvider(Context context) {
        super(context);
    }

    private String formatLocationAccuracy(int acc) {
        String value = null;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            switch (acc) {
                case Criteria.ACCURACY_COARSE: value = "Coarse"; break;
                case Criteria.ACCURACY_FINE: value = "Fine"; break;
            }
        } else {
            switch (acc) {
                case Criteria.ACCURACY_LOW: value = "Low"; break;
                case Criteria.ACCURACY_MEDIUM: value = "Medium"; break;
                case Criteria.ACCURACY_HIGH: value = "high"; break;
            }
        }
        if (null == value) {
            value = getString(R.string.unknown);
        }
        return String.format("%s (%d)", value, acc);
    }
    private String formatLocationPowerReq(int req) {
        String value;
        switch (req) {
            case Criteria.NO_REQUIREMENT: value = "No requirement"; break;
            case Criteria.POWER_LOW: value = "Low"; break;
            case Criteria.POWER_MEDIUM: value = "Medium"; break;
            case Criteria.POWER_HIGH: value = "High"; break;
            default: value = getString(R.string.unknown);
        }
        return String.format("%s (%d)", value, req);
    }
    private void addLocationItems(ArrayList<InfoItem> list, LocationProvider lp, boolean enabled) {
        String name = lp.getName();
        StringBuffer sb = new StringBuffer();
        sb.append(getString(R.string.location_accuracy)).append(formatLocationAccuracy(lp.getAccuracy()));
        sb.append('\n').append(getString(R.string.location_power_req)).append(formatLocationPowerReq(lp.getPowerRequirement()));
        sb.append('\n').append(getString(R.string.location_cost)).append(getString(lp.hasMonetaryCost()? R.string.location_may_charge: R.string.location_free));
        boolean rCel = lp.requiresCell();
        boolean rNet = lp.requiresNetwork();
        boolean rSat = lp.requiresSatellite();
        if (rCel | rNet | rSat) {
            sb.append("\n(").append(getString(R.string.location_requires));
            if (rCel) {
                sb.append(getString(R.string.location_req_cell));
            }
            if (rNet) {
                sb.append(getString(R.string.location_req_network));
            }
            if (rSat) {
                sb.append(getString(R.string.location_req_satellite));
            }
            sb.append(')');
        }
        boolean sAlt = lp.supportsAltitude();
        boolean sBng = lp.supportsBearing();
        boolean sSpd = lp.supportsSpeed();
        if (sAlt | sBng | sSpd) {
            sb.append("\nAdditional informations");
            if (sAlt) {
                sb.append("\n\taltitude");
            }
            if (sBng) {
                sb.append("\n\tbearing");
            }
            if (sSpd) {
                sb.append("\n\tspeed");
            }
            sb.append('\n');
        }
        list.add(new InfoItem(name, sb.toString()));
    }

    @Override
    ArrayList<InfoItem> getItems() {
        if (null == sLocationItems) {
            sLocationItems = new ArrayList<>();

            LocationManager lm = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);
            List<String> lpList = lm.getAllProviders();

            if (null == lpList || 0 == lpList.size()) {
                sLocationItems.add(new InfoItem(getString(R.string.item_location), getString(R.string.location_none)));
            } else {
                for (String name: lpList) {
                    LocationProvider lp = lm.getProvider(name);
                    if (null == lp) {
                        sLocationItems.add(new InfoItem(name, getString(R.string.invalid_item)));
                    } else {
                        boolean enabled = lm.isProviderEnabled(name);
                        addLocationItems(sLocationItems, lp, enabled);
                    }
                }
            }
        }
        return sLocationItems;
    }

    //  These methods are not used here

    @Override
    protected Object[] getInfoParams() {
        return new Object[0];
    }

    @Override
    protected InfoItem getItem(int infoId, Object... params) {
        return null;
    }

}

