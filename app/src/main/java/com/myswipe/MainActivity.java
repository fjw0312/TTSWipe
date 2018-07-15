package com.myswipe;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;

import com.MyApplication;
import com.NodeInfo.SwipeService;
import com.utils.FullScreenUI;
import com.utils.LogcatFileHelper;


public class MainActivity extends AppCompatActivity {


    private Context mContext;
    private long times;

    int  screenWith = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FullScreenUI.FullScreenIntoFlagUI(this);
    //    setContentView(R.layout.activity_main);
        mContext = getBaseContext();

        times = System.currentTimeMillis();

        //屏幕
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        screenWith = dm.widthPixels;
        SwipeService.SCREEN_WIDTH = screenWith;
        LogcatFileHelper.i("Jiong>>","屏幕宽度："+screenWith);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this, SwipeService.class);
                mContext.startService(intent);

           //     TipOverLaysView overLaysView = new TipOverLaysView(MyApplication.getContext());
          //      overLaysView.showOverlayView();

               MainActivity.this.finish();
            }
        }, 1000);
    }

}
