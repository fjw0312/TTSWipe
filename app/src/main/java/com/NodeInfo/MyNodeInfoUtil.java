package com.NodeInfo;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Path;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;

import com.MyApplication;
import com.utils.LogcatFileHelper;
import com.utils.MyBroadcastReceiver;

import java.util.List;

/**
 * Created by jiongfang on 2018/3/1.
 * 获得 无障碍服务的视图 操作接口工具
 */
public class MyNodeInfoUtil {
    private static final String TAG = "NodeInfoUtil";

    public MyNodeInfoUtil(AccessibilityService accessibilityService){
        this.mAccessibilityService = accessibilityService;
    }
    public static AccessibilityService mAccessibilityService = null;

    //输入 复制黏贴
    public static boolean setClipboard(AccessibilityNodeInfo NodeInfo, String Str) {
      //API>=21 时
        NodeInfo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,null);//清除进入内容
        Bundle arguments = new Bundle();
        arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, Str);
        NodeInfo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
       return true;
    }
    //输入 复制黏贴
    public static boolean setClipboard(Context context, AccessibilityNodeInfo NodeInfo, String clearbuttonId, String Str){
        //android>18 时   需要先清除
        AccessibilityNodeInfo newInfo = getCurrentWindowNodeInfo(clearbuttonId); //获得ting 控件
        if(newInfo!=null){
            newInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("text", Str);
        clipboard.setPrimaryClip(clip);
        //焦点（NodeInfo是AccessibilityNodeInfo对象）
        NodeInfo.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
        NodeInfo.performAction(AccessibilityNodeInfo.ACTION_PASTE);//粘贴进入内容

        return false;
    }
    //启动 一个第三方app      --- 会 重启kugou
    public static void startTApp(String inPackage, String inClassName){
        try {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setComponent(new ComponentName(inPackage, inClassName));
            MyApplication.getContext().startActivity(intent);
        }catch (ActivityNotFoundException e){
            e.printStackTrace();
           // MyBroadcastReceiver.sendBroad_MSG("请安装Kugou App");
            LogcatFileHelper.e("Jiong>>"+TAG,"不能启动 Kugou App");
        }
    }
    //启动  一个 第三方App  只需要包名    --- 不会 重启kugou
    public static void startTApp(Context context, String inPackage){
        PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(inPackage);
        if(intent==null){
            MyBroadcastReceiver.sendBroad_MSG_HAL("请安装探探 App");
           // LogcatFileHelper.e("Jiong>>"+TAG,"不能启动 Kugou App");
        }else{
            context.startActivity(intent);
        }
    }


    //判断 是否还在 目标App界面内
    public static int isInTargetApp(String inPackage){
        if(mAccessibilityService == null)  return -1;
        AccessibilityNodeInfo NodeInfo = mAccessibilityService.getRootInActiveWindow(); //获取整个窗口视图对象
       // LogcatFileHelper.i("Jiong>>HH","包名："+NodeInfo.getPackageName());
        if (NodeInfo != null) {
            if(inPackage.equals(NodeInfo.getPackageName())) {  //是否已启动 App
                NodeInfo.recycle();
                return 1;
            }else{
                Log.i("Jiong>>","不在 目标App 界面内");
                return 0;
            }
        }else{
            Log.i("Jiong>>","障碍服务 失效");
        }
        return -1;
    }

    //判别 启动目标App   //返回参数 1：在目标App 0:不在目标App -1:不能获取到界面信息
    public static int startTargetApp(Context context, String inPackage){
        if(mAccessibilityService == null)  return -1;
        AccessibilityNodeInfo NodeInfo = mAccessibilityService.getRootInActiveWindow(); //获取整个窗口视图对象
        if (NodeInfo != null) {
            if(inPackage.equals(NodeInfo.getPackageName())){  //是否已启动 App
                NodeInfo.recycle();
                return 1;
            }else{  //没启动App
                startTApp(context,inPackage);
                Log.i("Jiong>>","拉起App:"+inPackage);
                return 0;
            }

        }else{
            Log.i("Jiong>>","getRootInActiveWindow() == null  怀疑 无障碍服务 失效！");
            //可以 考虑重新 拉起service
            return -1;
        }
    }

    //回收List<AccessibilityNodeInfo>     //返回 该list 个数大小
    public static int recycleNodeInfoList(List<AccessibilityNodeInfo> list){
        int size = 0;
        if(list != null){
            size = list.size();
            for(int i=0;i<list.size();i++){
                list.get(i).recycle();
            }
            list.clear();
        }
        return size;
    }
    //回收AccessibilityNodeInfo   //nodeInfo!=null 返回true
    public static boolean recycleNodeInfo(AccessibilityNodeInfo nodeInfo){
        if(nodeInfo == null){
            return false;
        }else{
            nodeInfo.recycle();
            return true;
        }
    }
    //获得 当前视图
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static List<AccessibilityNodeInfo> getCurrentWindow_list(String nodeId){
        if(mAccessibilityService == null) return null;
        int time = 0;
        while(time<3){
            time++;
            AccessibilityNodeInfo NodeInfo = mAccessibilityService.getRootInActiveWindow(); //获取整个窗口视图对象
            if (NodeInfo != null) {
                List<AccessibilityNodeInfo> listView = NodeInfo.findAccessibilityNodeInfosByViewId(nodeId);
                NodeInfo.recycle();
                if(listView==null|| listView.size()==0){
                    //  MyBroadcastReceiver.sendBroad_Error_HAL("无法根据Id获得视图！");
                }else{
                    return listView;
                }
            }
                try {
                    Thread.sleep(100);
                 }catch (Exception e) {
                    e.printStackTrace();
                }

        }
            Log.i("Jiong>>","getCurrentWindow() 无法获得视图!");
            MyBroadcastReceiver.sendBroad_Error_HAL("无法获得视图！");

        return null;
    }
    //获得 当前视图
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static AccessibilityNodeInfo getCurrentWindowNodeInfo(String nodeId){
        if(mAccessibilityService == null) return null;
        AccessibilityNodeInfo Node = null;
        List<AccessibilityNodeInfo> listView;
        AccessibilityNodeInfo NodeInfo = mAccessibilityService.getRootInActiveWindow(); //获取整个窗口视图对象
        if (NodeInfo != null) {
            listView = NodeInfo.findAccessibilityNodeInfosByViewId(nodeId);
            NodeInfo.recycle();
            if(listView==null|| listView.size()==0){
               // Log.i("Jiong>>","getCurrentWindow() 无法根据Id获得视图!");
               // MyBroadcastReceiver.sendBroad_Error_HAL("无法根据Id获得视图！");
            }else{
                Node = listView.get(0);
            }
        }else{
            Log.i("Jiong>>","getCurrentWindow() 无法获得视图!");
            MyBroadcastReceiver.sendBroad_Error_HAL("无法获得视图！");
        }
        return Node;
    }
    //获得视图 列表
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static List<AccessibilityNodeInfo> getCurrentWindow_NodeInfoList_byText(String text){
        if(mAccessibilityService == null) return null;
        List<AccessibilityNodeInfo> listView = null;
        AccessibilityNodeInfo NodeInfo = mAccessibilityService.getRootInActiveWindow(); //获取整个窗口视图对象
        if(NodeInfo!=null){
            listView = NodeInfo.findAccessibilityNodeInfosByText(text);
            NodeInfo.recycle();
            if(listView==null|| listView.size()==0){
        //        Log.i("Jiong>>","getCurrentWindow() 无法根据Id获得视图!");
        //        MyBroadcastReceiver.sendBroad_Error_HAL("无法根据Id获得视图！");
            }
        }else{
            Log.i("Jiong>>","getCurrentWindow() 无法获得视图!");
            MyBroadcastReceiver.sendBroad_Error_HAL("无法获得视图！");
        }
        return listView;
    }
    //获得视图
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static AccessibilityNodeInfo getCurrentWindow_NodeInfo_byText(String text){
        if(mAccessibilityService == null) return null;
        AccessibilityNodeInfo Node = null;
        List<AccessibilityNodeInfo> listView = null;
        AccessibilityNodeInfo NodeInfo = mAccessibilityService.getRootInActiveWindow(); //获取整个窗口视图对象
        if(NodeInfo!=null){
            listView = NodeInfo.findAccessibilityNodeInfosByText(text);
            NodeInfo.recycle();
            if(listView==null|| listView.size()==0){
             //   Log.i("Jiong>>","getCurrentWindow() 无法根据Id获得视图!");
             //   MyBroadcastReceiver.sendBroad_Error_HAL("无法根据Id获得视图！");
            }else{
                    Node = listView.get(0);
            }
        }else{
            Log.i("Jiong>>","getCurrentWindow() 无法获得视图!");
 //           MyBroadcastReceiver.sendBroad_Error_HAL("无法获得视图！");
        }
        return Node;
    }

    public static AccessibilityNodeInfo getAccessibilityNodeInfo(AccessibilityNodeInfo NodeInfo, String nodeId){
        if(NodeInfo==null) return null;
        AccessibilityNodeInfo Node = null;
        List<AccessibilityNodeInfo> listView = NodeInfo.findAccessibilityNodeInfosByViewId(nodeId);
        if(NodeInfo!=null){
            if(listView==null|| listView.size()==0){
                //   Log.i("Jiong>>","getCurrentWindow() 无法根据Id获得视图!");
                //   MyBroadcastReceiver.sendBroad_Error_HAL("无法根据Id获得视图！");
            }else{
                    Node = listView.get(0);
            }
        }
        return Node;
    }

    public CurrentWindowListener currentWindowListener;
    public interface CurrentWindowListener{
        public void OnNodeInfoDoFunction(AccessibilityNodeInfo NodeInfo);
    }
    //获取 当前 视图 并做逻辑操作
    public void setCurrentWindowListener(CurrentWindowListener listener){
        if(mAccessibilityService == null) return;
        if(listener == null) return;
        currentWindowListener =  listener;
       // AccessibilityNodeInfo Node = null;
       // List<AccessibilityNodeInfo> listView = null;
        AccessibilityNodeInfo NodeInfo = mAccessibilityService.getRootInActiveWindow(); //获取整个窗口视图对象
        if(NodeInfo!=null){
            currentWindowListener.OnNodeInfoDoFunction(NodeInfo);
            NodeInfo.recycle();
        }
    }

    //获得子视图
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static AccessibilityNodeInfo getItemAccessibilityNodeInfo(String nodeId, int index){
        AccessibilityNodeInfo NodeInfo = getCurrentWindowNodeInfo(nodeId);
        if(NodeInfo!=null && NodeInfo.getChildCount()>index){
            return NodeInfo.getChild(index);
        }
        return null;
    }

    //判断 当前页面下  该控件是否存在
    public static boolean IsHasCurrentWindowNodeInfo_byText(String text){
        AccessibilityNodeInfo NodeInfo = getCurrentWindow_NodeInfo_byText(text);
        return recycleNodeInfo(NodeInfo); //返回该控件是否存在 并释放内存对象
    }
    //判断 当前页面下  该控件是否存在并 可见
    public static boolean IsHasAndVisibleCurrentWindowNodeInfo_byText(String text){
        boolean canVisible = false;
        AccessibilityNodeInfo NodeInfo = getCurrentWindow_NodeInfo_byText(text);
        if(NodeInfo!=null){
            canVisible = NodeInfo.isVisibleToUser();
            recycleNodeInfo(NodeInfo); //释放内存对象
        }
        return canVisible; //返回该控件是否存在并 可见
    }

    //判断 当前页面下  该控件是否存在
    public static boolean IsHasCurrentWindowNodeInfo(String nodeId){
        AccessibilityNodeInfo NodeInfo = getCurrentWindowNodeInfo(nodeId);
        return recycleNodeInfo(NodeInfo); //返回该控件是否存在 并释放内存对象
    }
    //判断 当前页面下  该控件是否存在并 可见
    public static boolean IsHasAndVisibleCurrentWindowNodeInfo(String nodeId){
        boolean canVisible = false;
        AccessibilityNodeInfo NodeInfo = getCurrentWindowNodeInfo(nodeId);
        if(NodeInfo!=null){
            canVisible = NodeInfo.isVisibleToUser();
            recycleNodeInfo(NodeInfo); //释放内存对象
        }
        return canVisible; //返回该控件是否存在并 可见
    }
    //触发点击 控件
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static void clickNodeInfo(AccessibilityNodeInfo NodeInfo){
        if(NodeInfo==null) return;
        NodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
    }
    //触发点击 控件
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static boolean clickNodeInfo_byText(String text){
        AccessibilityNodeInfo NodeInfo = getCurrentWindow_NodeInfo_byText(text);
        clickNodeInfo(NodeInfo);
        return recycleNodeInfo(NodeInfo); //返回该控件是否存在 并释放内存对象
    }
    //触发点击 控件
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static boolean clickNodeInfo(String nodeId){
        AccessibilityNodeInfo NodeInfo = getCurrentWindowNodeInfo(nodeId);
        clickNodeInfo(NodeInfo);
        return recycleNodeInfo(NodeInfo); //返回该控件是否存在 并释放内存对象
    }

    //触发点击 某个子view
    public static boolean clickNodeInfoChild(String nodeId, int index){
        AccessibilityNodeInfo NodeInfo = getItemAccessibilityNodeInfo(nodeId,index);
        clickNodeInfo(NodeInfo);
        return recycleNodeInfo(NodeInfo); //返回该控件是否存在 并释放内存对象
    }

    //触发点击 父控件
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static void clickNodeInfoParent(AccessibilityNodeInfo NodeInfo){
        if(NodeInfo==null || NodeInfo.getParent()==null) return;
        NodeInfo.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
    }
    //触发点击 父控件
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static boolean clickNodeInfoParent(String nodeId){
        AccessibilityNodeInfo NodeInfo = getCurrentWindowNodeInfo(nodeId);
        clickNodeInfoParent(NodeInfo);
        return recycleNodeInfo(NodeInfo); //返回该控件是否存在 并释放内存对象
    }



    //获得 视图内容Text
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static String getTextNodeInfo(String nodeId){
        String strContent = "";
        try {
            AccessibilityNodeInfo NodeInfo = getCurrentWindowNodeInfo(nodeId);
            if (NodeInfo != null) {
                CharSequence str = NodeInfo.getText();
                strContent = str.toString();
                recycleNodeInfo(NodeInfo);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return strContent;
    }

    //设置 视图EditView 内容
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static boolean setEditTextNodeInfo(String nodeId, String text){
        AccessibilityNodeInfo NodeInfo = getCurrentWindowNodeInfo(nodeId);
        if(NodeInfo!=null){
            setClipboard(NodeInfo,text);
            return recycleNodeInfo(NodeInfo);
        }
        return false;
    }
    //设置 视图EditView 内容
    public static boolean setFocusEditTextNodeInfo(String text){
        if(mAccessibilityService == null) return false;
        AccessibilityNodeInfo NodeInfo = mAccessibilityService.getRootInActiveWindow(); //获取整个窗口视图对象
        if(NodeInfo != null) {
            AccessibilityNodeInfo Node = NodeInfo.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
            if(Node != null && "android.widget.EditText".equals(Node.getClassName().toString())){
                //Log.i("Jiong","setEditTextNodeInfoByText 获到控件 类型="+ Node.getClassName().toString());
                setClipboard(Node,text);
                recycleNodeInfo(NodeInfo);
                return recycleNodeInfo(Node);
            }
            recycleNodeInfo(NodeInfo);
        }
        return false;
    }

    //设置 视图EditView 内容
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static boolean setFocusForwardEditTextNodeInfo(String text){
        if(mAccessibilityService == null) return false;
        AccessibilityNodeInfo NodeInfo = mAccessibilityService.getRootInActiveWindow(); //获取整个窗口视图对象
        if(NodeInfo != null) {
            AccessibilityNodeInfo Node = NodeInfo.focusSearch(View.FOCUS_FORWARD);
            if(Node != null && "android.widget.EditText".equals(Node.getClassName().toString())){
               // Log.i("Jiong","setEditTextNodeInfoByText 获到控件 类型="+ Node.getClassName().toString());
                setClipboard(Node,text);
                recycleNodeInfo(NodeInfo);
                return recycleNodeInfo(Node);
            }
            recycleNodeInfo(NodeInfo);
        }
        return false;
    }
    //点击 全局Home 按键
    public static void goGlobalHome(){
        if(mAccessibilityService ==null) return;
        mAccessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
    }

    //点击 全局返回 按键
    public static void goGlobalBack(){
        if(mAccessibilityService ==null) return;
        mAccessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
    }

    //点击 全局 向上滑动
    public static void goGlobalSwipeUp(){
        if(mAccessibilityService ==null) return;
        mAccessibilityService.performGlobalAction(AccessibilityService.GESTURE_SWIPE_UP);  //GESTURE_SWIPE_UP 向上滑动
        Log.i("Jiong-Rob","into goGlobalSwipeUp");
    }

    //由于 使用 performGlobalAction(AccessibilityService.GESTURE_SWIPE_LEFT); //这种方式滑动 不理想，故使用 dispatchGesture
    //界面滑动Api
    @TargetApi(Build.VERSION_CODES.N)
    public static void goSwipe(float startX, float startY, float endX, float endY){
        Path path = new Path();
        path.moveTo(startX, startY);
        path.lineTo(endX, endY);
        GestureDescription.Builder builder =  new GestureDescription.Builder().addStroke(new GestureDescription.StrokeDescription(path,100,100));
        GestureDescription gestureDescription = builder.build();
        if(mAccessibilityService != null){
            mAccessibilityService.dispatchGesture(gestureDescription, new AccessibilityService.GestureResultCallback() {
                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    super.onCancelled(gestureDescription);
                    LogcatFileHelper.i("Jiong>>","RobService dispatchGesture---- 回调取消----->");
                }

                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    super.onCompleted(gestureDescription);
                    LogcatFileHelper.i("Jiong>>","RobService dispatchGesture----回调结束----->");
                }
            }, null);
        }
    }
}
