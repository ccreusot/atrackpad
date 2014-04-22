package com.calimeraw.atrackpad.app.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.*;
import com.calimeraw.atrackpad.app.R;
import com.calimeraw.atrackpad.app.models.Connection;
import com.calimeraw.atrackpad.app.networks.NetworkTrackpadClient;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by cedric.creusot on 25/03/14.
 * class TrackpadFragment
 * desc: Fragment that will be used to send information to the server.
 */
public class TrackpadFragment extends Fragment implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener, View.OnTouchListener {

    private static final String  LOG = TrackpadFragment.class.getName();
    private static final int MOUSE_BUTTON_LEFT = 1;
    private static final int MOUSE_BUTTON_RIGHT = 2;

    private GestureDetectorCompat mGesture;
    private Connection mConnection;
    private NetworkTrackpadClient runningConnection;

    public static TrackpadFragment newInstance(Connection connection) {
        TrackpadFragment frag = new TrackpadFragment();
        Bundle args = new Bundle();

        args.putParcelable("connection", connection);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trackpad, container, false);

        assert view != null;
        view.setOnTouchListener(this);
        mGesture = new GestureDetectorCompat(getActivity(), this);
        mConnection = getArguments().getParcelable("connection");
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        runningConnection = new NetworkTrackpadClient(mConnection);
        runningConnection.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        runningConnection.end();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        runningConnection.end();
    }

    // Part wich correspond to the gesture management.
    private long mTimePress;
    private boolean mMultipleTouch;
    private boolean mRightClick = false;// Small temp variable to do no do a simple click left after a right click

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mMultipleTouch = (MotionEventCompat.getPointerCount(event) >= 2);
        if  (mMultipleTouch) {
            switch (MotionEventCompat.getActionMasked(event)) {
                case MotionEventCompat.ACTION_POINTER_DOWN:
                    mTimePress = event.getEventTime();
                    break;
                case MotionEventCompat.ACTION_POINTER_UP:
                    if (event.getEventTime() - mTimePress > ViewConfiguration.getTapTimeout() - 200) {
                        Log.d(LOG, "click left");
                        runningConnection.sendClick(MOUSE_BUTTON_RIGHT, 1);
                        mTimePress = 0;
                        mRightClick = true;
                    }
                    break;
                default:
                    mGesture.onTouchEvent(event);
                    mTimePress = event.getEventTime() + 500;
                    break;
            }
        }
        else {
            if (!mRightClick)
                mGesture.onTouchEvent(event);
            else
                mRightClick = false;
        }
        return true;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        Log.d(LOG, "click!");
        runningConnection.sendClick(MOUSE_BUTTON_LEFT, 1);
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        Log.d(LOG, "double click!");
        runningConnection.sendClick(MOUSE_BUTTON_LEFT, 2);
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (mMultipleTouch)
            runningConnection.sendScroll(distanceX, distanceY);
        else
            runningConnection.sendMove(distanceX, distanceY);
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        //Log.d(LOG, "x: " + (e2.getX() - e1.getX()) + " y: " + (e2.getY() - e1.getY()));
        return false;
    }
}