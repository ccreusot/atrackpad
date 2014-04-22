package com.calimeraw.atrackpad.app.networks;

import android.util.Log;
import com.calimeraw.atrackpad.app.models.Connection;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by cedric.creusot on 07/04/14.
 * class NetworkTrackpadClient
 * desc: This classes is use to manage the connection with the remote control server.
 */
public class NetworkTrackpadClient extends Thread {
    private static final String LOG = NetworkTrackpadClient.class.getName();

    // Flag for the packet to send
    private static final int PACKET_MOVE = 1;
    private static final int PACKET_CLICK = 2;
    private static final int PACKET_SCROLL = 3;

    private Socket mSocket;
    private Connection mConnection;
    private BlockingQueue<Packet> mMessageQueue;
    private boolean mRunning;
    private DataOutputStream mOos;

    public NetworkTrackpadClient(Connection connection) {
        mConnection = connection;
        mMessageQueue = new ArrayBlockingQueue<Packet>(128);
    }

    @Override
    public void run() {
        mRunning = true;
        try {
            mSocket = new Socket(mConnection.ip, mConnection.port);
            mOos = new DataOutputStream(mSocket.getOutputStream());
            while (mRunning) {
                    // We're waiting 1000 milliseconds for a packet to send to be available.
                    Packet packet = mMessageQueue.poll(1000, TimeUnit.MILLISECONDS);
                    if (packet != null) {
                        mOos.writeInt(packet.getOffset() + 4);
                        mOos.write(packet.value());
                        packet.release();
                        mOos.flush();
                    }
            }
            mSocket.close();
        } catch (IOException e) {
            Log.e(LOG, e.getMessage(), e);
        } catch (InterruptedException e) {
            Log.e(LOG, e.getMessage(), e);
        } finally {
            mRunning = false;
            Log.d(LOG, "stopping connection");
        }
    }

    // This method will add the packet to send to the queue.
    public void sendPacket(Packet toSend) {
        if (!mRunning) return;
        try {
            mMessageQueue.put(toSend);
        } catch (InterruptedException e) {
            Log.e(LOG, e.getMessage(), e);
        }
    }

    /**
     * Send a move packet from the client
     * @param distanceX, the distance x when the scroll event or the move event occur
     * @param distanceY, the distance y when the scroll event or the move event occur
     */
    public void sendMove(float distanceX, float distanceY) {
        try {
            Packet packet = new Packet()
                    .writeInt(PACKET_MOVE) // cmd
                    .writeFloat(distanceX)
                    .writeFloat(distanceY);
            sendPacket(packet);
        } catch (IOException e) {
            Log.e(LOG, e.getMessage(), e);
        }
    }

    /**
     * Send a click packet
     * @param button, this is correspond to the button number on the mouse 1 for left, 2 for right
     * @param nb_click, the number of click to send
     */
    public void sendClick(int button, int nb_click) {
        try {
            Packet packet = new Packet()
                    .writeInt(PACKET_CLICK) // cmd
                    .writeInt(button)
                    .writeInt(nb_click);
            sendPacket(packet);
        } catch (IOException e) {
            Log.e(LOG, e.getMessage(), e);
        }
    }

    /**
     * This packet looks like the moving packet except it's used as a scroll event.
     * @param distanceX
     * @param distanceY
     */
    public void sendScroll(float distanceX, float distanceY) {
        try {
            Packet packet = new Packet()
                    .writeInt(PACKET_SCROLL)
                    .writeFloat(distanceX)
                    .writeFloat(distanceY);
            sendPacket(packet);
        } catch (IOException e) {
            Log.e(LOG, e.getMessage(), e);
        }
    }

    public void end() {
        mRunning = false;
    }
}