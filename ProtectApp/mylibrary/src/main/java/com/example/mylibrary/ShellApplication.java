package com.example.mylibrary;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author 享学课堂 Alvin
 * @package com.xiangxue.alvin.applibrary
 * @fileName ShellApplication
 * @date on 2019/4/21
 * @qq 2464061231
 **/
public class ShellApplication extends Application {
    private static final String TAG = "ShellApplication";

    public static String getPassword(){
        return "abcdefghijklmnop";
    }

//    static {
//        System.loadLibrary("native-lib");
//    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);


        AESUtil.init(getPassword());
        //获取当前应用的apk文件
        File apkFile = new File(getApplicationInfo().sourceDir);
        //在应用私有空间创建一个用来存放被解压的apk源文件的目录:data/data/包名/files/fake_apk/app
        File unZipFile = getDir("fake_apk", MODE_PRIVATE);
        File app = new File(unZipFile, "app");
       unZipAndAecryptDex(apkFile,app);
        List list = new ArrayList<>();
        Log.d("FAKE", Arrays.toString(app.listFiles()));
        for (File file : app.listFiles()) {
            if (file.getName().endsWith(".dex")) {
                list.add(file);
            }
        }

        Log.d("FAKE", list.toString());
        try {
            V19.install(getClassLoader(), list, unZipFile);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    /**
     * 解压并解密dex
     * @param apkFile  被加密的apk文件
     * @param newAppDir 存放解压和解密后的apk源文件目录
     */
    private void unZipAndAecryptDex(File apkFile,File newAppDir){
        if (!newAppDir.exists()) {
            //解压apk到指定目录
            ZipUtil.unZip(apkFile, newAppDir);
            File[] files = newAppDir.listFiles();
            for (File file : files) {
                String name = file.getName();
                /**
                 * 是否还记得我们在加密的时候将不能加密的壳dex命名为classes.dex并拷贝到新apk中打包生成新的apk呢？
                 * 所以这里我们做脱壳，壳dex不需要进行解密操作。因为它根本就没有被加密。
                 */
                if (name.equals("classes.dex")) {

                } else if (name.endsWith(".dex")) {
                    /**
                     * 对被加密的核心dex进行解密，对应加密流程中的classes_.dex
                     */
                    try {
                        byte[] bytes = getBytes(file);
                        FileOutputStream fos = new FileOutputStream(file);
                        byte[] decrypt = AESUtil.decrypt(bytes);
//                        fos.write(bytes);
                        fos.write(decrypt);
                        fos.flush();
                        fos.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static Field findField(Object instance, String name) throws NoSuchFieldException {
        Class clazz = instance.getClass();

        while (clazz != null) {
            try {
                Field e = clazz.getDeclaredField(name);
                if (!e.isAccessible()) {
                    e.setAccessible(true);
                }

                return e;
            } catch (NoSuchFieldException var4) {
                clazz = clazz.getSuperclass();
            }
        }

        throw new NoSuchFieldException("Field " + name + " not found in " + instance.getClass());
    }

    private static Method findMethod(Object instance, String name, Class... parameterTypes)
            throws NoSuchMethodException {
        Class clazz = instance.getClass();
//        Method[] declaredMethods = clazz.getDeclaredMethods();
//        System.out.println("  findMethod ");
//        for (Method m : declaredMethods) {
//            System.out.print(m.getName() + "  : ");
//            Class<?>[] parameterTypes1 = m.getParameterTypes();
//            for (Class clazz1 : parameterTypes1) {
//                System.out.print(clazz1.getName() + " ");
//            }
//            System.out.println("");
//        }
        while (clazz != null) {
            try {
                Method e = clazz.getDeclaredMethod(name, parameterTypes);
                if (!e.isAccessible()) {
                    e.setAccessible(true);
                }

                return e;
            } catch (NoSuchMethodException var5) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchMethodException("Method " + name + " with parameters " + Arrays.asList
                (parameterTypes) + " not found in " + instance.getClass());
    }

    private static void expandFieldArray(Object instance, String fieldName, Object[]
            extraElements) throws NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException {
        Field jlrField = findField(instance, fieldName);
        Object[] original = (Object[]) ((Object[]) jlrField.get(instance));
        Object[] combined = (Object[]) ((Object[]) Array.newInstance(original.getClass()
                .getComponentType(), original.length + extraElements.length));
        System.arraycopy(original, 0, combined, 0, original.length);
        System.arraycopy(extraElements, 0, combined, original.length, extraElements.length);
        jlrField.set(instance, combined);
    }

    private static final class V19 {
        private V19() {
        }

        private static void install(ClassLoader loader, List<File> additionalClassPathEntries,
                                    File optimizedDirectory) throws IllegalArgumentException,
                IllegalAccessException, NoSuchFieldException, InvocationTargetException,
                NoSuchMethodException {
            /**
             * 通过反射找到BaseDexClassLoader中的pathList属性，
             * 这是一个ClassLoader中存放Dex文件列表的DexPathList变量，
             * 其内部维护着一个dex文件数组。ClassLoader加载类的时候就会从这dex数组中去查找。
             * 具体逻辑请查看源码http://androidxref.com/8.0.0_r4/xref/libcore/dalvik/src/main/java/dalvik/system/BaseDexClassLoader.java
             * 而我们要做的就是将解密出来的dex重新插入到这个数组里面。这个在前将类加载和热修复的时候已经有提到过。所以看源码是每个程序员必须具备的技能。
             */
            Field pathListField = findField(loader, "pathList");
            Object dexPathList = pathListField.get(loader);
            ArrayList suppressedExceptions = new ArrayList();
            Log.d(TAG, "Build.VERSION.SDK_INT " + Build.VERSION.SDK_INT);
            /**
             * 这里需要区分一下版本区别。每个版本的Android源码都有相应的改变，
             * 这就需要我们在做这些功能的时候不得不去考虑各个版本的适配。这也是Android开发让人头疼的地方。
             */
            if (Build.VERSION.SDK_INT >= 23) {
                //将解密后的dex文件插入到DexPathList的dexElements数组中。
                expandFieldArray(dexPathList, "dexElements", makePathElements(dexPathList, new
                                ArrayList(additionalClassPathEntries), optimizedDirectory,
                        suppressedExceptions));
            } else {
                expandFieldArray(dexPathList, "dexElements", makeDexElements(dexPathList, new
                                ArrayList(additionalClassPathEntries), optimizedDirectory,
                        suppressedExceptions));
            }

            if (suppressedExceptions.size() > 0) {
                Iterator suppressedExceptionsField = suppressedExceptions.iterator();

                while (suppressedExceptionsField.hasNext()) {
                    IOException dexElementsSuppressedExceptions = (IOException)
                            suppressedExceptionsField.next();
                    Log.w("MultiDex", "Exception in makeDexElement",
                            dexElementsSuppressedExceptions);
                }

                Field suppressedExceptionsField1 = findField(loader,
                        "dexElementsSuppressedExceptions");
                IOException[] dexElementsSuppressedExceptions1 = (IOException[]) ((IOException[])
                        suppressedExceptionsField1.get(loader));
                if (dexElementsSuppressedExceptions1 == null) {
                    dexElementsSuppressedExceptions1 = (IOException[]) suppressedExceptions
                            .toArray(new IOException[suppressedExceptions.size()]);
                } else {
                    IOException[] combined = new IOException[suppressedExceptions.size() +
                            dexElementsSuppressedExceptions1.length];
                    suppressedExceptions.toArray(combined);
                    System.arraycopy(dexElementsSuppressedExceptions1, 0, combined,
                            suppressedExceptions.size(), dexElementsSuppressedExceptions1.length);
                    dexElementsSuppressedExceptions1 = combined;
                }

                suppressedExceptionsField1.set(loader, dexElementsSuppressedExceptions1);
            }

        }

        /**
         * 创建我们自己的dex文件数组，可查看源码中的makeDexElements方法
         * 附上源码链接http://androidxref.com/8.0.0_r4/xref/libcore/dalvik/src/main/java/dalvik/system/DexPathList.java
         * @param dexPathList
         * @param files
         * @param optimizedDirectory
         * @param suppressedExceptions
         * @return
         * @throws IllegalAccessException
         * @throws InvocationTargetException
         * @throws NoSuchMethodException
         */
        private static Object[] makeDexElements(Object dexPathList,
                                                ArrayList<File> files, File
                                                        optimizedDirectory,
                                                ArrayList<IOException> suppressedExceptions) throws
                IllegalAccessException, InvocationTargetException, NoSuchMethodException {

                Method makeDexElements = findMethod(dexPathList, "makeDexElements", new
                        Class[]{ArrayList.class, File.class, ArrayList.class});
                return ((Object[]) makeDexElements.invoke(dexPathList, new Object[]{files,
                        optimizedDirectory, suppressedExceptions}));
           }
    }

    /**
     * A wrapper around
     * {@code private static final dalvik.system.DexPathList#makePathElements}.
     */
    private static Object[] makePathElements(
            Object dexPathList, ArrayList<File> files, File optimizedDirectory,
            ArrayList<IOException> suppressedExceptions)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        Method makePathElements;
        try {
            makePathElements = findMethod(dexPathList, "makePathElements", List.class, File.class,
                    List.class);
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "NoSuchMethodException: makePathElements(List,File,List) failure");
            try {
                makePathElements = findMethod(dexPathList, "makePathElements", ArrayList.class, File.class, ArrayList.class);
            } catch (NoSuchMethodException e1) {
                Log.e(TAG, "NoSuchMethodException: makeDexElements(ArrayList,File,ArrayList) failure");
                try {
                    Log.e(TAG, "NoSuchMethodException: try use v19 instead");
                    return V19.makeDexElements(dexPathList, files, optimizedDirectory, suppressedExceptions);
                } catch (NoSuchMethodException e2) {
                    Log.e(TAG, "NoSuchMethodException: makeDexElements(List,File,List) failure");
                    throw e2;
                }
            }
        }
        return (Object[]) makePathElements.invoke(dexPathList, files, optimizedDirectory, suppressedExceptions);
    }

    private byte[] getBytes(File file) throws Exception {
        RandomAccessFile r = new RandomAccessFile(file, "r");
        byte[] buffer = new byte[(int) r.length()];
        r.readFully(buffer);
        r.close();
        return buffer;
    }
}
