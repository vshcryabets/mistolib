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
package com.v2soft.misto.math;

import com.v2soft.misto.Providers.BitmapProvider;

import android.graphics.Point;
import android.location.Location;

public class Projection 
{
	private int mZoom;
	private long mBasePointX, mBasePointY;
	private BitmapProvider mProvider;
	
	public Projection(BitmapProvider provider)
	{
		mProvider = provider;
	}
	
	/**
	 * Create a new location point from specified coordinates relative to the top-left
	 * of the map view
	 * @return new location point
	 */
	public Location fromPixels(int x, int y, Location out)
	{
		if ( out == null )
			out = new Location("com.v2soft.misto.math.Projection");
		long scale = mProvider.worldWidthPx(mZoom);
		double long_offset = ((double)mBasePointX+x)*360/scale-180;
		// This is the inverse of the Gudermannian function
		// http://en.wikipedia.org/wiki/Gudermannian_function
		double ny = (-scale/2+(mBasePointY+y))/(scale/(2*Math.PI));
		double lat_offset = -Math.toDegrees(
					2*Math.atan(
						Math.exp(ny)
						)-Math.PI/2
				);
		out.setLatitude(lat_offset);
		out.setLongitude(long_offset);
		return out;
	}

	/**
	 * Converts a distance in meters (along the equator) to 
	 * one in (horizontal) pixels at the current zoom level.
	 * @param meters
	 * @return
	 */
	public float metersToEquatorPixels(float meters)
	{
		float res = -1;
		// TODO: write math logic
		return res;
	}
     
	/**
	 * Converts the given GeoPoint to onscreen pixel coordinates, 
	 * relative to the top-left of the MapView that provided this Projection.
	 * Mathematics details can be found at http://en.wikipedia.org/wiki/Mercator_projection
	 * @param in The latitude/longitude pair to convert.
	 * @param out A pre-existing object to use for the output; 
	 * if null, a new Point will be allocated and returned.
	 * @return
	 */
	public Point toPixels(Location in, android.graphics.Point out)
	{
		if (  out == null )
			out = new Point();
		long scale = mProvider.worldWidthPx(mZoom);
		double dx = ((in.getLongitude()+180)*scale/360);
		long x = (long) Math.round(dx);
		
		double sin_lat = Math.sin(Math.toRadians(in.getLatitude()));
		double y = 
			Math.log(
				(1+sin_lat)/
				(1-sin_lat)
				)*0.5;
		y = y/(2*Math.PI);
		y = (0.5 - y)*scale;
		out.x = (int) x;
		out.y = (int) y;
		return out;
	}

	public void setZoom(int zoom) 
	{
		mZoom = zoom;
	}
	
	public void setBasePoint(Point point)
	{
		mBasePointX = point.x;
		mBasePointY = point.y;
	}
}
