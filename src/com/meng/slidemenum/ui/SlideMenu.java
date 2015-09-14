package com.meng.slidemenum.ui;

import com.nineoldandroids.animation.FloatEvaluator;
import com.nineoldandroids.view.ViewHelper;

import android.animation.TypeEvaluator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.support.v4.widget.ViewDragHelper.Callback;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

public class SlideMenu extends FrameLayout{

	private View menuView;//菜单界面
	private View mainView;//主界面
	private int menuWidth,menuHeight;//菜单 宽和高
	private int mainWidth,mainHeight;//主界面的宽和
	private ViewDragHelper viewDragHelper;
	private float dragRange;//拖拽范围
	private FloatEvaluator floatEvaluator;
	private OnDragStateChangeListener listener;//监听器
	
	private DragState mState = DragState.Close;//当前的状态,默认是关闭状态
	/**
	 * 定义拖拽状态常量
	 * @author Administrator
	 *
	 */
	enum DragState{
		Open,Close;
	}

	public SlideMenu(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	public SlideMenu(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	public SlideMenu(Context context) {
		super(context);
		init();
	}
	
	public DragState getDragState(){
		return mState;
	}
	/**
	 * 初始化方法
	 */
	private void init(){
		floatEvaluator = new FloatEvaluator();
		viewDragHelper = ViewDragHelper.create(this, callback);
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		//简单滴异常处理
		if(getChildCount()!=2){
			throw new IllegalArgumentException("The SildeMenu only have 2 children!");
		}
		
		menuView = getChildAt(0);
		mainView = getChildAt(1);
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		menuWidth = menuView.getMeasuredWidth();
		menuHeight = menuView.getMeasuredHeight();
		mainWidth = mainView.getMeasuredWidth();
		mainHeight = mainView.getMeasuredHeight();
		
		int width = getMeasuredWidth();
		dragRange = width*0.6f;
		
		//1.一开始将menuView缩小
		ViewHelper.setScaleX(menuView, 0.5f);
		ViewHelper.setScaleY(menuView, 0.5f);
		//2.一开始让menuView往左移动
		ViewHelper.setTranslationX(menuView, -menuWidth/2);
		//3.一开始改变menuView的alpha值
		ViewHelper.setAlpha(menuView, 0.3f);
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return viewDragHelper.shouldInterceptTouchEvent(ev);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		viewDragHelper.processTouchEvent(event);
		return true;
	}
	
	private ViewDragHelper.Callback callback = new Callback() {
		
		@Override
		public boolean tryCaptureView(View child, int pointerId) {
			return child==menuView || child==mainView;
		}
		@Override
		public int getViewHorizontalDragRange(View child) {
			return (int) dragRange;
		}
		@Override
		public int clampViewPositionHorizontal(View child, int left, int dx) {
			if(child==mainView){
				if(left>dragRange)left=(int) dragRange;
				if(left<0)left=0;
			}
			return left;
		}
		@Override
		public void onViewPositionChanged(View changedView, int left, int top,
				int dx, int dy) {
			super.onViewPositionChanged(changedView, left, top, dx, dy);
//			Log.e("tag", "onViewPositionChanged dx:"+dx);
			if(changedView==menuView){
				//1.首先固定菜单View到左边
				menuView.layout(0, 0, menuWidth, menuHeight);
				
				//2.让mainView进行伴随移动
				int newLeft = mainView.getLeft()+dx;
				//3.对newLeft进行限制
				if(newLeft>dragRange)newLeft = (int) dragRange;//限制右边
				if(newLeft<0)newLeft = 0;//限制左边
				mainView.layout(newLeft,mainView.getTop(),newLeft+mainWidth, mainView.getBottom());
			}
			
			//1.计算出滑动的百分比
			float dragFraction = mainView.getLeft()/dragRange;//0-1
			//2.执行伴随动画
			dispatchDragAnim(dragFraction);
			//3.执行接口方法回调
			if(dragFraction==0 && mState!=DragState.Close){
				mState = DragState.Close;
				if(listener!=null){
					listener.onClose();
				}
			}else if (dragFraction==1 && mState!=DragState.Open) {
				mState = DragState.Open;
				if(listener!=null){
					listener.onOpen();
				}
			}else if (dragFraction>0 && dragFraction<1) {
				if(listener!=null){
					listener.onDragging(dragFraction);
				}
			}
		}
		@Override
		public void onViewReleased(View releasedChild, float xvel, float yvel) {
			super.onViewReleased(releasedChild, xvel, yvel);
			Log.e("tag", "xvel: "+xvel);
			if(mainView.getLeft()>dragRange/2){
				//在右半边
				open();
			}else {
				//在左半边
				close();
			}
			
			if(xvel>300 && mState!=DragState.Open){
				//应该打开，
				open();
			}
			if(xvel<-300 && mState!=DragState.Close){
				//应该打开，
				close();
			}
		}
	};
	/**
	 * 分发拖拽动画
	 * @param dragFraction
	 */
	private void dispatchDragAnim(float dragFraction){
		//dragFraction:0-1
		//1.给mainView增加缩放动画
//		float scaleValue = 0.8f + (1-dragFraction)*0.2f;
		ViewHelper.setScaleY(mainView, floatEvaluator.evaluate(dragFraction, 1f, 0.8f));
		ViewHelper.setScaleX(mainView, floatEvaluator.evaluate(dragFraction, 1f, 0.8f));
		//2.给menuView增加放大动画
		ViewHelper.setScaleY(menuView, floatEvaluator.evaluate(dragFraction, 0.5f, 1f));
		ViewHelper.setScaleX(menuView, floatEvaluator.evaluate(dragFraction, 0.5f, 1f));
		//3.给menuView增加移动动画
		ViewHelper.setTranslationX(menuView, floatEvaluator.evaluate(dragFraction, -menuWidth/2, 0));
		//4.给menuView增加透明动画
		ViewHelper.setAlpha(menuView, floatEvaluator.evaluate(dragFraction, 0.3f, 1f));
		//5.给SlideMenu增加背景的灰色遮罩
		getBackground().setColorFilter((Integer) ColorUtil.evaluateColor(dragFraction, Color.BLACK, Color.TRANSPARENT), Mode.SRC_OVER);
//		getBackground().setColorFilter((Integer) ColorUtil.evaluateColor(dragFraction, Color.RED, Color.YELLOW), Mode.SRC_OVER);
	}
	
	/**
	 * 打开菜单
	 */
	public void open(){
		viewDragHelper.smoothSlideViewTo(mainView, (int) dragRange, mainView.getTop());
		ViewCompat.postInvalidateOnAnimation(SlideMenu.this);
	}
	/**
	 * 关闭菜单
	 */
	public void close(){
		viewDragHelper.smoothSlideViewTo(mainView, 0, mainView.getTop());
		ViewCompat.postInvalidateOnAnimation(SlideMenu.this);
	}
	
	public void computeScroll() {
		if(viewDragHelper.continueSettling(true)){
			ViewCompat.postInvalidateOnAnimation(SlideMenu.this);
		}
	};
	/**
	 * 设置监听器
	 * @param listener
	 */
	public void setOnDragStateChangeListener(OnDragStateChangeListener listener){
		this.listener = listener;
	}
	/**
	 * 拖拽状态改变的监听器
	 * @author Administrator
	 *
	 */
	public interface OnDragStateChangeListener{
		/**
		 * 当打开的时候回调
		 */
		void onOpen();
		/**
		 * 当关闭的时候回调
		 */
		void onClose();
		/**
		 * 当拖拽的时候回调
		 * @param dragFraction
		 */
		void onDragging(float dragFraction);
	}
	
}
