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
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ZoomButtonsController;
import android.widget.ZoomButtonsController.OnZoomListener;

public class MapView extends FrameLayout implements OnZoomListener {
	private FrameLayout mControlsLayer;
	private ZoomButtonsController mZoomButtons;
	private TileMapView mTileMapView;
	private ArrayList<View> mLayers = new ArrayList<View>();
	private MapnikAdapter mAdapter;
	private int mZoom = 12;
    private int sx,sy;
    private Projection mProjection;
    private long mBaseX, mBaseY;


	public MapView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public MapView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public MapView(Context context) {
		super(context);
		init();
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
		return location;
	}    
	

	private void init() 
	{
		
		mTileMapView = new TileMapView(getContext());
		mLayers.add(mTileMapView);
		mAdapter = new MapnikAdapter(getContext(), 
				getCurrentLocation(getContext()), mZoom);
		mTileMapView.setDataAdapter(mAdapter);
		this.addView(mTileMapView);
		
		mControlsLayer = new FrameLayout(getContext());
		this.addView(mControlsLayer);
		
		mZoomButtons = new ZoomButtonsController(mControlsLayer);
		mZoomButtons.setAutoDismissed(true);
		mZoomButtons.setOnZoomListener(this);
//		LinearLayout.LayoutParams params = 
//			(android.widget.LinearLayout.LayoutParams) mZoomButtons.getContainer().getLayoutParams();
//		params.gravity = Gravity.BOTTOM | Gravity.RIGHT;
//		mZoomButtons.getContainer().setLayoutParams(params);
		setBuiltInZoomControls(true);
		
		mProjection = new Projection(mAdapter.getProvider());
		mProjection.setZoom(mZoom);
		Location curent = getCurrentLocation(getContext());
		Point p = mProjection.toPixels(curent, null);
		mBaseX = p.x;
		mBaseY = p.y;
		mProjection.setBasePoint(p);
		Location n = mProjection.fromPixels(0, 0, null);
	}

	public void setBuiltInZoomControls(boolean value)
	{
		if ( value )
		{
			mControlsLayer.addView(mZoomButtons.getContainer());			
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
				sx = (int) event.getX();
				sy = (int) event.getY();
			}
			if ( event.getAction() == MotionEvent.ACTION_UP )
			{
				int dx = (int) (event.getX()-sx);
				int dy = (int) (event.getY()-sy);
				scrollBy(-dx, -dy);
			}
			if ( event.getAction() == MotionEvent.ACTION_MOVE)
			{
				int dx = (int) (event.getX()-sx);				
				int dy = (int) (event.getY()-sy);
				if ( Math.abs(dx*dy) > 100 )
				{
					scrollBy(-dx, -dy);
					sx = (int) event.getX();
					sy = (int) event.getY();
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
		return mZoom;
	}
	
	@Override
	public void scrollBy(int x, int y) {
		for (View view : mLayers) {
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

}
