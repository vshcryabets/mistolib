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

	public TileMapView(Context context) 
	{
		super(context);
		init();
	}

	private void init() 
	{
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

	public TileMapView(Context context, AttributeSet attrs) 
	{
		super(context);
		init();
	}
	
	public TileMapView(Context context, AttributeSet attrs, int defStyle) 
	{
		super(context);
		init();
	}
	
	public void setDataAdapter(TileMapAdapter adapter)
	{
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
		if ( mLeftOffset > -mTileChangeBound )
		{
			moveTilesToRight();
			changed = true;
		}
		if ( mLeftOffset < -mTileWidth-mTileChangeBound )
		{
			moveTilesToLeft();
			changed = true;
		}
		if ( mTopOffset > -32 )
		{
			moveTilesToBottom();
			changed = true;
		}
		if ( mTopOffset < -mTileHeight-mTileChangeBound )
		{
			moveTilesToTop();
			changed = true;
		}
		if ( changed )
			fillDataArray();
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
		mTileMatrix.postScale(mZoomLevel, mZoomLevel);
		canvas.drawBitmap(mBitmap, mTileMatrix, mPaint);
		super.onDraw(canvas);
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) 
	{
		super.onLayout(changed, left, top, right, bottom);
		buildDataArray();
	}

	private void buildDataArray() 
	{
		if ( mAdapter == null ) return;
		if ( this.getWidth() == 0 ) return;
		mTileHeight = mAdapter.getTileHeight();
		mTileWidth = mAdapter.getTileWidth();
		mTopOffset -= mAdapter.getTileHeight();
		mLeftOffset -= mAdapter.getTileWidth();
		mTileHorizCount = this.getWidth() / mTileWidth + 2;
		mTileVertCount = this.getHeight() / mTileHeight + 2;		
		mDataArray = new TileInfo[mTileVertCount][mTileHorizCount];
		mBitmap = Bitmap.createBitmap(mTileHorizCount*mTileWidth, 
				mTileVertCount*mTileHeight, 
				Config.RGB_565);
		mBitmap.prepareToDraw();
		fillDataArray();
	}

	private void fillDataArray() 
	{
		if ( mAdapter == null ) return;
		for ( int y = 0; y < mTileVertCount; y++ )
		{
			for ( int x = 0; x < mTileHorizCount; x++)
			{
				int id = y*10000+x+1; 
				if ( mDataArray[y][x] == null )
				{
					mDataArray[y][x] = mAdapter.getTileInfoAsync(mTopLeftX+x, mTopLeftY+y, mDataArray[y][x]);
				}
				mDataArray[y][x].setExternalId(id);
			}
		}
		rebuildBitmap();
	}

	private void rebuildBitmap() 
	{
		if ( mBitmap == null ) return;
		Canvas c = new Canvas(mBitmap);
		for ( int y = 0; y < mTileVertCount; y++ )
		{
			for ( int x = 0; x < mTileHorizCount; x++)
			{
				if ( mDataArray[y][x].getExternalId() > 0 )
				{
					// draw tile to bitmap
					float left = x * mTileWidth;
					float top = y * mTileHeight;
					if ( mDataArray[y][x].getBitmap() != null )
					{
						c.drawBitmap(mDataArray[y][x].getBitmap(), left, top, mPaint);
						mDataArray[y][x].setExternalId(0);
//						Log.d("rebuildBitmp", "Updted "+mDataArray[y][x].toString());
					}
					else
                        c.drawRect(left, top, left+mTileWidth-1, 
                                        top+mTileHeight-1, mPaint);
//					c.drawText(mDataArray[y][x].toString(), left+5, top+5, mTextPaint);
				}
			}
		}
		Log.d("rebuildBitmp", "Finished");
		this.postInvalidate();
	}
	
	public void addZoom(float level)
	{
		mZoomLevel += level;
		this.postInvalidate();
	}
	
	private class TileMapViewObserver extends DataSetObserver
	{
		@Override
		public void onChanged() 
		{
			Log.d("Observer", "Chnged");
			TileMapView.this.rebuildBitmap();
		}
		
		@Override
		public void onInvalidated() 
		{
			TileMapView.this.rebuildBitmap();		
		}
	}
}
