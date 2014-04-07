package com.calimeraw.atrackpad.app.networks;

import android.os.Bundle;
import android.util.Log;
import com.calimeraw.atrackpad.app.models.Connection;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by cedric.creusot on 07/04/14.
 * class NetworkTrackpadClient
 * desc: This classes is use to manage the connection with the remote control server.
 */
public class NetworkTrackpadClient extends Thread {
    private static final String LOG = NetworkTrackpadClient.class.getName();
    private Socket mSocket;
    private Connection mConnection;
    private Queue<Packet> mMessageQueue;
    private boolean mRunning;
    private DataOutputStream mOos;

    public NetworkTrackpadClient(Connection connection) {
        mConnection = connection;
        mMessageQueue = new ConcurrentLinkedQueue<Packet>();
    }

    @Override
    public void run() {
        mRunning = true;
        try {
            mSocket = new Socket(mConnection.ip, mConnection.port);
            mOos = new DataOutputStream(mSocket.getOutputStream());
            while (mRunning) {
                while (!mMessageQueue.isEmpty()) {
                    Packet packet = mMessageQueue.poll();
                    mOos.writeInt(packet.getOffset() + 4);
                    mOos.write(packet.value());
                    packet.release();
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
        try {
            Packet packet = new Packet()
                    .writeInt(1) // cmd
                    .writeFloat(x)
                    .writeFloat(y);
            mMessageQueue.offer(packet);
        } catch (IOException e) {
            Log.e(LOG, e.getMessage(), e);
        }
    }

    public void toSendClick(int button) {
        if (!mRunning) return;
        try {
            Packet packet = new Packet()
                    .writeInt(2) // cmd
                    .writeInt(button);
            mMessageQueue.offer(packet);
        } catch (IOException e) {
            Log.e(LOG, e.getMessage(), e);
        }
    }

    public void toSendScroll(float x, float y) {
        if (!mRunning) return;
        try {
            Packet packet = new Packet()
                    .writeInt(3)
                    .writeFloat(x)
                    .writeFloat(y);
            mMessageQueue.offer(packet);
        } catch (IOException e) {
            Log.e(LOG, e.getMessage(), e);
        }
    }

    public void end() {
        mRunning = false;
    }
}