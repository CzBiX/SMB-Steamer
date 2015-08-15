package com.czbix.smbsteamer.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.czbix.smbsteamer.R;
import com.czbix.smbsteamer.dao.model.Server;
import com.google.common.base.Preconditions;

import java.util.List;

public class ServerAdapter extends ArrayAdapter<Server> {
    private final LayoutInflater mInflater;

    public ServerAdapter(Context context, List<Server> servers) {
        super(context, 0, servers);

        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.row_server, parent , false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = ((ViewHolder) convertView.getTag());
            Preconditions.checkNotNull(viewHolder);
        }

        final Server item = getItem(position);
        viewHolder.fillData(item);

        return convertView;
    }

    private final class ViewHolder {
        private final TextView mServerName;

        public ViewHolder(View view) {
            mServerName = (TextView) view.findViewById(R.id.server_name);
        }

        public void fillData(Server item) {
            mServerName.setText(item.getName());
        }
    }
}
