package com.utils;

import android.util.Log;

import com.MyApplication;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/***
 * LogcatFileHAL
 * 打印本地log
 * log.e  log.w  log.i log.d
 * 使用时需要 new LogcatFileHAL();
 * 
 * */
public class LogcatFileHelper {

	public static boolean  IsLog = true;  //是否调试打印Log
	public static boolean  IsUse  = false;  //使用 本地日志 打印功能
	public static int TimeFlag = 0;  // 0：默认不打印时间   1：打印时间数  2：打印时间字符串
	public static String FileName = "Logcat.txt";


	public LogcatFileHelper() {
		super();
		// TODO Auto-generated constructor stub
//		fileHAL = new FileHAL(FilePath+FileName);
		FileName = MyApplication.SAVE_FILE_PATH + MyApplication.DATA_DIR + FileName;
		if(!MyThread.isAlive()){
			MyThread.start();
		}
	}
	public LogcatFileHelper(boolean isLog, boolean isUse, int timeFlag) {
		super();
		// TODO Auto-generated constructor stub
		this.IsLog = isLog;
		this.IsUse = isUse;
		this.TimeFlag = timeFlag;
		FileName = MyApplication.SAVE_FILE_PATH + MyApplication.DATA_DIR + FileName;
		if(!MyThread.isAlive()){
			MyThread.start();
		}

	}

	private static List<String> msgFile_lst = new ArrayList<String>();

//	static FileHAL fileHAL = null;
	public static void deleteLogFile(){
		FileHelper fileHAL = new FileHelper(FileName);
		fileHAL.deleteFile();
	}
	 
	//获得 系统 时间
	private static long getSysTime(){
		long time = System.currentTimeMillis();
		return time;
	}
	// 时间 数值  转化为  字符串
	private static String timeToString(long time){
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//时间格式转换
		Date date = new Date(time);
		String strTime = formatter.format(date);
		return strTime;
	}
	//时间 	字符串  转化为 数值
	private static String getStringSysTime(){
		long time = getSysTime();
		String str = "";
		if(TimeFlag == 0){  
		
		}else if(TimeFlag == 1){
			str = String.valueOf(time+"    ");
		}else if(TimeFlag == 2){
			str = timeToString(time)+"    ";
		}
		return str;
	}
	private static Thread MyThread = new Thread(){
		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			while(true){
				synchronized(msgFile_lst){
					if(msgFile_lst.size()>6000){
						msgFile_lst.clear();  //过大保护
						msgFile_lst.add("日志打印太频繁 超过6000条无法响应");
					}
					for(int i =0;i<msgFile_lst.size();i++){
						FileHelper fileHAL = new FileHelper(FileName);
						fileHAL.write_line(msgFile_lst.get(i));
					}
					msgFile_lst.clear(); 
				}
				try {
					Thread.sleep(1);   //1 ms 处理周期
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}	
		}
		
	};
	
	public static void e(String Tag, String msg){
		if(IsLog) Log.e(Tag, msg);
		if(IsUse){
			synchronized(msgFile_lst){
				String str = "Log.e  "+getStringSysTime() + Tag +">        "+msg;
				msgFile_lst.add(str);
			}
		}		
	}
	public  static void w(String Tag, String msg){
		if(IsLog) Log.w(Tag, msg);
		if(IsUse){
			synchronized(msgFile_lst){
				String str = "Log.w  "+getStringSysTime() + Tag +">        "+msg;
				msgFile_lst.add(str);
			}
		}
	}
	public static void i(String Tag, String msg){
		if(IsLog) Log.i(Tag, msg);
		if(IsUse){
			synchronized(msgFile_lst){
				String str = "Log.i  "+getStringSysTime() + Tag +">        "+msg;
				msgFile_lst.add(str);
			}
		}
	}
	public static void d(String Tag, String msg){
		if(IsUse){
			synchronized(msgFile_lst){
				String str = "Log.d  "+getStringSysTime() + Tag +">        "+msg;
				msgFile_lst.add(str);
			}
		}
	}
	
	

}
