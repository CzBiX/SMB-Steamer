package com.czbix.smbsteamer.ui.activity;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.czbix.smbsteamer.ui.fragment.FileListFragment;

public class FileListActivity extends AppCompatActivity {
    private FileListFragment mFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mFragment = FileListFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, mFragment).commit();
    }

    @Override
    public void onBackPressed() {
        if (mFragment.onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }
}
