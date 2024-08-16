package com.heon9u.alarm_weather_app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.heon9u.alarm_weather_app.R;

public class Start extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start);

        Handler timer = new Handler();          //핸들러 변수 생성

        timer.postDelayed(new Runnable(){       //4초후 쓰레드를 생성하는 postDelayed 메소드
            public void run(){
                //intent 생성
                Intent intent = new Intent(Start.this, measurement_screen.class);
                startActivity(intent);          //다음 화면으로 이동
                finish();                       // 이 화면 종료
            }
        }, 4000);                      //4000은 4초를 의미
    }
}
