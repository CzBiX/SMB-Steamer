package com.czbix.smbsteamer.ui.fragment;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

import com.czbix.smbsteamer.dao.ServerDao;
import com.czbix.smbsteamer.dao.model.Server;
import com.czbix.smbsteamer.ui.adapter.ServerAdapter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * <p/>
 * Activities containing this fragment MUST implement the {@link Listener}
 * interface.
 */
public class ServerListFragment extends ListFragment {
    private Listener mListener;
    private ServerAdapter mAdapter;
    private AsyncTask<Void, Void, List<Server>> mTask;

    public static ServerListFragment newInstance() {
        return new ServerListFragment();
    }

    public ServerListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadServers();
    }

    public void reloadServers() {
        loadServers();
    }

    private void loadServers() {
        if (mTask != null) {
            mTask.cancel(false);
        }

        mTask = new AsyncTask<Void, Void, List<Server>>() {
            @Override
            protected List<Server> doInBackground(Void... params) {
                final Server server = ServerDao.getServer();

                if (server == null) {
                    return ImmutableList.of();
                }
                return ImmutableList.of(server);
            }

            @Override
            protected void onPostExecute(List<Server> servers) {
                if (mAdapter == null) {
                    mAdapter = new ServerAdapter(getActivity(), Lists.newArrayList(servers));
                    setListAdapter(mAdapter);
                } else {
                    mAdapter.clear();
                    mAdapter.addAll(servers);
                }
            }
        };
        mTask.execute();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (Listener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement Listener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mTask != null && mTask.getStatus() != AsyncTask.Status.FINISHED) {
            mTask.cancel(false);
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        mListener.onServerClick(mAdapter.getItem(position));
    }

    public interface Listener {
        void onServerClick(Server server);
    }
}
