
package com.example.ghostpb;

import android.widget.Chronometer;
import android.os.SystemClock;
import android.content.Context;
import android.util.AttributeSet;
import android.os.Handler;
import android.os.Message;


//A chronometer extended from Android's to support additional features
//such as displaying milliseconds and getting back the time

//Class implements a simple timer

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
        //super(parameter list) will call the superclass constructor
        //with a matching parameter list.
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

    //Sets the listener to be called when the chronometer changes.
    public void setOnChronometerTickListener(OnChronometerTickListener listener) {
        mOnChronometerTickListener = listener;
    }

    //return The listener (may be null) that is listening for chronometer change events
    public OnChronometerTickListener getOnChronometerTickListener() {
        return mOnChronometerTickListener;
    }

    //Start counting up.  This does not affect the base as set from setBase, just the view display.
    //Chronometer works by regularly scheduling messages to the handler, even when the
    //Widget is not visible.  To make sure resource leaks do not occur, the user should
    //make sure that each start() call has a reciprocal call to stop

    public void start() {
        mStarted = true;
        updateRunning();
    }

    //Stop counting up.  This does not affect the base as set from setBase, just the view display.
    //This stops the messages to the handler,
    public void stop() {
        mStarted = false;
        updateRunning();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mVisible = false;
        updateRunning();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        mVisible = visibility == VISIBLE;
        updateRunning();
    }

    //synchronized prevents multiple threads from running
    //the code contained within the braces
    //Essentially, only 1 thread is allowed to run this code at a time
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
                        TICK_WHAT), 10);
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
                        10);
            }
        }
    };

    void dispatchChronometerTick() {
        if (mOnChronometerTickListener != null) {
            mOnChronometerTickListener.onChronometerTick(this);
        }
    }

    public long getTimePassed() {
        String chronometerText = this.getText().toString();
        String array[] = chronometerText.split(":");

        //Calculates time passed in milliseconds
        //Array[0] = hours
        //Array[1] = minutes
        //Array[2] = seconds
        //Array[3] = milliseconds

        long timePassed = Integer.parseInt(array[0]) * MILLIS_IN_HOURS
                + Integer.parseInt(array[1]) * MILLIS_IN_MINUTES
                + Integer.parseInt(array[2]) * MILLIS_IN_SECONDS
                + Integer.parseInt(array[3]);

        return timePassed;
    }
}