package com.apk.aes;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class SignatureUtil {
	/**
	 * 为加密后的apk文件添加签名
	 * @param unsignedApk
	 * @param signedApk
	 * @throws InterruptedException
	 * @throws IOException
	 */
	   public static void signature(File unsignedApk, File signedApk) throws InterruptedException, IOException {
	        String cmd[] = {"cmd.exe", "/C","jarsigner",  "-sigalg", "MD5withRSA",
	                "-digestalg", "SHA1",
	                "-keystore", "C:/Users/cizongfa/.android/debug.keystore",
	                "-storepass", "android",
	                "-keypass", "android",
	                "-signedjar", signedApk.getAbsolutePath(),
	                unsignedApk.getAbsolutePath(),
	                "androiddebugkey"};
	        Process process = Runtime.getRuntime().exec(cmd);
	        System.out.println("start sign");
//	        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//	        String line;
//	        while ((line = reader.readLine()) != null)
//	            System.out.println("tasklist: " + line);
	        try {
	            int waitResult = process.waitFor();
	            System.out.println("waitResult: " + waitResult);
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	            throw e;
	        }
	        System.out.println("process.exitValue() " + process.exitValue() );
	        if (process.exitValue() != 0) {
	        	InputStream inputStream = process.getErrorStream();
	        	int len;
	        	byte[] buffer = new byte[2048];
	        	ByteArrayOutputStream bos = new ByteArrayOutputStream();
	        	while((len=inputStream.read(buffer)) != -1){
	        		bos.write(buffer,0,len);
	        	}
	        	System.out.println(new String(bos.toByteArray(),"GBK"));
	            throw new RuntimeException("签名执行失败");
	        }
	        System.out.println("finish signed");
	        process.destroy();
	    }
}
