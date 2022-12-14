package com.example.custnetframework.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.custnetframework.R;
import com.zyz.http.*;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class CustNetFrameActivity extends AppCompatActivity {

    private CatHttpClient mCatHttpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cust_net_frame);

        mCatHttpClient = new CatHttpClient.Builder()
                .connTimeOut(10 * 1000)
                .writeTimeOut(10 * 1000)
                .build();

        /**
         * 这段是因为android 创建文件需要的运行时权限，一般从文件选择器选择需要的文件，
         * 与框架需要的file无根本关系
         */
        if (Build.VERSION.SDK_INT >= 23) {
            int REQUEST_CODE_CONTACT = 101;
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            //验证是否许可权限
            for (String str : permissions) {
                if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    //申请权限
                    this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
                    return;
                }
            }
        }
    }


    /**
     * 同步，默认是get请求
     *
     * @param view
     */
    public void syncGetClick(View view) {
        Request request = new Request.Builder()
                .url("https://www.baidu.com")
                .build();

        Response response = mCatHttpClient.newCall(request).execute();

        if (response.code() == 200) {
            Logger.e("syncGetClick Success response msg " + response.body().string());
        }
    }

    /**
     * 异步，默认是get请求
     * <p>
     * 回调在主线程中
     *
     * @param view
     */
    public void asyncGetClick(View view) {

        Request request = new Request.Builder()
                .url("https://www.baidu.com")
                .build();

        mCatHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Response response) {
                if (response.code() == 200) {
                    // 执行在主线程中，建议不要直接持有activity引用，利用weak+static
                    String msg = response.body().string();
                    Logger.e("asyncGetClick onResponse msg = " + msg);
                }
            }

            @Override
            public void onFail(Request request, IOException e) {
                e.printStackTrace();
            }
        });

    }

    /**
     * get请求(不添加 get 默认也是get请求)
     *
     * @param view
     */
    public void getClick(View view) {
        Request request = new Request.Builder()
                .url("https://www.baidu.com")
                .get()
                .build();

        Response response = mCatHttpClient.newCall(request).execute();
        if (response.code() == 200) {
            Logger.e(response.body().string());
        }
    }

    /**
     * post请求，提交表单
     * <p>
     * url替换为你实际要使用的url
     *
     * @param view
     */
    public void postClick(View view) {
        FormBody body = new FormBody.Builder()
                .add("username", "浩哥")
                .add("pwd", "abc")
                .build();

        Request request = new Request.Builder()
                .url("https://www.baidu.com")
                .post(body)
                .build();

        Response response = mCatHttpClient.newCall(request).execute();

        if (response.code() == 200) {
            Logger.e("msg" + response.body().string());
        }

    }


    /**
     * 提交文件
     * <p>
     * 1.支持单个文件
     * <p>
     * 2.支持多个文件
     * <p>
     * 3.支持form+单个/多个　文件共同提交
     *
     * @param view
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void multipartClick(View view) {

        String path = Environment.getExternalStorageDirectory().getAbsolutePath();


        File file = new File(path + "/tempCropped.jpeg");
        File file1 = new File(path + "/head_image.png");
        File file2 = new File(path + "/shumei.txt");
        File file3 = new File(path + "/IMG_0358.MP4");

        MultipartBody body = new MultipartBody.Builder()
                .addForm("token", "123")
//                .addPart("image/jpeg", "temp.jpg", file)
                .addPart("image/png", "head_image.png", file1)
//                .addPart("text/plain", "1.txt", file2)
//                .addPart("video/mpeg4", "2.mp4", file3)
                .build();

        Request request = new Request.Builder()
                .url("http://carbid.zertone2.com/app/user/updateuserinfo")
                .post(body)
                .build();

        Response response = mCatHttpClient.newCall(request).execute();

        if (response != null && response.code() == 200) {
            Logger.e("msg== " + response.body().string());
        }
    }

    public void downloadFile(View view) {
        final ImageView iv = findViewById(R.id.iv_content);

        Request request = new Request.Builder()
                .url("https://t7.baidu.com/it/u=4162611394,4275913936&fm=193&f=GIF")
                .build();

        mCatHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Response response) {
                byte[] bytes = response.body().bytes();
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                iv.setImageBitmap(bitmap);
            }

            @Override
            public void onFail(Request request, IOException e) {
                Logger.e("downloadFile onFail errMsg = " + e.getMessage());
            }
        });

    }
}
