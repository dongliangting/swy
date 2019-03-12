package com.iflytek.aiui.demo.chat.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by dubin on 2017/4/22.
 */

public class SdCardUtils {
    private static String IMAGE_CACHE_DIR;
    /*@获取SD卡路径: mnt/sd/。如果sd卡不存在，保存在/data/data/com.my.app/files */
    public static String getSDCardPath(Context context) {
        String sdPath = "";
        if (isSDCardEnable()) {
            sdPath = Environment.getExternalStorageDirectory().getPath() + File.separator;
        } else {
            sdPath = context.getFilesDir().getPath() + File.separator;
        }
        return sdPath;
    }
        /* @ Des: 判断SDCard是否可用*/

    public static boolean isSDCardEnable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static String getPicturePath(Context context) {
        if (IMAGE_CACHE_DIR == null) {
            try {
                IMAGE_CACHE_DIR = context.getExternalCacheDir().getAbsolutePath() + File.separator + "image" + File.separator;
            } catch (Exception e) {
                IMAGE_CACHE_DIR = getSDCardPath(context);
            }
            File file = new File(IMAGE_CACHE_DIR);
            if (!file.exists()) {
                file.mkdirs();
            }
        }
        return IMAGE_CACHE_DIR;
    }

    public static File createPath(Context context, String pathName) {
        String path = "";
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            path = Environment.getExternalStorageDirectory() + "/foryou/";
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }
        } else {
            path = context.getFilesDir() + File.separator;
        }
        path = path + pathName;

        File file = new File(path);
        if (file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
            }
        }
        return file;
    }
    /**
     * 随机生产文件名
     *
     * @return
     */
    public static String generateFileName() {
        return UUID.randomUUID().toString();
    }
    /**
     * 保存bitmap到本地
     *
     * @param context
     * @param mBitmap
     * @return
     */
    public static String saveBitmap(Context context, Bitmap mBitmap) {
        File filePic;
        try {
            filePic = new File(getPicturePath(context) + generateFileName() + ".jpg");
            if (!filePic.exists()) {
                filePic.getParentFile().mkdirs();
                filePic.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(filePic);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }

        return filePic.getAbsolutePath();
    }

}
