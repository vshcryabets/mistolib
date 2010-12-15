package com.v2soft.V2MapView;

import java.util.List;

import com.v2soft.misto.UI.TileMapView;
import com.v2soft.misto.UI.adapter.MapnikAdapter;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;

public class MapView extends Activity implements OnClickListener 
{
	private TileMapView tileMapUI;
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        tileMapUI = (TileMapView)findViewById(R.id.tileMapUI);
        tileMapUI.addZoom((float) -0.1);
               
        MapnikAdapter adapter = new MapnikAdapter(this, getCurrentLocation(this),12);
        tileMapUI.setDataAdapter(adapter);
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
    
    int sx,sy;
    
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
				tileMapUI.scrollBy(-dx, -dy);
			}
			if ( event.getAction() == MotionEvent.ACTION_MOVE)
			{
				int dx = (int) (event.getX()-sx);				
				int dy = (int) (event.getY()-sy);
				if ( Math.abs(dx*dy) > 100 )
				{
					tileMapUI.scrollBy(-dx, -dy);
					sx = (int) event.getX();
					sy = (int) event.getY();
				}
			}
		}
		catch (Exception e) 
		{
			Log.d("MapView::onTouchEvent", e.toString());
		}
    	return super.onTouchEvent(event);
    }
    
    @Override
    protected void onResume() 
    {
    	super.onResume();
    }

	@Override
	public void onClick(View arg0) 
	{
		try
		{
			int id = arg0.getId();
			switch (id) {
//			case R.id.btnLeftUI:
//				tileMapUI.scrollBy(-10,0);
//				break;
//			case R.id.btnRightUI:
//				tileMapUI.scrollBy(10,0);
//				break;
			}
		}
		catch (Exception e) 
		{
			Log.d("MapView::onClick", e.toString());
		}
	}
}