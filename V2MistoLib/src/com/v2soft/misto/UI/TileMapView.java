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

import com.v2soft.misto.Debug.BitmapManager;
import com.v2soft.misto.Providers.TileInfo;
import com.v2soft.misto.UI.adapter.TileMapAdapter;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;

public class TileMapView extends MapViewOverlay
{
	private static final int CONST_TILECHANGEBOUND = 32;
	public static final String LOG_TAG = TileMapView.class.getSimpleName();
	private TileMapAdapter mAdapter;
	private TileInfo [][] mDataArray;
	private int mTileHorizCount, mTileVertCount;
	private int mTileWidth, mTileHeight;
	private int mTopLeftX = 0, mTopLeftY = 0;
	private Paint mPaint, mTextPaint;
	private int mTopOffset, mLeftOffset;
	private float mZoomLevel;
	private Matrix mTileMatrix;
	private int mTileChangeBound;
	private TileMapViewObserver mObserver;
	private Bitmap mBitmap;
//	private float mRotateAngle;

	public TileMapView(Context context) {
		this(context, null);
	}

	public TileMapView(Context context, AttributeSet attrs)	{
		this(context,attrs,0);
	}
	
	public TileMapView(Context context, AttributeSet attrs, int defStyle) {
		super(context);
		init();
	}

	private void init() {
//		setRotateAngle(0);
		mTileChangeBound = CONST_TILECHANGEBOUND;
		mPaint = new Paint();
		mPaint.setFilterBitmap(true);
		mPaint.setColor(0xFF909000);
		mPaint.setTextSize(10);
		
		mTextPaint = new Paint();
		mTextPaint.setColor(0xFF010100);
		mTextPaint.setTextSize(10);

		mTopOffset = 0;
		mLeftOffset = 0;
		mZoomLevel = 1;
		mTileMatrix = new Matrix();
		mObserver = new TileMapViewObserver();
	}
	
	public void setDataAdapter(TileMapAdapter adapter) throws Exception {
		mAdapter = adapter;
		mAdapter.registerDataSetObserver(mObserver);
		buildDataArray();
	}
	
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		mTopOffset += (t-oldt);
		mLeftOffset += (l-oldl);
		this.invalidate();
		super.onScrollChanged(0, 0, 0, 0);
	}
	
	public void changeOffset(int dx, int dy)
	{
		mTopOffset -= dy;
		mLeftOffset -= dx;
	}
	
	@Override
	public void scrollBy(int dx, int dy) 
	{
		changeOffset(dx, dy);
		boolean changed = false;
		if ( mLeftOffset > -mTileChangeBound ) {
			moveTilesToRight();
			changed = true;
		} else if ( mLeftOffset < -mTileWidth-mTileChangeBound ) {
			moveTilesToLeft();
			changed = true;
		}
		if ( mTopOffset > -32 )	{
			moveTilesToBottom();
			changed = true;
		} else if ( mTopOffset < -mTileHeight-mTileChangeBound ) {
			moveTilesToTop();
			changed = true;
		}
		if ( changed ) {
			try {
				fillDataArray();
			} catch (Exception e) {
				Log.e(LOG_TAG, e.toString(), e);
			}
		}
		this.invalidate();
	}
	
	private void moveTilesToBottom() 
	{
		while ( mTopOffset > -mTileChangeBound )
		{
			mTopLeftY--;
			mTopOffset -= mTileHeight;
			// move down
			for ( int y = mTileVertCount-1; y > 0; y-- )
			{				
				for ( int x = 0; x < mTileHorizCount; x++)
				{
					mDataArray[y][x] = mDataArray[y-1][x];
					mDataArray[y-1][x] = null;
				}
			}
		}
	}

	private void moveTilesToTop() 
	{
		int top_bound = -mTileHeight-mTileChangeBound;
		while ( mTopOffset < top_bound )
		{
			mTopLeftY++;
			mTopOffset += mTileHeight;
			// move up
			for ( int y = 0; y < mTileVertCount-1; y++ )
			{				
				for ( int x = 0; x < mTileHorizCount; x++)
				{
					mDataArray[y][x] = mDataArray[y+1][x];
					mDataArray[y+1][x] = null;
				}
			}
		}
	}

	private void moveTilesToRight() 	
	{		
		while ( mLeftOffset > -mTileChangeBound )
		{
			mTopLeftX--;
			mLeftOffset -= mTileWidth;
			// move right
			for ( int y = 0; y < mTileVertCount; y++ )
			{				
				for ( int x = mTileHorizCount-1; x > 0; x--)
				{
					mDataArray[y][x] = mDataArray[y][x-1];
				}
				mDataArray[y][0] = null;
			}
		}
	}

	private void moveTilesToLeft() 	
	{
		int left_bound = -mTileWidth-mTileChangeBound;
		while ( mLeftOffset < left_bound )
		{
			mTopLeftX++;
			mLeftOffset += mTileWidth;
			// move left
			for ( int y = 0; y < mTileVertCount; y++ )
			{				
				for ( int x = 0; x < mTileHorizCount-1; x++)
				{
					mDataArray[y][x] = mDataArray[y][x+1];
				}
				mDataArray[y][mTileHorizCount-1] = null;
			}
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) 
	{
		int left = mLeftOffset;
		int top = mTopOffset;
		mTileMatrix.setTranslate(left, top);
//		mTileMatrix.setRotate(mRotateAngle);
		mTileMatrix.postScale(mZoomLevel, mZoomLevel);
//		mTileMatrix.postRotate(mRotateAngle);
		canvas.drawBitmap(mBitmap, mTileMatrix, mPaint);
		BitmapManager.showUsage();
		super.onDraw(canvas);
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) 
	{
		super.onLayout(changed, left, top, right, bottom);
		try {
			buildDataArray();
		} catch (Exception e) {
			Log.e(LOG_TAG, e.toString(), e);
		}
	}

	private void buildDataArray() throws Exception {
		if ( mAdapter == null ) return;
		if ( this.getWidth() == 0 ) return;		
		mTileHeight = mAdapter.getTileHeight();
		mTileWidth = mAdapter.getTileWidth();
		mTopOffset -= mAdapter.getTileHeight();
		mLeftOffset -= mAdapter.getTileWidth();
		mTileHorizCount = this.getWidth() / mTileWidth + 2;
		mTileVertCount = this.getHeight() / mTileHeight + 2;		
		Log.d(LOG_TAG, "Init tiles array TP="+mTileWidth+"x"+mTileHeight+" Cnt="+mTileHorizCount+"x"+mTileVertCount);
		mDataArray = new TileInfo[mTileVertCount][mTileHorizCount];
		mBitmap = Bitmap.createBitmap(mTileHorizCount*mTileWidth, 
				mTileVertCount*mTileHeight, 
				Config.RGB_565);
		BitmapManager.registerBitmap(mBitmap, "TileMap main");
		fillDataArray();
	}

	private void fillDataArray() throws Exception {
		if ( mAdapter == null ) throw new Exception("mDataadapter not initialized");
		for ( int y = 0; y < mTileVertCount; y++ )
		{
			for ( int x = 0; x < mTileHorizCount; x++)
			{
				int id = y*10000+x+1; 
				if ( mDataArray[y][x] != null )
					mDataArray[y][x].recycle();
				mDataArray[y][x] = mAdapter.getTileInfoAsync(mTopLeftX+x, mTopLeftY+y, mDataArray[y][x]);
				mDataArray[y][x].setExternalId(id);
			}
		}
		System.gc();
		rebuildBitmap();
	}

//	private Runnable mRebuilder = new Runnable() 
//	{
//		@Override
//		public void run() {
//			// TODO Auto-generated method stub
//			
//		}
//	};
	
	private void rebuildBitmap() 
	{
		if ( mBitmap == null ) return;
		final Canvas c = new Canvas(mBitmap);
		for ( int y = 0; y < mTileVertCount; y++ ) {
			for ( int x = 0; x < mTileHorizCount; x++) {
				Log.d(LOG_TAG, "mDataArray["+y+"]["+x+"]="+mDataArray[y][x]);
				if ( mDataArray[y][x].getExternalId() > 0 )
				{
					// draw tile to bitmap
					final float left = x * mTileWidth;
					final float top = y * mTileHeight;
					if ( mDataArray[y][x].getBitmap() != null )
					{
						c.drawBitmap(mDataArray[y][x].getBitmap(), left, top, mPaint);
						mDataArray[y][x].setExternalId(0);
						mDataArray[y][x].recycle();
//						Log.d("rebuildBitmp", "Updted "+mDataArray[y][x].toString());
					}
					else
                        c.drawRect(left, top, left+mTileWidth-1, 
                                        top+mTileHeight-1, mPaint);
					c.drawText(mDataArray[y][x].toString(), left+5, top+5, mTextPaint);
				}
			}
		}
//		Log.d("rebuildBitmp", "Finished");
		this.postInvalidate();
	}
	
	public void addZoom(float level)
	{
		mZoomLevel += level;
		this.postInvalidate();
	}
	
//	public void setRotateAngle(float mRotateAngle) {
//		this.mRotateAngle = mRotateAngle;
//		this.postInvalidate();
//	}
//
//	public float getRotateAngle() {
//		return mRotateAngle;
//	}

	private class TileMapViewObserver extends DataSetObserver {
		@Override
		public void onChanged() {
			Log.d(LOG_TAG, "TileMapViewObserver::onChanged");
			TileMapView.this.rebuildBitmap();
		}
		
		@Override
		public void onInvalidated() {
			Log.d(LOG_TAG, "TileMapViewObserver::onInvalidated");
			TileMapView.this.rebuildBitmap();		
		}
	}
}
