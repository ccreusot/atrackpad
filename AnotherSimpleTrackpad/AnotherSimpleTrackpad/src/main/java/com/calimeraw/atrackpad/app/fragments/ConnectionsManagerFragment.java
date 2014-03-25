package com.calimeraw.atrackpad.app.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import com.calimeraw.atrackpad.app.R;
import com.calimeraw.atrackpad.app.adapters.SpinnerConnectionAdapter;
import com.calimeraw.atrackpad.app.database.ConnectionsDB;
import com.calimeraw.atrackpad.app.models.Connection;

import java.util.List;

/**
 * Created by cedric.creusot on 25/03/14.
 * class ConnectionsManagerFragment
 * desc: Small fragment will be used to select/connect or edit, add any connection profile
 */
public class ConnectionsManagerFragment  extends Fragment implements ConnectionDialogFragment.DialogListener {

    private static final String LOG = ConnectionsManagerFragment.class.getName();

    private Spinner mProfiles;
    private Connection mConnection;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_connect, container, false);

        assert rootView != null;
        mProfiles = (Spinner)rootView.findViewById(R.id.connection_list);
        mProfiles.setAdapter(new SpinnerConnectionAdapter(getActivity(), getConnections()));
        mProfiles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mConnection = (Connection)parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        if (!mProfiles.getAdapter().isEmpty())
            mConnection = (Connection)mProfiles.getItemAtPosition(0);

        Button add = (Button)rootView.findViewById(R.id.add_connection);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                Fragment prev = getFragmentManager().findFragmentByTag("dialog");
                if (prev != null)
                    ft.remove(prev);
                ft.addToBackStack(null);

                ConnectionDialogFragment dialog = ConnectionDialogFragment.newInstance(ConnectionDialogFragment.Action.Add, mConnection);
                dialog.setDialogListener(ConnectionsManagerFragment.this);
                dialog.show(ft, "dialog");
            }
        });
        Button edit = (Button)rootView.findViewById(R.id.edit_connection);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mConnection != null) {
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    Fragment prev = getFragmentManager().findFragmentByTag("dialog");
                    if (prev != null)
                        ft.remove(prev);
                    ft.addToBackStack(null);

                    ConnectionDialogFragment dialog = ConnectionDialogFragment.newInstance(ConnectionDialogFragment.Action.Edit, mConnection);
                    dialog.setDialogListener(ConnectionsManagerFragment.this);
                    dialog.show(ft, "dialog");
                }
            }
        });
        Button connect = (Button)rootView.findViewById(R.id.connect);
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mConnection != null) {
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.container, TrackpadFragment.newInstance(mConnection));
                    ft.addToBackStack(null);
                    ft.commit();
                }
            }
        });
        return rootView;
    }

    private List<Connection> getConnections() {
        List<Connection> connections;
        ConnectionsDB db = new ConnectionsDB(getActivity());
        connections = db.getConnections();
        db.close();
        return connections;
    }

    @Override
    public void onValidate() {
        ((SpinnerConnectionAdapter)mProfiles.getAdapter()).setConnectionList(getConnections());
        ((SpinnerConnectionAdapter)mProfiles.getAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onCancel() {

    }
}