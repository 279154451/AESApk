package com.apk.aes;

import java.io.File;
import java.io.FileOutputStream;

public class ApkAesMain {
	 public static void main(String[] args) throws Exception {
		 System.out.println("start");
		 AESUtil.init(AESUtil.DEFAULT_PWD);//��ʼ������
	
		 	/**
		 	 * 1���ֱ���apk��aarĿ¼����������tempĿ¼������ż��ܵ�δ�����apk�ļ�
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
		 	 * 2����ѹԭapk�ļ���apk/tempĿ¼��,������dex�ļ�
		 	 */
		 	File sourceApk = new File("source/apk/app-debug.apk");
	        File newApkDir = new File(sourceApk.getParent() + File.separator + "temp");
	        if(!newApkDir.exists()) {
	        	newApkDir.mkdirs();
	        }
	      //��ѹԭapk,����dex
	        AESUtil.encryptAPKFile(sourceApk,newApkDir);
	        if (newApkDir.isDirectory()) {
				File[] listFiles = newApkDir.listFiles();
				for (File file : listFiles) {
					if (file.isFile()) {
						//�޸�classes.dex��Ϊclasses_.dex,����Ȼ���aar�е�classes.dex����
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
	         * 3.��ѹaar�ļ�(���ܽ��м��ܵĲ���),�����е�dex�ļ�������apk/temp����
	         */
	        File aarFile = new File("source/aar/mylibrary-debug.aar");
	    	 File sourceAarDex = null;
	    	try {
	    		//ͨ��dx���߽�jar�ļ�ת����dex�ļ�
	    		sourceAarDex  = DxUtil.jar2Dex(aarFile);
	    	}catch(Exception e){
	    		e.printStackTrace();
	    	}

	        File copyAarDex = new File(newApkDir.getPath() + File.separator + "classes.dex");
	        if (!copyAarDex.exists()) {
	        	copyAarDex.createNewFile();
			}
	        //����aar�е�classes.dex��apk/temp��
	        FileOutputStream fos = new FileOutputStream(copyAarDex);
	        byte[] fbytes = ByteUtil.getBytes(sourceAarDex);
	        fos.write(fbytes);
	        fos.flush();
	        fos.close();
	        
	        /**
	         * 4.���apk/tempĿ¼�����µ�δǩ��apk�ļ�
	         */
	        File unsignedApk = new File("result/apk-unsigned.apk");
	        unsignedApk.getParentFile().mkdirs();
	        ZipUtil.zip(newApkDir, unsignedApk);
	        
	        /**
	         * 5.����apk���ǩ��,����ǩ��apk
	         */
	        File signedApk = new File("result/apk-signed.apk");
	        SignatureUtil.signature(unsignedApk, signedApk);
	        System.out.println("end");
	    }

}
