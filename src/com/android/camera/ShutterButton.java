/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.camera;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

/**
 * A button designed to be used for the on-screen shutter button.
 * It's currently an {@code ImageView} that can call a delegate when the
 * pressed state changes.
 */
public class ShutterButton extends ImageView {
	
	private static final String TAG = "ShutterButton";

    private boolean mTouchEnabled = true;
    
    private long mStart;//������ʼʱ��  
    private int mRepeatCount;//�����ظ�����
    private RepeatListener mRepeatListener;//
    private long mInterval = 500;//����һ�γ���ʱ��
    
    private Runnable mThread = new Runnable(){  
        public void run(){  
            Log.d(TAG, "mRepeaterThread run()");  
            doRepeat(false);  
            if (isPressed()){  
                Log.d(TAG, "mRepeaterThread run() press");  
                postDelayed(this, mInterval);//�ӳ�mInterval��ִ�е�ǰ�߳�  
            }  
        }  
    };  
    
    /** 
     * @param end ��ʾ���һ�γ����������������¼� 
     */  
    private void doRepeat(boolean end) {  
        Log.d(TAG, "mRepeaterThread run() end=" + end);  
        long now = SystemClock.elapsedRealtime();//��ȡ��ǰʱ��  
        if (mRepeatListener != null)  
        {  
        	mRepeatListener.onRepeat(this, now - mStart, end ? -1 : mRepeatCount++);  
        }  
    }
    
    /** 
     * �������� 
     */  
    private void endRepeat(){  
        doRepeat(true);  
        mStart = 0;  
    }
    
    /** 
     * ���ó��������¼�����ʼ��mInterval 
     */  
    public void setRepeatListener(RepeatListener listener, long interval){  
        Log.d(TAG, "++++++++++++++++setRepeatListener interval=" + interval);  
        mRepeatListener = listener;  
        mInterval = interval;  
    } 
    
    @Override  
    public boolean performLongClick()  
    {  
        Log.d(TAG, "================performLongClick");  
        mStart = SystemClock.elapsedRealtime();//��ȡϵͳ��ǰʱ��  
        mRepeatCount = 0;  
        //post(mThread);//����post()������ִ��mThread  
        return true;  
    } 
    
    @Override  
    public boolean onTouchEvent(MotionEvent event){  
        if (event.getAction() == MotionEvent.ACTION_UP){  
            Log.d(TAG, "onTouchEvent UP");  
            //removeCallbacks(mThread);//ɾ�����е���δִ�е��̶߳���     
            //if (mStart != 0){  
            //    endRepeat();  
            //}  
        }  
        return super.onTouchEvent(event);  
    }  

    /**
     * A callback to be invoked when a ShutterButton's pressed state changes.
     */
    public interface OnShutterButtonListener {
        /**
         * Called when a ShutterButton has been pressed.
         *
         * @param pressed The ShutterButton that was pressed.
         */
        void onShutterButtonFocus(boolean pressed);
        void onShutterButtonClick();
    }

    private OnShutterButtonListener mListener;
    private boolean mOldPressed;

    public ShutterButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFocusable(true); //���ý���  
        setLongClickable(true); //���ó����¼����������ִ��performLongClick()  
    }

    public void setOnShutterButtonListener(OnShutterButtonListener listener) {
        mListener = listener;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent m) {
        if (mTouchEnabled) {
            return super.dispatchTouchEvent(m);
        } else {
            return false;
        }
    }

    public void enableTouch(boolean enable) {
        mTouchEnabled = enable;
    }

    /**
     * Hook into the drawable state changing to get changes to isPressed -- the
     * onPressed listener doesn't always get called when the pressed state
     * changes.
     */
    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        final boolean pressed = isPressed();
        if (pressed != mOldPressed) {
            if (!pressed) {
                // When pressing the physical camera button the sequence of
                // events is:
                //    focus pressed, optional camera pressed, focus released.
                // We want to emulate this sequence of events with the shutter
                // button. When clicking using a trackball button, the view
                // system changes the drawable state before posting click
                // notification, so the sequence of events is:
                //    pressed(true), optional click, pressed(false)
                // When clicking using touch events, the view system changes the
                // drawable state after posting click notification, so the
                // sequence of events is:
                //    pressed(true), pressed(false), optional click
                // Since we're emulating the physical camera button, we want to
                // have the same order of events. So we want the optional click
                // callback to be delivered before the pressed(false) callback.
                //
                // To do this, we delay the posting of the pressed(false) event
                // slightly by pushing it on the event queue. This moves it
                // after the optional click notification, so our client always
                // sees events in this sequence:
                //     pressed(true), optional click, pressed(false)
                post(new Runnable() {
                    @Override
                    public void run() {
                        callShutterButtonFocus(pressed);
                    }
                });
            } else {
                callShutterButtonFocus(pressed);
            }
            mOldPressed = pressed;
        }
    }

    private void callShutterButtonFocus(boolean pressed) {
        if (mListener != null) {
            mListener.onShutterButtonFocus(pressed);
        }
    }

    @Override
    public boolean performClick() {
        boolean result = super.performClick();
        if (mListener != null && getVisibility() == View.VISIBLE) {
            mListener.onShutterButtonClick();
        }
        return result;
    }
    
    public interface RepeatListener
    {
        /** manpeng
         * @param v �û������Button����
         * @param duration �ӳٵĺ�����
         * @param repeatcount �ظ������ص�
         */
        void onRepeat(View v, long duration, int repeatcount);
    }
}
