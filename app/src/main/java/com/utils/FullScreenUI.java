package com.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by Administrator on 2017/10/19.
 * 设置 页面 全屏 沉浸模式/状态、主题、导航栏隐藏模式
 * 注意4.4版本时，还存在bug.可参考  http://www.jianshu.com/p/dc20e98b9a90
 *
 */

public class FullScreenUI {

    //全屏 沉浸模式
    public static void FullScreenIntoFlagUI(AppCompatActivity activity){
        Window window = activity.getWindow();
        if (Build.VERSION.SDK_INT >= 21) {  //5.0版本以上
            View decorView = window.getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            window.setNavigationBarColor(Color.TRANSPARENT);
            window.setStatusBarColor(Color.TRANSPARENT);
        }else{
            //Window window = activity.getWindow();
            WindowManager.LayoutParams attributes = window.getAttributes();
            int flagTranslucentStatus = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            int flagTranslucentNavigation = WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
            attributes.flags |= flagTranslucentStatus;
//                attributes.flags |= flagTranslucentNavigation;
            window.setAttributes(attributes);

        }
        //隐藏 标题栏
        ActionBar actionBar = activity.getSupportActionBar(); //activity.getActionBar();
        if(actionBar != null){
            actionBar.hide();
        }
    }

    //全屏保存导航栏 沉浸模式
    public static void FullScreenUI(AppCompatActivity activity){
        Window window = activity.getWindow();
        if (Build.VERSION.SDK_INT >= 21) {  //5.0版本以上
            View decorView = window.getDecorView();
            int option =View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
        //    window.setNavigationBarColor(Color.TRANSPARENT);
            window.setStatusBarColor(Color.TRANSPARENT);
        }else{
            //Window window = activity.getWindow();
            WindowManager.LayoutParams attributes = window.getAttributes();
            int flagTranslucentStatus = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            attributes.flags |= flagTranslucentStatus;
//                attributes.flags |= flagTranslucentNavigation;
            window.setAttributes(attributes);

        }
        //隐藏 标题栏
        ActionBar actionBar = activity.getSupportActionBar(); //activity.getActionBar();
        if(actionBar != null){
            actionBar.hide();
        }


    }

    //全屏状态、主题、导航栏隐藏模式
    public static void FullSreenHideFlagUI(Activity activity){
        if ( Build.VERSION.SDK_INT >= 19) {
            View decorView = activity.getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }else{
            new AlertDialog.Builder(activity).setMessage("android系统版本太低，建议手机扔掉！").show();
        }
    }


}
