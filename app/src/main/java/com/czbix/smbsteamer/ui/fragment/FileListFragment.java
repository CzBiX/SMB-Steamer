package com.czbix.smbsteamer.ui.fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.czbix.smbsteamer.dao.model.Server;
import com.czbix.smbsteamer.helper.PreferenceHelper;
import com.czbix.smbsteamer.model.SmbFileItem;
import com.czbix.smbsteamer.service.StreamService;
import com.czbix.smbsteamer.ui.adapter.FileAdapter;
import com.czbix.smbsteamer.util.IoUtils;
import com.czbix.smbsteamer.util.SmbUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

public class FileListFragment extends ListFragment {
    public static final String ARG_SERVER = "server";

    private FileAdapter mAdapter;
    private FileListTask mFileListTask;
    private Stack<SmbFileItem> mHistory;

    public static FileListFragment newInstance(Bundle args) {
        final FileListFragment fragment = new FileListFragment();
        fragment.setArguments(args);
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

        final Bundle arguments = getArguments();
        final Server server = arguments.getParcelable(ARG_SERVER);

        mAdapter = new FileAdapter(getActivity());
        setListAdapter(mAdapter);

        mHistory = new Stack<>();
        initRoot(server);
    }

    private void initRoot(Server server) {
        try {
            SmbFile smbFile = new SmbFile(String.format("smb://%s/%s/", server.getHost(), server.getShare()),
                    server.getCredential());
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
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(item.getHttpUri(), SmbUtils.getMimeType(item.get()));
        startActivity(intent);
    }

    private void startListTask() {
        if (mFileListTask != null && mFileListTask.getStatus() != AsyncTask.Status.FINISHED) {
            mFileListTask.cancel(false);
        }

        mFileListTask = new FileListTask(this);
        mFileListTask.execute(getCurFile());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mFileListTask != null && mFileListTask.getStatus() != AsyncTask.Status.FINISHED) {
            mFileListTask.cancel(false);
            mFileListTask = null;
        }
    }

    public boolean onBackPressed() {
        if (mHistory.size() > 1) {
            mHistory.pop();
            startListTask();
            return true;
        }

        return false;
    }

    public final static class FileListTask extends AsyncTask<SmbFileItem, Void, List<SmbFileItem>> {
        private static final String TAG = FileListTask.class.getSimpleName();
        private final FileListFragment mFragment;

        private Exception mException;

        public FileListTask(FileListFragment fragment) {
            mFragment = fragment;
        }

        @Override
        protected void onPreExecute() {
            mFragment.setListShownNoAnimation(false);
            mFragment.mAdapter.clear();
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
                final boolean onlyVideo = PreferenceHelper.isOnlyVideo();

                for (SmbFile file : files) {
                    if (onlyVideo) {
                        if (file.isFile() && !IoUtils.isVideoFile(file.getName())) {
                            continue;
                        }
                    }
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
                    mFragment.setListShown(true);
                    return;
                }
                throw new RuntimeException(mException);
            }

            Log.d(TAG, "result length: " + smbFiles.size());
            mFragment.mAdapter.addAll(smbFiles);
            mFragment.setListShown(true);
        }
    }
}
