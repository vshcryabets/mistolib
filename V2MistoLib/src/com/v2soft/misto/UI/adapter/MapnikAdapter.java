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
package com.v2soft.misto.UI.adapter;

import android.content.Context;
import android.location.Location;

import com.v2soft.misto.Providers.BitmapProvider;
import com.v2soft.misto.Providers.BitmapProviderListener;
import com.v2soft.misto.Providers.MapnikProvider;
import com.v2soft.misto.Providers.TileInfo;

public class MapnikAdapter extends TileMapAdapter implements BitmapProviderListener
{
	private TileInfo mBaseTile;
	private MapnikProvider mProvider;

	public MapnikAdapter(Context context) 
	{
		mProvider = new MapnikProvider(context);
		mProvider.addListener(this);
	}
	
	public void setBasePoint(Location location, int zoom)
	{
		mBaseTile = mProvider.getTileInfoByLocation(location, zoom);
		notifyDataSetChanged();
	}
	
	@Override
	public TileInfo getTileInfo(int x, int y, TileInfo tile) {
		if ( tile == null )
			tile = new TileInfo();
		mProvider.getTileByOffset(mBaseTile,x,y).copyTo(tile);
		mProvider.prepareTileImage(tile);
		return tile;
	}

	@Override
	public TileInfo getTileInfoAsync(int x, int y, TileInfo tile) {
		if ( tile == null )
			tile = new TileInfo();
		mProvider.getTileByOffset(mBaseTile,x,y).copyTo(tile);
		mProvider.prepareTileImageAsync(tile);
		return tile;
	}

	@Override
	public int getTileWidth() {
		return mBaseTile.getWidth();
	}

	@Override
	public int getTileHeight() {
		return mBaseTile.getHeight();
	}

	@Override
	public void onTileReady(TileInfo tile) 
	{
		notifyDataSetChanged();
	}

	public BitmapProvider getProvider() 
	{
		return mProvider;
	}
}
