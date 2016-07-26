package com.mycompany.myfirstapp;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import java.util.HashMap;

public class MsgProvider extends ContentProvider {
    static final String PROVIDER_NAME   = "bupt.chatRoom.provider";
    static final String URL             = "content://" + PROVIDER_NAME + "/chats";
    static final Uri CONTENT_URI     = Uri.parse(URL);
    static final String _ID             = "_id";

    private static HashMap<String, String> STUDENTS_PROJECTION_MAP;

    static final int CHATS   = 1;
    static final int CHATS_ID = 2;

    static final UriMatcher uriMatcher;
    static{
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "chats", CHATS);
        uriMatcher.addURI(PROVIDER_NAME, "chats/#", CHATS_ID);
    }

    private SQLiteDatabase db;

    // Database specific constant declarations
    static final String DATABASE_NAME       = "College";
    static final String CHATS_TABLE_NAME = "chats";
    static final int    DATABASE_VERSION    = 1;

    static final String CREATE_DB_TABLE     =
            " CREATE TABLE " + CHATS_TABLE_NAME +
                    " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    " msg TEXT NOT NULL, " +
                    " msg_type INTEGER NOT NULL," +
                    " talking_man TEXT NOT NULL," +
                    " seq INTEGER NOT NULL);";

    // Helper class that actually creates and manages the provider's underlying data repository.
    private static class DatabaseHelper extends SQLiteOpenHelper {
        /*构造方法:
        * 第一个参数是 Context，这个没什么好说的，必须要有
          它才能对数据库进行操作。
          第二个参数是数据库名，创建数据库时使用的就是这里指定的名
          称。
          第三个参数允许我们在查询数据的时候返回一个自定义的 Cursor，一般都是传入 null。
          第四个参数表示当前数据库的版本号，可用于对数据库进行升级操作
        * */
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        /*在此创建数据库*/
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_DB_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + CHATS_TABLE_NAME);
            onCreate(db);
        }
    }//end class

    @Override
    public boolean onCreate() {
        Context context = getContext();
        DatabaseHelper dbHelper = new DatabaseHelper(context);

        // Create a write able database which will trigger its creation if it doesn't already exist.
        db = dbHelper.getWritableDatabase();
        return (db == null)? false:true;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // Add a new student record
        long rowID = db.insert( CHATS_TABLE_NAME, "", values);

        // If record is added successfully
        if (rowID > 0) {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);//为该URI加上一个ID
            getContext().getContentResolver().notifyChange(_uri, null);

            return _uri;
        }

        throw new SQLException("Failed to add a record into " + uri);
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(CHATS_TABLE_NAME);

        switch (uriMatcher.match(uri)) {
            case CHATS:
                qb.setProjectionMap(STUDENTS_PROJECTION_MAP);
                break;
            case CHATS_ID:
                qb.appendWhere( _ID + "=" + uri.getPathSegments().get(1));
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        if (sortOrder == null || sortOrder == ""){
            // By default sort
            sortOrder = "seq";
        }
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        // register to watch a content URI for changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)){
            case CHATS:
                count = db.delete(CHATS_TABLE_NAME, selection, selectionArgs);
                break;
            case CHATS_ID:
                String id = uri.getPathSegments().get(1);
                count = db.delete( CHATS_TABLE_NAME, _ID + " = " + id +
                        (!TextUtils.isEmpty(selection) ? " AND (" +
                                selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)){
            case CHATS:
                count = db.update(CHATS_TABLE_NAME, values, selection, selectionArgs);
                break;
            case CHATS_ID:
                count = db.update(CHATS_TABLE_NAME, values, _ID +
                        " = " + uri.getPathSegments().get(1) + (!TextUtils.isEmpty(selection) ? " AND (" +
                        selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri );
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)){
            // Get all student records
            case CHATS:
                return "vnd.android.cursor.dir/vnd.chatRoom.message";

            // Get a particular student
            case CHATS_ID:
                return "vnd.android.cursor.item/vnd.chatRoom,message";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }
}
