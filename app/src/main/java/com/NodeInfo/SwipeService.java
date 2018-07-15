package com.NodeInfo;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Path;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.TextView;

import com.MyApplication;
import com.myswipe.DailogOverLaysView;
import com.myswipe.R;
import com.myswipe.TipOverLaysView;
import com.utils.LogcatFileHelper;
import com.utils.MyBroadcastReceiver;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by jiongfang on 2018/3/1.
 * 无障碍 服务 使用时 资源文件必须注册
 */
public class SwipeService extends AccessibilityService {

    private final static String TAG = "SwipeService";
    private final static String TT_PACKAGE = "com.p1.mobile.putong";
    public static int SCREEN_WIDTH = 600;
    private Context mContext;

    TipOverLaysView tipOverLaysView;
    DailogOverLaysView dailogOverLaysView;

    boolean  startSwipe = false;
    public static long time  = 200;
    public long countSwipe = 0;

    /**
     * 该辅助功能开关是否打开了
     * @param context：上下文
     * @return
     */
    private boolean isAccessibilitySettingsOn(Context context) {
        int accessibilityEnable = 0;
        String serviceName = context.getPackageName() + "/"+SwipeService.this.getClass().getName();
       // LogcatFileHelper.i("Jiong"+TAG,"into isAccessibilitySettingsOn   "+serviceName);
        try {
            accessibilityEnable = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED, 0);
        } catch (Exception e) {
            LogcatFileHelper.e("Jiong"+TAG, "get accessibility enable failed, the err:" + e.getMessage());
        }
        if (accessibilityEnable == 1) {
             TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');
            String settingValue = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();
                   // LogcatFileHelper.i("Jiong"+TAG,"accessibilityService--->"+accessibilityService);
                    if (accessibilityService.equalsIgnoreCase(serviceName)) {
                        LogcatFileHelper.i("Jiong"+TAG, "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        }else {
            LogcatFileHelper.d(TAG,"Accessibility service disable");
        }
        return false;
    }
    private boolean isServiceEnabled() {  //判断无障碍服务是否开启
        AccessibilityManager accessibilityManager = (AccessibilityManager)getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> accessibilityServices = accessibilityManager.getEnabledAccessibilityServiceList( AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
        for (AccessibilityServiceInfo info : accessibilityServices) {
            String thisClassName = this.getClass().getSimpleName();
            LogcatFileHelper.i("Jiong>>","判断无障碍未开启  >>> "+info.getId());
            if (info.getId().contains(thisClassName)) {
                LogcatFileHelper.i("Jiong>>","判断无障碍服务是否开启  开启"+thisClassName);
                return true;
            }
        }
        LogcatFileHelper.i("Jiong>>","判断无障碍服务是否开启  --- 未开启");
        return false;
    }
    private void enableRobService() { //跳转到无障碍服务设置页面
        Intent accessibleIntent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        accessibleIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(accessibleIntent);
    }




    @Override
    public void onInterrupt() {
    }

    //  可以  不用在页面中  就捕获到按键 事件
    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        LogcatFileHelper.i("Jiong>."+TAG,"RobService onKeyEvent========KeyCode="+event.getKeyCode());
        return super.onKeyEvent(event);
    }

    @Override  //捕获到界面的点击事件
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType(); //获得 事件类型
       //  Log.d("Jiong","onAccessibilityEvent !  "+eventType);
     //   AccessibilityNodeInfo noteInfo = event.getSource();  //获得点击的对象
    }

    boolean IsServiceConnected = false;
    MyNodeInfoUtil nodeInfoUtil;
    String getMsg = "";

    @Override
    public void onCreate() {
        super.onCreate();
        LogcatFileHelper.i("Jiong>>"+TAG,"onCreate  into！");
        mContext = this.getBaseContext();
        //注册 广播
        registerReciver(mContext);
        //实例化 Hander
        uiHandler = new UiHandler(Looper.getMainLooper(), this);

        runThread();

        if(isServiceEnabled() ) {  //无障碍 已真正使能    //使能开关打开 -启动服务   （不会自动执行onServiceConnected了 ）
            LogcatFileHelper.i("Jiong","isServiceEnabled !");
            //实例化 一个MyNodeInfoUtil
            nodeInfoUtil = new MyNodeInfoUtil(this);
            IsServiceConnected = true;
    //        runThread();
        }else{  // //无障碍 没真正使能
            if(isAccessibilitySettingsOn(mContext)){
                //延时 再判断
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(isServiceEnabled() ){
                            LogcatFileHelper.i("Jiong"," 延时在判断 isServiceEnabled !");
                            //实例化 一个MyNodeInfoUtil
                            nodeInfoUtil = new MyNodeInfoUtil(SwipeService.this);
                            IsServiceConnected = true;
                            onServiceConnected();//当系统 设置使能使，迅速开机启动 会 isAccessibilitySettingsOn = true 一定延时后 会系统打开使能开关 执行onServiceConnected，故延时2s
                        }else{  //未真正开启无障碍 跳到打开设置页面
                            String msgContent = "请打开'炯文很牛逼'服务!";
                            LogcatFileHelper.i("Jiong",msgContent);
                            MyBroadcastReceiver.sendBroad_MSG_HAL(msgContent);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    enableRobService();
                                }
                            },500);
                        }
                    }
                },2000);
            }else{  //未打开 无障碍 开关 跳到打开设置页面
                String msgContent = "请打'炯文很牛逼'服务!";
                LogcatFileHelper.i("Jiong",msgContent);
                MyBroadcastReceiver.sendBroad_MSG_HAL(msgContent);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        enableRobService();
                    }
                },500);
            }
        }
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
         //判断确认 无障碍服务 服务和功能启动  后  进入 业务处理
        runThread();
         //业务处理
        return super.onStartCommand(intent, flags, startId);
    }

    @Override   //只会  手动打开手机服务时进入
    protected void onServiceConnected() {
        super.onServiceConnected();
        //实例化 一个MyNodeInfoUtil
        nodeInfoUtil = new MyNodeInfoUtil(this);
        IsServiceConnected = true;
        LogcatFileHelper.i("Jiong>>","into onServiceConnected");

   //     runThread();
    }
    @Override
    public void onDestroy() {
        // 去除 广播注册
        unregisterReceiver(receiver);
        //去除 浮窗
        if(tipOverLaysView != null){
            tipOverLaysView.removeOverlays();
        }
        if(dailogOverLaysView != null ){
            dailogOverLaysView.removeOverlays();
        }
        //停止线程
        if(myThread != null){
            myThread.interrupt();
            myThread = null;
        }
        // 去除消息 handler
        if(uiHandler != null ){
            uiHandler.removeCallbacksAndMessages(null);
        }


        MyBroadcastReceiver.sendBroad_MSG_HAL("无障碍服务 被KiLL ！");
        LogcatFileHelper.e("Jiong","RobService->onDestroy !  无障碍服务 被KiLL");
        super.onDestroy();
    }

    //定义静态 内部类 handler
    private UiHandler uiHandler;
    private static class UiHandler extends Handler{
        //定义 弱引用
        private WeakReference<SwipeService> weakReference;
        //构造
        public UiHandler(Looper looper, SwipeService service) {
            super(looper);
            this.weakReference = new WeakReference<SwipeService>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //先 获取 弱引用
            if( weakReference ==null || weakReference.get() == null ) return;
            SwipeService swipeService = weakReference.get();
            //消息处理
            switch(msg.what){
                case 0: //显示 浮窗
                    if( swipeService.tipOverLaysView == null ){
                        swipeService.tipOverLaysView = new TipOverLaysView(MyApplication.getContext());
                        swipeService.tipOverLaysView.showOverlayView();
                        String[] strArray =  new String[]{"5","4","3","2","1","Go"};
                        for(int i=0;i<strArray.length;i++){
                            Message message = new Message();
                            message.what = 2;
                            message.obj  = strArray[i];
                            swipeService.uiHandler.sendMessageDelayed(message, 1000*i);
                        }
                        swipeService.uiHandler.sendEmptyMessageDelayed(1, 1000*strArray.length);
                    }
                    break;
                case 1: //去除 浮窗
                    if(swipeService.tipOverLaysView != null){
                        swipeService.tipOverLaysView.removeOverlays();
                        swipeService.startSwipe = true;
                    }
                    break;
                case 2: //浮窗 字体内容更新
                    String str = (String)msg.obj;
                    if(swipeService.tipOverLaysView != null && !TextUtils.isEmpty(str)){
                        Message message = new Message();
                        message.what = 0;
                        message.obj  = str;
                        swipeService.tipOverLaysView.uiHandler.sendMessage(message);
                    }
                    break;
                case 10:  //浮窗一次性 显示  -- 用于显示 滑动结束提示
                    String str2 = (String)msg.obj;
                    swipeService.tipOverLaysView = null;
                    swipeService.tipOverLaysView = new TipOverLaysView(MyApplication.getContext());
                    swipeService.tipOverLaysView.showOverlayView();
                    Message message = new Message();
                    message.what = 1;
                    message.obj  = str2;
                    swipeService.tipOverLaysView.uiHandler.sendMessage(message);
                    swipeService.uiHandler.sendEmptyMessageDelayed(1,5000);
                    break;
                case 100: //显示交互浮窗 DialogOverLaysView 提示输入 滑动次数
                    if(swipeService.dailogOverLaysView == null ){
                        swipeService.dailogOverLaysView = new DailogOverLaysView(MyApplication.getContext());
                        swipeService.dailogOverLaysView.showOverlayView();
                    }
                    break;

                default: break;
            }
        }
    }


    private MyThread myThread;
    private void runThread(){
        if(myThread == null){
            myThread = new MyThread();
            myThread.start();
        }else{
            if(!myThread.isAlive()){
                myThread = null;
                myThread = new MyThread();
                myThread.start();
            }
        }
    }
    //心跳 检测服务 线程
    private class MyThread extends Thread {
        @Override
        public void run() {
            super.run();

            try {
                //返回桌面 并保证 打开探探
                boolean backHome = false;
                boolean sendDialogShow = false;
                while(true){
                    LogcatFileHelper.i("Jiong","线程  ---- alive");
                    //显示 对话框浮窗
                    if(!sendDialogShow){
                        uiHandler.sendEmptyMessage(100);
                        sendDialogShow = true;
                    }

                    if(nodeInfoUtil != null){
                        //返回 桌面
                        if(!backHome){
                            nodeInfoUtil.goGlobalHome();
                            Thread.sleep(500);
                            backHome = true;
                        }

                        //启动 探探App
                        nodeInfoUtil.startTApp(mContext,TT_PACKAGE);
                        Thread.sleep(2000);

                        int ret = nodeInfoUtil.isInTargetApp(TT_PACKAGE);
                        if( ret == 1){
                            LogcatFileHelper.i("Jiong","线程中--探探已启动！");
                            break;
                        }else if(ret == 0){
                            //启动 探探App
                            nodeInfoUtil.startTApp(mContext,TT_PACKAGE);

                        }
                    }
                    Thread.sleep(1000);
                }

                Thread.sleep(1000);
                //考虑 显示 对话框 交互输入滑动次数
                //uiHandler.sendEmptyMessage(100);
               // LogcatFileHelper.i("Jiong","显示Dialog");
                //Thread.sleep(10000);

                //启动 倒计时 浮窗 ---  有些手机 需要权限开启才显示
                uiHandler.sendEmptyMessage(0);
                //判断倒计时 结束
                while(!startSwipe){
                    Thread.sleep(200);
                }

                //开始滑动
                MyBroadcastReceiver.sendBroad_MSG_HAL("开始滑动！");
                Path path = new Path();
                path.moveTo(100, 500);
                //path.lineTo(SCREEN_WIDTH > 600 ? SCREEN_WIDTH:600,500);
                path.lineTo(600,500);
                GestureDescription.Builder builder =  new GestureDescription.Builder().addStroke(new GestureDescription.StrokeDescription(path,20,100)); //long startTime, long duration
                GestureDescription gestureDescription = builder.build();
                long count = time;
                while (count > 0) {
                    count--;
                    countSwipe++;
                    Thread.sleep(200);
                    LogcatFileHelper.i("Jiong","线程  ---->> alive");
                    if(nodeInfoUtil != null){
                        SwipeService.this.dispatchGesture(gestureDescription, new GestureResultCallback() {
                            @Override
                            public void onCancelled(GestureDescription gestureDescription) {
                                super.onCancelled(gestureDescription);
                                LogcatFileHelper.i("Jiong>>","RobService dispatchGesture---- 回调取消----->");
                            }

                            @Override
                            public void onCompleted(GestureDescription gestureDescription) {
                                super.onCompleted(gestureDescription);
                                LogcatFileHelper.i("Jiong>>","RobService dispatchGesture----回调结束----->countSwipe="+countSwipe);
                            }
                        }, null);
                    }
                }

                //发送 滑动结束 浮窗提示
                Message message = new Message();
                message.what = 10;
                message.obj = "滑动结束！\n 您滑动妹子：\n "+countSwipe+" 位";
                uiHandler.sendMessage(message);

                Thread.sleep(10*1000);
                SwipeService.this.stopSelf();

            }catch (InterruptedException e){
                LogcatFileHelper.e("Jiong>>","TAG--Die!");
            }
        }
    }
    /**
     * 校验某个服务是否还活着
     * serviceName :传进来的服务的名称
     */
    public static boolean isServiceRunning(Context context, String serviceName){
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> infos = am.getRunningServices(100);
        for(ActivityManager.RunningServiceInfo info : infos){
            String name = info.service.getClassName();
            //String packagename = info.service.getPackageName();  // com.kugouk9
           // Log.d("RobService","获得的服务包名称:"+packagename);
            if(name.equals(serviceName)){
                return true;
            }
        }
        return false;
    }

    //广播接收器  注册
    RobBroadcastReceiver receiver;
    public final static String ACTION_MSG_START_CHECK_UPDATE = "com.mycom.Robstvice.Test"; //检测更新
    private void registerReciver(Context context){
        receiver = new RobBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_MSG_START_CHECK_UPDATE);

        context.registerReceiver(receiver,intentFilter);
    }
    private class RobBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogcatFileHelper.i("Jiong>>"+TAG,"接收到广播："+action);

        }
    }



}
