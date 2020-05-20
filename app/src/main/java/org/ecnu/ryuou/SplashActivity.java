package org.ecnu.ryuou;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Message;

import android.os.Handler;
import java.util.logging.LogRecord;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Message;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;


public class SplashActivity extends AppCompatActivity {

    private  Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
        if(isFinishing()){
            return;
        }
         Intent intent= new Intent(SplashActivity.this,MainActivity.class);
        startActivity(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏，一定要在setContentView之前
        if (getSupportActionBar()!=null){
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_splash);
        handler.sendMessageDelayed(Message.obtain(),3000);
        }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
};



    // 判断是否是第一次启动程序 利用 SharedPreferences 将数据保存在本地
