package com.czbix.smbsteamer.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.czbix.smbsteamer.BuildConfig;
import com.czbix.smbsteamer.R;
import com.czbix.smbsteamer.model.SmbFileItem;
import com.czbix.smbsteamer.service.StreamService;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

public class FileListFragment extends ListFragment {
    private FileListAdapter mAdapter;
    private FileListTask mFileListTask;
    private Stack<SmbFileItem> mHistory;

    public static FileListFragment newInstance() {
        final FileListFragment fragment = new FileListFragment();
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FileListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new FileListAdapter(getActivity());
        setListAdapter(mAdapter);

        mHistory = new Stack<>();
        initRoot();
    }

    private void initRoot() {
        try {
            String server = BuildConfig.DEBUG ? "192.168.1.1/" : "";
            SmbFile smbFile = new SmbFile("smb://" + server, NtlmPasswordAuthentication.ANONYMOUS);
            final SmbFileItem smbFileItem;
            try {
                smbFileItem = new SmbFileItem(smbFile, true);
            } catch (SmbException e) {
                throw new RuntimeException(e);
            }
            mHistory.push(smbFileItem);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        startListTask();
    }

    private SmbFileItem getCurFile() {
        return mHistory.peek();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        final SmbFileItem item = (SmbFileItem) l.getItemAtPosition(position);
        if (item.isDirectory()) {
            mHistory.push(item);
            startListTask();
            return;
        }

        if (!StreamService.isRunning()) {
            getActivity().startService(new Intent(getActivity(), StreamService.class));
        }
        final Intent intent = new Intent(Intent.ACTION_VIEW, item.getHttpUri());
        startActivity(intent);
    }

    private void startListTask() {
        if (mFileListTask != null && mFileListTask.getStatus() != AsyncTask.Status.FINISHED) {
            mFileListTask.cancel(false);
        }

        mAdapter.clear();

        mFileListTask = new FileListTask(this);
        mFileListTask.execute(getCurFile());
    }

    public boolean onBackPressed() {
        if (mHistory.size() > 1) {
            mHistory.pop();
            startListTask();
            return true;
        }

        return false;
    }

    public final static class FileListAdapter extends ArrayAdapter<SmbFileItem> {
        private final LayoutInflater mInflater;

        public FileListAdapter(Context context) {
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

    public final static class FileListTask extends AsyncTask<SmbFileItem, Void, List<SmbFileItem>> {
        private static final String TAG = FileListTask.class.getSimpleName();
        private final FileListFragment mFragment;

        private Exception mException;

        public FileListTask(FileListFragment fragment) {
            mFragment = fragment;
        }

        @Override
        protected List<SmbFileItem> doInBackground(SmbFileItem... params) {
            Preconditions.checkArgument(params.length == 1, "must have one SmbFile");
            final SmbFileItem item = params[0];
            final SmbFile[] files;
            try {
                files = item.get().listFiles();
                Arrays.sort(files, new Comparator<SmbFile>() {
                    @Override
                    public int compare(SmbFile lhs, SmbFile rhs) {
                        return lhs.getName().compareTo(rhs.getName());
                    }
                });

                final ImmutableList.Builder<SmbFileItem> builder = ImmutableList.builder();
                for (SmbFile file : files) {
                    builder.add(new SmbFileItem(file, false));
                }

                return builder.build();
            } catch (Exception e) {
                mException = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<SmbFileItem> smbFiles) {
            if (mException != null) {
                if (mException instanceof SmbException) {
                    Toast.makeText(mFragment.getActivity(), mException.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.w(TAG, mException);
                    return;
                }
                throw new RuntimeException(mException);
            }

            Log.d(TAG, "result length: " + smbFiles.size());
            mFragment.mAdapter.addAll(smbFiles);
        }
    }
}
