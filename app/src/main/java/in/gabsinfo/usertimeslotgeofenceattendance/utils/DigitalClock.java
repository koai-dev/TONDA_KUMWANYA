package in.gabsinfo.usertimeslotgeofenceattendance.utils;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;

import androidx.appcompat.widget.AppCompatTextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class DigitalClock extends AppCompatTextView {
    private final static String TAG = "DigitalClock";

    private Calendar mCalendar;
    private String mFormat = "hh:mm aa";

    private Runnable mTicker;
    private Handler mHandler;

    private boolean mTickerStopped = false;
    private long milliSecond=System.currentTimeMillis();
    private OnClockTickChangeEvent onClockTickChangeEventInterface;
    SimpleDateFormat parseFormat = new SimpleDateFormat("hh:mm aa",Locale.US);

    public DigitalClock(Context context) {
        super(context);
        initClock(context);
    }

    public DigitalClock(Context context, AttributeSet attrs) {
        super(context, attrs);
        initClock(context);
    }

    private void initClock(Context context) {
        if (mCalendar == null) {
            mCalendar = Calendar.getInstance();
        }
        
    }
    
    public void setClockTickEventListener(OnClockTickChangeEvent onClockTickChangeEventListener){
    	onClockTickChangeEventInterface=onClockTickChangeEventListener;    	
    }
    
    
    public void setServerDate(long serverTimeInMillis) {
    	milliSecond=serverTimeInMillis;
    }

    @Override
    protected void onAttachedToWindow() {
        mTickerStopped = false;
        super.onAttachedToWindow();
        mHandler = new Handler();

        mTicker = new Runnable() {
            public void run() {
                if (mTickerStopped)
                    return;
                mCalendar.setTimeInMillis(milliSecond);
                setText(parseFormat.format(mCalendar.getTime()));
                //setText(DateFormat.format(mFormat, mCalendar));
                invalidate();
                long now = SystemClock.uptimeMillis();
                // long next = now + (1000 - now % 1000);
                long next = now + (1000 - System.currentTimeMillis() % 1000);

                // Debug
                Log.d(TAG, "" + now);
                Log.d(TAG, "" + next);
                Log.d(TAG, "" + mCalendar.getTimeInMillis());
                milliSecond=milliSecond+1000;
                
                if(onClockTickChangeEventInterface!=null)
                	onClockTickChangeEventInterface.OnTick(milliSecond);

                // TODO
                mHandler.postAtTime(mTicker, next);
            }
        };
        mTicker.run();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mTickerStopped = true;
    }

    public void setFormat(String format) {
        mFormat = format;
    }

}