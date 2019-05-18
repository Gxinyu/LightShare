package com.example.lightshare.share;


import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import com.example.lightshare.Config;
import com.example.lightshare.utils.BitmpUtils;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXTextObject;
import com.tencent.mm.sdk.modelmsg.WXVideoObject;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import java.util.List;

import static com.tencent.mm.sdk.modelmsg.SendMessageToWX.Req.WXSceneSession;
import static com.tencent.mm.sdk.modelmsg.SendMessageToWX.Req.WXSceneTimeline;

/**
 * Created by cntv11 on 2018/1/13.
 */

public class WXShareTask {

    public static final int IMAGE_MAX_SIZE = 32768;//微信分享图片大小限制


    /**
     * 子线程中执行操作
     *
     * @param activity
     * @param builder
     */
    public static void executeShare(Activity activity, final ShareKeeper.Builder builder) {
        boolean weixinAvilible = isWeixinAvilible(activity);
        OnShareListener mOnShareListener = builder.mOnShareListener;
        //监测是否有客户端
        if (!weixinAvilible) {
            mOnShareListener.onShareFailed(builder.mPlatform, builder.mShareType,"未检测到客户端");
        } else {
            final IWXAPI wxApi = WXAPIFactory.createWXAPI(activity, Config.WECHAT_APP_ID);
            wxApi.registerApp(Config.WECHAT_APP_ID);
            final Bitmap mLocalImage = BitmapFactory.decodeFile(builder.mImagePath);;
            final String mImageUrl = builder.mImageUrl;
            //根据有没有图片分为三种情况
            new Thread() {
                public void run() {
                    if (mLocalImage != null) {
                        performShare(mLocalImage, wxApi, builder);
                    } else if (!TextUtils.isEmpty(mImageUrl)) {
                        Bitmap urlBitmap = BitmpUtils.getUrlBitmap(builder.mImageUrl);
                        performShare(urlBitmap, wxApi, builder);
                    } else {
                        performShare(null, wxApi, builder);
                    }
                }
            }.start();
        }
    }

    /**
     * 执行
     *
     * @param bitmap
     * @param iwxapi
     * @param builder
     */
    private static void performShare(Bitmap bitmap, IWXAPI iwxapi, ShareKeeper.Builder builder) {
        OnShareListener mOnShareListener = builder.mOnShareListener;
        WXMediaMessage.IMediaObject iMediaObject = createIMediaObject(builder, bitmap);

        if (iMediaObject != null) {
            //检测参数可以分享
            boolean checkArgs = iMediaObject.checkArgs();
            if (checkArgs) {
                //共同的部分
                WXMediaMessage msg = new WXMediaMessage(iMediaObject);
                msg.title = builder.mTitle;//标题
                msg.description = builder.mDesc;//描述
                //缩略图
                if (bitmap != null) {
                    byte[] thumbData = BitmpUtils.compressBitmapSpecifySize(bitmap, IMAGE_MAX_SIZE);
                    if (thumbData.length > IMAGE_MAX_SIZE) {
                        if (mOnShareListener != null) {
                            mOnShareListener.onShareFailed(builder.mPlatform, builder.mShareType,"分享的缩略图过大");
                        }
                        return;
                    }
                    msg.thumbData = thumbData;//缩略数据（图片内容）
                }
                //共同的部分
                SendMessageToWX.Req req = new SendMessageToWX.Req();
                req.transaction = buildDiffTransaction(builder.mShareType);
                req.message = msg;
                int mScene = builder.mPlatform == ShareKeeper.PLATFORM_WECHAT ? WXSceneSession : WXSceneTimeline;
                req.scene = mScene;
                iwxapi.sendReq(req);
            } else {
                if (mOnShareListener != null) {
                    mOnShareListener.onShareFailed(builder.mPlatform, builder.mShareType,"分享参数异常");
                }
            }
        }
    }

    /**
     * 创建分享的IMediaObject
     *
     * @param builder
     * @param bitmap
     * @return
     */
    private static WXMediaMessage.IMediaObject createIMediaObject(ShareKeeper.Builder builder, Bitmap bitmap) {

        int mPlatform = builder.mPlatform;
        int mShareType = builder.mShareType;
        WXMediaMessage.IMediaObject mediaObject = null;
        OnShareListener mOnShareListener = builder.mOnShareListener;

        if (mShareType == ShareKeeper.TYPE_DEFAULT) {
            WXWebpageObject webpageObject = new WXWebpageObject();
            String mWebUrl = builder.mWebUrl;
            if (!TextUtils.isEmpty(mWebUrl)) {
                webpageObject.webpageUrl = mWebUrl;
                mediaObject = webpageObject;
            } else {
                if (mOnShareListener != null) {
                    mOnShareListener.onShareFailed(mPlatform, mShareType,"分享的链接不能为空!");
                }
            }
        } else if (mShareType == ShareKeeper.TYPE_PICTURE) {
            if (bitmap != null) {
                WXImageObject imgObj = new WXImageObject(bitmap);
                mediaObject = imgObj;
            } else {
                if (mOnShareListener != null) {
                    mOnShareListener.onShareFailed(mPlatform, mShareType,"分享的图片不能为空!");
                }
            }
        } else if (mShareType == ShareKeeper.TYPE_AVDIO) {
            String videoUrl = builder.mAVdioUrl;
            if (!TextUtils.isEmpty(videoUrl)) {
                WXVideoObject videoObject = new WXVideoObject();
                videoObject.videoUrl = videoUrl;
                mediaObject = videoObject;
            } else {
                if (mOnShareListener != null) {
                    mOnShareListener.onShareFailed(mPlatform, mShareType,"分享的视频链接不能为空!");
                }
            }
        } else if (mShareType == ShareKeeper.TYPE_TXT) {
            String desc = builder.mDesc;
            if (!TextUtils.isEmpty(desc)) {
                WXTextObject textObj = new WXTextObject();
                textObj.text = desc;
                mediaObject = textObj;
            } else {
                if (mOnShareListener != null) {
                    mOnShareListener.onShareFailed(mPlatform, mShareType,"分享的文本不能为空!");
                }
            }
        }
        return mediaObject;
    }

    /**
     * 创建不同的tag
     * 主要作用是过滤作用
     * 用于回调的区分
     * 用同一个也可以
     *
     * @param mShareType
     * @return
     */
    private static String buildDiffTransaction(int mShareType) {
        String tag = "";
        if (mShareType == ShareKeeper.TYPE_DEFAULT) {
            tag = "default";
        } else if (mShareType == ShareKeeper.TYPE_PICTURE) {
            tag = "image";
        } else if (mShareType == ShareKeeper.TYPE_AVDIO) {
            tag = "avdio";
        } else if (mShareType == ShareKeeper.TYPE_TXT) {
            tag = "text";
        }
        //加上时间戳
        return tag + System.currentTimeMillis();
    }

    /**
     * 手机是否安装微信客户端
     *
     * @param context
     * @return
     */
    public static boolean isWeixinAvilible(Context context) {
        final PackageManager packageManager = context.getPackageManager();// 获取packagemanager
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);// 获取所有已安装程序的包信息
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                if (pn.equals("com.tencent.mm")) {
                    return true;
                }
            }
        }
        return false;
    }

}
