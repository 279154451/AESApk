package com.apk.aes;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;

public class DxUtil {
	 public static File jar2Dex(File aarFile) throws IOException, InterruptedException {
	        File fakeDex = new File(aarFile.getParent() + File.separator + "temp");
	        System.out.println("jar2Dex: aarFile.getParent(): " + aarFile.getParent());
	      //解压aar到 fakeDex 目录下
	        ZipUtil.unZip(aarFile, fakeDex);
	      //过滤找到对应的fakeDex 下的classes.jar
	        File[] files = fakeDex.listFiles(new FilenameFilter() {
	            @Override
	            public boolean accept(File file, String s) {
	                return s.equals("classes.jar");
	            }
	        });
	        if (files == null || files.length <= 0) {
	        	 System.out.println("the aar is invalidate" + aarFile.getParent());
	            throw new RuntimeException("the aar is invalidate");
	        }
	        File classes_jar = files[0];
	       // 将classes.jar 变成classes.dex
	        File aarDex = new File(classes_jar.getParentFile(), "classes.dex");

	      //我们要将jar 转变成为dex 需要使用android tools 里面的dx.bat
	        //使用java 调用windows 下的命令
	        System.out.println("start  dxCommand");
	        dxCommand(aarDex, classes_jar);
	        System.out.println("end  dxCommand");
	        return aarDex;
	    }

	    public static void dxCommand(File aarDex, File classes_jar) throws IOException, InterruptedException {
	        Runtime runtime = Runtime.getRuntime();
	        //这里需要注意,commond中dx需要配置环境变量后才可以这样写,否则需要指定dx.bat的绝对路径
	        String commond = "cmd.exe /C dx --dex --output=" + aarDex.getAbsolutePath() + " " +classes_jar.getAbsolutePath();
	        Process process = runtime.exec(commond);
	        System.out.println("runtime  dxCommand");
	   
	            
	        try {
//	            //获取进程的标准输入流  
//	            final InputStream is1 = process.getInputStream();   
//	            //获取进城的错误流  
//	            final InputStream is2 = process.getErrorStream();  
//	            //启动两个线程，一个线程负责读标准输出流，另一个负责读标准错误流  
//	            new Thread() {  
//	               public void run() {  
//	                  BufferedReader br1 = new BufferedReader(new InputStreamReader(is1));  
//	                   try {  
//	                       String line1 = null;  
//	                       while ((line1 = br1.readLine()) != null) {  
//	                             if (line1 != null){}  
//	                         }  
//	                   } catch (IOException e) {  
//	                        e.printStackTrace();  
//	                   }  
//	                   finally{  
//	                        try {  
//	                          is1.close();  
//	                        } catch (IOException e) {  
//	                           e.printStackTrace();  
//	                       }  
//	                     }  
//	                   }  
//	                }.start(); 
//	        	new Thread() {   
//	        	      public void  run() {   
//	        	       BufferedReader br2 = new  BufferedReader(new  InputStreamReader(is2));   
//	        	          try {   
//	        	             String line2 = null ;   
//	        	             while ((line2 = br2.readLine()) !=  null ) {   
//	        	                  if (line2 != null){}  
//	        	             }   
//	        	           } catch (IOException e) {   
//	        	                 e.printStackTrace();  
//	        	           }   
//	        	          finally{  
//	        	             try {  
//	        	                 is2.close();  
//	        	             } catch (IOException e) {  
//	        	                 e.printStackTrace();  
//	        	             }  
//	        	           }  
//	        	        }   
//	        	      }.start();    
	        	      
	            process.waitFor();
	            System.out.println("waitFor  dxCommand");
	        } catch (InterruptedException e) {
//	        	try{  
//	        		process.getErrorStream().close();  
//	        		process.getInputStream().close();  
//	        		process.getOutputStream().close();  
//	                }  catch(Exception ee){}  
	        	 System.out.println("InterruptedException  dxCommand");
	            e.printStackTrace();
	            throw e;
	        }
	        if (process.exitValue() != 0) {
	        	 System.out.println("getErrorStream  dxCommand");
	        	InputStream inputStream = process.getErrorStream();
	        	int len;
	        	byte[] buffer = new byte[2048];
	        	ByteArrayOutputStream bos = new ByteArrayOutputStream();
	        	while((len=inputStream.read(buffer)) != -1){
	        		bos.write(buffer,0,len);
	        	}
	        	System.out.println(new String(bos.toByteArray(),"GBK"));
	            throw new RuntimeException("dx run failed");
	        }
	        process.destroy();
	    }
}
