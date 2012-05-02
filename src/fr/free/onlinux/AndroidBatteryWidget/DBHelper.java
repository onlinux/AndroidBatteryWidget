package fr.free.onlinux.AndroidBatteryWidget;

import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DBHelper {
	private final static String TAG = "BAT-S DB";
	private static final String DATABASE_NAME  = "onlinux";
    
    private static final String TABLE_QUEUE = "battery";
    
    private static final String TABLE_QUEUE_CREATE =
    	"create table " + TABLE_QUEUE + " ("
    	+ "id      integer primary key autoincrement, " 
    	+ "rtime TIMESTAMP NOT NULL DEFAULT current_timestamp, "
    	+ "rectime biginteger       not null,"
    	+ "level   unsigned integer not null,"
    	+ "status   integer ,"
    	+ "plugged integer   "
    	+ ");";
    
    public static final String DELETEOLD =
    	"delete from " + TABLE_QUEUE + " where rtime < date('now', '-3 days');";

    
    private SQLiteDatabase db = null;
    private Context mCtx      = null;
    private static Boolean debug = false;
    
    public DBHelper ( Context ctx )
    {
    	mCtx = ctx;
    }

    public void record( int level , int status, int plugged )
    {
    	SQLiteDatabase db = getDatabase();
    	
    	ContentValues values = new ContentValues();
    	values.put( "rectime", Calendar.getInstance().getTimeInMillis() );
    	values.put( "level",   level );
    	values.put( "status", status);
    	values.put( "plugged", plugged);
    	
    	db.insert( TABLE_QUEUE, null, values );
    	Log.i(TAG,"---------- record " + level);
    }
    
    public void deleteUpTo( int id )
    {
    	if (debug) Log.i(TAG,"---------- deleteUpTo " + id);
    	SQLiteDatabase db = getDatabase();
    	db.delete( TABLE_QUEUE, "id <= ?", new String[]{""+id});
    }

    public Cursor read()
    {
/*    	CREATE TABLE current_list (
    		       item_id INTEGER NOT NULL,
    		       added_on TIMESTAMP NOT NULL DEFAULT current_timestamp,
    		       added_by VARCHAR(50) NOT NULL,
    		       quantity INTEGER NOT NULL,
    		       units VARCHAR(50) NOT NULL,
    		       CONSTRAINT current_list_pk PRIMARY KEY (item_id)
    		);*/

//    	 Cursor cursor = database.rawQuery(
//    	         "SELECT item_id AS _id," +
//    	         " (strftime('%s', added_on) * 1000) AS added_on," +
//    	         " added_by, quantity, units" +
//    	         " FROM current_list", new String[0]);
//    	 long millis = cursor.getLong(cursor.getColumnIndexOrThrow("added_on"));
//    	 Date addedOn = new Date(millis);

    	
    	if (debug) Log.i(TAG,"---------- read");
    	Cursor c = getDatabase().rawQuery(
   	         "SELECT id," +
   	         " (strftime('%s', rtime) * 1000) AS rtime," +
   	         " level, status, plugged" +
   	         " FROM battery", new String[0]);
    	
//    	Cursor c = getDatabase().query(
//				TABLE_QUEUE,
//				new String[] { "id", "rectime", "level" , "status", "plugged" },
//				null, null, null, null, null, null
//		);
    	return c;
    }
    
    public void close() {
    	if (debug) Log.i(TAG,"---------- close DB");
    	if( db == null ) return;
    	
    	try {
    		db.close();
        } catch (SQLException e){
   	    }
        db = null;
    }
    
    protected SQLiteDatabase getDatabase ()
    {
    	if (debug) Log.i(TAG,"---------- getDatabase");
    	if( db == null )
    	{
    		try {
				db = mCtx.openOrCreateDatabase( DATABASE_NAME, 0, null );
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.e(TAG,"---------- getDatabase error " + e);
				e.printStackTrace();
				return null;
			}
    		if( !tableExists( TABLE_QUEUE ) ) createTable( TABLE_QUEUE_CREATE );
    	}
    	return db;
    }
    
    private boolean tableExists ( String table )
    {
    	boolean result = false;
    	try {
    		Cursor c = getDatabase().query(
    				"sqlite_master",           new String[]{ "name" },
    				"type='table' and name=?", new String[]{ table  },
    				null, null, null, null
    		);
    		result = c.getCount() == 0 ? false : true;
    		c.close();
    	} catch ( Exception e ) {
    	}
    	if (debug) Log.i(TAG,"---------- tableExists " + result);
    	return result;
    }
    
    public  void deleteOldEntries () {
    	createTable(DELETEOLD);
    }
    
    private void createTable ( String sql )
    {	
    	if (debug) Log.i(TAG,"---------- sql: " + sql);
    	try {
    		getDatabase().execSQL( sql );
		}
    	catch( SQLException e)
		{
    	}
    }

}
