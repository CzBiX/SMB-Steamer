package com.czbix.smbsteamer.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.czbix.smbsteamer.BuildConfig;
import com.czbix.smbsteamer.R;
import com.czbix.smbsteamer.dao.model.Credential;
import com.czbix.smbsteamer.dao.model.Server;
import com.czbix.smbsteamer.ui.fragment.ServerListFragment;

public class ServerListActivity extends AppCompatActivity implements ServerListFragment.Listener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ServerListFragment fragment = ServerListFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_server, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            if (BuildConfig.DEBUG) {
                // TODO: remove test code
                final Server server = new Server("192.168.1.1", "media", Credential.ANONYMOUS);
                onServerClick(server);
            }
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
}
