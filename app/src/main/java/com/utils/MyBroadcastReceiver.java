package com.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.MyApplication;


/***
 * my custom BroadcastReceiver
 * author:fjw0312
 * date:2017.7.12
 * 
 * need to AndroidMainfest.xml  add:
        <receiver           
             android:exported="false"
             android:name="mybroadcast.MyBroadcastReceiver">
	        <intent-filter>
	            <action android:name="Fang.MyBroadcast.Error" />
	            <action android:name="Fang.MyBroadcast.MSG" />
	        </intent-filter>
    	</receiver>
 * 
 * 
 * eg: send ErrorMsg
 * 			Intent intent = new Intent("Fang.MyBroadcast.Error");
			intent.putExtra("fang", "HttpURLConnectionHAL-GET Error");
			MyApplication.getContextObject().sendBroad_Error_HAL(intent);
 * */
public class MyBroadcastReceiver extends BroadcastReceiver{ 

	final static String Fang_Error = "Fang.MyBroadcast.Error";
	final static String Fang_MSG = "Fang.MyBroadcast.MSG";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		//Log.i("MyBroadcastReceiver>","into onReceive");
		String extraStr = intent.getStringExtra("fang");
		if(intent.getAction().equals(Fang_Error)){
			Toast.makeText(context, "Error:"+extraStr, Toast.LENGTH_SHORT).show();
		}else if(intent.getAction().equals(Fang_MSG)){
			Toast.makeText(context, "MSG:"+extraStr, Toast.LENGTH_SHORT).show();
		}
	}
	

	//自定义  外部广播发送接口    sendBroad  API
	public static void sendBroad_Error_HAL(String strContent){
	    Intent intent = new Intent(Fang_Error);
		intent.putExtra("fang", strContent);
		MyApplication.getContext().sendBroadcast(intent);
	}
	public static void sendBroad_MSG_HAL(String strContent){
	    Intent intent = new Intent(Fang_MSG);
		intent.putExtra("fang", strContent);
		MyApplication.getContext().sendBroadcast(intent);
	}
	

}
