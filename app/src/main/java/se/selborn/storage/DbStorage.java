package se.selborn.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import java.util.ArrayList;
import java.util.Date;

import com.google.gson.Gson;

import se.selborn.gps.Position;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.os.Environment;
import android.util.Log;

public class DbStorage extends SQLiteOpenHelper {
 

	private static final String DB_NAME = "livelog.db";
	private static final String TB_NAME = "livelog";
	private static final int DATABASE_VERSION = 2;
	
	private static final String CREATE_PASS_TBL = "CREATE TABLE pass (guid varchar(50) not null, starttime long, stoptime long, length double, avgspeed double);";
	private static final String CREATE_DB = 
			"CREATE TABLE livelog (Gpslatitude DOUBLE NOT NULL , Gpslongitude DOUBLE NOT NULL, Gpsspeed DOUBLE NOT NULL , GpsTime long NOT NULL , GpsAltitude INTEGER, pulse INTEGER, GpsBearing INTEGER, logCreatedAt long, guid VARCHAR NOT NULL );";
	
	//Backuptable
	private static final String CREATE_DB_BAK = 
			"CREATE TABLE livelogbak (Gpslatitude DOUBLE NOT NULL , Gpslongitude DOUBLE NOT NULL, Gpsspeed DOUBLE NOT NULL , GpsTime long NOT NULL , GpsAltitude INTEGER, pulse INTEGER, GpsBearing INTEGER, logCreatedAt long, guid VARCHAR NOT NULL );";
	
	//private static final String CREATE_DB = "CREATE TABLE livelog (Gpslatitude DOUBLE NOT NULL , Gpslongitude DOUBLE NOT NULL)";
 
	private static Context _myContext;
	
	private static SQLiteDatabase mDbSqlite;
	
	

	public DbStorage(Context context) {
		
		super(context, DB_NAME, null, DATABASE_VERSION);
		
		_myContext = context;
		mDbSqlite = getWritableDatabase();
		//onUpgrade(mDbSqlite, 2, 3);
		//isDatabaseExist();
		
		
	}

	@Override
	public void onCreate(SQLiteDatabase database) {

		database.execSQL("DROP TABLE IF EXISTS pass");
		database.execSQL("DROP TABLE IF EXISTS livelog");
		database.execSQL("DROP TABLE IF EXISTS livelogbak");
		
		database.execSQL(CREATE_PASS_TBL);
		database.execSQL(CREATE_DB);
		database.execSQL(CREATE_DB_BAK);
	}
	
	
	public static int getRowCount() {
		return 0;
	}
	
	
	//To get positions and move gotten positions to backup table.
	public static ArrayList<Position> getPositions(int noOfRecorsReturned, String currentGuid) {
		
		ArrayList<Position> retPositions = new ArrayList<Position>(noOfRecorsReturned);
		String limit = String.format("limit %s", String.valueOf(noOfRecorsReturned));
		//Cursor cur = _db.query("livelog", null, " guid = ? limit 5;", new String[] { currentGuid }, null, null, null);
		
		Cursor cur = mDbSqlite.query("livelog", null, String.format(" guid = ? %s;", limit), new String[] { currentGuid }, null, null, null);
		
		
		
		if (cur.getCount() < 1) {
			cur.close();
		} else {
			cur.moveToFirst();
			while (cur.moveToNext()) {
				
				Position aPos = new Position();
				
				aPos.setLatitude(cur.getDouble(0));
				aPos.setLongitude(cur.getDouble(1));
				aPos.setSpeed(cur.getDouble(2));
				aPos.setGpsDateTime(cur.getLong(3));
				
				
				retPositions.add(aPos);
				
			}
			
		}
		
		
		
		return retPositions;
	}
	
	public static ArrayList<Pass> getPassess() {
		ArrayList<Pass> myPasses = new ArrayList<Pass>();
		
		Cursor cur = mDbSqlite.query("pass", null, null, null, null, null,null); 
		
		Pass ps = new Pass();
		
		if (cur.getCount() > 1) {
			cur.moveToFirst();
			
			while (cur.moveToNext()) {
				
				ps.setGuid(cur.getString(0));
				ps.setStartTime(new Date(cur.getLong(1)));
				ps.setStopTime(new Date(cur.getLong(2)));
				ps.setDistance(cur.getDouble(3));
				ps.setAvgspeed(cur.getDouble(4));
				
				myPasses.add(ps);
				
			}
		}
		
		return myPasses;
	}
	
	
	
	//Just creating a new entry.
	public static void createEntry(String guid) {
		//guid varchar not null, starttime datetime, stoptime datetime, length double, avgspeed double
		
		long startTime = new Date().getTime();
		
		ContentValues cvValues = new ContentValues();
		cvValues.put("guid", guid);
		cvValues.put("starttime", startTime);
		cvValues.put("stoptime", startTime);
		cvValues.put("length", 1010293.3);
		cvValues.put("avgSpeed", 32.2);
		
		
		long result = mDbSqlite.insert("pass", null, cvValues);
		
		
	}
	
	//Update stoptime and other paramteters?
	public static void updateEntry(String guid, double distance, double avgSpeed) {
		long stopTime = new Date().getTime();
		
		ContentValues cvValues = new ContentValues();
		cvValues.put("stoptime", stopTime);
		cvValues.put("distance", distance);
		cvValues.put("avgspeed", avgSpeed);
		
		String where = "guid=?";
		String[] whereargs = new String[] { guid };
		
		
		int upd = mDbSqlite.update("livelog", cvValues, where, whereargs);
		
	}
	
	public static synchronized void savePosition(Location loc, String guid) {
		
		ContentValues cvValues = new  ContentValues();
		
		cvValues.put("Gpslatitude", loc.getLatitude());
		cvValues.put("Gpslongitude", loc.getLongitude());
		cvValues.put("Gpsspeed", loc.getSpeed());
		cvValues.put("GpsTime", loc.getTime());
		cvValues.put("GpsAltitude", loc.getAltitude());
		cvValues.put("GpsBearing", loc.getBearing());
		
		cvValues.put("logCreatedat", new Date().getTime());
		cvValues.put("guid", guid);
		
		
		long result = mDbSqlite.insert("livelog", null, cvValues);
		
		cvValues.clear();
	}

	//Returns bytearray with data to send to server. Also 
	public synchronized static byte[] getDBContent(String currentGuid) {
	
		Gson gson = new Gson();
		byte[] byteData = null;
		//All records for a guid 
		Cursor cur = mDbSqlite.query("livelog", null, String.format(" guid = ? %s;"), new String[] { currentGuid }, null, null, null);
		
		ArrayList<Position> positions = new ArrayList<Position>();
		
		try {
		
			if (cur.getCount() < 1) {
				cur.close();
				return null;
			} else {
				cur.moveToFirst();
				while (cur.moveToNext()) {
					
					Position aPos = new Position();
					
					aPos.setLatitude(cur.getDouble(0));
					aPos.setLongitude(cur.getDouble(1));
					aPos.setSpeed(cur.getDouble(2));
					aPos.setGpsDateTime(cur.getLong(3));
					
					positions.add(aPos);
				}
				
			}
			
			byteData = String.valueOf( new StringBuilder().append(gson.toJson(positions))).getBytes();
		
			String sqlCommand = String.format("INSERT INTO livelogbak SELECT * FROM livelog where guid = '%s' ", currentGuid);
			mDbSqlite.execSQL(sqlCommand);
			
			sqlCommand = String.format("DELETE FROM livelog where guid = '%s'", currentGuid);
			mDbSqlite.execSQL(sqlCommand);
			
		} catch (SQLiteException ep ) {
			ep.printStackTrace();
		} catch (Exception ep) {
			ep.printStackTrace();
		}
		
		
		return byteData;
		
	}
	
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(DbStorage.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TB_NAME);
		onCreate(db);
	}

	
	//Exporting SQLite db to storageCard.
	public boolean exportDBToStorageCard() {
		
		
		FileChannel source=null, destination=null;
		String dbPath = mDbSqlite.getPath();
		
		File currentDB = new File( mDbSqlite.getPath());
		File backupDB = new File (Environment.getExternalStorageDirectory(), "livelog.db");
		
		try {
			source = new FileInputStream(currentDB).getChannel();
			destination = new FileOutputStream(backupDB).getChannel();
			long bts = destination.transferFrom(source, 0, source.size());
			source.close();
			destination.close();
		} catch (IOException ep) {
			ep.printStackTrace();
			return false;
		}
		
		return true;
	}

}
