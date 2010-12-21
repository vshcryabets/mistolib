package com.v2soft.V2MapView;

import com.v2soft.misto.UI.MapView;
import com.v2soft.misto.UI.PointOverlay;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public class Main extends Activity implements OnClickListener 
{
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
    
    
    @Override
    protected void onResume() 
    {
		Location t = new Location("test");
		t.setLongitude(35.120125);
		t.setLatitude(47.853041);
		
		PointOverlay overlay = new PointOverlay(this, t);
		MapView map = (MapView)findViewById(R.id.tileMapUI);
		map.addOverlay(overlay);

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
			Log.d("Main::onClick", e.toString());
		}
	}
}