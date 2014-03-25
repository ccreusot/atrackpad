package com.calimeraw.atrackpad.app.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.calimeraw.atrackpad.app.models.Connection;

import java.util.List;
import java.util.zip.Inflater;

/**
 * Created by cedric.creusot on 21/03/14.
 * class SpinnerConnectionAdapter
 * desc: Small adapter to used with the spinner and a connection profile list.
 */
public class SpinnerConnectionAdapter extends BaseAdapter {

    private List<Connection> connectionList;
    private Context context;

    public SpinnerConnectionAdapter(Context context, List<Connection> connectionList) {
        this.context = context;
        this.connectionList = connectionList;
    }

    public void setConnectionList(List<Connection> connectionList) {
        this.connectionList = connectionList;
    }

    @Override
    public int getCount() {
        return connectionList.size();
    }

    @Override
    public Object getItem(int position) {
        return connectionList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = View.inflate(context, android.R.layout.simple_expandable_list_item_1, null);
        TextView text = (TextView) convertView.findViewById(android.R.id.text1);
        text.setText(((Connection)getItem(position)).name);
        return convertView;
    }
}
