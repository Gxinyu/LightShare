package com.example.lightshare.utils;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;

import java.io.File;

/**
 * @author gexinyu
 */
public class UriUtils {

    /**
     * 根据intent返回值获取URI的值
     *
     * @param context
     * @param data
     * @return
     */
    public static Uri getUriFromIntent(Context context, Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (Build.VERSION.SDK_INT >= 19) {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                String docId = DocumentsContract.getDocumentId(uri);
                if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                    String id = docId.split(":")[1];
                    String selection = MediaStore.Images.Media._ID + "=" + id;
                    imagePath = getUriPath(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
                } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                    Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                    imagePath = getUriPath(context, contentUri, null);
                }
            } else if ("content".equalsIgnoreCase(uri.getScheme())) {
                imagePath = getUriPath(context, uri, null);
            } else if ("file".equalsIgnoreCase(uri.getScheme())) {
                imagePath = uri.getPath();
            }
        } else {
            uri = data.getData();
            imagePath = getUriPath(context, uri, null);
        }

        File file = new File(imagePath);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(context,
                    getFileprovider(context), file);
        } else {
            uri = Uri.fromFile(file);
        }

        return uri;
    }


    /**
     * 从bitmap获取URi
     *
     * @param context
     * @param bitmap
     * @return
     */
    public static Uri getUriFromBitmap(Context context, Bitmap bitmap) {
        Uri uri = Uri.parse(MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, null, null));
        return uri;
    }


    /**
     * @param context
     * @param uri
     * @param selection
     * @return
     */
    private static String getUriPath(Context context, Uri uri, String selection) {
        String path = null;
        Cursor cursor = context.getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }


    /**
     * 根据文件获取URI
     *
     * @param context
     * @param file
     * @return
     */
    public static Uri getUri(Context context, File file) {
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(context,
                    getFileprovider(context), file);
        } else {
            uri = Uri.fromFile(file);
        }
        //这里尽量还是要保证URI不要为空，否则报空指针异常
        return uri;
    }

    /**
     * 根据文件路径获取URI
     *
     * @param context
     * @param filePath
     * @return
     */
    public static Uri getUri(Context context, String filePath) {
        //要先判断当前文件是否存在,如果不存在的话需要重新创建
        File file = new File(filePath);

        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(context.getApplicationContext(),
                    getFileprovider(context), file);
        } else {
            uri = Uri.fromFile(file);
        }

        return uri;
    }

    /**
     * 获取当前的FileProvider
     *
     * @param context
     * @return
     */
    public static String getFileprovider(Context context) {
        return context.getApplicationContext().getApplicationInfo().processName + ".fileProvider";
    }
}
