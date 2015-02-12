package com.nemustech.study.sysinfo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class InfoListView extends ListView {
    @SuppressWarnings("unused")
    private static final String TAG = InfoListView.class.getSimpleName();

    public InfoListView(Context context) {
        this(context, null, 0);
    }

    public InfoListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InfoListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public static class InfoItem {
        public InfoItem() {
        }
        public InfoItem(String name, String value) {
            this.name = name;
            this.value = value;
        }
        String name;
        String value;
    }

    public static class InfoItemAdapter extends ArrayAdapter<InfoItem> {
        @SuppressWarnings("unused")
        private static final String TAG = InfoItemAdapter.class.getSimpleName();

        public InfoItemAdapter(Context context, int resource, ArrayList<InfoItem> items) {
            super(context, resource, items);
        }

        public View getView(int pos, View convertView, ViewGroup parent) {
            View content = convertView;
            if (null == content) {
                content = View.inflate(getContext(), R.layout.content_item, null);
            }
            InfoItem item = getItem(pos);
            ((TextView)content.findViewById(R.id.content_name)).setText(item.name);
            ((TextView)content.findViewById(R.id.content_value)).setText(item.value);
            return content;
        }
    }
}
