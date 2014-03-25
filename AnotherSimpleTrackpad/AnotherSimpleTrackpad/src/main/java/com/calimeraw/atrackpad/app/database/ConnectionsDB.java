package com.calimeraw.atrackpad.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.calimeraw.atrackpad.app.models.Connection;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cedric.creusot on 21/03/14.
 * class ConnectionsDB
 * desc: Small database to save the connection profile
 */
public class ConnectionsDB extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "connections";
    private static final String DATABASE_CREATE_TABLE =
            "CREATE TABLE " + DATABASE_NAME +
            " (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, ip TEXT, port INTEGER);";

    public ConnectionsDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void addConnection(Connection connection) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", connection.name);
        contentValues.put("ip", connection.ip);
        contentValues.put("port", connection.port);

        SQLiteDatabase dbw = getWritableDatabase();
        assert dbw != null; // dbw should not be null.
        dbw.insert(DATABASE_NAME, null, contentValues);
    }

    public void updateConnection(Connection connection, Connection update) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", connection.name);
        contentValues.put("ip", connection.ip);
        contentValues.put("port", connection.port);

        SQLiteDatabase dbw = getWritableDatabase();
        assert dbw != null; // dbw should not be null.
        dbw.update(DATABASE_NAME, contentValues, "_id=?", new String[] {String.valueOf(connection.id)});
    }

    public void removeConnection(Connection connection) {
        SQLiteDatabase dbw = getWritableDatabase();
        assert  dbw != null;
        dbw.delete(DATABASE_NAME, "_id=?", new String[] {Long.toString(connection.id)});
    }

    public List<Connection> getConnections() {
        List<Connection> connections = new ArrayList<Connection>();

        SQLiteDatabase dbr = getReadableDatabase();
        assert dbr != null; // dbr shouwld not be null.
        Cursor cursor = dbr.query(DATABASE_NAME, new String[] {"_id","name", "ip", "port"}, null, null, null, null, "name");
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                connections.add(new Connection(cursor.getLong(0), cursor.getString(1), cursor.getString(2), cursor.getInt(3)));
            }
        }
        cursor.close();
        return connections;
    }
}
