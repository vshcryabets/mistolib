package com.v2soft.V2MapView;

import java.util.List;

import com.v2soft.misto.UI.MapView;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;

public class Main extends Activity implements OnClickListener 
{
	private MapView tileMapUI;
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        tileMapUI = (MapView)findViewById(R.id.tileMapUI);
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
//				tileMapUI.scrollBy(-dx, -dy);
			}
			if ( event.getAction() == MotionEvent.ACTION_MOVE)
			{
				int dx = (int) (event.getX()-sx);				
				int dy = (int) (event.getY()-sy);
				if ( Math.abs(dx*dy) > 100 )
				{
//					tileMapUI.scrollBy(-dx, -dy);
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