package com.example.lightshare.share;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import com.example.lightshare.Config;
import com.example.lightshare.utils.BitmpUtils;
import com.example.lightshare.utils.FileUtils;
import com.example.lightshare.utils.UriUtils;
import com.tencent.connect.share.QQShare;
import com.tencent.connect.share.QzoneShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author gexinyu
 */
public class QQShareTask {

    public static final String QQ_PACKAGE_NAME = "com.tencent.mobileqq";
    public static final String QQ_SHARE_COMPONENT_NAME = "com.tencent.mobileqq.activity.JumpActivity";
    public static QQShareListener mQQShareListener;
    //分享方式，默认使用SDK，可以修改为native方式
    public static boolean mShareWithSDK = true;

    /**
     * 执行分享
     *
     * @param activity
     * @param builder
     */
    public static void executeShare(final Activity activity, final ShareKeeper.Builder builder) {
        boolean qqClientAvailable = isQQClientAvailable(activity);
        OnShareListener mOnShareListener = builder.mOnShareListener;
        //监测是否有客户端
        if (!qqClientAvailable) {
            mOnShareListener.onShareFailed(builder.mPlatform, builder.mShareType, "客户端未安装");
        } else {
            //使用application的context初始化实例化
            final Tencent mTencent = Tencent.createInstance(Config.TENCENT_APP_ID, activity.getApplicationContext());
            //根据平台区分
            if (builder.mPlatform == ShareKeeper.PLATFORM_QQ) {
                performQQShare(activity, mTencent, builder);
            } else {
                //Q空间分享
                performQZoneShare(activity, mTencent, builder);
            }
        }
    }

    /**
     * Q空间只分享图文
     *
     * @param activity
     * @param mTencent
     * @param builder
     */
    private static void performQZoneShare(Activity activity, Tencent mTencent, ShareKeeper.Builder builder) {
        Bundle params = new Bundle();
        params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT);
        params.putString(QzoneShare.SHARE_TO_QQ_TITLE, builder.mTitle + "");
        params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, builder.mDesc + "");
        params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, builder.mWebUrl);
        //分享到QQ空间的图片可以使本地或者网络但是只会取第一个参数
        ArrayList imageUrls = new ArrayList();
        imageUrls.add(builder.mImagePath);
        imageUrls.add(builder.mImageUrl);
        params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, imageUrls);
        params.putInt(QQShare.SHARE_TO_QQ_EXT_INT, QQShare.SHARE_TO_QQ_FLAG_QZONE_AUTO_OPEN);
        //创建监听器
        mQQShareListener = new QQShareListener(builder);
        mTencent.shareToQzone(activity, params, mQQShareListener);
    }

    /**
     * qq分享类型较多
     *
     * @param activity
     * @param mTencent
     * @param builder
     */
    private static void performQQShare(Activity activity, Tencent mTencent, ShareKeeper.Builder builder) {
        if (builder.mShareType == ShareKeeper.TYPE_TXT) {
            performQQShareTxt(activity, builder);
        } else if (builder.mShareType == ShareKeeper.TYPE_PICTURE) {
            performQQSharePic(activity, mTencent, builder);
        } else if (builder.mShareType == ShareKeeper.TYPE_AVDIO) {
            performQQShareAVDio(activity, mTencent, builder);
        } else {
            //其他的就使用默认的分享
            performQQShareWeb(activity, mTencent, builder);
        }
    }

    /**
     * 分享网页  默认类型
     *
     * @param activity
     * @param mTencent
     * @param builder
     */
    private static void performQQShareWeb(Activity activity, Tencent mTencent, ShareKeeper.Builder builder) {
        Bundle params = new Bundle();
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);//默认分享类型
        params.putString(QQShare.SHARE_TO_QQ_TITLE, builder.mTitle + "");//分享标题（默认不可以为空，防止空指针）
        params.putString(QQShare.SHARE_TO_QQ_SUMMARY, builder.mDesc + "");//分享简介（默认可以为空防止空指针）
        params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, builder.mWebUrl);//weburl（除了图片以外都需要）
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, builder.mImageUrl);//网络图片(缩略图，没有也不影响)
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, builder.mImagePath);//本地图片路径（单纯的本地图片分享使用）
        //创建监听器
        mQQShareListener = new QQShareListener(builder);
        mTencent.shareToQQ(activity, params, mQQShareListener);
    }

    /**
     * QQ分享纯文本
     *
     * @param activity
     * @param builder
     */
    private static void performQQShareTxt(Activity activity, ShareKeeper.Builder builder) {
        //关于QQ纯文本分享的要单独处理
        OnShareListener mOnShareListener = builder.mOnShareListener;
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(NetiveShareTask.TYPE_TXT);
        intent.putExtra(Intent.EXTRA_SUBJECT, "分享");
        intent.putExtra(Intent.EXTRA_TEXT, builder.mDesc + "");//不能为空
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(new ComponentName(QQ_PACKAGE_NAME, QQ_SHARE_COMPONENT_NAME));
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivityForResult(intent, NetiveShareTask.SHARE_REQUEST_CODE);
        } else {
            if (mOnShareListener != null) {
                mOnShareListener.onShareFailed(builder.mPlatform, builder.mShareType, "分享所在Activity异常!");
            }
        }
    }

    /**
     * QQ分享图片
     * 两种情况，一是本地图片，二是网络图片
     *
     * @param activity
     * @param mTencent
     * @param builder
     */
    private static void performQQSharePic(Activity activity, Tencent mTencent, ShareKeeper.Builder builder) {
        String mImageUrl = builder.mImageUrl;
        String mLocalImage = builder.mImagePath;
        OnShareListener mOnShareListener = builder.mOnShareListener;

        if (!TextUtils.isEmpty(mLocalImage) && FileUtils.isExist(mLocalImage)) {
            //分享本地图片也可以有两种方式，但是SDK监听齐全，所以不适用native方式
            performShareLocalPic(activity, mTencent, builder);
        } else if (!TextUtils.isEmpty(mImageUrl)) {
            if (!mImageUrl.startsWith("http://") && !mImageUrl.startsWith("https://")) {
                if (mOnShareListener != null) {
                    mOnShareListener.onShareFailed(builder.mPlatform, builder.mShareType, "分享的图片异常!");
                }
            } else {
                performShareNetPic(activity, mTencent, builder);
            }
        } else {
            if (mOnShareListener != null) {
                mOnShareListener.onShareFailed(builder.mPlatform, builder.mShareType, "分享的图片不能为空!");
            }
        }
    }

    /**
     * 分享网络图片
     * 需要先下载到本地
     * 然后设置到本地路径去分享
     *
     * @param activity
     * @param builder
     */
    private static void performShareNetPic(final Activity activity, final Tencent mTencent, final ShareKeeper.Builder builder) {
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
                    performShareLocalPic(activity, mTencent, builder);
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
     * 分享图片通过SDK
     *
     * @param activity
     * @param mTencent
     * @param builder
     */
    private static void performShareLocalPic(Activity activity, Tencent mTencent, ShareKeeper.Builder builder) {
        Bundle params = new Bundle();
        //处理不同平台类型的类型
        mQQShareListener = new QQShareListener(builder);
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE);//分享类型
        params.putString(QQShare.SHARE_TO_QQ_TITLE, builder.mTitle + "");//分享标题（默认不可以为空，防止空指针）
        params.putString(QQShare.SHARE_TO_QQ_SUMMARY, builder.mDesc + "");//分享简介（默认可以为空防止空指针）
        params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, builder.mWebUrl);//weburl（除了图片以外都需要）
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, builder.mImageUrl);//网络图片(缩略图，没有也不影响)
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, builder.mImagePath);//本地图片路径（单纯的本地图片分享使用）
        mTencent.shareToQQ(activity, params, mQQShareListener);
    }

    /**
     * 分享视频
     *
     * @param activity
     * @param mTencent
     * @param builder
     */
    private static void performQQShareAVDio(Activity activity, Tencent mTencent, ShareKeeper.Builder builder) {
        if (mShareWithSDK) {
            performQQShareaAVideoWithSDK(activity, mTencent, builder);
        } else {
            performQQShareAVideoNative(activity, builder);
        }
    }

    /**
     * QQ分享本地视频
     *
     * @param activity
     * @param builder
     */
    private static void performQQShareAVideoNative(Activity activity, ShareKeeper.Builder builder) {
        String mAVdioPath = builder.mAVdioPath;
        OnShareListener mOnShareListener = builder.mOnShareListener;
        Uri uri = UriUtils.getUri(activity, mAVdioPath);
        if (uri == null) {
            if (mOnShareListener != null) {
                mOnShareListener.onShareFailed(builder.mPlatform, builder.mShareType, "分享本地视频地址异常!");
            }
            return;
        }
        Intent intent = new Intent("android.intent.action.SEND");
        intent.setType(NetiveShareTask.TYPE_VIDEO);
        intent.putExtra(Intent.EXTRA_TEXT, builder.mTitle + "");//不能为空
        intent.putExtra(Intent.EXTRA_SUBJECT, builder.mDesc + "");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(new ComponentName(QQ_PACKAGE_NAME, QQ_SHARE_COMPONENT_NAME));
        activity.startActivityForResult(intent, NetiveShareTask.SHARE_REQUEST_CODE);
    }

    /**
     * QQ分享网页音视频
     *
     * @param activity
     * @param mTencent
     * @param builder
     */
    private static void performQQShareaAVideoWithSDK(Activity activity, Tencent mTencent, ShareKeeper.Builder builder) {
        Bundle params = new Bundle();
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_AUDIO);//默认分享类型
        params.putString(QQShare.SHARE_TO_QQ_TITLE, builder.mTitle + "");//分享标题（默认不可以为空，防止空指针）
        params.putString(QQShare.SHARE_TO_QQ_SUMMARY, builder.mDesc + "");//分享简介（默认可以为空防止空指针）
        params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, builder.mWebUrl);//weburl（除了图片以外都需要）
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, builder.mImageUrl);//网络图片(缩略图，没有也不影响)
        params.putString(QQShare.SHARE_TO_QQ_AUDIO_URL, builder.mAVdioUrl);//本地图片路径（单纯的本地图片分享使用）
        //创建监听器
        mQQShareListener = new QQShareListener(builder);
        mTencent.shareToQQ(activity, params, mQQShareListener);
    }


    /**
     * 内部类 QQ分享的监听器
     */
    public static class QQShareListener implements IUiListener {

        private ShareKeeper.Builder mBuilder;
        private OnShareListener mOnShareListener;

        public QQShareListener(ShareKeeper.Builder builder) {
            if (builder != null) {
                mBuilder = builder;
                mOnShareListener = builder.mOnShareListener;
            }
        }

        @Override
        public void onComplete(Object o) {
            if (mOnShareListener != null) {
                mOnShareListener.onShareSuccess(mBuilder.mPlatform, mBuilder.mShareType);
            }
        }

        @Override
        public void onError(UiError uiError) {
            if (mOnShareListener != null) {
                mOnShareListener.onShareFailed(mBuilder.mPlatform, mBuilder.mShareType, uiError.errorMessage);
            }
        }

        @Override
        public void onCancel() {
            if (mOnShareListener != null) {
                mOnShareListener.onCancleShare(mBuilder.mPlatform, mBuilder.mShareType, "");
            }
        }
    }

    /**
     * 获取监听器
     *
     * @return
     */
    public static IUiListener getIUiListener() {
        return mQQShareListener;
    }

    public static void onDestory() {
        if (mQQShareListener != null) {
            mQQShareListener = null;
        }
    }

    /**
     * 手机是否安装QQ客户端
     *
     * @param context
     * @return
     */
    public static boolean isQQClientAvailable(Context context) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                if (pn.equals(QQ_PACKAGE_NAME)) {
                    return true;
                }
            }
        }
        return false;
    }
}
