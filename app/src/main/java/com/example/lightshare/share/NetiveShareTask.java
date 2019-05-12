package com.example.lightshare.share;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import com.example.lightshare.utils.BitmpUtils;
import com.example.lightshare.utils.FileUtils;
import com.example.lightshare.utils.UriUtils;
import java.io.IOException;

/**
 * 利用系统原生的分享
 */
public class NetiveShareTask {

    public static final int SHARE_REQUEST_CODE = 1000;
    public static final String TYPE_TXT = "text/plain";
    public static final String TYPE_IMAGE = "image/*";
    public static final String TYPE_VIDEO = "video/*";

    /**
     * 执行原生分享
     *
     * @param activity
     * @param builder
     */
    public static void executeShare(Activity activity, ShareKeeper.Builder builder) {
        OnShareListener mOnShareListener = builder.mOnShareListener;
        //使用application的context初始化实例化
        //根据平台区分
        if (builder.mShareType == ShareKeeper.TYPE_TXT) {
            performNativeShareTxt(activity, builder);
        } else if (builder.mShareType == ShareKeeper.TYPE_PICTURE) {
            performNativeSharePic(activity, builder);
        } else if (builder.mShareType == ShareKeeper.TYPE_AVDIO) {
            performNativeShareVideo(activity, builder);
        }
    }

    /**
     * 分享文本
     *
     * @param activity
     * @param builder
     */
    private static void performNativeShareTxt(Activity activity, ShareKeeper.Builder builder) {
        OnShareListener mOnShareListener = builder.mOnShareListener;
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(NetiveShareTask.TYPE_TXT);
        intent.putExtra(Intent.EXTRA_TEXT, builder.mDesc + "");//不能为空
        Intent chooser = Intent.createChooser(intent, builder.mTitle);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivityForResult(chooser, SHARE_REQUEST_CODE);
        } else {
            if (mOnShareListener != null) {
                mOnShareListener.onShareFailed(builder.mPlatform, builder.mShareType, "分享所在Activity异常!");
            }
        }
    }

    /**
     * 分享图片
     *
     * @param activity
     * @param builder
     */
    private static void performNativeSharePic(Activity activity, ShareKeeper.Builder builder) {

        String mImageUrl = builder.mImageUrl;
        String mLocalImage = builder.mImagePath;
        OnShareListener mOnShareListener = builder.mOnShareListener;

        if (!TextUtils.isEmpty(mLocalImage) && FileUtils.isExist(mLocalImage)) {
            //分享本地图片也可以有两种方式，但是SDK监听齐全，所以不适用native方式
            performNativeShareLocalPic(activity, builder);
        } else if (!TextUtils.isEmpty(mImageUrl)) {
            if (!mImageUrl.startsWith("http://") && !mImageUrl.startsWith("https://")) {
                if (mOnShareListener != null) {
                    mOnShareListener.onShareFailed(builder.mPlatform, builder.mShareType, "分享的图片异常!");
                }
            } else {
                performNativeShareNetPic(activity, builder);
            }
        } else {
            if (mOnShareListener != null) {
                mOnShareListener.onShareFailed(builder.mPlatform, builder.mShareType, "分享的图片不能为空!");
            }
        }

    }

    /**
     * 原生分享网络图片
     *
     * @param activity
     * @param builder
     */
    private static void performNativeShareNetPic(final Activity activity, final ShareKeeper.Builder builder) {
        new Thread() {
            public void run() {
                OnShareListener mOnShareListener = builder.mOnShareListener;
                try {
                    String mImageUrl = builder.mImageUrl;
                    //先获取图片然后保存到本地分享
                    Bitmap urlBitmap = BitmpUtils.getUrlBitmap(mImageUrl);
                    //检测生成的图片是否存在
                    if (urlBitmap == null && mOnShareListener != null) {
                        mOnShareListener.onShareFailed(builder.mPlatform, builder.mShareType, "分享图片为空!");
                        return;
                    }
                    //SDK分享网络图片
                    final String saveBitmapToLocal = BitmpUtils.saveBitmapToLocal(activity, urlBitmap);
                    builder.setImagePath(saveBitmapToLocal);
                    //最后转为本地分享
                    performNativeSharePic(activity, builder);
                } catch (IOException e) {
                    e.printStackTrace();
                    if (mOnShareListener != null) {
                        mOnShareListener.onShareFailed(builder.mPlatform, builder.mShareType, "分享异常：" + e.getMessage());
                    }
                }
            }
        }.start();
    }


    /**
     * 原生本地图片分享
     *
     * @param activity
     * @param builder
     */
    private static void performNativeShareLocalPic(Activity activity, ShareKeeper.Builder builder) {
        String mImagePath = builder.mImagePath;
        OnShareListener mOnShareListener = builder.mOnShareListener;
        Uri uri = UriUtils.getUri(activity, mImagePath);
        if (uri == null) {
            if (mOnShareListener != null) {
                mOnShareListener.onShareFailed(builder.mPlatform, builder.mShareType, "分享本地视频地址异常!");
            }
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(NetiveShareTask.TYPE_IMAGE);
        intent.putExtra(Intent.EXTRA_TEXT, builder.mTitle + "");//不能为空
        intent.putExtra(Intent.EXTRA_SUBJECT, builder.mDesc + "");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Intent chooser = Intent.createChooser(intent, builder.mTitle);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivityForResult(chooser, SHARE_REQUEST_CODE);
        } else {
            if (mOnShareListener != null) {
                mOnShareListener.onShareFailed(builder.mPlatform, builder.mShareType, "分享所在Activity异常!");
            }
        }
    }

    /**
     * 原生分享本地视频
     *
     * @param activity
     * @param builder
     */
    private static void performNativeShareVideo(Activity activity, ShareKeeper.Builder builder) {
        String mAVdioPath = builder.mAVdioPath;
        OnShareListener mOnShareListener = builder.mOnShareListener;
        Uri uri = UriUtils.getUri(activity, mAVdioPath);
        if (uri == null) {
            if (mOnShareListener != null) {
                mOnShareListener.onShareFailed(builder.mPlatform, builder.mShareType, "分享本地视频地址异常!");
            }
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(NetiveShareTask.TYPE_VIDEO);
        intent.putExtra(Intent.EXTRA_TEXT, builder.mTitle + "");//不能为空
        intent.putExtra(Intent.EXTRA_SUBJECT, builder.mDesc + "");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Intent chooser = Intent.createChooser(intent, builder.mTitle);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivityForResult(chooser, SHARE_REQUEST_CODE);
        } else {
            if (mOnShareListener != null) {
                mOnShareListener.onShareFailed(builder.mPlatform, builder.mShareType, "分享所在Activity异常!");
            }
        }
    }

}
