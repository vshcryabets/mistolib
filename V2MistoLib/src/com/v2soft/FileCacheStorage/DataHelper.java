// ***** BEGIN LICENSE BLOCK *****
// Version: MPL 1.1
// 
// The contents of this file are subject to the Mozilla Public License Version 
// 1.1 (the "License"); you may not use this file except in compliance with 
// the License. You may obtain a copy of the License at 
// http://www.mozilla.org/MPL/
// 
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
// for the specific language governing rights and limitations under the
// License.
// 
// The Initial Developer of the Original Code is 
//	2V Software (vshcryabets@2vsoft.com).
// Portions created by the Initial Developer are Copyright (C) 2010
// the Initial Developer. All Rights Reserved.
// 
// 
// ***** END LICENSE BLOCK *****
package com.v2soft.FileCacheStorage;

import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataHelper
{
	private static final long SCALE = 1000;
	private static final String TAG = "com.v2soft.FileCacheStorage.DataHelper";
	private static final int DATABASE_VERSION = 1;
	private static final String TABLE_FILES = "files";
	
    private static final String DATABASE_CREATE =
        "create table files (name varchar(128) primary key, "
        + "validthru int);"; 
	
    private SQLiteStatement insertStmt;
	private static final String INSERT_FILE = "insert into "
		   + TABLE_FILES + "(name, validthru) values (?, ?)";
    private SQLiteStatement deleteStmt;
	private static final String DELETE_FILE = "delete from "
		   + TABLE_FILES + " WHERE name=?";
	
    private OwnOpenHelper mOpenHelper;
    private SQLiteDatabase mDatabase;
	
	class OwnOpenHelper extends SQLiteOpenHelper
	{
		public OwnOpenHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase arg0) 
		{
			arg0.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase arg0, int oldVersion, int newVersion) 
		{
	        Log.w(TAG, "Upgrading database from version " + oldVersion 
	                + " to "
	                + newVersion + ", which will destroy all old data");
	          arg0.execSQL("DROP TABLE IF EXISTS "+TABLE_FILES);
	          onCreate(arg0);
		}		
	}
    
	public DataHelper(Context context,String database_name)
	{
		mOpenHelper = new OwnOpenHelper(context, database_name, null, DATABASE_VERSION);
		mDatabase = mOpenHelper.getWritableDatabase();
		insertStmt = mDatabase.compileStatement(INSERT_FILE);
		deleteStmt = mDatabase.compileStatement(DELETE_FILE);
	}

	public long add(String filename, Date validtime) 
	{
		insertStmt.bindString(1, filename);
		insertStmt.bindLong(2, validtime.getTime()/SCALE);
		return insertStmt.executeInsert();
	}

	public void delete(String filename) 
	{
		deleteStmt.bindString(1, filename);
		deleteStmt.execute();
	}

	public ArrayList<String> getOldFiles(Date date) 
	{
		long time = date.getTime()/SCALE;
		Cursor cursor = mDatabase.query(TABLE_FILES, new String[]{"name"}, 
				"validthru<?", new String[]{time+""}, null, null, null);
		ArrayList<String> result = new ArrayList<String>();
		if ( cursor.moveToFirst() )
		{
			do
			{
				String name = cursor.getString(0);
				result.add(name);
			}
			while (cursor.moveToNext());
		}
		if (cursor != null && !cursor.isClosed())
		{
			cursor.close();
		}
		return result;
	}

	public boolean isInCache(String local_name) 
	{
		boolean result = false;
		Cursor cursor = mDatabase.query(TABLE_FILES, new String[]{"count(*)"}, 
				"name=?", new String[]{local_name}, null, null, null);
		if ( cursor.moveToFirst() )
			result = true;
		if (cursor != null && !cursor.isClosed())
		{
			cursor.close();
		}
		return result;
	}
}
