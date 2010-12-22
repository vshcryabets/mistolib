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

import com.v2soft.misto.Debug.BitmapManager;

import android.graphics.Bitmap;

public class TileInfo 
{
	//-----------------------------------------------------------------------------------------------
    // Variables
    //-----------------------------------------------------------------------------------------------
	private int longitude;
	private int latitude;
	private int zoom;
	private int width;
	private int height;
	private Bitmap mBitmap;
	private int mExternalId;
	//-----------------------------------------------------------------------------------------------
    // Getters and setters
    //-----------------------------------------------------------------------------------------------
	public int getLatitude() {
		return latitude;
	}
	public void setLatitude(int latitude) {
		this.latitude = latitude;
	}
	public void setLongitude(int longitude) {
		this.longitude = longitude;
	}
	public int getLongitude() {
		return longitude;
	}
	public void setZoom(int zoom) {
		this.zoom = zoom;
	}
	public int getZoom() {
		return zoom;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public int getWidth() {
		return width;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public int getHeight() {
		return height;
	}
	
	public void copyTo(TileInfo res) 
	{
		res.setLatitude(latitude);
		res.setLongitude(longitude);
		res.setZoom(zoom);
		res.setWidth(width);
		res.setHeight(height);
	}
	
	public void setBitmap(Bitmap value)
	{
		mBitmap = value;
	}
	
	public Bitmap getBitmap() 
	{
		return mBitmap;
	}
	public void setExternalId(int mExternalId) {
		this.mExternalId = mExternalId;
	}
	public int getExternalId() {
		return mExternalId;
	}

	@Override
	public String toString() 
	{
		return "T:"+longitude+":"+latitude+"x"+zoom;
	}
	
	/**
	 * Free up the memory associated with this tile's bitmap
	 */
	public void recycle() 
	{
		if ( mBitmap == null ) return;
		if ( mBitmap.isRecycled() ) return;
		BitmapManager.releaseBitmap(mBitmap);
		mBitmap = null;
	}
}
