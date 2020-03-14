package com.example.ghostpb;

import android.widget.Chronometer;
import android.os.SystemClock;
import android.content.Context;
import android.util.AttributeSet;
import android.os.Handler;
import android.os.Message;

public class ChronometerExtended extends Chronometer {

    private long mBase;
    private boolean mVisible;
    private boolean mStarted;
    private boolean mRunning;
    public static final long MILLIS_IN_SECONDS = 1000;
    public static final long MILLIS_IN_MINUTES = 60000;
    public static final long MILLIS_IN_HOURS = 3600000;
    private static final int TICK_WHAT = 2;
    private OnChronometerTickListener mOnChronometerTickListener;

    public ChronometerExtended(Context context) {
        super(context);
    }
    public ChronometerExtended(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mBase = SystemClock.elapsedRealtime();
        updateText(mBase);
    }

    public void setBase(long base) {
        mBase = base;
        dispatchChronometerTick();
        updateText(SystemClock.elapsedRealtime());
    }

    public long getBase() {
        return mBase;
    }

    public void setOnChronometerTickListener(
            OnChronometerTickListener listener) {
        mOnChronometerTickListener = listener;
    }

    public OnChronometerTickListener getOnChronometerTickListener() {
        return mOnChronometerTickListener;
    }

    public void start() {
        mStarted = true;
        updateRunning();
    }

    public void stop() {
        mStarted = false;
        updateRunning();
    }

    @Override
    protected void onDetachedFromWindow() {
        super .onDetachedFromWindow();
        mVisible = false;
        updateRunning();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super .onWindowVisibilityChanged(visibility);
        mVisible = visibility == VISIBLE;
        updateRunning();
    }

    private synchronized void updateText(long now) {
        long timeElapsed = now - mBase;

        int milliseconds  = (int)((timeElapsed % 1000));
        int seconds = (int)((timeElapsed/MILLIS_IN_SECONDS) % 60);
        int minutes = (int)((timeElapsed/MILLIS_IN_MINUTES) % 60);
        int hours   = (int)((timeElapsed/MILLIS_IN_HOURS)   % 24);

        setText(String.format("%02d:%02d:%02d:%03d", hours, minutes, seconds, milliseconds));
    }

    private void updateRunning() {
        boolean running = mVisible && mStarted;
        if (running != mRunning) {
            if (running) {
                updateText(SystemClock.elapsedRealtime());
                dispatchChronometerTick();
                mHandler.sendMessageDelayed(Message.obtain(mHandler,
                        TICK_WHAT), 100);
            } else {
                mHandler.removeMessages(TICK_WHAT);
            }
            mRunning = running;
        }
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message m) {
            if (mRunning) {
                updateText(SystemClock.elapsedRealtime());
                dispatchChronometerTick();
                sendMessageDelayed(Message.obtain(this , TICK_WHAT),
                        100);
            }
        }
    };

    void dispatchChronometerTick() {
        if (mOnChronometerTickListener != null) {
            mOnChronometerTickListener.onChronometerTick(this);
        }
    }
}