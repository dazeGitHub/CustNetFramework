package com.example.custnetframework.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.custnetframework.R
import com.example.custnetframework.data.Config
import okhttp3.*
import java.io.IOException
import java.util.concurrent.BlockingQueue
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.LinkedBlockingQueue


class TestOkHttpActivity : AppCompatActivity() {

    private var TAG = "TAG"

    val okHttpClient = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_ok_http)

        Log.e(TAG, "初始时 当前线程 CurrentThread name = " + Thread.currentThread().name)

        findViewById<View>(R.id.btn_get_execute).setOnClickListener {
            //同步请求必须新启动一个线程来执行
            Thread {
                doGetExecute()
            }.start()
        }

        findViewById<View>(R.id.btn_get_enqueue).setOnClickListener {
            doGetEnqueue()
        }

        findViewById<View>(R.id.btn_post_execute).setOnClickListener {
            //同步请求必须新启动一个线程来执行
            Thread {
                doPostFormExecute()
            }.start()
        }

        findViewById<View>(R.id.btn_post_enque).setOnClickListener {
            doPostFormEnqueue()
        }
    }

    //Get 同步请求
    private fun doGetExecute() {
        val request: Request = Request.Builder() //利用建造者模式创建Request对象
            .url(Config.URL) //设置请求的URL
            .build() //生成Request对象

        val okHttpClient = OkHttpClient()
        var response: Response? = null

        try {
            //将请求添加到请求队列等待执行，并返回执行后的Response对象
            response = okHttpClient.newCall(request).execute()
            //获取Http Status Code.其中200表示成功
            if (response.code == 200) {
                //这里需要注意，response.body().string()是获取返回的结果，此句话只能调用一次，再次调用获得不到结果。
                //所以先将结果使用result变量接收
                val result = response.body?.string()
                Log.d(
                    TAG, "doGetExecute onResponse " + "\n"
                            + " CurrentThread name = " + Thread.currentThread().name
                            + " result = \n"
                            + result
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (response != null) {
                response.body?.close()
            }
        }
    }

    //Get 异步请求
    private fun doGetEnqueue() {
        val request: Request = Request.Builder() //利用建造者模式创建Request对象
            .url(Config.URL) //设置请求的URL
            .build() //生成Request对象

        //enqueue就是将此次的call请求加入异步请求队列，会开启新的线程执行，并将执行的结果通过Callback接口回调的形式返回。
        okHttpClient.newCall(request).enqueue(object : Callback {

            override fun onResponse(call: Call, response: Response) {
                //请求成功的回调方法
                val result = response.body?.string()

                Log.d(
                    TAG, "doGetEnqueue onResponse " + "\n"
                            + " CurrentThread name = " + Thread.currentThread().name
                            + " result = \n"
                            + result
                )
                //关闭body
                response.body?.close()
            }

            override fun onFailure(call: Call, e: java.io.IOException) {
                //请求失败的回调方法
                Log.d(
                    TAG, "doGetEnqueue onFailure " + "\n"
                            + " CurrentThread name = " + Thread.currentThread().name
                            + " exception = \n"
                            + e.message
                )
            }
        })
    }

    //Post 同步请求, 使用表单
    private fun doPostFormExecute() {
        val formBody: RequestBody = FormBody.Builder()
            .add("username", "test")
            .add("password", "test")
            .build()

        val request: Request = Request.Builder()
            .url(Config.URL + "/users")
            .post(formBody)
            .build()

        val call = okHttpClient.newCall(request)
        val response = call.execute()
        val code = response.code
        val result = response.body?.string()
        Log.d(
            TAG, "doPostFormExecute Result " + "\n"
                    + " CurrentThread name = " + Thread.currentThread().name
                    + " code = $code"
                    + " result = \n"
                    + result
        )
    }

    private fun doPostFormEnqueue() {
        val formBody: RequestBody = FormBody.Builder()
            .add("username", "test")
            .add("password", "test")
            .build()

        val request: Request = Request.Builder()
            .url(Config.URL + "/users")
            .post(formBody)
            .build()

        okHttpClient.newCall(request).enqueue(object : Callback {

            override fun onResponse(call: Call, response: Response) {
                //请求成功的回调方法
                val result = response.body!!.string()
                Log.d(
                    TAG, "doPostFormEnqueue onResponse" + "\n"
                            + " CurrentThread name = " + Thread.currentThread().name
                            + " result = \n"
                            + result
                )
                //关闭 body
                response.body?.close()
            }

            override fun onFailure(call: Call, e: IOException) {
                //请求失败的回调方法
                Log.d(
                    TAG, "doPostFormEnqueue onFailure " + "\n"
                            + " CurrentThread name = " + Thread.currentThread().name
                            + " exception = \n"
                            + e.message
                )
            }
        })
    }
}