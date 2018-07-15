package com.myswipe;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.NodeInfo.SwipeService;
import com.utils.LogcatFileHelper;

import java.lang.ref.WeakReference;

/**
 * Created by jiongfang on 2018/7/14.
 * 系统 浮窗
 */
public class DailogOverLaysView {

    public DailogOverLaysView(Context context){
        mContext = context;
        if(uiHandler == null){
            uiHandler = new UiHandler(Looper.getMainLooper(),this);
        }
    }

    private static final String TAG = "DailogOverLaysView";
    private Context mContext;
    private View mView;
    private static boolean isShow = false;
    //布局参数.
    WindowManager.LayoutParams params;
    //实例化的WindowManager.
    WindowManager windowManager;
    //初始化 浮窗
    private void initOverlaysView(){
        if (isShow) {
            return;
        }
        isShow = true;

        if(this.findViewInterface == null){
            setUpView();
        }

        //赋值WindowManager&LayoutParam.
        params = new WindowManager.LayoutParams();
        windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        //设置type.系统提示型窗口，TYPE_SYSTEM_ALERT一般都在应用程序窗口之上.

        if(Build.VERSION.SDK_INT >= 26){  //android 8.0 及以上
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }else if(Build.VERSION.SDK_INT == 25){  //android 7.1
            //params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;  // 需要授权 能触摸
             params.type = WindowManager.LayoutParams.TYPE_PHONE;
        }else if(Build.VERSION.SDK_INT > 19){  //android 7.0 及以下
            params.type = WindowManager.LayoutParams.TYPE_TOAST;  // 不用授权 <=24 可使用
        }else{  //android 4.4 以下
            params.type = WindowManager.LayoutParams.TYPE_PHONE; // (需要权限）不能触摸 23 26版本不能使用
        }
    /*
        if (Build.VERSION.SDK_INT >= 23) {
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;    //sdk >= 23
            // params.type = WindowManager.LayoutParams.TYPE_PHONE;
        }else{
            params.type = WindowManager.LayoutParams.TYPE_TOAST;            //sdk  <= 23
        }
    */
        //设置flags.
    //    params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE  //不可聚焦及不可使用按钮对悬浮窗进行操控.
    //            |WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
    //            |WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        params.flags = WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM; //设置flags. 聚焦

        params.gravity = Gravity.CENTER; //设置窗口初始停靠位置. //params.gravity = Gravity.LEFT | Gravity.TOP;
        params.format = PixelFormat.TRANSLUCENT;// params.format = PixelFormat.RGBA_8888;//设置效果为背景透明.

        //设置悬浮窗口长宽数据.
        //注意，这里的width和height均使用px而非dp.这里我偷了个懒
        //如果你想完全对应布局设置，需要先获取到机器的dpi
        //px与dp的换算为px = dp * (dpi / 160).
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        //params.x = 0;
        //params.y = 0;

        if(mView != null ){
            windowManager.addView(mView,params);
        }

        LogcatFileHelper.i("Jiong>>"+TAG,"into initOverlaysView!");
    }


    TextView content;
    EditText edit_input;
    TextView submit;
    //添加视图布局
    public View setUpView() { //获得 控件 并 操作
        mView = LayoutInflater.from(mContext).inflate(R.layout.overlay_dailog, null);
        //获得控件
        content = (TextView)mView.findViewById(R.id.content);
        edit_input = (EditText)mView.findViewById(R.id.edit_input);
        submit = (TextView)mView.findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String input = edit_input.getText().toString();
                long time = 0;
                if(!TextUtils.isEmpty(input)){
                    try{
                        time = Long.parseLong(input);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    if(time > 0){
                        SwipeService.time = time;
                    }
                }
                removeOverlays();
            }
        });

        return mView;
    }
    public View setUpView(FindViewInterface findViewInterface,int layout_id) { //获得 控件 并 操作
        this.findViewInterface = findViewInterface;
        if(this.findViewInterface != null){  //后期 布局代入
            mView = LayoutInflater.from(mContext).inflate(layout_id, null);
            this.findViewInterface.OnFindViewById(mView);
        }else{  //布局 写死
            setUpView();
        }
        return mView;
    }
    //可以 后期布局带入
    private FindViewInterface findViewInterface;
    public interface FindViewInterface{
        public void OnFindViewById(View view);
    }

    //显示 浮窗视图
    public void showOverlayView(){
            LogcatFileHelper.i("Jiong>>"+TAG,"showOverlayView");
            initOverlaysView();
    }

    // 关闭 浮窗
    public void removeOverlays(){
        isShow = false;
        try {
            uiHandler.removeCallbacksAndMessages(null);
            windowManager.removeView(mView);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public UiHandler uiHandler;
    public static class UiHandler extends Handler{
        private WeakReference<DailogOverLaysView> weakReference;
        public UiHandler(Looper looper, DailogOverLaysView tipOverLaysView) {
            super(looper);
            this.weakReference  = new WeakReference<DailogOverLaysView>(tipOverLaysView);
        }
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(weakReference == null || weakReference.get() == null) return;
            DailogOverLaysView tipOverLaysView = weakReference.get();
            switch (msg.what){
                case 0:

                    break;
                case 1:

                    break;
                default: break;
            }
        }
    }
}
/**
 * 使用方式
 * 1. 定义该变量 OverlaysBaseView overlay  实例化 new OverlaysBaseView
 * 2. 设置视图 setUpView(FindViewInterface,int)  // 或 直接改写 该类 setUpView() 方法
 * 3. 显示浮窗 showOverlayView();
 * 4. 去除 浮窗 removeOverlays();
 * */