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
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class TileMapView extends View
{
	private static final int CONST_TILECHANGEBOUND = 32;
	private TileMapAdapter mAdapter;
	private TileInfo [][] mDataArray;
	private int mTileHorizCount, mTileVertCount;
	private int mTileWidth, mTileHeight;
	private int mTileDrawWidth, mTileDrawHeight;
	private int mTopLeftX = 0, mTopLeftY = 0;
	private Paint mPaint;
	private int mTopOffset, mLeftOffset;
	private float mZoomLevel;
	private Matrix mTileMatrix;
	private int mTileChangeBound;
	private TileMapViewObserver mObserver;

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
		mTopOffset = 0;
		mLeftOffset = 0;
		mZoomLevel = 1;
		mTileMatrix = new Matrix();
		mObserver = new TileMapViewObserver();
	}

	public TileMapView(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		init();
	}
	
	public TileMapView(Context context, AttributeSet attrs, int defStyle) 
	{
		super(context, attrs, defStyle);
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
	
	@Override
	public void scrollBy(int dx, int dy) 
	{
		mTopOffset -= dy;
		mLeftOffset -= dx;
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
		for ( int y = 0; y < mTileVertCount; y++ )
		{
			for ( int x = 0; x < mTileHorizCount; x++)
			{
				TileInfo tile = mDataArray[y][x];
				int left = x*mTileWidth+mLeftOffset;
				int top = y*mTileWidth+mTopOffset;
				mTileMatrix.setTranslate(left, top);
				mTileMatrix.postScale(mZoomLevel, mZoomLevel);
				if ( tile.getBitmap() != null )
					canvas.drawBitmap(tile.getBitmap(), mTileMatrix, mPaint);
				else
					canvas.drawRect(left, top, left+mTileDrawWidth-2, 
							top+mTileDrawHeight-2, mPaint);
			}
		}
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
		mTopOffset = -mAdapter.getTileHeight();
		mTileDrawWidth = (int) (mTileWidth*mZoomLevel);
		mTileDrawHeight = (int)(mTileHeight*mZoomLevel);
		mLeftOffset = -mAdapter.getTileWidth();
		mTileHorizCount = this.getWidth() / mTileWidth + 2;
		mTileVertCount = this.getHeight() / mTileHeight + 2;		
		mDataArray = new TileInfo[mTileVertCount][mTileHorizCount];
		fillDataArray();
	}

	private void fillDataArray() 
	{
		if ( mAdapter == null ) return;
		for ( int y = 0; y < mTileVertCount; y++ )
		{
			for ( int x = 0; x < mTileHorizCount; x++)
			{
				if ( mDataArray[y][x] == null )
				{
					int id = y*10000+x; 
					mDataArray[y][x] = mAdapter.getTileInfoAsync(mTopLeftX+x, mTopLeftY+y, mDataArray[y][x]);
					mDataArray[y][x].setExternalId(id);
				}
			}
		}
	}

	public void updateTile(TileInfo tile) 
	{
//		int x = tile.getExternalId() & 0xFFFF;
//		int y = tile.getExternalId() / 0x10000;
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
			TileMapView.this.postInvalidate();
		}
		
		@Override
		public void onInvalidated() 
		{
			TileMapView.this.postInvalidate();		
		}
	}
}
