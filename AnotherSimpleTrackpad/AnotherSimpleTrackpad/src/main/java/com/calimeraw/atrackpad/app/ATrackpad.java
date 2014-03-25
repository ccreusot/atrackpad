package com.calimeraw.atrackpad.app;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import com.calimeraw.atrackpad.app.fragments.ConnectionsManagerFragment;


public class ATrackpad extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atrackpad);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new ConnectionsManagerFragment())
                    .commit();
        }
    }
}
