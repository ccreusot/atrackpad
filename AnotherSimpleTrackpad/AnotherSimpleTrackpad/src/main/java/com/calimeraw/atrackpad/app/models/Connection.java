package com.calimeraw.atrackpad.app.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by cedric.creusot on 21/03/14.
 * class Connection
 * desc: Class used as a structure containing the information to connect to the server
 */
public class Connection implements Parcelable {
    public long id;
    public String name;
    public String ip;
    public int port;

    public static final Parcelable.Creator<Connection> CREATOR = new Parcelable.Creator<Connection>() {
        @Override
        public Connection createFromParcel(Parcel source) {
            return new Connection(source);
        }

        @Override
        public Connection[] newArray(int size) {
            return new Connection[0];
        }
    };

    public Connection(long id, String name, String ip, int port) {
        this.id = id;
        this.name = name;
        this.ip = ip;
        this.port = port;
    }

    public Connection(String name, String ip, int port) {
        this.name = name;
        this.ip = ip;
        this.port = port;
    }

    public Connection(Parcel source) {
        id = source.readLong();
        name = source.readString();
        ip = source.readString();
        port = source.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(ip);
        dest.writeInt(port);
    }
}
