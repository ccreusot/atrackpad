package com.calimeraw.atrackpad.app.fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.calimeraw.atrackpad.app.R;
import com.calimeraw.atrackpad.app.database.ConnectionsDB;
import com.calimeraw.atrackpad.app.models.Connection;

/**
 * Created by cedric.creusot on 25/03/14.
 * class ConnectionDialogFragment
 * desc: Small class that will be used to set the information of a connection profile.
 */
public class ConnectionDialogFragment extends DialogFragment {

    public enum Action {
        Add,
        Edit
    }

    private DialogListener mListener;
    private Action mAction;
    private Connection mConnection;

    public static ConnectionDialogFragment newInstance(Action action, Connection connection) {
        ConnectionDialogFragment frag = new ConnectionDialogFragment();
        Bundle args = new Bundle();

        args.putString("action", action.name());
        if (connection != null)
            args.putParcelable("connection", connection);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialog, container, false);

        assert view != null;
        final TextView name = (TextView)view.findViewById(R.id.connection_name);
        final TextView ip = (TextView)view.findViewById(R.id.connection_ip);
        final TextView port = (TextView)view.findViewById(R.id.connection_port);

        Button positive = (Button)view.findViewById(R.id.validate);
        positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (name.getText() != null && ip.getText() != null && port.getText() != null) {
                    ConnectionsDB db = new ConnectionsDB(getActivity());
                    if (mConnection != null && mAction.equals(Action.Edit)) {
                        db.updateConnection(new Connection(
                                mConnection.id,
                                name.getText().toString(),
                                ip.getText().toString(),
                                Integer.parseInt(port.getText().toString())), mConnection);
                    } else {
                        db.addConnection(new Connection(name.getText().toString(),
                                ip.getText().toString(),
                                Integer.parseInt(port.getText().toString())));
                    }
                    db.close();
                    getDialog().dismiss();
                    onValidate();
                }
            }
        });
        Button remove = (Button)view.findViewById(R.id.neutral);
        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mConnection != null) {
                    ConnectionsDB db = new ConnectionsDB(getActivity());
                    db.removeConnection(mConnection);
                    db.close();
                    getDialog().dismiss();
                    onValidate();
                }
            }
        });
        Button cancel = (Button)view.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().cancel();
                onCancel();
            }
        });

        // Depending of the action
        mAction = Action.valueOf(getArguments().getString("action"));
        mConnection = getArguments().getParcelable("connection");
        String title = "Add Connection";
        if (mAction.equals(Action.Add)) {
            remove.setVisibility(View.GONE);
        } else {
            title = "Edit Connection";
            name.setText(mConnection.name);
            ip.setText(mConnection.ip);
            port.setText(Integer.toString(mConnection.port));
        }
        getDialog().setTitle(title);

        return view;
    }

    public void onValidate() {
        if (mListener != null)
            mListener.onValidate();
    }

    public void onCancel() {
        if (mListener != null)
            mListener.onCancel();
    }

    public void setDialogListener(DialogListener listener) {
        this.mListener = listener;
    }

    public interface DialogListener {
        public void onValidate();
        public void onCancel();
    }
}
