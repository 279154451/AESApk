package com.apk.aes;

import java.io.File;
import java.io.FileOutputStream;

public class ApkAesMain {
	 public static void main(String[] args) throws Exception {
		 System.out.println("start");
		 AESUtil.init(AESUtil.DEFAULT_PWD);//初始化加密
	
		 	/**
		 	 * 1、分别在apk和aar目录下生成两个temp目录用来存放加密的未打包的apk文件
		 	 */
		 	File apkTemp = new File("source/apk/temp");
		 	if(apkTemp.exists()) {
		 		File[] files = apkTemp.listFiles();
		 		for(File file:files) {
		 			if(file.exists()) {
		 				file.delete();
		 			}
		 		}
		 	}
		 	File aarTemp = new File("source/aar/temp");
		 	if(aarTemp.exists()) {
		 		File[] files = aarTemp.listFiles();
		 		for(File file:files) {
		 			if(file.exists()) {
		 				file.delete();
		 			}
		 		}
		 	}
		 	/**
		 	 * 2、解压原apk文件到apk/temp目录下,并加密dex文件
		 	 */
		 	File sourceApk = new File("source/apk/app-debug.apk");
	        File newApkDir = new File(sourceApk.getParent() + File.separator + "temp");
	        if(!newApkDir.exists()) {
	        	newApkDir.mkdirs();
	        }
	      //解压原apk,加密dex
	        AESUtil.encryptAPKFile(sourceApk,newApkDir);
	        if (newApkDir.isDirectory()) {
				File[] listFiles = newApkDir.listFiles();
				for (File file : listFiles) {
					if (file.isFile()) {
						//修改classes.dex名为classes_.dex,避免等会与aar中的classes.dex重名
						if (file.getName().endsWith(".dex")) {
							String name = file.getName();
							int cursor = name.indexOf(".dex");
							String newName = file.getParent()+ File.separator + 
									name.substring(0, cursor) + "_" + ".dex";
							file.renameTo(new File(newName));
						}
					}
				}
			}
	        /**
	         * 3.解压aar文件(不能进行加密的部分),将其中的dex文件拷贝到apk/temp中来
	         */
	        File aarFile = new File("source/aar/mylibrary-debug.aar");
	    	 File sourceAarDex = null;
	    	try {
	    		//通过dx工具将jar文件转换成dex文件
	    		sourceAarDex  = DxUtil.jar2Dex(aarFile);
	    	}catch(Exception e){
	    		e.printStackTrace();
	    	}

	        File copyAarDex = new File(newApkDir.getPath() + File.separator + "classes.dex");
	        if (!copyAarDex.exists()) {
	        	copyAarDex.createNewFile();
			}
	        //拷贝aar中的classes.dex到apk/temp中
	        FileOutputStream fos = new FileOutputStream(copyAarDex);
	        byte[] fbytes = ByteUtil.getBytes(sourceAarDex);
	        fos.write(fbytes);
	        fos.flush();
	        fos.close();
	        
	        /**
	         * 4.打包apk/temp目录生成新的未签名apk文件
	         */
	        File unsignedApk = new File("result/apk-unsigned.apk");
	        unsignedApk.getParentFile().mkdirs();
	        ZipUtil.zip(newApkDir, unsignedApk);
	        
	        /**
	         * 5.给新apk添加签名,生成签名apk
	         */
	        File signedApk = new File("result/apk-signed.apk");
	        SignatureUtil.signature(unsignedApk, signedApk);
	        System.out.println("end");
	    }

}
