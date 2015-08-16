package com.czbix.smbsteamer.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.czbix.smbsteamer.BuildConfig;
import com.czbix.smbsteamer.R;
import com.czbix.smbsteamer.dao.ServerDao;
import com.czbix.smbsteamer.dao.model.Credential;
import com.czbix.smbsteamer.dao.model.Server;
import com.czbix.smbsteamer.ui.dialog.AddServerDialog;
import com.czbix.smbsteamer.ui.fragment.ServerListFragment;

public class ServerListActivity extends AppCompatActivity implements ServerListFragment.Listener, AddServerDialog.Listener {
    private ServerListFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFragment = ServerListFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, mFragment).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_server, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                if (BuildConfig.DEBUG) {
                    // TODO: remove test code
                    final Server server = new Server("192.168.1.1", "media", null, Credential.ANONYMOUS);
                    onServerClick(server);
                }
                return true;
            case R.id.action_add_server:
                final AddServerDialog dialog = new AddServerDialog();
                dialog.show(getSupportFragmentManager(), "add_server");
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onServerClick(Server server) {
        final Intent intent = new Intent(this, FileListActivity.class);
        intent.putExtra(FileListActivity.ARG_SERVER, server);

        startActivity(intent);
    }

    @Override
    public void onDismiss() {}

    @Override
    public void onAdd(Server server) {
        ServerDao.putServer(server);
        mFragment.reloadServers();
    }
}
