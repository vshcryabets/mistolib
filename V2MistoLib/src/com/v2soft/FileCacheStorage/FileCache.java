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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Date;
import android.content.Context;

public class FileCache 
{	
	private static final String DB_FILE_NAME = "cache";
	private DataHelper mDataBase;
	private String mCasheDirectory;
	
	/**
	 * 
	 * @param context current application context
	 * @param cache_directory Cache directory. It will be created if not exist
	 */
	public FileCache(Context context, String cache_directory)
	{
		initCacheDirectory(cache_directory);
		initDBFile(context, DB_FILE_NAME);
	}

	private void initCacheDirectory(String cache_directory)
	{
		mCasheDirectory = cache_directory;
		File file = new File(cache_directory);
		if ( !file.exists())
			file.mkdirs();
	}

	private void initDBFile(Context context, String database_name) 
	{
		mDataBase = new DataHelper(context, database_name);
		clearCacheAsync();
	}

	/**
	 * Add new file to cache
	 * @param filename new file name
	 * @param validtime valid time of file
	 * @return RandomAccessFile object
	 * @throws FileNotFoundException - this should never happens
	 */
	public synchronized RandomAccessFile addFile(String filename, Date validtime) throws FileNotFoundException
	{
		RandomAccessFile file = new RandomAccessFile(mCasheDirectory+File.separator+filename, "rw");
		mDataBase.add(filename,validtime);
		return file;
	}
	
	public RandomAccessFile getFile(String filename) throws FileNotFoundException
	{
		if ( !isFileInCache(filename))
			throw new FileNotFoundException("There is no "+filename);
		return new RandomAccessFile(mCasheDirectory+
				File.separator+filename, "rw");		
	}

	public InputStream getFileInputStream(String filename) throws FileNotFoundException
	{
		if ( !isFileInCache(filename))
			throw new FileNotFoundException("There is no "+filename);
		return new FileInputStream(mCasheDirectory+
				File.separator+filename);		
	}

	/**
	 * Delete file from cache
	 * @param filename name of file that should be deleted
	 */
	public void deleteFile(String filename)
	{
		File file = new File(mCasheDirectory+File.separator+filename);
		if ( file.exists() )
		{
			file.delete();
		}
		mDataBase.delete(filename);
		return;
	}

	/**
	 * Remove old files, this is asynchronious call
	 */
	public void clearCacheAsync()
	{
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() 
			{
				clearCache();
			}
		}, "FileCache clear thread");
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
	}
	
	/**
	 * Remove old files from cache
	 */
	public void clearCache()
	{
		ArrayList<String> files = mDataBase.getOldFiles(new Date());
		for (String string : files) 
		{
			deleteFile(string);
		}
	}
	
	public void removeAll()
	{
		// TODO: realize
	}
	
	public void updateFile(String filename, Date validtime)
	{
		// TODO: realize
	}

	/**
	 * This function check that specified file are already in cache
	 * @param local_name file name
	 * @return true if file present in cache
	 */
	public synchronized boolean isFileInCache(String local_name) 
	{
		File file = new File( mCasheDirectory + File.separator + local_name);
		if ( !file.exists() )
		{
			if ( mDataBase.isInCache(local_name) )
			{
				// file not exists in cache, but record present, remove it from DB
				mDataBase.delete(local_name);
			}
			return false;
		}
		return mDataBase.isInCache(local_name);
	}
}
