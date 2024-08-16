package com.heon9u.alarm_weather_app;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.heon9u.alarm_weather_app.R;
import com.heon9u.alarm_weather_app.alarm.MainActivity;
import com.heon9u.alarm_weather_app.measure.GpsTracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class measurement_screen extends AppCompatActivity {
    private GpsTracker gpsTracker;
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSION_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    private static String TAG = "phptest";

    private static final String TAG_JSON = "sensor";
    private static final String TAG_NAME1 = "tmp";
    private static final String TAG_NAME2 = "hmd";
    private static final String TAG_NAME3 = "pm";
    private static final String TAG_NAME4 = "cd";
    private static final String TAG_NAME5 = "so";
    private static final String TAG_NAME6 = "vo";
    private static final String TAG_NAME7 = "co";

    ArrayList<HashMap<String, String>> mArrayList;
    ListView mlistView;
    String mJsonString;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.measurement_screen);

        Button developer_info_btn = (Button) findViewById(R.id.button_alarm);
        developer_info_btn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view){
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        if (!checkLocationServicesStatus()){
            showDialogForLocationServiceSetting();
        } else{
            checkRunTimePermission();
        }
        final TextView textview_address = (TextView)findViewById(R.id.text_address);

        Button ShowLocationButton = (Button) findViewById(R.id.btn_address);
        ShowLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0){
                gpsTracker = new GpsTracker(measurement_screen.this);
                double latitude = gpsTracker.getLatitude();
                double longitude = gpsTracker.getLongitude();

                String address = getCurrentAddress(latitude, longitude);
                textview_address.setText(address);

                Toast.makeText(measurement_screen.this, "현재위치 \n위도 "+latitude+"\n경도 "+longitude, Toast.LENGTH_LONG).show();
            }
        });
    }

    //ActivityCompat.requestPermission를 사용한 퍼미션 요청의 결과를 리턴받는 메소드
    @Override
    public void onRequestPermissionsResult(int permsRequestCode, @NonNull String[] permissions, @NonNull int[] grandResults) {
        super.onRequestPermissionsResult(permsRequestCode, permissions, grandResults);
        if (permsRequestCode == PERMISSION_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            //요청코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수마늠 수신되었다면
            boolean check_result = true;

            //모든 퍼미션을 허용했는지 체크한다.
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if (check_result) {
                //위치 값을 가져올수있다.
            } else {
                //거부한 퍼미션이 있다면 앱을 사용할수 없는 이유를 설명해주고 앱을 종료한다. (2가지 경우)
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0]) || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {
                    Toast.makeText(measurement_screen.this, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(measurement_screen.this, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    void checkRunTimePermission(){
        //런타임 퍼미션 처리
        //1. 위치 퍼미션을 가지고 있는지 체크한다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(measurement_screen.this, Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(measurement_screen.this, Manifest.permission.ACCESS_COARSE_LOCATION);

        if(hasFineLocationPermission == PackageManager.PERMISSION_GRANTED && hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED){
            //2.이미 퍼미션을 가지고 있다면(안드로이드 6.0이하 버전은 런타임퍼미션을 이미허용된 걸로 인식) 위치값을 가져올수있다.
        } else{
            //2.퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요하다. (2가지 경우)
            //2-1.사용자가 퍼미션 거부를 한적이 있는 경우에는
            if(ActivityCompat.shouldShowRequestPermissionRationale(measurement_screen.this, REQUIRED_PERMISSIONS[0])){
                //2-2.요청을 진행하기 전에 사용자에게 퍼미션이 필요한 이유를 설명해줄 필요가 있다.
                Toast.makeText(measurement_screen.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();

                //2-2-1.사용자에게 퍼미션 요청을 한다. 요청결과는 onRequestPermissionResult에서 수신된다.
                ActivityCompat.requestPermissions(measurement_screen.this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE);
            } else {
                //3-1.사용자가 퍼미션 거부를 한적이 없는 경우에는 퍼미션 요청을 바로 한다. 요청결과는 onRequestPermissionResult에서 수신된다.
                ActivityCompat.requestPermissions(measurement_screen.this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE);
            }
        }
    }
    public String getCurrentAddress(double latitude, double longitude){
        //지오코더 (GPS를 주소로 변환)
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;

        try{
            addresses = geocoder.getFromLocation(latitude, longitude, 7);
        } catch (IOException ioException){
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException){
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";
        }

        if (addresses == null || addresses.size() == 0){
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";
        }

        Address address = addresses.get(0);
        return address.getAddressLine(0).toString()+"\n";
    }

    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting(){
        AlertDialog.Builder builder = new AlertDialog.Builder(measurement_screen.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n" + "위치 설정을 수정하실래요?");

        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id){
                Intent callGPSSettingIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id){
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode){
            case GPS_ENABLE_REQUEST_CODE:;

                //사용자가 GPS활성 시켰는지 검사
                if(checkLocationServicesStatus()){
                    if(checkLocationServicesStatus()){
                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
                        checkRunTimePermission();
                        return;
                    }
                }
                break;
        }
    }

    public boolean checkLocationServicesStatus(){
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


    //php
    private class GetData extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(measurement_screen.this,
                    "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            Log.d(TAG, "response  - " + result);

            if (result == null){

            }
            else {

                mJsonString = result;
                showResult();
            }
        }


        @Override
        protected String doInBackground(String... params) {

            String serverURL = params[0];


            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();


                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.connect();


                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "response code - " + responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }


                bufferedReader.close();


                return sb.toString().trim();


            } catch (Exception e) {

                Log.d(TAG, "InsertData: Error ", e);
                errorString = e.toString();

                return null;
            }

        }
    }


    private void showResult(){
        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);


            //for(int i=0;i<jsonArray.length();i++)
            int i = jsonArray.length()-1;
            JSONObject item = jsonArray.getJSONObject(i);

            String name1 = item.getString(TAG_NAME1);
            String name2 = item.getString(TAG_NAME2);
            String name3 = item.getString(TAG_NAME3);
            String name4 = item.getString(TAG_NAME4);
            String name5 = item.getString(TAG_NAME5);
            String name6 = item.getString(TAG_NAME6);
            String name7 = item.getString(TAG_NAME7);

            HashMap<String,String> hashMap = new HashMap<>();

            hashMap.put(TAG_NAME1, name1);
            hashMap.put(TAG_NAME2, name2);
            hashMap.put(TAG_NAME3, name3);
            hashMap.put(TAG_NAME4, name4);
            hashMap.put(TAG_NAME5, name5);
            hashMap.put(TAG_NAME6, name6);
            hashMap.put(TAG_NAME7, name7);
            mArrayList.add(hashMap);

            ListAdapter adapter = new SimpleAdapter(
                    measurement_screen.this, mArrayList, R.layout.value_list,
                    new String[]{TAG_NAME3,TAG_NAME6,TAG_NAME7,TAG_NAME4,TAG_NAME5,TAG_NAME1,TAG_NAME2},
                    new int[]{R.id.ms_pm_value,R.id.ms_tvoc_value,R.id.ms_co2_value, R.id.ms_co_value, R.id.ms_so2_value, R.id.ms_tmp_value, R.id.ms_hmd_value}
            );

            mlistView.setAdapter(adapter);

        } catch (JSONException e) {

            Log.d(TAG, "showResult : ", e);
        }

    }

}

