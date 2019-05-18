package com.example.lightshare.share;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.IntDef;

import com.sina.weibo.sdk.share.WbShareCallback;
import com.sina.weibo.sdk.share.WbShareHandler;
import com.tencent.connect.common.Constants;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * @author gexinyu
 */
public class ShareKeeper {

    //5种平台
    public static final int PLATFORM_QQ = 1;
    public static final int PLATFORM_QZONE = 2;
    public static final int PLATFORM_WECHAT = 3;
    public static final int PLATFORM_WECHAT_MOMENTS = 4;
    public static final int PLATFORM_WEIBO = 5;
    public static final int PLATFORM_NATIVE = 6;

    //四种类型
    public static final int TYPE_DEFAULT = 100;//默认
    public static final int TYPE_TXT = 101;//纯文本
    public static final int TYPE_PICTURE = 102;//图片
    public static final int TYPE_AVDIO = 103;//音视频

    @IntDef({PLATFORM_QQ, PLATFORM_QZONE, PLATFORM_WECHAT, PLATFORM_WECHAT_MOMENTS, PLATFORM_WEIBO, PLATFORM_NATIVE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Platform {
    }

    @IntDef({TYPE_DEFAULT, TYPE_TXT, TYPE_PICTURE, TYPE_AVDIO})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ShareType {
    }

    private Builder mShareBuilder = null;

    //私有构造
    private ShareKeeper() {
    }

    private static class SingleTonHoler {
        private static ShareKeeper INSTANCE = new ShareKeeper();
    }

    public static ShareKeeper getInstance() {
        return SingleTonHoler.INSTANCE;
    }

    /**
     * 创建构造
     *
     * @param context
     * @return
     */
    public Builder builder(Context context) {
        Builder builder = new Builder(context);
        return builder;
    }

    //*****************************构造builder***********************
    public final class Builder implements Serializable {

        public Context mContext;
        public int mPlatform = ShareKeeper.PLATFORM_QQ;//分享平台
        public int mShareType = ShareKeeper.TYPE_DEFAULT;//类型
        public String mTitle;
        public String mDesc;
        public String mWebUrl;//分享的链接
        public String mImageUrl;//网络图片
        public String mImagePath;//本地图片路径
        public String mAVdioUrl;//音视频的链接
        public String mAVdioPath;//音视频的本地链接
        public OnShareListener mOnShareListener;


        Builder(Context context) {
            mContext = context;
        }

        /**
         * 设置分享类型
         *
         * @param platform
         */
        public Builder setPlatform(@Platform int platform) {
            this.mPlatform = platform;
            return this;
        }

        /**
         * 设置分享类型
         *
         * @param shareType
         */
        public Builder setShareType(@ShareType int shareType) {
            this.mShareType = shareType;
            return this;
        }

        /**
         * 设置标题
         *
         * @param title
         */
        public Builder setTitle(String title) {
            this.mTitle = title;
            return this;
        }

        /**
         * 设置描述简介
         *
         * @param desc
         */
        public Builder setDesc(String desc) {
            this.mDesc = desc;
            return this;
        }


        /**
         * 分享链接
         *
         * @param webUrl
         * @return
         */
        public Builder setWebUrl(String webUrl) {
            this.mWebUrl = webUrl;
            return this;
        }

        /**
         * 设置分享图片路径
         *
         * @param imageUrl
         */
        public Builder setImageUrl(String imageUrl) {
            this.mImageUrl = imageUrl;
            return this;
        }

        /**
         * 设置本地图片路径
         *
         * @param imagePath
         */
        public Builder setImagePath(String imagePath) {
            mImagePath = imagePath;
            return this;
        }

        /**
         * 设置音视频的链接
         *
         * @param audioUrl
         * @return
         */
        public Builder setAVdioUrl(String audioUrl) {
            this.mAVdioUrl = audioUrl;
            return this;
        }

        /**
         * 本地音视频路径
         *
         * @param mAVdioPath
         */
        public Builder setAVdioPath(String mAVdioPath) {
            this.mAVdioPath = mAVdioPath;
            return this;
        }

        /**
         * 设置分享监听器
         *
         * @param onShareListener
         * @return
         */
        public Builder setOnShareListener(OnShareListener onShareListener) {
            this.mOnShareListener = onShareListener;
            return this;
        }

        /**
         * 开始分享
         *
         * @return
         */
        public void share() {
            performShare(this);
        }
    }


    /**
     * 开始校验分享
     *
     * @param builder
     */
    private void performShare(final Builder builder) {
        if (builder != null) {
            OnShareListener mOnShareListener = builder.mOnShareListener;
            Context mContext = builder.mContext;
            if (mContext instanceof Activity) {
                Activity activity = (Activity) mContext;
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
                        shareToPlatform(activity, builder);
                    } else {
                        if (mOnShareListener != null) {
                            mOnShareListener.onShareFailed(builder.mPlatform, builder.mShareType, "分享所在Activity已经被销毁");
                        }
                    }
                } else {
                    if (activity != null && !activity.isFinishing()) {
                        shareToPlatform(activity, builder);
                    } else {
                        if (mOnShareListener != null) {
                            mOnShareListener.onShareFailed(builder.mPlatform, builder.mShareType, "分享所在Activity已经被销毁");
                        }
                    }
                }
            } else {
                if (mOnShareListener != null) {
                    mOnShareListener.onShareFailed(builder.mPlatform, builder.mShareType, "分享所在Activity不存在");
                }
            }
        }
    }

    /**
     * 分享到不同的平台
     *
     * @param activity
     * @param builder
     */
    private void shareToPlatform(Activity activity, Builder builder) {
        //根据分享类型来分享内容
        mShareBuilder = builder;
        int mPlatform = builder.mPlatform;
        if (mPlatform == PLATFORM_QQ
                || mPlatform == PLATFORM_QZONE) {
            QQShareTask.executeShare(activity, builder);
        } else if (mPlatform == PLATFORM_WECHAT
                || mPlatform == PLATFORM_WECHAT_MOMENTS) {
            WXShareTask.executeShare(activity, builder);
        } else if (mPlatform == PLATFORM_WEIBO) {
            WBShareTask.executeShare(activity, builder);
        } else if (mPlatform == PLATFORM_NATIVE) {
            NetiveShareTask.executeShare(activity, builder);
        }


    }

    /**
     * 处理微信的分享结果
     */
    public void performWechatShareResult(BaseResp resp) {
        if (mShareBuilder != null) {
            int mPlatform = mShareBuilder.mPlatform;
            int mShareType = mShareBuilder.mShareType;
            OnShareListener onShareListener = mShareBuilder.mOnShareListener;
            if (onShareListener != null && resp != null) {
                int errCode = resp.errCode;
                if (errCode == BaseResp.ErrCode.ERR_OK) {
                    onShareListener.onShareSuccess(mPlatform, mShareType);
                } else if (errCode == BaseResp.ErrCode.ERR_SENT_FAILED) {
                    onShareListener.onShareFailed(mPlatform, mShareType, "发送失败");
                } else if (errCode == BaseResp.ErrCode.ERR_AUTH_DENIED) {
                    onShareListener.onShareFailed(mPlatform, mShareType, "认证被否决");
                } else if (errCode == BaseResp.ErrCode.ERR_UNSUPPORT) {
                    onShareListener.onShareFailed(mPlatform, mShareType, "版本不支持");
                } else if (errCode == BaseResp.ErrCode.ERR_COMM) {
                    onShareListener.onShareFailed(mPlatform, mShareType, "一般错误");
                } else if (errCode == BaseResp.ErrCode.ERR_USER_CANCEL) {
                    onShareListener.onCancleShare(mPlatform, mShareType, "取消分享");
                } else {
                    onShareListener.onShareFailed(mPlatform, mShareType, "未知错误");
                }
            }
        }
    }

    /**
     * 处理QQ分享
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void performQQOrNativeShareResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (mShareBuilder != null) {
            OnShareListener mOnShareListener = mShareBuilder.mOnShareListener;
            //需要判断是否是QQ分享
            if (requestCode == Constants.REQUEST_QQ_SHARE
                    || requestCode == Constants.REQUEST_QZONE_SHARE
                    || requestCode == Constants.REQUEST_OLD_SHARE) {
                IUiListener mIUiListener = QQShareTask.getIUiListener();
                if (mOnShareListener != null && mIUiListener != null) {
                    Tencent.onActivityResultData(requestCode, resultCode, data, mIUiListener);
                }
            } else if (requestCode == NetiveShareTask.SHARE_REQUEST_CODE) {
                //说明是QQ原生方式分享
                if (mOnShareListener != null) {
                    mOnShareListener.onShareSuccess(mShareBuilder.mPlatform, mShareBuilder.mShareType);
                }
            }
        }
    }

    /**
     * 处理微博的分享监听
     *
     * @param intent
     */
    public void performWBShareResult(Intent intent) {
        WbShareHandler wbShareHandler = WBShareTask.getWbShareHandler();
        if (wbShareHandler != null) {
            if (mShareBuilder != null) {
                final int mPlatform = mShareBuilder.mPlatform;
                final int mShareType = mShareBuilder.mShareType;
                final OnShareListener mOnShareListener = mShareBuilder.mOnShareListener;
                if (mOnShareListener != null) {
                    wbShareHandler.doResultIntent(intent, new WbShareCallback() {
                        @Override
                        public void onWbShareSuccess() {
                            mOnShareListener.onShareSuccess(mPlatform, mShareType);
                        }

                        @Override
                        public void onWbShareCancel() {
                            mOnShareListener.onCancleShare(mPlatform, mShareType, "取消分享!");
                        }

                        @Override
                        public void onWbShareFail() {
                            mOnShareListener.onShareFailed(mPlatform, mShareType, "分享失败!");
                        }
                    });
                }
            }
        }
    }


    /**
     * 处理当前activity销毁的时候回调
     */
    public void onDestory() {
        if (mShareBuilder != null) {
            mShareBuilder = null;
        }

        WBShareTask.onDestory();
        QQShareTask.onDestory();
    }
}

