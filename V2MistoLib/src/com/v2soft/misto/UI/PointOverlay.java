package com.v2soft.misto.UI;

import android.content.Context;
//import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.location.Location;

public class PointOverlay extends MapViewOverlay 
{
	private Location mPoint;
//	private Bitmap mBitmap;
	private Paint mPaint;
	
	public PointOverlay(Context context, Location point) 
	{
		super(context);
		mPoint = point;
		mPaint = new Paint();
		mPaint.setColor(Color.parseColor("#3251FF"));
		mPaint.setStyle(Style.FILL_AND_STROKE);
		mPaint.setStrokeWidth(2);
		
	}

	@Override
	protected void onDraw(Canvas canvas) 
	{
		Point p = getMapView().getProjection().toPixels(mPoint, null);
		if (( p.x > 0 ) && (p.y > 0 ) )
		{
			if (( p.x < this.getWidth() ) && ( p.y < this.getHeight() ))
			//	canvas.drawBitmap(mBitmap, p.x, p.x, mPaint);
				canvas.drawCircle(p.x, p.y, 10, mPaint);
		}
		super.onDraw(canvas);
	}
	
	@Override
	public void scrollBy(int x, int y) {
		this.invalidate();
//		super.scrollBy(x, y);
	}
	
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) 
	{
		
//		super.onScrollChanged(l, t, oldl, oldt);
	}
}
