package com.calimeraw.atrackpad.app.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.*;
import com.calimeraw.atrackpad.app.R;
import com.calimeraw.atrackpad.app.models.Connection;

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
public class TrackpadFragment extends Fragment implements GestureDetector.OnGestureListener, View.OnTouchListener {

    private static final String  LOG = TrackpadFragment.class.getName();

    private GestureDetectorCompat mGesture;
    private Connection mConnection;
    private RunningConnection runningConnection;

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
        runningConnection = new RunningConnection(mConnection);
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

    private long mTimePress;
    private boolean mMultipleTouch;

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
                        runningConnection.toSendClick(2);
                        mTimePress = 0;
                    }
                    break;
                default:
                    mGesture.onTouchEvent(event);
                    mTimePress = event.getEventTime() + 500;
                    break;
            }
        }
        else {
            mGesture.onTouchEvent(event);
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
        Log.d(LOG, "click!");
        runningConnection.toSendClick(1);
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        Log.d(LOG, "x: " + (distanceX) + " y: " + (distanceY));
        if (mMultipleTouch)
            runningConnection.toSendScroll(distanceX, distanceY);
        else
            runningConnection.toSendMove(distanceX, distanceY);
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

    public static class RunningConnection extends Thread {
        private static final String LOG = RunningConnection.class.getName();
        private Socket mSocket;
        private Connection mConnection;
        private Queue<Bundle> mMessageQueue;
        private boolean mRunning;
        private DataOutputStream mOos;

        public RunningConnection(Connection connection) {
            mConnection = connection;
            mMessageQueue = new ConcurrentLinkedQueue<Bundle>();
        }

        @Override
        public void run() {
            mRunning = true;
            try {
                mSocket = new Socket(mConnection.ip, mConnection.port);
                mOos = new DataOutputStream(mSocket.getOutputStream());
                while (mRunning) {
                    while (!mMessageQueue.isEmpty()) {
                        Bundle toSend = mMessageQueue.poll();
                        int cmd = toSend.getInt("cmd");
                        switch (cmd) {
                            case 1:
                                mOos.writeInt(16);
                                mOos.writeInt(cmd);
                                mOos.writeFloat(toSend.getFloat("x"));
                                mOos.writeFloat(toSend.getFloat("y"));
                                break;
                            case 2:
                                mOos.writeInt(12);
                                mOos.writeInt(cmd);
                                mOos.writeInt(toSend.getInt("button"));
                                break;
                            case 3:
                                mOos.writeInt(16);
                                mOos.writeInt(cmd);
                                mOos.writeFloat(toSend.getFloat("x"));
                                mOos.writeFloat(toSend.getFloat("y"));
                                break;
                        }
                        mOos.flush();
                    }
                }
                mSocket.close();
            } catch (IOException e) {
                Log.e(LOG, e.getMessage(), e);
            } finally {
                mRunning = false;
                Log.d(LOG, "stopping connection");
            }
        }

        public void toSendMove(float x, float y) {
            if (!mRunning) return;
            Bundle bundle = new Bundle();
            bundle.putInt("cmd", 1);
            bundle.putFloat("x", x);
            bundle.putFloat("y", y);
            mMessageQueue.offer(bundle);
        }

        public void toSendClick(int button) {
            if (!mRunning) return;
            Bundle bundle = new Bundle();
            bundle.putInt("cmd", 2);
            bundle.putInt("button", button);
            mMessageQueue.offer(bundle);
        }

        public void toSendScroll(float x, float y) {
            if (!mRunning) return;
            Bundle bundle = new Bundle();
            bundle.putInt("cmd", 3);
            bundle.putFloat("x", x);
            bundle.putFloat("y", y);
            mMessageQueue.offer(bundle);
        }

        public void end() {
            mRunning = false;
        }
    }

}