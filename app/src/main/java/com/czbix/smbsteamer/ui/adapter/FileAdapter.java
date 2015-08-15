package com.czbix.smbsteamer.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.czbix.smbsteamer.R;
import com.czbix.smbsteamer.model.SmbFileItem;
import com.google.common.base.Preconditions;

public final class FileAdapter extends ArrayAdapter<SmbFileItem> {
    private final LayoutInflater mInflater;

    public FileAdapter(Context context) {
        super(context, 0);

        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.row_file, parent , false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = ((ViewHolder) convertView.getTag());
            Preconditions.checkNotNull(viewHolder);
        }

        final SmbFileItem item = getItem(position);
        viewHolder.fillData(item);

        return convertView;
    }

    private final class ViewHolder {
        private final TextView mFileName;

        public ViewHolder(View view) {
            mFileName = (TextView) view.findViewById(R.id.file_name);
        }

        public void fillData(SmbFileItem item) {
            mFileName.setText(item.getName());
        }
    }
}
