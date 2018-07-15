package com;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import com.utils.LogcatFileHelper;


/***
 * 全局 Application  Context 获取
 * author:fjw0312
 * date:2017.7.12
 * notice: need to AndroidMainfest.xml add:
 * <application
 *     android:name="com.MyApplication"
 * use: MyApplication.getContext();
 * */
public class MyApplication extends Application{  
	private static Context context;
	public  static String SAVE_FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()+"/";
	public  static String DATA_DIR = "Data/";
	public  static String CRASH_DIR = "Crash/";
	public  static String CACHE_DIR = "Cache/";
	public  static String LOADED_DIR = "Doaded/";


	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		context = getApplicationContext();
		SAVE_FILE_PATH = SAVE_FILE_PATH+getPackageName()+"/";

		//初始化crash 异常捕获
//		CrashHandler crashHandler = CrashHandler.getInstance();
//		crashHandler.init(getApplicationContext());
		//初始化 日志打印
		new LogcatFileHelper(true,false,2);

	}

	//获取 context
	public static Context getContext(){
		return context;
	}

}
