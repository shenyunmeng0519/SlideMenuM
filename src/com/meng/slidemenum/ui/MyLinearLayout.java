package com.meng.slidemenum.ui;

import com.meng.slidemenum.ui.SlideMenu.DragState;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

/**
 * 当SlideMenu处于open的时候，能够拦截并消费所有的触摸事件
 * @author Administrator
 *
 */
public class MyLinearLayout extends LinearLayout{
	private SlideMenu slideMenu;
	public MyLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public MyLinearLayout(Context context) {
		super(context);
	}
	public void setSlideMenu(SlideMenu slideMenu) {
		this.slideMenu = slideMenu;
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if(slideMenu!=null && slideMenu.getDragState()==DragState.Open){
			//当SlideMenu打开，则拦截
			return true;
		}
		return super.onInterceptTouchEvent(ev);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(slideMenu!=null && slideMenu.getDragState()==DragState.Open){
			//当SlideMenu打开，则消费掉
			if(event.getAction()==MotionEvent.ACTION_UP){
				//抬起的时候，关闭侧滑菜单
				slideMenu.close();
			}
			return true;
		}
		return super.onTouchEvent(event);
	}

}
