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
	      //��ѹaar�� fakeDex Ŀ¼��
	        ZipUtil.unZip(aarFile, fakeDex);
	      //�����ҵ���Ӧ��fakeDex �µ�classes.jar
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
	       // ��classes.jar ���classes.dex
	        File aarDex = new File(classes_jar.getParentFile(), "classes.dex");

	      //����Ҫ��jar ת���Ϊdex ��Ҫʹ��android tools �����dx.bat
	        //ʹ��java ����windows �µ�����
	        System.out.println("start  dxCommand");
	        dxCommand(aarDex, classes_jar);
	        System.out.println("end  dxCommand");
	        return aarDex;
	    }

	    public static void dxCommand(File aarDex, File classes_jar) throws IOException, InterruptedException {
	        Runtime runtime = Runtime.getRuntime();
	        //������Ҫע��,commond��dx��Ҫ���û���������ſ�������д,������Ҫָ��dx.bat�ľ���·��
	        String commond = "cmd.exe /C dx --dex --output=" + aarDex.getAbsolutePath() + " " +classes_jar.getAbsolutePath();
	        Process process = runtime.exec(commond);
	        System.out.println("runtime  dxCommand");
	   
	            
	        try {
//	            //��ȡ���̵ı�׼������  
//	            final InputStream is1 = process.getInputStream();   
//	            //��ȡ���ǵĴ�����  
//	            final InputStream is2 = process.getErrorStream();  
//	            //���������̣߳�һ���̸߳������׼���������һ���������׼������  
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
