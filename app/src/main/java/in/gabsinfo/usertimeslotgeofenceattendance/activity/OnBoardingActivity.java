package in.gabsinfo.usertimeslotgeofenceattendance.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import in.gabsinfo.usertimeslotgeofenceattendance.R;
import in.gabsinfo.usertimeslotgeofenceattendance.adapter.OnBoardAdapter;
import in.gabsinfo.usertimeslotgeofenceattendance.fragment.SettingsFragment;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.SharePreferences;

public class OnBoardingActivity extends AppCompatActivity implements View.OnClickListener {

    RelativeLayout mRootLayout;
    ViewPager mViewPager;
    LinearLayout mDotsLayout;
    Button mBtnSkip,mBtnNext;
    RelativeLayout mRlContainer;

    private TextView dots[];
    OnBoardAdapter sliderAdapter;

    // img Array
    public String[] image_slide ={
            "profile.json",
            "scanner.json",
            "calender.json",
            "notification.json",
            "reminder.json"
    };

    // heading Array
    private int[] heading_slide ={
            R.string.ob_header1,
            R.string.ob_header2,
            R.string.ob_header3,
            R.string.ob_header4,
            R.string.ob_header5
    };

    // description Array
    private int[] description_slide ={
            R.string.ob_desc1,
            R.string.ob_desc2,
            R.string.ob_desc3,
            R.string.ob_desc4,
            R.string.ob_desc5
    };
   ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_boarding);

        mRootLayout =findViewById(R.id.rl_root);
        mViewPager =findViewById(R.id.sViewPager);
        mDotsLayout =findViewById(R.id.layoutDots);
        mBtnSkip=findViewById(R.id.btn_skip);
        mBtnNext=findViewById(R.id.btn_next);
        mRlContainer = findViewById(R.id.rl_container);

        // create Adapter object
        sliderAdapter = new OnBoardAdapter(this,image_slide,heading_slide,description_slide);
        // set adapter in ViewPager
        mViewPager.setAdapter(sliderAdapter);
        // set PageChangeListener
        mViewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        // adding bottom dots -> addBottomDots(0);
//      addDotIndicator(0);
        addBottomDots(0);

        //Set Status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#EA4B4B"));
        }
        mRootLayout.setBackgroundColor(Color.parseColor("#EA4B4B"));

        mBtnSkip.setOnClickListener(this);
        mBtnNext.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.btn_next){
            // checking for last page
            // if last page home screen will be launched
            int current = getItem(1);
//        if (current < layouts.length) {
            if (current < image_slide.length) {
                // move to next screen
                mViewPager.setCurrentItem(current);
            } else {
                loadFragment(new SettingsFragment(), null);
            }
        }else if (v.getId()==R.id.btn_skip){
            loadFragment(new SettingsFragment(), null);
        }
    }


    private int getItem(int i) {
        return mViewPager.getCurrentItem() + i;
    }


    // viewPagerPage ChangeListener according to Dots-Points
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            switch (position){
                case  0:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        getWindow().setStatusBarColor(Color.parseColor("#EA4B4B"));
                    }
                    mRootLayout.setBackgroundColor(Color.parseColor("#EA4B4B"));
                    break;
                case  1:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        getWindow().setStatusBarColor(Color.parseColor("#3287F8"));
                    }
                    mRootLayout.setBackgroundColor(Color.parseColor("#3287F8"));
                    break;
                case  2:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        getWindow().setStatusBarColor(Color.parseColor("#00B59B"));
                    }
                    mRootLayout.setBackgroundColor(Color.parseColor("#00B59B"));
                    break;
                case  3:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        getWindow().setStatusBarColor(Color.parseColor("#FF428D"));
                    }
                    mRootLayout.setBackgroundColor(Color.parseColor("#FF428D"));
                    break;
                case  4:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        getWindow().setStatusBarColor(Color.parseColor("#336187"));
                    }
                    mRootLayout.setBackgroundColor(Color.parseColor("#336187"));
                    break;
            }

            addBottomDots(position);

            // changing the next button text 'NEXT' / 'GOT IT'
            if (position == image_slide.length - 1) {
                // last page. make button text to GOT IT
                mBtnNext.setText(getString(R.string.start));
                mBtnSkip.setVisibility(View.GONE);
            } else {
                // still pages are left
                mBtnNext.setText(getString(R.string.next));
                mBtnSkip.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    // set of Dots points
    private void addBottomDots(int currentPage) {
        dots = new TextView[5];
        mDotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(getResources().getColor(android.R.color.white));  // dot_inactive
            mDotsLayout.addView(dots[i]);
        }

        if (dots.length > 0)
            dots[currentPage].setTextColor(getResources().getColor(android.R.color.black)); // dot_active
    }



    public void loadFragment(Fragment fragment, Bundle bundle) {
        mRlContainer.setVisibility(View.VISIBLE);
        if (fragment != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(ContextCompat.getColor(this,
                        R.color.colorPrimaryDark));
            }
            fragment.setArguments(bundle);
            try {
                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.slide_from_right,
                                R.anim.slide_to_left,
                                R.anim.slide_from_left,
                                R.anim.slide_to_right)
                        .replace(R.id.rl_container, fragment).commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void addFragment(Fragment fragment, Bundle bundle) {
        if (fragment != null) {
            fragment.setArguments(bundle);
            try {
                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.slide_from_right,
                                R.anim.slide_to_left,
                                R.anim.slide_from_left,
                                R.anim.slide_to_right)
                        .add(R.id.rl_container, fragment).addToBackStack(null).commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void launchActivity() {
        SharePreferences.setBool(SharePreferences.KEY_IS_WELCOME_SCREEN_SHOWN, true);
        startActivity(new Intent(this, SignInActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount()>0){
            getSupportFragmentManager().popBackStack();
        }else {
            super.onBackPressed();
            finish();
        }
    }

}
