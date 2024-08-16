package com.heon9u.alarm_weather_app.measure;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.heon9u.alarm_weather_app.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class MainActivity_measure extends AppCompatActivity {
   private GpsTracker gpsTracker;
   private static final int GPS_ENABLE_REQUEST_CODE = 2001;
   private static final int PERMISSION_REQUEST_CODE = 100;
   String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

   @Override
    protected void onCreate(Bundle savedInstanceState){
       super.onCreate(savedInstanceState);
       setContentView(R.layout.measurement_screen);

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
               gpsTracker = new GpsTracker(MainActivity_measure.this);
               double latitude = gpsTracker.getLatitude();
               double longitude = gpsTracker.getLongitude();

               String address = getCurrentAddress(latitude, longitude);
               textview_address.setText(address);

               Toast.makeText(MainActivity_measure.this, "현재위치 \n위도 "+latitude+"\n경도 "+longitude, Toast.LENGTH_LONG).show();
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
                    Toast.makeText(MainActivity_measure.this, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(MainActivity_measure.this, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    void checkRunTimePermission(){
       //런타임 퍼미션 처리
       //1. 위치 퍼미션을 가지고 있는지 체크한다.
       int hasFineLocationPermission = ContextCompat.checkSelfPermission(MainActivity_measure.this, Manifest.permission.ACCESS_FINE_LOCATION);
       int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(MainActivity_measure.this, Manifest.permission.ACCESS_COARSE_LOCATION);

       if(hasFineLocationPermission == PackageManager.PERMISSION_GRANTED && hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED){
           //2.이미 퍼미션을 가지고 있다면(안드로이드 6.0이하 버전은 런타임퍼미션을 이미허용된 걸로 인식) 위치값을 가져올수있다.
       } else{
           //2.퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요하다. (2가지 경우)
           //2-1.사용자가 퍼미션 거부를 한적이 있는 경우에는
           if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity_measure.this, REQUIRED_PERMISSIONS[0])){
               //2-2.요청을 진행하기 전에 사용자에게 퍼미션이 필요한 이유를 설명해줄 필요가 있다.
               Toast.makeText(MainActivity_measure.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();

               //2-2-1.사용자에게 퍼미션 요청을 한다. 요청결과는 onRequestPermissionResult에서 수신된다.
               ActivityCompat.requestPermissions(MainActivity_measure.this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE);
           } else {
               //3-1.사용자가 퍼미션 거부를 한적이 없는 경우에는 퍼미션 요청을 바로 한다. 요청결과는 onRequestPermissionResult에서 수신된다.
               ActivityCompat.requestPermissions(MainActivity_measure.this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE);
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
       AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity_measure.this);
       builder.setTitle("위치 서비스 비활성화");
       builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n" + "위치 설정을 수정하실래요?");
       builder.setCancelable(true);
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
}