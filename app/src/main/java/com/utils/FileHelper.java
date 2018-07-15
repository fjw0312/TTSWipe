package com.utils;


import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/***
 *  文件File IO 操作
 * 
 * */
public class FileHelper {

	File file = null;   //文件类变量
	Writer fws = null;   //写入字符流
	Reader frs = null;   //读出字符流
	InputStream fis = null;  //读取字节流
	OutputStream fos = null; //写入字节流
	BufferedReader bufread = null; //高级读取文本流
	PrintWriter  priwrite = null; //高级写入文本流

	public FileHelper() {
		super();
		// TODO Auto-generated constructor stub
	}
	public FileHelper(String strFileName) {
		super();
		// TODO Auto-generated constructor stub
		file = new File(strFileName);
		if(!file.exists() || !file.isFile()){
            //先 判断 保证路劲存在
            if(hasFilePath(strFileName)){
                try{
                    file.createNewFile(); //新建 文件
                }catch(IOException e){
                    MyBroadcastReceiver.sendBroad_Error_HAL("文件-创建失败!");
                    Log.e("文件","创建失败");
                }
            }
        }
		//判断文件是否可读
		if(!file.canRead()){
			MyBroadcastReceiver.sendBroad_Error_HAL("文件-不可读!");
			Log.e("文件","不可读");
		}	
		
		//判断文件是否可写
		if(!file.canWrite()){
			MyBroadcastReceiver.sendBroad_Error_HAL("文件-不可写!");
			Log.e("文件","不可写");
		}
	}

	//保证文件目录是否存在
	public boolean hasFilePath(String strFileName){
	    if("".equals(strFileName)) return false;
	    int lastIndex = strFileName.lastIndexOf("/");
	    String path = strFileName.substring(0,lastIndex+1);
	    if("".equals(path)) return false;
	    File file = new File(path);
	    if(file.exists() && file.isDirectory()) return true; //文件路劲已存在
        try{
            file.mkdirs();
        }catch(Exception e){
            Log.e("文件路劲","创建失败");
            e.printStackTrace();
        }
        if(file.exists() && file.isDirectory()){
            return true;
        }else{
            return false;
        }
    }
	
	//--删除文件
	public void deleteFile(){
		if(file.exists()){
			file.delete();
		}
	}
	
	//---------------------------HAL---------------------------//
	//读出字节
	public byte[] read_byte(byte[] buf){
			
		try{		
			fis = new FileInputStream(file);
			for(int i=0; (fis.read(buf,0,buf.length))!=-1;i++);
			fis.close();
			}catch(Exception e){
				Log.e("文件","读取字符流失败");
				return null;
			}
				
			return buf;
	}	
	//写入字节
	public boolean write_byte(byte[] buf){
			
		try{
			fos = new FileOutputStream(file);		
			fos.write(buf);
			fos.close();
			}catch(Exception e){
				Log.e("文件","写入字符流失败");
				return false;
			}
		
			return true;
	}

	//读取 字符串
	public String read_buf(String buf){
		
		try{
			char c[] = new char[1024]; //读取最大1024byte
			frs = new FileReader(file);
			for(int i=0; (frs.read(c))!=-1;i++);
			frs.close();
			buf = c.toString();
			}catch(Exception e){
				Log.e("文件","读取字符流失败");
			}
		
		return buf;
	}
	//写入字符串
	public boolean write_buf(String buf){
		
		try{
		fws = new FileWriter(file);
		fws.write(buf);
		fws.close();
		}catch(Exception e){
			Log.e("文件","写入字符流失败");
			return false;
		}
		
		return true;
	}
	//=================================使用高级 流操作=========================================
	//*************************写入部分
	//高级缓冲流 尾部写入  传入字符串
	public boolean write_str(String buf){

		try{
			priwrite = new PrintWriter(new FileWriter(file,true),true); //true
			priwrite.print(buf);  //
			priwrite.close();
			}catch(Exception e){
				Log.e("文件","写入字符流失败");
				return false;
			}
		
			return true;
	}
	//高级缓冲流 写入字符串行  传入字符串
	public boolean write_line(String buf){

		try{
			priwrite = new PrintWriter(new FileWriter(file,true),true); //true尾部追加
			priwrite.println(buf);  //尾部追加写入行字符串
			priwrite.close();
			}catch(Exception e){
				Log.e("文件","写入字符流失败");
				return false;
			}
		
			return true;
	}
	//高级缓冲流 写入多行字符串行  传入字符串
	public boolean write_lines(String[] bufs){
		if(bufs==null) return false;
		
		try{
			priwrite = new PrintWriter(new FileWriter(file,true),true); //true尾部追加
			for(int i=0;i<bufs.length;i++){
				priwrite.println(bufs[i]);  //
			}
			priwrite.close();
			}catch(Exception e){
				Log.e("文件","写入字符流失败");
				return false;
			}
		
			return true;
	}
	//***********************读取部分

	//读出 行  头一行字符串 返回字符串
	public String read_first_line(){				
		String buf_line = "";
		try{
			bufread = new BufferedReader( new FileReader(file));
			if((buf_line = bufread.readLine())==null){
				bufread.close();
				return buf_line;
				}
			bufread.close();				
			}catch(Exception e){
				Log.e("文件","读出字符流失败");
			}
						
			return buf_line;
	}
	//读取文件 最后一行 字符串
	public String read_later_line(){							
		String buf_later_line = "";
		try{
			 String line = "";
			 bufread = new BufferedReader( new FileReader(file));
			 while((line = bufread.readLine())!=null){	
				 buf_later_line = line;
			  }
			// Log.i("local_file->read_later_line读取到最后一行字符串",buf_later_line);
			  bufread.close();
			}catch(Exception e){
				Log.e("文件","读出字符流失败");
			}
						
			return buf_later_line;
	}
	//读取行  所有字符串 返回字符串数组
	public List<String> read_all_line(){
	
		String buf = new String();	
		List<String> buflist1 = new ArrayList<String>();
		buflist1.clear();
		try{			
			bufread = new BufferedReader( new FileReader(file));
			while((buf = bufread.readLine())!=null){
				buflist1.add(buf);
			}
			bufread.close();				
			}catch(Exception e){
				Log.e("文件","读出字符流失败");
			}
				
			return buflist1;
	}
	//读取含有某些 字符串的 --如果含有某字符串  整行输出  否则""
	public String search_str_line(String buf){
		String buf_str_line = "";
		String buff = "";
		try{
			 bufread = new BufferedReader( new FileReader(file));
			 while((buff = bufread.readLine())!=null){
		//		 if(buf_str_line.indexOf(buf)!= -1)  break;	 //包含有某字符就退出while
				 if(buff.contains(buf)){
					 buf_str_line = buff;
					 break;
				 }
			  }				 
			  bufread.close();
			}catch(Exception e){
				Log.e("文件","读出字符流失败");
			}
		return buf_str_line;
	}	
	////读取含有某些 字符串的 --如果含有某字符串  所有整行输出  否则null
	public List<String> search_str_all_line(String buf){
		String buff = "";
		List<String> buflist2 = new ArrayList<String>();
		buflist2.clear();
		try{
			 bufread = new BufferedReader( new FileReader(file));
			 while((buff = bufread.readLine())!=null){
		//		 if(buf_str_line.indexOf(buf)!= -1)  break;	 //包含有某字符就退出while
				 if(buff.contains(buf)){
					 buflist2.add(buff);
				 }
			  }				 
			  bufread.close();
			}catch(Exception e){
			Log.e("文件","读出字符流失败");
			}
		return buflist2;
	}
	
	//****************************修改文件内容 部分
	//修改含有某个字符串的  整行字符串 参数filename文件  str搜索字符  newline新字符行内容
	public static boolean exchange_str_line(String fileName,String str, String newline){

		String buff = "";
		try{
			 File file1 = new File(fileName);
			 File file2 = new File(fileName+"txt");
			 file2.createNewFile(); //
			 
			 PrintWriter priwrite = new PrintWriter(new FileWriter(file2,true),true); //true
			 BufferedReader bufread = new BufferedReader( new FileReader(file1));
			 while((buff = bufread.readLine())!=null){
				 if(buff.contains(str)){					 
					 buff = newline;
				 }
				 priwrite.println(buff);  //尾部写入
			  }				 
			  bufread.close();
			  priwrite.close();
			  file1.delete();
			  File file3 = new File(fileName);
			  file2.renameTo(file3);
			}catch(Exception e){
				Log.e("文件","读出字符流失败");
				return false;
			}
		return true;
	}

}
