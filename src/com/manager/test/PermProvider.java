
package com.manager.test;

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
import android.util.Log;

public class PermProvider extends ContentProvider {
	private SQLiteOpenHelper mOpenHelper;
	
    public static final Uri CONTENT_URI =
                Uri.parse("content://com.manager.test/permission");
    
	private static final int PERMISSIONS = 1;
    private static final int PERMISSIONS_ID = 2;
    private static final UriMatcher sURLMatcher = new UriMatcher(
            UriMatcher.NO_MATCH);

    static {
    	sURLMatcher.addURI("com.manager.test", "permission", PERMISSIONS);
        sURLMatcher.addURI("com.manager.test", "permission/#", PERMISSIONS_ID);
    }
	
    public PermProvider() {
    }
    
    @Override
    public boolean onCreate() {
        mOpenHelper = new PermDataHelper(getContext());
        return true;
    }
    
    @Override
    public Cursor query(Uri url, String[] projectionIn, String selection,
            String[] selectionArgs, String sort) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        // Generate the body of the query
        int match = sURLMatcher.match(url);
        switch (match) {
        	case PERMISSIONS:
        		qb.setTables("deny_tb");
        		break;
            case PERMISSIONS_ID:
                qb.setTables("deny_tb");
                qb.appendWhere("_id=");
                qb.appendWhere(url.getPathSegments().get(1));
                break;
            default:
                throw new IllegalArgumentException("Unknown URL " + url);
        }

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor ret = qb.query(db, projectionIn, selection, selectionArgs,
                              null, null, sort);

        if (ret == null) {
            Log.d("ven","Alarms.query: failed");
        } else {
            ret.setNotificationUri(getContext().getContentResolver(), url);
        }
        return ret;
    }
	
    @Override
    public String getType(Uri url) {
        int match = sURLMatcher.match(url);
        switch (match) {
            case PERMISSIONS:
                return "vnd.android.cursor.dir/deny_tb";
            case PERMISSIONS_ID:
                return "vnd.android.cursor.item/deny_tb";
            default:
                throw new IllegalArgumentException("Unknown URL");
        }
    }
    
    @Override
    public int update(Uri url, ContentValues values, String where, String[] whereArgs) {
        int count;
        long rowId = 0;
        int match = sURLMatcher.match(url);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        switch (match) {
            case PERMISSIONS_ID: {
                String segment = url.getPathSegments().get(1);
                rowId = Long.parseLong(segment);
                count = db.update("deny_tb", values, "_id=" + rowId, null);
                break;
            }
            default: {
                throw new UnsupportedOperationException(
                        "Cannot update URL: " + url);
            }
        }
        Log.d("ven","notifyChange() rowId: " + rowId + " url " + url);
        getContext().getContentResolver().notifyChange(url, null);
        return count;
    }

    @Override
    public Uri insert(Uri url, ContentValues initialValues) {
        if (sURLMatcher.match(url) != PERMISSIONS) {
            throw new IllegalArgumentException("Cannot insert into URL: " + url);
        }

        ContentValues values = new ContentValues(initialValues);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert("deny_tb", null, values);
        if (rowId < 0) {
            throw new SQLException("Failed to insert row into " + url);
        }
        Log.d("ven","Added to deny_tb rowId = " + rowId);

        Uri newUrl = ContentUris.withAppendedId(CONTENT_URI, rowId);
        getContext().getContentResolver().notifyChange(newUrl, null);
        return newUrl;
    }

    public int delete(Uri url, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        long rowId = 0;
        switch (sURLMatcher.match(url)) {
            case PERMISSIONS:
                count = db.delete("deny_tb", where, whereArgs);
                break;
            case PERMISSIONS_ID:
                String segment = url.getPathSegments().get(1);
                rowId = Long.parseLong(segment);
                if (TextUtils.isEmpty(where)) {
                    where = "_id=" + rowId;
                } else {
                    where = "_id=" + rowId + " AND (" + where + ")";
                }
                count = db.delete("deny_tb", where, whereArgs);
                break;
            default:
                throw new IllegalArgumentException("Cannot delete from URL: " + url);
        }

        getContext().getContentResolver().notifyChange(url, null);
        return count;
    }
	
	private class PermDataHelper extends SQLiteOpenHelper {  
		
		static final String dbName = "deny_perm.db";
		static final String denyTable="deny_tb";
		static final int VERSION = 1;
		public PermDataHelper(Context context) {
			super(context, dbName, null, VERSION);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			String sql = "CREATE TABLE " + denyTable + 
					" (_id INTEGER PRIMARY KEY autoincrement, package TEXT,permission TEXT)";
			db.execSQL(sql);
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			String sql = " DROP TABLE IF EXISTS " + denyTable;
			db.execSQL(sql);
			onCreate(db);
		}
		
	}
}