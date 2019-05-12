package com.example.lightshare;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import com.example.lightshare.share.OnShareListener;
import com.example.lightshare.share.QQShareTask;
import com.example.lightshare.share.ShareKeeper;


public class MainActivity extends AppCompatActivity implements OnShareListener {

    private String title = "分享标题";
    private String desc = "分享简介";
    private String imageUrl = "https://upload-images.jianshu.io/upload_images/6282067-44c21d511d89c9d4";
    private String videoUrl = "http://v.youku.com/v_show/id_XMzI0MzA3NjI1Ng==.html?spm=a2hww.20022069.m_215416.5~5~5~5!2~A";
    private String localVideoPath = "/storage/emulated/0/hpplay_demo/local_media/test_video.mp4";//自己手机视频的路径
    private String localPicPath = "/storage/emulated/0/Pictures/1555730589639.jpg";//自己手机图片的路径
    private String webUrl = "https://blog.csdn.net/change987654321/article/details/53199139";

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //申请权限
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, 1);
            }
        }
    }

    /**
     * QQ分享默认的
     *
     * @param view
     */
    public void qqDefault(View view) {
        ShareKeeper.getInstance()
                .builder(this)
                .setPlatform(ShareKeeper.PLATFORM_QQ)
                .setShareType(ShareKeeper.TYPE_DEFAULT)
                .setTitle(title)
                .setDesc(desc)
                .setImageUrl(imageUrl)
                .setWebUrl(webUrl)
                .setOnShareListener(this)
                .share();
    }

    /**
     * 分享QQ纯文本
     *
     * @param view
     */
    public void qqTxt(View view) {
        ShareKeeper.getInstance()
                .builder(this)
                .setPlatform(ShareKeeper.PLATFORM_QQ)
                .setShareType(ShareKeeper.TYPE_TXT)
                .setDesc(desc)
                .setOnShareListener(this)
                .share();
    }

    /**
     * QQ分享本地图片
     *
     * @param view
     */
    public void qqLocalPic(View view) {
        ShareKeeper.getInstance()
                .builder(this)
                .setPlatform(ShareKeeper.PLATFORM_QQ)
                .setShareType(ShareKeeper.TYPE_PICTURE)
                .setTitle(title)
                .setDesc(desc)
                .setImagePath(localPicPath)
                .setOnShareListener(this)
                .share();
    }

    /**
     * QQ分享网络图片
     *
     * @param view
     */
    public void qqNetPic(View view) {
        ShareKeeper.getInstance()
                .builder(this)
                .setPlatform(ShareKeeper.PLATFORM_QQ)
                .setShareType(ShareKeeper.TYPE_PICTURE)
                .setTitle(title)
                .setDesc(desc)
                .setImageUrl(imageUrl)
                .setOnShareListener(this)
                .share();
    }

    /**
     * 分享QQ网页视频
     *
     * @param view
     */
    public void qqWebVideo(View view) {
        QQShareTask.mShareWithSDK = true;
        ShareKeeper.getInstance()
                .builder(this)
                .setPlatform(ShareKeeper.PLATFORM_QQ)
                .setShareType(ShareKeeper.TYPE_AVDIO)
                .setTitle(title)
                .setDesc(desc)
                .setImageUrl(imageUrl)
                .setWebUrl(webUrl)
                .setAVdioUrl(videoUrl)
                .setOnShareListener(this)
                .share();

    }

    /**
     * 分享QQ本地视频
     *
     * @param view
     */
    public void qqLocalVideo(View view) {
        //需要修改值
        QQShareTask.mShareWithSDK = false;
        ShareKeeper.getInstance()
                .builder(this)
                .setPlatform(ShareKeeper.PLATFORM_QQ)
                .setShareType(ShareKeeper.TYPE_AVDIO)
                .setTitle(title)
                .setDesc(desc)
                .setImageUrl(imageUrl)
                .setWebUrl(webUrl)
                .setAVdioPath(localVideoPath)
                .setOnShareListener(this)
                .share();

    }

    /**
     * QQ空间图文分享
     *
     * @param view
     */
    public void qZoneDefault(View view) {
        ShareKeeper.getInstance()
                .builder(this)
                .setPlatform(ShareKeeper.PLATFORM_QZONE)
                .setShareType(ShareKeeper.TYPE_DEFAULT)
                .setTitle(title)
                .setDesc(desc)
                .setImageUrl(imageUrl)
                .setWebUrl(webUrl)
                .setOnShareListener(this)
                .share();
    }


    /**
     * 微信分享默认的
     * ++++++++++++++++++++++++++++++++++++++++++++++++++++微信分享+++++++++++++++++++++++++++++++++++++
     * ++++++++++++++++++++++++++++++++++++++++++++++++++++++微信分享+++++++++++++++++++++++++++++++++++
     *
     * @param view
     */
    public void wechatDefault(View view) {
        ShareKeeper.getInstance()
                .builder(this)
                .setPlatform(ShareKeeper.PLATFORM_WECHAT)
                .setShareType(ShareKeeper.TYPE_DEFAULT)
                .setTitle(title)
                .setDesc(desc)
                .setImageUrl(imageUrl)
                .setWebUrl(webUrl)
                .setOnShareListener(this)
                .share();
    }

    /**
     * 分享微信纯文本
     *
     * @param view
     */
    public void wechatTxt(View view) {
        ShareKeeper.getInstance()
                .builder(this)
                .setPlatform(ShareKeeper.PLATFORM_WECHAT)
                .setShareType(ShareKeeper.TYPE_TXT)
                .setDesc(desc)
                .setOnShareListener(this)
                .share();
    }

    /**
     * 分享微信视频
     *
     * @param view
     */
    public void wechatVideo(View view) {
        ShareKeeper.getInstance()
                .builder(this)
                .setPlatform(ShareKeeper.PLATFORM_WECHAT)
                .setShareType(ShareKeeper.TYPE_AVDIO)
                .setTitle(title)
                .setDesc(desc)
                .setImageUrl(imageUrl)
                .setAVdioUrl(videoUrl)
                .setOnShareListener(this)
                .share();

    }

    /**
     * 微信分享本地图片
     *
     * @param view
     */
    public void wechatLocalPic(View view) {
        ShareKeeper.getInstance()
                .builder(this)
                .setPlatform(ShareKeeper.PLATFORM_WECHAT)
                .setShareType(ShareKeeper.TYPE_PICTURE)
                .setImagePath(localPicPath)
                .setOnShareListener(this)
                .share();
    }

    /**
     * 微信分享网络图片
     *
     * @param view
     */
    public void wechatNetPic(View view) {
        ShareKeeper.getInstance()
                .builder(this)
                .setPlatform(ShareKeeper.PLATFORM_WECHAT)
                .setShareType(ShareKeeper.TYPE_PICTURE)
                .setImageUrl(imageUrl)
                .setOnShareListener(this)
                .share();
    }


    /**
     * 原生
     * ++++++++++++++++++++++++++++++++++++++++++++++++++++原生分享+++++++++++++++++++++++++++++++++++++
     * ++++++++++++++++++++++++++++++++++++++++++++++++++++++原生分享+++++++++++++++++++++++++++++++++++
     * @param view
     */

    /**
     * 原生分享文本
     *
     * @param view
     */
    public void nativeLocalPic(View view) {
        ShareKeeper.getInstance()
                .builder(this)
                .setPlatform(ShareKeeper.PLATFORM_NATIVE)
                .setShareType(ShareKeeper.TYPE_PICTURE)
                .setDesc(desc)
                .setImagePath(localPicPath)
                .setOnShareListener(this)
                .share();
    }

    /**
     * 原生分享视频
     *
     * @param view
     */
    public void nativeNetPic(View view) {
        ShareKeeper.getInstance()
                .builder(this)
                .setPlatform(ShareKeeper.PLATFORM_NATIVE)
                .setShareType(ShareKeeper.TYPE_PICTURE)
                .setDesc(desc)
                .setImageUrl(imageUrl)
                .setOnShareListener(this)
                .share();
    }

    /**
     * 原生分享文本
     *
     * @param view
     */
    public void nativeTxt(View view) {
        ShareKeeper.getInstance()
                .builder(this)
                .setPlatform(ShareKeeper.PLATFORM_NATIVE)
                .setShareType(ShareKeeper.TYPE_TXT)
                .setDesc(desc)
                .setOnShareListener(this)
                .share();
    }

    /**
     * 原生分享视频
     *
     * @param view
     */
    public void nativeVideo(View view) {
        ShareKeeper.getInstance()
                .builder(this)
                .setPlatform(ShareKeeper.PLATFORM_NATIVE)
                .setShareType(ShareKeeper.TYPE_AVDIO)
                .setAVdioPath(localVideoPath)
                .setOnShareListener(this)
                .share();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ShareKeeper.getInstance().performQQOrNativeShareResult(requestCode, resultCode, data);
        Log.e("TAG", "分享code：" + requestCode);
    }

    @Override
    public void onShareSuccess(int sharePlatForm, int shareType) {
        Log.e("TAG", sharePlatForm + "分享成功");
    }

    @Override
    public void onShareFailed(int sharePlatForm, int shareType, String failedMessage) {
        Log.e("TAG", sharePlatForm + "分享失败：" + failedMessage);
    }

    @Override
    public void onCancleShare(int sharePlatForm, int shareType, String message) {
        Log.e("TAG", sharePlatForm + "分享取消：" + message);
    }

    @Override
    protected void onDestroy() {
        ShareKeeper.getInstance().onDestory();
        super.onDestroy();
    }
}