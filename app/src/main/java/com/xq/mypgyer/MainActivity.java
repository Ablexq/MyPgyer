package com.xq.mypgyer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.pgyersdk.crash.PgyCrashManager;
import com.pgyersdk.feedback.PgyerFeedbackManager;
import com.pgyersdk.update.DownloadFileListener;
import com.pgyersdk.update.PgyUpdateManager;
import com.pgyersdk.update.UpdateManagerListener;
import com.pgyersdk.update.javabean.AppBean;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = String.format("==============%s=============", MainActivity.class.getSimpleName());
    private TextView mTvVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {
        Button btnCheckUpdate = (Button) findViewById(R.id.btnCheckUpdate);
        btnCheckUpdate.setOnClickListener(this);
        Button btnCrashReport = (Button) findViewById(R.id.btnCrashReport);
        btnCrashReport.setOnClickListener(this);
        Button btnOpenFeedback = (Button) findViewById(R.id.btnOpenFeedback);
        btnOpenFeedback.setOnClickListener(this);
        Button btnOpenFeedback2 = (Button) findViewById(R.id.btnOpenFeedback2);
        btnOpenFeedback2.setOnClickListener(this);
        Button btnCloseFeedback = (Button) findViewById(R.id.btnCloseFeedback);
        btnCloseFeedback.setOnClickListener(this);
        mTvVersion = findViewById(R.id.tv_version);
        mTvVersion.setText(String.format("VERSION_CODE：%s \n VERSION_NAME: %s ",
                VersionUtil.packageCode(this), VersionUtil.packageName(this)));
    }

    private void testCrashReport() {
        throw new RuntimeException("这是个测试bug!");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            /*升级*/
            case R.id.btnCheckUpdate:
                /** 新版本 **/
                new PgyUpdateManager.Builder()
                        .setForced(true)                //设置是否强制提示更新,非自定义回调更新接口此方法有用
                        .setUserCanRetry(false)         //失败后是否提示重新下载，非自定义下载 apk 回调此方法有用
                        .setDeleteHistroyApk(false)     // 检查更新前是否删除本地历史 Apk， 默认为true
                        .setUpdateManagerListener(new UpdateManagerListener() {
                            @Override
                            public void onNoUpdateAvailable() {
                                //没有更新是回调此方法
                                Log.d(TAG, "there is no new version");
                            }

                            @Override
                            public void onUpdateAvailable(AppBean appBean) {
                                //有更新回调此方法
                                Log.d(TAG, "there is new version can update"
                                        + "new versionCode is " + appBean.getVersionCode());
                                //调用以下方法，DownloadFileListener 才有效；
                                //如果完全使用自己的下载方法，不需要设置DownloadFileListener
                                PgyUpdateManager.downLoadApk(appBean.getDownloadURL());
                            }

                            @Override
                            public void checkUpdateFailed(Exception e) {
                                //更新检测失败回调
                                //更新拒绝（应用被下架，过期，不在安装有效期，下载次数用尽）以及无网络情况会调用此接口
                                Log.e(TAG, "check update failed ", e);
                            }
                        })
                        //注意 ：
                        //下载方法调用 PgyUpdateManager.downLoadApk(appBean.getDownloadURL()); 此回调才有效
                        //此方法是方便用户自己实现下载进度和状态的 UI 提供的回调
                        //想要使用蒲公英的默认下载进度的UI则不设置此方法
                        .setDownloadFileListener(new DownloadFileListener() {
                            @Override
                            public void downloadFailed() {
                                //下载失败
                                Log.e(TAG, "download apk failed");
                            }

                            @Override
                            public void downloadSuccessful(File file) {
                                Log.e(TAG, "download apk success");
                                // 使用蒲公英提供的安装方法提示用户 安装apk
                                PgyUpdateManager.installApk(file);
                            }

                            @Override
                            public void onProgressUpdate(Integer... integers) {
                                Log.e(TAG, "update download apk progress" + integers[0]);
                            }
                        })
                        .register();
                break;

            /*异常*/
            case R.id.btnCrashReport:
                PgyCrashManager.register(); //推荐使用
                testCrashReport();
                break;

            /*反馈*/
            case R.id.btnOpenFeedback:
                // 默认采用摇一摇弹出 Dialog 方式
                new PgyerFeedbackManager.PgyerFeedbackBuilder().builder().register();
                break;

            case R.id.btnOpenFeedback2:
                // 采用摇一摇弹出 Activity 方式
                new PgyerFeedbackManager.PgyerFeedbackBuilder()
                        .setDisplayType(PgyerFeedbackManager.TYPE.DIALOG_TYPE)
                        .builder()
                        .register();
                break;

            case R.id.btnCloseFeedback:
                break;

            default:
                break;
        }
    }
}