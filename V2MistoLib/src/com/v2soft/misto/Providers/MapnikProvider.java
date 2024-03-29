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
package com.v2soft.misto.Providers;

import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Calendar;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.v2soft.FileCacheStorage.FileCache;
import com.v2soft.misto.Debug.BitmapManager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.util.Log;

public class MapnikProvider extends BitmapProvider
{
	//-----------------------------------------------------------------------------------------------
    // Constants
    //-----------------------------------------------------------------------------------------------
	private static final String BASE_HOST = "tile.openstreetmap.org";
	private static final String IMAGE_EXT = ".png";
	//-----------------------------------------------------------------------------------------------
    // Vars
    //-----------------------------------------------------------------------------------------------
	private FileCache mLocalCache;
	//-----------------------------------------------------------------------------------------------
    // Constructors
    //-----------------------------------------------------------------------------------------------	
	public MapnikProvider(Context context)
	{
		super(context);
		mLocalCache = new FileCache(context, "/sdcard/V2MapView/cache/mapnik");
	}
	//-----------------------------------------------------------------------------------------------
    // Provider functions
    //-----------------------------------------------------------------------------------------------		
	
	@Override
	public TileInfo getTileInfoByLocation(Location location, int zoom) 
	{
		TileInfo res = new TileInfo();
		res.setHeight(TILE_SIZE);
		res.setWidth(TILE_SIZE);
		res.setZoom(zoom);
		res.setLatitude((int) Math.floor((1 - Math.log(Math.tan(location.getLatitude() * Math.PI / 180) + 
				1 / Math.cos(location.getLatitude() * Math.PI / 180)) / Math.PI) / 2 * (1 << zoom)));
		double longitude = Math.floor((location.getLongitude() + 180) *worldTilesCount(zoom)/360);
		res.setLongitude((int) longitude);
		return res;
	}

	@Override
	public synchronized boolean prepareTileImage(TileInfo info) 
	{
		try
		{
			String local_name = String.format("%d_%d_%d.png",info.getZoom(), info.getLongitude(), info.getLatitude());
			if (!mLocalCache.isFileInCache(local_name))
			{
				String query = String.format("http://%s/%d/%d/%d%s", BASE_HOST, info.getZoom(),
						info.getLongitude(), info.getLatitude(), IMAGE_EXT);
				Log.d("Uploading tile ", query);
				
				HttpParams httpParameters = new BasicHttpParams();
				// Set the timeout in milliseconds until a connection is established.
				int timeoutConnection = 3000;
				HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
				// Set the default socket timeout (SO_TIMEOUT) 
				// in milliseconds which is the timeout for waiting for data.
				int timeoutSocket = 5000;
				HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
				
				DefaultHttpClient client = new DefaultHttpClient(httpParameters);
				
				HttpGet request = new HttpGet(query);
				
				HttpResponse response = client.execute(request);
				HttpEntity entity = response.getEntity();
				int code = response.getStatusLine().getStatusCode();
				if ( code == 200 )
				{
					Calendar cal = Calendar.getInstance();
					cal.add(Calendar.MONTH, 1);					
					RandomAccessFile out = mLocalCache.addFile(local_name, cal.getTime());
					InputStream in = entity.getContent();
					byte [] buffer = new byte[4096];
					int readed = 0;
					while ( (readed = in.read(buffer))>0)
					{
						out.write(buffer, 0, readed);
					}
					out.close();
					in.close();
				}
			}
			InputStream in = mLocalCache.getFileInputStream(local_name);
			final Bitmap bitmap = BitmapFactory.decodeStream(in);
			BitmapManager.registerBitmap(bitmap, info.toString());
			info.setBitmap(bitmap);
			in.close();
			return true;
		}
		catch (Exception e) 
		{
			Log.d("MapnikProvider::prepareTileImage", e.toString());
		}
		return false;
	}


	@Override
	public TileInfo getTileByOffset(TileInfo mBaseTile, int x, int y) 
	{
		TileInfo res = new TileInfo();
		mBaseTile.copyTo(res);
		res.setLatitude(res.getLatitude()+y);
		res.setLongitude(res.getLongitude()+x);
		return res;
	}
	


}
