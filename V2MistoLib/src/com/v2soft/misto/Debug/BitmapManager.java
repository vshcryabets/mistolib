package com.v2soft.misto.Debug;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.util.Log;

public class BitmapManager {
	private static final String LOG_TAG = BitmapManager.class.getSimpleName();
	private static ArrayList<Bitmap> mBitmaps = new ArrayList<Bitmap>();
	private static ArrayList<String> mDescription = new ArrayList<String>();
	
	public static void registerBitmap(Bitmap bitmap, String desc)
	{
		mBitmaps.add(bitmap);
		mDescription.add(desc);
	}
	
	public static void releaseBitmap(Bitmap bitmap)
	{
		bitmap.recycle();
		int idx = mBitmaps.indexOf(bitmap);
		mBitmaps.remove(idx);
		mDescription.remove(idx);
	}
	
	public static void showUsage()
	{
		long size = 0;
		for ( int i = 0; i < mBitmaps.size(); i++) 
		{
			final Bitmap bitmap = mBitmaps.get(i);
			final String desc = mDescription.get(i);
			int bpp = bitmap.getRowBytes()/bitmap.getWidth();
			int s = bitmap.getWidth()*bitmap.getHeight();
			size += s*bpp;
			Log.d(LOG_TAG, "Object "+desc+"  "+s*bpp);
		}
		Log.d(LOG_TAG, "Current memory used for bitmaps = "+size);
	}
}
