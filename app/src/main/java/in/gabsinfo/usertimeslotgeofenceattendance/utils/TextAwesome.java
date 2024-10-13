package in.gabsinfo.usertimeslotgeofenceattendance.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;

import androidx.appcompat.widget.AppCompatTextView;


/**
 * Created by tutsberry.com
 */
 public class TextAwesome extends AppCompatTextView {
    private static final String TAG = TextAwesome.class.getSimpleName();
    //Cache the font load status to improve performance
    private static Typeface font;
 
    public TextAwesome(Context context) {
       super(context);
       setFont(context);
    }
 
    public TextAwesome(Context context, AttributeSet attrs) {
      super(context, attrs);
      setFont(context);
    }
 
    public TextAwesome(Context context, AttributeSet attrs, int defStyleAttr) {
      super(context, attrs, defStyleAttr);
      setFont(context);
    }
 
    private void setFont(Context context) {
      // prevent exception in Android Studio / ADT interface builder
      if (this.isInEditMode()) {
        return;
      }
 
     //Check for font is already loaded
     if(font == null) {
       try {
          font = Typeface.createFromAsset(context.getAssets(), "fontawesome-webfont.ttf");
       } catch (RuntimeException e) {
          Log.e(TAG, "Font awesome not loaded");
       }
     }
 
     //Finally set the font
     setTypeface(font);
   }
}