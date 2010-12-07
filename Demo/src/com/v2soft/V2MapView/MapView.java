package com.v2soft.V2MapView;

import java.util.List;

import com.v2soft.misto.Providers.BitmapProvider;
import com.v2soft.misto.Providers.BitmapProviderListener;
import com.v2soft.misto.Providers.MapnikProvider;
import com.v2soft.misto.Providers.TileInfo;
import com.v2soft.misto.UI.TileMapAdapter;
import com.v2soft.misto.UI.TileMapView;

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
	private BitmapProvider provider;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        tileMapUI = (TileMapView)findViewById(R.id.tileMapUI);
        provider = new MapnikProvider(this);
               
        MyAdapter adapter = new MyAdapter();
        tileMapUI.setDataAdapter(adapter);
        provider.addListener(adapter);
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
    class MyAdapter implements TileMapAdapter, BitmapProviderListener
    {
    	TileInfo mBaseTile;
    	
    	public MyAdapter() 
    	{
    		Location location = getCurrentLocation(MapView.this);
    		mBaseTile = provider.getTileInfoByLocation(location, 12);
		}

		@Override
		public TileInfo getTileInfo(int x, int y, TileInfo tile) 
		{
			if ( tile == null )
				tile = new TileInfo();
			provider.getTileByOffset(mBaseTile,x,y).copyTo(tile);
			provider.prepareTileImage(tile);
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
		public TileInfo getTileInfoAsync(int x, int y, TileInfo tile) 
		{
			if ( tile == null )
				tile = new TileInfo();
			provider.getTileByOffset(mBaseTile,x,y).copyTo(tile);
			provider.prepareTileImageAsync(tile);
			return tile;
		}

		@Override
		public void onTileReady(TileInfo tile) 
		{
			tileMapUI.updateTile(tile);
		}
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