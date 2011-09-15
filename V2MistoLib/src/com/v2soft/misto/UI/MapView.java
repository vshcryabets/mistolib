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
package com.v2soft.misto.UI;

import java.util.ArrayList;
import java.util.List;

import com.v2soft.misto.UI.adapter.MapnikAdapter;
import com.v2soft.misto.math.Projection;

import android.content.Context;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.ZoomButtonsController;
import android.widget.ZoomButtonsController.OnZoomListener;

public class MapView extends FrameLayout implements OnZoomListener {
	private static final String LOG_TAG = MapView.class.getSimpleName();
	private FrameLayout mControlsLayer;
	private ZoomButtonsController mZoomButtons;
	private TileMapView mTileMapView;
	private ArrayList<MapViewOverlay> mLayers = new ArrayList<MapViewOverlay>();
	private MapnikAdapter mAdapter;
	private int mZoom = 17;
        private int mTouchLastX, mTouchLastY;
	private Projection mProjection;
	private Location mCenterPoint = null; 

	public MapView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mCenterPoint = getCurrentLocation(context);
		mTileMapView = new TileMapView(context);
//		mTileMapView.setRotateAngle(45);
		addOverlay(mTileMapView);
		mAdapter = new MapnikAdapter(context);
		try {
			mTileMapView.setDataAdapter(mAdapter);
		} catch (Exception e) {
			Log.e(LOG_TAG, e.toString(), e);
		}
		
		mControlsLayer = new FrameLayout(getContext());
		this.addView(mControlsLayer);
		
		mZoomButtons = new ZoomButtonsController(mControlsLayer);
		mZoomButtons.setAutoDismissed(true);
		mZoomButtons.setOnZoomListener(this);
		setBuiltInZoomControls(true);	
	}

	public MapView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public MapView(Context context) {
		this(context, null);
	}	
	
	/**
	 * Get current location
	 * @param context context
	 * @return null if can't find location
	 */	
	public static Location getCurrentLocation(Context context)
	{
		Location location = null;
		LocationManager lm = (LocationManager) context.getSystemService( 
				Context.LOCATION_SERVICE);
		location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if ( location == null )
		{
			// try to get location from other providers
		    List<String> providers = lm.getProviders(true);  
		    for (int i=providers.size()-1; i>=0; i--) 
		    {  
		    	location = lm.getLastKnownLocation(providers.get(i));  
		        if ( location != null) break;  
		    }
		}
		if ( location == null ) {
			location = new Location("MapView");
			//http://maps.google.com/?ll=47.857388,35.107273&spn=0.003686,0.009645&t=h&z=17&vpsrc=6
			location.setLatitude(47.857388);
			location.setLongitude(35.107273);
		}
		return location;
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) 
	{
		// create projection
		mProjection = new Projection(mAdapter.getProvider());
		mProjection.setZoom(mZoom);

		Point p = mProjection.toWorldPixels(mCenterPoint, null);
		p.x = p.x - (right-left)/2;
		p.y = p.y - (bottom-top)/2;
		mProjection.setBasePoint(p);
		
		Location baseLocation = mProjection.fromPixels(0, 0, null);
		mAdapter.setBasePoint(baseLocation, mZoom);
		
		// count base offset
		int offset_x = p.x % 256;
		int offset_y = p.y % 256;
		Log.d("Proposed offset",offset_x+"x"+offset_y);
//		mTileMapView.changeOffset(-(256-offset_x), -offset_y);
		super.onLayout(changed, left, top, right, bottom);
	}
	
	public void setBuiltInZoomControls(boolean value)
	{
		if ( value )
		{
			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
			params.gravity = Gravity.BOTTOM | Gravity.RIGHT;
			mControlsLayer.addView(mZoomButtons.getContainer(),params);			
		}
		else
		{
			mControlsLayer.removeView(mZoomButtons.getContainer());
		}
			
	}
	
    @Override
    public boolean onTouchEvent(MotionEvent event) 
    {
		try
		{
			if ( event.getAction() == MotionEvent.ACTION_DOWN )
			{
				mTouchLastX = (int) event.getX();
				mTouchLastY = (int) event.getY();
			}
			if ( event.getAction() == MotionEvent.ACTION_UP )
			{
				int dx = (int) (event.getX()-mTouchLastX);
				int dy = (int) (event.getY()-mTouchLastY);
				scrollBy(-dx, -dy);
			}
			if ( event.getAction() == MotionEvent.ACTION_MOVE)
			{
				int dx = (int) (event.getX()-mTouchLastX);				
				int dy = (int) (event.getY()-mTouchLastY);
				if ( Math.abs(dx*dy) > 100 )
				{
					scrollBy(-dx, -dy);
					mTouchLastX = (int) event.getX();
					mTouchLastY = (int) event.getY();
				}
			}
		}
		catch (Exception e) 
		{
			Log.d("MapView::onTouchEvent", e.toString());
		}
    	return true;
    }	
    
	public int getZoomLevel()
	{
		return 0;
	}
	
	@Override
	public void scrollBy(int x, int y) {
		mProjection.scrollBy(x,y);
		for (MapViewOverlay view : mLayers) {
			view.scrollBy(x, y);
		}
	}

	@Override
	public void onVisibilityChanged(boolean arg0) 
	{
	}

	@Override
	public void onZoom(boolean arg0) 
	{
		if ( arg0 )
		{
			// zoom in
			mTileMapView.addZoom((float)0.1);
		}
		else
		{
			// zoom out
			mTileMapView.addZoom((float)-0.1);
		}
	}
	
	public Projection getProjection() {return mProjection;}

	//-------------------------------------------------------------------------------------------
	// Overlays functions
	//-------------------------------------------------------------------------------------------
	public void addOverlay(MapViewOverlay overlay)
	{
		mLayers.add(overlay);
		this.addView(overlay);
		overlay.setMapView(this);
	}

	public void removeOverlay(MapViewOverlay overlay)
	{
		mLayers.remove(overlay);
		this.removeView(overlay);
		overlay.setMapView(null);
	}
}
