package com.example.lightshare.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

/**
 * @author gexinyu
 */
public class BitmpUtils {

    /**
     * bitmap转为字节
     *
     * @param bmp
     * @param needRecycle
     * @return
     */
    public static byte[] bitmapToByte(final Bitmap bmp, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, output);
        if (needRecycle) {
            bmp.recycle();
        }
        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 网络图片转为bitmap
     *
     * @param url
     * @return
     */
    public static Bitmap getUrlBitmap(String url) {
        Bitmap bitmap = null;
        URL imageurl = null;
        try {
            imageurl = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            HttpURLConnection conn = (HttpURLConnection) imageurl.openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 按照质量压缩
     *
     * @param bmp
     * @param maxByteSize
     * @return
     */
    public static Bitmap compressToBitmap(final Bitmap bmp, final long maxByteSize) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, output);
        int options = 100;
        int length = output.size();
        while (length > maxByteSize && options > 0) {
            output.reset(); //清空baos
            bmp.compress(Bitmap.CompressFormat.JPEG, options, output);//这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;
            length = output.size();
        }
        return bmp;
    }

    /**
     * 图片压缩到指定大小
     *
     * @param bmp
     * @param maxByteSize
     * @return
     */
    public static byte[] compressBitmapSpecifySize(final Bitmap bmp, final long maxByteSize) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] bytes = null;
        int options = 100;
        int outSize = baos.size();

        if (outSize <= maxByteSize) {
            bytes = baos.toByteArray();
        } else {
            while (outSize > maxByteSize && options > 0) {
                baos.reset(); //清空baos
                bmp.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
                outSize = baos.size();
                options -= 10;
            }

            if (outSize > maxByteSize) {
                bytes = compressBitmapSize(bmp, options, maxByteSize);
            } else {
                bytes = baos.toByteArray();
            }
        }
        if (!bmp.isRecycled()) bmp.recycle();
        return bytes;
    }

    /**
     * 需要压缩
     *
     * @param bmp
     */
    private static byte[] compressBitmapSize(Bitmap bmp, int options, final long maxByteSize) {
        //继续在当前的质量下压缩
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, options, baos);
        int outSize = baos.size();
        int orginOpi = options;

        while (outSize > maxByteSize && options > 0) {
            baos.reset(); //清空baos
            Bitmap bitmap = BitmpUtils.scaleBitmap(bmp, options * 0.1f);
            bitmap.compress(Bitmap.CompressFormat.JPEG, orginOpi, baos);//这里压缩options%，把压缩后的数据存放到baos中
            options--;
        }

        byte[] bytes = baos.toByteArray();
        return bytes;
    }


    /**
     * 按比例缩放图片
     *
     * @param origin 原图
     * @param ratio  比例
     * @return 新的bitmap
     */
    public static Bitmap scaleBitmap(Bitmap origin, float ratio) {
        return scaleBitmap(origin, ratio, false);
    }


    /**
     * 按比例缩放图片
     *
     * @param origin 原图
     * @param ratio  比例
     * @return 新的bitmap
     */
    public static Bitmap scaleBitmap(Bitmap origin, float ratio, boolean recycle) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.preScale(ratio, ratio);

        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, true);
        if (newBM.equals(origin)) {
            return newBM;
        }
        if (recycle) {
            origin.recycle();
        }

        return newBM;
    }


    /**
     * 保存bitmap到指定本地文件夹
     *
     * @param bm
     * @throws IOException
     */
    public static String saveBitmapToLocal(Context context, Bitmap bm) throws IOException {
        String filePath = Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera/";
        String fileName = UUID.randomUUID().toString() + ".jpg";
        return saveBitmapToLocal(context, bm, filePath, fileName, true);
    }

    /**
     * 保存图片到本地
     *
     * @param bmp    图片
     * @param path   路径
     * @param name   图片名称
     * @param notify 是否通知相册
     * @throws IOException
     */
    public static String saveBitmapToLocal(Context context, Bitmap bmp, String path, String name, boolean notify) throws IOException {
        File localBitmapFile = FileUtils.makeFilePath(path, name);
        BufferedOutputStream bos = null;
        bos = new BufferedOutputStream(new FileOutputStream(localBitmapFile));
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        bos.flush();
        bos.close();

        //把图片保存后声明这个广播事件通知系统相册有新图片到来
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        //通知相册
        if (notify) {
            Uri uri = UriUtils.getUri(context, localBitmapFile);
            intent.setData(uri);
            context.sendBroadcast(intent);
        }

        return localBitmapFile.getAbsolutePath();
    }
}
