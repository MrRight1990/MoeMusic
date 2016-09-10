package com.cpacm.core.utils;

import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;

import com.cpacm.core.CoreApplication;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

/**
 * manage file include download , upload , read ,write
 * Created by cpcam on 2015/7/7.
 */
public class FileUtils {

    private static final int DELAY_TIME = 10000;

    public final static String CACHE_DIR = "Beats";
    public final static String GLIDE_CACHE_DIR = "glide";
    public final static String SONG_CACHE_DIR = "songs";

    /**
     * 获取缓存主目录
     *
     * @return
     */
    public static String getCacheDir() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            // 创建一个文件夹对象，赋值为外部存储器的目录
            File sdcardDir = Environment.getExternalStorageDirectory();
            //得到一个路径，内容是sdcard的文件夹路径和名字
            String path = sdcardDir.getPath() + File.separator + CACHE_DIR;
            File path1 = new File(path);
            if (!path1.exists()) {
                path1.mkdirs();
            }
            return path1.getPath();
        }
        return null;
    }

    /**
     * 获取存放歌曲的目录
     *
     * @return
     */
    public static String getSongDir() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            //得到一个路径，内容是sdcard的文件夹路径和名字
            String path = getCacheDir() + File.separator + SONG_CACHE_DIR;
            File path1 = new File(path);
            if (!path1.exists()) {
                path1.mkdirs();
            }
            return path1.getPath();
        }
        return null;
    }

    /**
     * 读取Assets目录下的文件
     *
     * @param context
     * @param name
     * @return
     */
    public static String getAssets(Context context, String name) {
        String result = null;
        try {
            InputStream in = context.getAssets().open(name);  //获得AssetManger 对象, 调用其open 方法取得  对应的inputStream对象
            int size = in.available();//取得数据流的数据大小
            byte[] buffer = new byte[size];
            in.read(buffer);
            in.close();
            result = new String(buffer);
        } catch (Exception e) {
            MoeLogger.d("getAssets:" + result);
        }
        return result;
    }

    public static void mp3Scanner(String path) {
        MediaScannerConnection.scanFile(CoreApplication.getInstance().getApplicationContext(),
                new String[]{path}, null, null);
    }

    /**
     * open apk
     *
     * @param context
     * @param apk
     */
    public static void openApk(Context context, File apk) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(apk),
                "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    /**
     * download apk from server
     *
     * @param path the apk path
     * @param fp   file progress listener
     * @return apk file
     * @throws Exception
     */
    public static File getFilefromServerToProgress(String path, FileProgress fp) throws Exception {
        //如果相等的话表示当前的sdcard挂载在手机上并且是可用的
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(DELAY_TIME);
            int max = conn.getContentLength();
            InputStream is = conn.getInputStream();
            File file = new File(Environment.getExternalStorageDirectory(), "ifen.apk");
            FileOutputStream fos = new FileOutputStream(file);
            BufferedInputStream bis = new BufferedInputStream(is);
            byte[] buffer = new byte[1024];
            int len;
            int total = 0;
            while ((len = bis.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
                total += len;
                //获取当前下载量
                if (fp != null)
                    fp.getProgress(total, max);
            }
            fos.close();
            bis.close();
            is.close();
            return file;
        } else {
            return null;
        }
    }

    private static Pattern FilePattern = Pattern.compile("[\\\\/:*?\"<>|]");

    public static String filenameFilter(String str) {
        return str == null ? null : FilePattern.matcher(str).replaceAll("");
    }

    public interface FileProgress {
        void getProgress(int total, int max);
    }
}