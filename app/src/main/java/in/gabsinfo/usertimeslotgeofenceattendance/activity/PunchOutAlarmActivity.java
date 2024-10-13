package in.gabsinfo.usertimeslotgeofenceattendance.activity;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import in.gabsinfo.usertimeslotgeofenceattendance.R;

public class PunchOutAlarmActivity extends AppCompatActivity {

    private MediaPlayer mMediaPlayer;
    private Vibrator mVibrator;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.activity_punch_out_alarm);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        TextView tvLabel = findViewById(R.id.tv_label);
        Button btnOk = findViewById(R.id.btnOk);

        if (getIntent() != null
                && getIntent().hasExtra("UserName")) {
            tvLabel.setText("Hi ".concat(
                    getIntent().getStringExtra("UserName")).concat(
                    ", \nYou have completed your office working hours.\nIts time to go home."));
        } else {
            tvLabel.setText("Hi ".concat(
                    ", \nYou have completed your office working hours.\nIts time to go home."));
        }

        mMediaPlayer = MediaPlayer.create(this, R.raw.ton);
        mMediaPlayer.setLooping(true);
        mMediaPlayer.start();

        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] mVibratePattern = new long[]{0, 400, 800, 600, 800, 800, 800, 1000};
        // 3 : Repeat this pattern from 0 element of an array
        mVibrator.vibrate(mVibratePattern, 0);


        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mVibrator.cancel();
                mMediaPlayer.stop();

                Intent intent=new Intent(PunchOutAlarmActivity.this,SplashActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });


    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent == null) {
            return;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onDestroy() {
        if (mVibrator != null)
            mVibrator.cancel();

        if (mMediaPlayer != null)
            mMediaPlayer.stop();
        super.onDestroy();
    }
}
