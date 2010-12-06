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

import java.util.ArrayList;
import android.location.Location;

/**
 * Map data provider
 * @author vshryabets@2vsoft.com
 * @version 1.0.0
 */
public abstract class BitmapProvider 
{
	public abstract boolean prepareTileImage(TileInfo info);	
	public abstract void prepareTileImageAsync(TileInfo info);
	public abstract TileInfo getTileInfoByLocation(Location location, int zoom);
	public abstract TileInfo getTileByOffset(TileInfo mBaseTile, int x, int y);
	
	public TileInfo getSouthTile(TileInfo tile)
	{
		return getTileByOffset(tile, 0, 1);
	}
	
	public TileInfo getEastTile(TileInfo tile)
	{
		return getTileByOffset(tile, 1, 0);
	}
	
	public TileInfo getNorthTile(TileInfo tile)
	{
		return getTileByOffset(tile, 0, -1);
	}
	
	public TileInfo getWestTile(TileInfo tile)
	{
		return getTileByOffset(tile, -1, 0);
	}
	//-----------------------------------------------------------------------------------------------
    // Events
    //-----------------------------------------------------------------------------------------------
    protected ArrayList<BitmapProviderListener> listeners = new ArrayList<BitmapProviderListener>();
    public void addListener(BitmapProviderListener listener)
    {
            listeners.add(listener);
    }

    public void removeListener(BitmapProviderListener listener)
    {
            listeners.remove(listener);
    }
}
