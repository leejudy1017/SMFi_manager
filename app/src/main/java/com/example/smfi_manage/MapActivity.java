package com.example.smfi_manage;

import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.DropBoxManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Retrofit;

public class MapActivity  extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private Marker currentMarker = null;

    private Marker tp1Marker;
    private Marker tp2Marker;
    private Marker site1Marker;
    private Marker site2Marker;
    private Circle circle1 = null;
    private Circle circle2 = null;

    private UiSettings mUiSettings;

    private View mLayout;  // Snackbar 사용하기 위해서는 View가 필요합니다.
    private static final String TAG = "googlemap_example";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int UPDATE_INTERVAL_MS = 3000;  // 1초
    private static final int FASTEST_UPDATE_INTERVAL_MS = 100; // 0.5초

    // onRequestPermissionsResult에서 수신된 결과에서 ActivityCompat.requestPermissions를 사용한 퍼미션 요청을 구별하기 위해 사용됩니다.
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    boolean needRequest = false;

    // 앱을 실행하기 위해 필요한 퍼미션을 정의합니다.
    String[] REQUIRED_PERMISSIONS = {android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};  // 외부 저장소

    Location mCurrentLocatiion;
    LatLng currentPosition;
    int tracking ;
    Double error_range ;
    Double spot1_atenna_angle ;
    Double spot2_atenna_angle ;
    boolean alarm1;
    boolean alarm2;


    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private Location  location;


    SearchView searchView;
    String searchText;
    TextView spot1_detail;
    TextView spot2_detail;
    Button spot1_title;
    Button spot2_title;
    Button spot1_btn;
    Button spot2_btn;
    TextView distance_tp2;
    TextView distance_tp1;

    GoogleMap mMap;
    private Geocoder geocoder;

    private Context mContext;
    String tp1;
    String tp2;
    String site1;
    String site2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mContext = this;

        tp1Marker = null;
        tp2Marker = null;
        site1Marker = null;
        site2Marker = null;

        tp1 = PreferenceManager.getString(mContext,"tp1");
        tp2 = PreferenceManager.getString(mContext,"tp2");
        site1 = PreferenceManager.getString(mContext,"site1");
        site2 = PreferenceManager.getString(mContext,"site2");


        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();

        builder.addLocationRequest(locationRequest);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLayout = findViewById(R.id.layout_main);

        tracking=0;
        error_range =0.0;
        spot1_atenna_angle =0.0;
        spot2_atenna_angle =0.0;

        alarm1=true;
        alarm2=true;

        //구글 맵 호출
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this); //getMapAsync must be called on the main thread.

        spot1_detail = findViewById(R.id.spot1_detail);
        spot2_detail = findViewById(R.id.spot2_detail);
        spot1_title = findViewById(R.id.spot1);
        spot2_title = findViewById(R.id.spot2);
        spot1_btn = findViewById(R.id.spot1_detail);
        spot2_btn = findViewById(R.id.spot2_detail);

        distance_tp1= findViewById(R.id.distance_tp1);
        distance_tp2 = findViewById(R.id.distance_tp2);

        //검색기능
        searchView = findViewById(R.id.searchView);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchView.getWindowToken(),0);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchText = query;
                Log.i("search key: ", searchText);
                List<Address> addressList = null;


                try {
                    // editText에 입력한 텍스트(주소, 지역, 장소 등)을 지오 코딩을 이용해 변환
                    addressList = geocoder.getFromLocationName(searchText, 10);

                    if(addressList.size()!=0){
                        // 콤마를 기준으로 split
                        String[] splitStr = addressList.get(0).toString().split(",");

                        String latitude = "";
                        String longitude = "";

                        for (int i = 0; i < splitStr.length; i++) {
                            if (splitStr[i].contains("latitude")) {
                                latitude = splitStr[i].split("=")[1];
                                latitude = String.format("%.4f",Double.parseDouble(latitude));
                                Log.i("latitude: ", latitude);
                            }
                            if (splitStr[i].contains("longitude")) {
                                longitude = splitStr[i].split("=")[1];
                                longitude = String.format("%.4f",Double.parseDouble(longitude));
                                Log.i("longitude: ", longitude);
                            }
                        }

                        // 좌표(위도, 경도) 생성
                        LatLng point = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
                        String markerSnippet = "위도: " + latitude + " 경도: " + longitude;

                        // 해당 좌표로 화면 줌
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 17));

                        // TP 설정
                        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
                        LayoutInflater inflater = getLayoutInflater();
                        View view = inflater.inflate(R.layout.dialog_place_select_tp, null);
                        builder.setView(view);
                        final Button button_tp1 = (Button) view.findViewById(R.id.button_dialog_TP1);
                        final Button button_tp2 = (Button) view.findViewById(R.id.button_dialog_TP2);

                        final AlertDialog dialog = builder.create();
                        final String[] string_placeTitle = {"알수없음"};
                        String finalLatitude = latitude;
                        String finalLongitude = longitude;
                        try {
                            List<Address> resultList = geocoder.getFromLocation(Double.parseDouble(finalLatitude),Double.parseDouble(finalLongitude),1);
                            if(resultList!=null && resultList.size()>0){
                                string_placeTitle[0] = resultList.get(0).getAddressLine(0);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(MapActivity.this, "주소를 확인할 수 없습니다.", Toast.LENGTH_SHORT).show();
                        }

                        button_tp1.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                if(site1Marker!=null){
                                    site1Marker=null;
                                    //site1Marker.remove();
                                }
                                if(tp1Marker!=null){
                                    tp1Marker=null;
                                }

                                BitmapDrawable bitmap = (BitmapDrawable) getResources().getDrawable(R.drawable.marker4);
                                Bitmap b = bitmap.getBitmap();
                                Bitmap smallMarker = Bitmap.createScaledBitmap(b,90,150,false);

                                tp1Marker= mMap.addMarker(new MarkerOptions()
                                .title(string_placeTitle[0])
                                .position(new LatLng(Double.parseDouble(finalLatitude), Double.parseDouble(finalLongitude)))
                                .snippet(markerSnippet)
                                .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                                .alpha(0.7f));

                                PreferenceManager.setString(mContext,"site1","");
                                PreferenceManager.setString(mContext,"tp1",tp1Marker.getTitle()+"SMFI"+tp1Marker.getPosition().latitude+"SMFI"+tp1Marker.getPosition().longitude);

                                distance_tp1.setText("TP 1 까지의 거리: " + (int)distance(tp1Marker.getPosition().latitude, tp1Marker.getPosition().longitude, location.getLatitude(), location.getLongitude(), "meter") + " m");

                                    dialog.dismiss();

                            }
                        });

                        button_tp2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                if(site2Marker!=null){
                                    site2Marker=null;
                                    //site2Marker.remove();
                                }
                                if(tp2Marker!=null){
                                    tp2Marker=null;
                                }

                                BitmapDrawable bitmap = (BitmapDrawable) getResources().getDrawable(R.drawable.marker3);
                                Bitmap b = bitmap.getBitmap();
                                Bitmap smallMarker = Bitmap.createScaledBitmap(b,90,150,false);


                                tp2Marker= mMap.addMarker(new MarkerOptions()
                                        .title(string_placeTitle[0])
                                        .position(new LatLng(Double.parseDouble(finalLatitude), Double.parseDouble(finalLongitude)))
                                        .snippet(markerSnippet)
                                        .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                                        .alpha(0.7f));

                                PreferenceManager.setString(mContext,"site2","");
                                PreferenceManager.setString(mContext,"tp2",tp2Marker.getTitle()+"SMFI"+tp2Marker.getPosition().latitude+"SMFI"+tp2Marker.getPosition().longitude);

                                distance_tp2.setText("TP 2 까지의 거리: " + (int)distance(tp2Marker.getPosition().latitude, tp2Marker.getPosition().longitude, location.getLatitude(), location.getLongitude(), "meter") + " m");

                                dialog.dismiss();
                            }
                        });

                        dialog.show();
                    }
                    else{
                        Toast.makeText(MapActivity.this, "일치하는 정보가 없습니다.", Toast.LENGTH_SHORT).show();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(MapActivity.this, "일치하는 정보가 없습니다.", Toast.LENGTH_SHORT).show();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

    }

    public void onClick(View v) {

        switch (v.getId()) {


            case R.id.plus_btn:

                AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                View view = inflater.inflate(R.layout.dialog_place_add, null);
                builder.setView(view);
                final Button button_submit = (Button) view.findViewById(R.id.button_dialog_placeInfo);
                final EditText editText_latitude = (EditText) view.findViewById(R.id.editText_dialog_latitude);
                final EditText editText_longitude = (EditText) view.findViewById(R.id.editText_dialog_longitude);

                final AlertDialog dialog = builder.create();
                button_submit.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {

                         if (editText_latitude.getText().toString().equals("") || Double.parseDouble(editText_latitude.getText().toString())<-90 ||  Double.parseDouble(editText_latitude.getText().toString())>90 ) {
                            Toast.makeText(MapActivity.this, "위도를 정확하게 입력해주세요. (-90 ~ +90)", Toast.LENGTH_SHORT).show();
                        } else if (editText_longitude.getText().toString().equals("")|| Double.parseDouble(editText_longitude.getText().toString())<-180 ||  Double.parseDouble(editText_longitude.getText().toString())>180 ) {
                            Toast.makeText(MapActivity.this, "경도를 입력해주세요. (-180 ~ +180)", Toast.LENGTH_SHORT).show();
                        } else {
                             marker_plus(String.format("%.4f",Double.parseDouble(editText_latitude.getText().toString())),String.format("%.4f",Double.parseDouble(editText_longitude.getText().toString())));
                             dialog.dismiss();
                        }
                    }

                });
                dialog.show();
                break;

            case R.id.logout:

                spot1_title.setText("Site 1 설정");
                spot1_detail.setText("");
                spot2_title.setText("Site 2 설정");
                spot2_detail.setText("");
                mMap.clear();
                tracking=0;
                alarm1=true;
                alarm2=true;
                Intent intent2 = new Intent(getApplication(), LoginActivity.class);
                startActivity(intent2);
                finish();
                break;

            case R.id.clear:

                PreferenceManager.setString(mContext,"tp1","");
                PreferenceManager.setString(mContext,"tp2","");
                PreferenceManager.setString(mContext,"site1","");
                PreferenceManager.setString(mContext,"site2","");

                if(tp1Marker!=null){
                    tp1Marker.remove();
                    tp1Marker=null;
                }
                if(tp2Marker!=null){
                    tp2Marker.remove();
                    tp2Marker=null;
                }
                if(site1Marker!=null){
                    site1Marker.remove();
                    site1Marker=null;
                }
                if(site2Marker!=null){
                    site2Marker.remove();
                    site2Marker=null;
                }
                if(circle1!=null){
                    circle1.remove();
                }
                if(circle2!=null){
                    circle2.remove();
                }

                alarm1=true;
                alarm2=true;
                distance_tp1.setText("TP 1 을 설정해 주세요.");
                distance_tp2.setText("TP 2 을 설정해 주세요.");
                spot1_title.setText("Site 1 설정");
                spot1_detail.setText("");
                spot2_title.setText("Site 2 설정");
                spot2_detail.setText("");
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),location.getLongitude()), 17));

                break;

            case R.id.error_range:

                AlertDialog.Builder builder2 = new AlertDialog.Builder(MapActivity.this);
                LayoutInflater inflater2 = getLayoutInflater();
                View view2 = inflater2.inflate(R.layout.dialog_error_range, null);
                builder2.setView(view2);
                final EditText errorRange = view2.findViewById(R.id.distance_error_range);
                final Button save_Btn = (Button) view2.findViewById(R.id.save_Btn);
                final AlertDialog dialog2 = builder2.create();
                errorRange.setText(error_range+"");

                save_Btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        error_range =Double.parseDouble(errorRange.getText().toString());
                        Toast.makeText(MapActivity.this, "거리 오차범위가 "+error_range+" 로 저장되었습니다. ", Toast.LENGTH_SHORT).show();
                        dialog2.dismiss();
                    }
                });
                dialog2.show();
                break;

            case R.id.antenna_angle:

                AlertDialog.Builder builder3 = new AlertDialog.Builder(MapActivity.this);
                LayoutInflater inflater3 = getLayoutInflater();
                View view3 = inflater3.inflate(R.layout.dialog_antenna_angle, null);
                builder3.setView(view3);

                final EditText angle1 = view3.findViewById(R.id.angle1);
                final EditText angle2 = view3.findViewById(R.id.angle2);
                final Button saveBtn = (Button) view3.findViewById(R.id.save_Btn);
                final Button deleteBtn = (Button) view3.findViewById(R.id.button_delete_Btn);
                final AlertDialog dialog3 = builder3.create();

                angle1.setText(spot1_atenna_angle+"");
                angle2.setText(spot2_atenna_angle+"");

                final Button x331 =  (Button) view3.findViewById(R.id.x331);
                final Button p291 =  (Button) view3.findViewById(R.id.p291);
                final Button p301 =  (Button) view3.findViewById(R.id.p301);
                final Button x332 =  (Button) view3.findViewById(R.id.x332);
                final Button p292 =  (Button) view3.findViewById(R.id.p292);
                final Button p302 =  (Button) view3.findViewById(R.id.p302);

                x331.setOnClickListener(v1 -> angle1.setText("17"));
                p291.setOnClickListener(v1 -> angle1.setText("6.5"));
                p301.setOnClickListener(v1 -> angle1.setText("5"));
                x332.setOnClickListener(v1 -> angle2.setText("17"));
                p292.setOnClickListener(v1 -> angle2.setText("6.5"));
                p302.setOnClickListener(v1 -> angle2.setText("5"));

                saveBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        spot1_atenna_angle =Double.parseDouble(angle1.getText().toString());
                        spot2_atenna_angle = Double.parseDouble(angle2.getText().toString());

                        Toast.makeText(MapActivity.this, "안테나 각도가 저장되었습니다.", Toast.LENGTH_SHORT).show();
                        dialog3.dismiss();
                    }
                });

                deleteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog3.dismiss();
                    }
                });
                dialog3.show();
                break;

            case R.id.spot1:
                String markerSnippet = "위도:" + String.format("%.4f",location.getLatitude()) + " 경도:" + String.format("%.4f",location.getLongitude())+ " 고도:" + String.format("%.4f",location.getAltitude());
                if(tp1Marker==null){
                    Toast.makeText(MapActivity.this, "TP 1 를 먼저 설정해주세요.", Toast.LENGTH_SHORT).show();
                }
                else if(distance(tp1Marker.getPosition().latitude,tp1Marker.getPosition().longitude,location.getLatitude(), location.getLongitude(),"meter")<=error_range){
                    //반경범위 내에 들어오는지
                    // Site 설정
                    AlertDialog.Builder builder5 = new AlertDialog.Builder(MapActivity.this);
                    LayoutInflater inflater5 = getLayoutInflater();
                    View view5 = inflater5.inflate(R.layout.dialog_place_near, null);
                    builder5.setView(view5);

                    final TextView textView = (TextView) view5.findViewById(R.id.textView);
                    final Button button_tp1 = (Button) view5.findViewById(R.id.button_dialog_Btn);
                    final Button button_tp2 = (Button) view5.findViewById(R.id.button_dialog_deleteBtn);
                    textView.setText("Site 1 설정을 하시겠습니까?");
                    final AlertDialog dialog5 = builder5.create();

                    final String[] string_placeTitle = {"알수없음"};
                    button_tp1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            try {
                                List<Address> resultList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                string_placeTitle[0] = resultList.get(0).getAddressLine(0);
                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(MapActivity.this, "주소를 확인할 수 없습니다.", Toast.LENGTH_SHORT).show();
                            }

                            if(site1Marker!=null) {site1Marker.remove();}

                            BitmapDrawable bitmap = (BitmapDrawable) getResources().getDrawable(R.drawable.marker4);
                            Bitmap b = bitmap.getBitmap();
                            Bitmap smallMarker = Bitmap.createScaledBitmap(b,90,150,false);

                            site1Marker = mMap.addMarker(new MarkerOptions()
                                    .title(string_placeTitle[0])
                                    .position(new LatLng(location.getLatitude(), location.getLongitude()))
                                    .snippet(markerSnippet)
                                    .icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));

                            PreferenceManager.setString(mContext,"site1",site1Marker.getTitle()+"SMFI"+site1Marker.getPosition().latitude+"SMFI"+site1Marker.getPosition().longitude);
                            PreferenceManager.setString(mContext,"tp1","");

                            distance_tp1.setText("Site 1 설정되었습니다.");
                            spot1_title.setText(string_placeTitle[0]);
                            spot1_detail.setText("경도: "+String.format("%.4f",location.getLatitude())+"\n위도: "+String.format("%.4f",location.getLongitude())+"\n고도: "+String.format("%.4f",location.getAltitude())+"\n거리: "+"알수없음"+"\n빔아크: "+"알수없음");
                            circle1.remove();
                            tp1Marker.remove();
                            dialog5.dismiss();
                        }
                    });

                    button_tp2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog5.dismiss();
                        }
                    });
                    dialog5.show();
                }
                else{
                    Toast.makeText(MapActivity.this, "반경범위 안에서만 \n Site 설정이 가능합니다.", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.spot2:
                String markerSnippet2 = "위도:" + String.format("%.4f",location.getLatitude()) + " 경도:" + String.format("%.4f",location.getLongitude())+ " 고도:" + String.format("%.4f",location.getAltitude());
                if(tp2Marker==null){
                    Toast.makeText(MapActivity.this, "TP 2 를 먼저 설정해주세요.", Toast.LENGTH_SHORT).show();
                }
                else if(distance(tp2Marker.getPosition().latitude,tp2Marker.getPosition().longitude,location.getLatitude(), location.getLongitude(),"meter")<=error_range){

                    // Site 설정
                    AlertDialog.Builder builder5 = new AlertDialog.Builder(MapActivity.this);
                    LayoutInflater inflater5 = getLayoutInflater();
                    View view5 = inflater5.inflate(R.layout.dialog_place_near, null);
                    builder5.setView(view5);

                    final TextView textView = (TextView) view5.findViewById(R.id.textView);
                    final Button button_tp1 = (Button) view5.findViewById(R.id.button_dialog_Btn);
                    final Button button_tp2 = (Button) view5.findViewById(R.id.button_dialog_deleteBtn);

                    final AlertDialog dialog5 = builder5.create();
                    textView.setText("Site 2 설정을 하시겠습니까?");

                    final String[] string_placeTitle = {"알수없음"};
                    button_tp1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            try {
                                List<Address> resultList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                string_placeTitle[0] = resultList.get(0).getAddressLine(0);
                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(MapActivity.this, "주소를 확인할 수 없습니다.", Toast.LENGTH_SHORT).show();
                            }

                            if(site2Marker!=null) {site2Marker.remove();}

                            BitmapDrawable bitmap = (BitmapDrawable) getResources().getDrawable(R.drawable.marker3);
                            Bitmap b = bitmap.getBitmap();
                            Bitmap smallMarker = Bitmap.createScaledBitmap(b,90,150,false);

                            site2Marker = mMap.addMarker(new MarkerOptions()
                                    .title(string_placeTitle[0])
                                    .position(new LatLng(location.getLatitude(), location.getLongitude()))
                                    .snippet(markerSnippet2)
                                    .icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));

                            PreferenceManager.setString(mContext,"site2",site2Marker.getTitle()+"SMFI"+site2Marker.getPosition().latitude+"SMFI"+site2Marker.getPosition().longitude);
                            PreferenceManager.setString(mContext,"tp2","");

                            distance_tp2.setText("Site 2 설정되었습니다.");
                            spot2_title.setText(string_placeTitle[0]);
                            spot2_detail.setText("경도: "+String.format("%.4f",location.getLatitude())+"\n위도: "+String.format("%.4f",location.getLongitude())+"\n고도: "+String.format("%.4f",location.getAltitude())+"\n거리: "+"알수없음"+"\n빔아크: "+"알수없음");
                            circle2.remove();
                            tp2Marker.remove();
                            dialog5.dismiss();
                        }
                    });

                    button_tp2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog5.dismiss();
                        }
                    });
                    dialog5.show();

                }
                else{
                    Toast.makeText(MapActivity.this, "반경범위 안에서만 \n Site 설정이 가능합니다.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public void marker_plus(String lat,String lon){
        // 해당 좌표로 화면 줌
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(lat),Double.parseDouble(lon)), 17));
        String markerSnippet = "위도: " + lat + " 경도: " + lon ;

        // TP 설정
        AlertDialog.Builder builder2 = new AlertDialog.Builder(MapActivity.this);
        LayoutInflater inflater2 = getLayoutInflater();
        View view2 = inflater2.inflate(R.layout.dialog_place_select_tp, null);
        builder2.setView(view2);
        final Button button_tp1 = (Button) view2.findViewById(R.id.button_dialog_TP1);
        final Button button_tp2 = (Button) view2.findViewById(R.id.button_dialog_TP2);

        final AlertDialog dialog = builder2.create();
        final String[] string_placeTitle = {"알수없음"};

        button_tp1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    try {
                        List<Address> resultList = geocoder.getFromLocation(Double.parseDouble(lat),Double.parseDouble(lon),1);
                        if(resultList!=null && resultList.size()>0){
                            string_placeTitle[0] = resultList.get(0).getAddressLine(0);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(MapActivity.this, "주소를 확인할 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }

                    if(site1Marker!=null){
                        site1Marker=null;
                        //site1Marker.remove();
                    }
                    if(tp1Marker!=null){
                        tp1Marker=null;
                    }

                BitmapDrawable bitmap = (BitmapDrawable) getResources().getDrawable(R.drawable.marker4);
                Bitmap b = bitmap.getBitmap();
                Bitmap smallMarker = Bitmap.createScaledBitmap(b,90,150,false);

                    tp1Marker = mMap.addMarker(new MarkerOptions()
                    .title(string_placeTitle[0])
                    .position(new LatLng(Double.parseDouble(lat),Double.parseDouble(lon)))
                    .snippet(markerSnippet)
                    .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                            .alpha(0.7f));

                PreferenceManager.setString(mContext,"tp1",tp1Marker.getTitle()+"SMFI"+tp1Marker.getPosition().latitude+"SMFI"+tp1Marker.getPosition().longitude);
                PreferenceManager.setString(mContext,"site1","");

                    distance_tp1.setText("TP 1 까지의 거리: "+ (int)distance(tp1Marker.getPosition().latitude,tp1Marker.getPosition().longitude,location.getLatitude(),location.getLongitude(),"meter")+" (m)");

                    dialog.dismiss();
                }

        });

        button_tp2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    try {
                        List<Address> resultList = geocoder.getFromLocation(Double.parseDouble(lat),Double.parseDouble(lon),1);
                        if(resultList!=null && resultList.size()>0){
                            string_placeTitle[0] = resultList.get(0).getAddressLine(0);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(MapActivity.this, "주소를 확인할 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                if(site2Marker!=null){
                    site2Marker=null;
                    //site2Marker.remove();
                }
                if(tp2Marker!=null){
                    tp2Marker=null;
                }

                BitmapDrawable bitmap = (BitmapDrawable) getResources().getDrawable(R.drawable.marker3);
                Bitmap b = bitmap.getBitmap();
                Bitmap smallMarker = Bitmap.createScaledBitmap(b,90,150,false);


                tp2Marker = mMap.addMarker(new MarkerOptions()
                        .title(string_placeTitle[0])
                        .position(new LatLng(Double.parseDouble(lat),Double.parseDouble(lon)))
                        .snippet(markerSnippet)
                        .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                        .alpha(0.7f));

                PreferenceManager.setString(mContext,"tp2",tp2Marker.getTitle()+"SMFI"+tp2Marker.getPosition().latitude+"SMFI"+tp2Marker.getPosition().longitude);
                PreferenceManager.setString(mContext,"site2","");

                distance_tp2.setText("TP 2 까지의 거리: "+ (int)distance(tp2Marker.getPosition().latitude,tp2Marker.getPosition().longitude,location.getLatitude(),location.getLongitude(),"meter")+" (m)");

                dialog.dismiss();
                }

        });
        dialog.show();

    }

    private static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {

        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));

        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;

        if (unit == "kilometer") {
            dist = dist * 1.609344;
        } else if (unit == "meter") {
            dist = dist * 1609.344;
        }

        return (dist);
    }

    // This function converts decimal degrees to radians
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    // This function converts radians to decimal degrees
    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        // 구글 맵 객체를 불러온다.
        mMap = googleMap;
        geocoder = new Geocoder(this);

        mUiSettings = mMap.getUiSettings();
        mUiSettings.setCompassEnabled(true);

        //런타임 퍼미션 요청 대화상자나 GPS 활성 요청 대화상자 보이기전에
        //지도의 초기위치를 서울로 이동
        setDefaultLocation();
        mMap.setOnMarkerClickListener(this);

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED && hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) { startLocationUpdates(); }
        else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {
                Snackbar.make(mLayout, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.",
                        Snackbar.LENGTH_INDEFINITE).setAction("확인", view -> ActivityCompat.requestPermissions(MapActivity.this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE)).show(); }
            else {
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE); }
        }

        //맵 터치 이벤트
        mMap.setOnMapLongClickListener(latLng -> {

            // 해당 좌표로 화면 줌
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
            String markerSnippet = "위도: " + String.format("%.4f",latLng.latitude) + " 경도: " + String.format("%.4f",latLng.longitude) ;


                // TP 설정
                AlertDialog.Builder builder2 = new AlertDialog.Builder(MapActivity.this);
                LayoutInflater inflater2 = getLayoutInflater();
                View view2 = inflater2.inflate(R.layout.dialog_place_select_tp, null);
                builder2.setView(view2);
                final Button button_tp1 = (Button) view2.findViewById(R.id.button_dialog_TP1);
                final Button button_tp2 = (Button) view2.findViewById(R.id.button_dialog_TP2);

                final AlertDialog dialog = builder2.create();

                final String[] string_placeTitle = {"알수없음"};
                button_tp1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        try {
                            List<Address> resultList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                            string_placeTitle[0] = resultList.get(0).getAddressLine(0);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(MapActivity.this, "주소를 확인할 수 없습니다.", Toast.LENGTH_SHORT).show();
                        }

                        if(site1Marker!=null){
                            site1Marker=null;
                            //site1Marker.remove();
                        }
                        if(tp1Marker!=null){
                            tp1Marker.remove();
                        }

                        BitmapDrawable bitmap = (BitmapDrawable) getResources().getDrawable(R.drawable.marker4);
                        Bitmap b = bitmap.getBitmap();
                        Bitmap smallMarker = Bitmap.createScaledBitmap(b,90,150,false);

                        tp1Marker = mMap.addMarker(new MarkerOptions()
                                .title(string_placeTitle[0])
                                .position(new LatLng(latLng.latitude,latLng.longitude))
                                .snippet(markerSnippet)
                                .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                                .alpha(0.7f));

                        PreferenceManager.setString(mContext,"tp1",tp1Marker.getTitle()+"SMFI"+tp1Marker.getPosition().latitude+"SMFI"+tp1Marker.getPosition().longitude);
                        PreferenceManager.setString(mContext,"site1","");


                        distance_tp1.setText("TP 1 까지의 거리: " + (int)distance(tp1Marker.getPosition().latitude, tp1Marker.getPosition().longitude, location.getLatitude(), location.getLongitude(), "meter") + " (m)");

                        dialog.dismiss();
                    }
                });

                button_tp2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        try {
                            List<Address> resultList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                            string_placeTitle[0] = resultList.get(0).getAddressLine(0);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(MapActivity.this, "주소를 확인할 수 없습니다.", Toast.LENGTH_SHORT).show();
                        }

                        if(site2Marker!=null){
                            site2Marker=null;
                            //site2Marker.remove();
                        }
                        if(tp2Marker!=null){
                            tp2Marker.remove();
                        }

                        BitmapDrawable bitmap = (BitmapDrawable) getResources().getDrawable(R.drawable.marker3);
                        Bitmap b = bitmap.getBitmap();
                        Bitmap smallMarker = Bitmap.createScaledBitmap(b,90,150,false);

                        tp2Marker = mMap.addMarker(new MarkerOptions()
                                .title(string_placeTitle[0])
                                .position(new LatLng(latLng.latitude,latLng.longitude))
                                .snippet(markerSnippet)
                                .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                                .alpha(0.7f));

                        PreferenceManager.setString(mContext,"tp2",tp2Marker.getTitle()+"SMFI"+tp2Marker.getPosition().latitude+"SMFI"+tp2Marker.getPosition().longitude);
                        PreferenceManager.setString(mContext,"site2","");

                        distance_tp2.setText("TP 2 까지의 거리: " + (int)distance(tp2Marker.getPosition().latitude, tp2Marker.getPosition().longitude, location.getLatitude(), location.getLongitude(), "meter") + " (m)");

                        dialog.dismiss();
                    }

                });
                dialog.show();

        });

        //정보창 클릭 이벤트 (delete)
        googleMap.setOnInfoWindowClickListener(marker -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.dialog_place_delete, null);
            builder.setView(view);
            final Button deleteBtn = (Button) view.findViewById(R.id.button_dialog_deleteBtn);
            final Button confirmBtn = (Button) view.findViewById(R.id.button_dialog_Btn);
            final AlertDialog dialog = builder.create();

            deleteBtn.setOnClickListener(v -> {
                if((site1Marker != null)&&(site1Marker.getPosition().latitude==marker.getPosition().latitude) && (site1Marker.getPosition().longitude==marker.getPosition().longitude)){
                    //site 1 지우기
                    distance_tp1.setText("TP 1 을 설정해 주세요.");
                    spot1_title.setText("Site 1 설정");
                    spot1_detail.setText("");

                    tp1Marker=null;
                    site1Marker.remove();
                    //site1Marker=null;

                    PreferenceManager.setString(mContext,"tp1","");
                    PreferenceManager.setString(mContext,"site1","");

                    if(site2Marker!=null){
                        spot2_detail.setText("경도: "+String.format("%.4f",site2Marker.getPosition().latitude)+"\n위도: "+String.format("%.4f",site2Marker.getPosition().longitude)+"\n고도: "+"알수없음"+"\n거리: "+"알수없음"+"\n빔아크: "+"알수없음");
                    }
                }
                if((site2Marker != null)&&(site2Marker.getPosition().latitude==marker.getPosition().latitude) && (site2Marker.getPosition().longitude==marker.getPosition().longitude)){
                    //site 2 지우기
                    distance_tp2.setText("TP 2 을 설정해 주세요.");
                    spot2_title.setText("Site 2 설정");
                    spot2_detail.setText("");
                    //site2Marker=null;
                    tp2Marker=null;
                    site2Marker.remove();
                    PreferenceManager.setString(mContext,"tp2","");
                    PreferenceManager.setString(mContext,"site2","");
                    if(site1Marker!=null){
                        spot1_detail.setText("경도: "+String.format("%.4f",site1Marker.getPosition().latitude)+"\n위도: "+String.format("%.4f",site1Marker.getPosition().longitude)+"\n고도: "+"알수없음"+"\n거리: "+"알수없음"+"\n빔아크: "+"알수없음");
                    }
                }

                if((tp1Marker != null)&&tp1Marker.getPosition().latitude==marker.getPosition().latitude && tp1Marker.getPosition().longitude==marker.getPosition().longitude){
                    tp1Marker=null;
                    PreferenceManager.setString(mContext,"tp1","");
                    PreferenceManager.setString(mContext,"site1","");
                    distance_tp1.setText("TP 1 을 설정해 주세요.");
                    circle1.remove();
                }
                if((tp2Marker != null)&&tp2Marker.getPosition().latitude==marker.getPosition().latitude && tp2Marker.getPosition().longitude==marker.getPosition().longitude){
                    tp2Marker=null;
                    PreferenceManager.setString(mContext,"tp2","");
                    PreferenceManager.setString(mContext,"site2","");
                    distance_tp2.setText("TP 2 을 설정해 주세요.");
                    circle2.remove();
                }

                marker.remove();
                dialog.dismiss();
            });

            confirmBtn.setOnClickListener(v -> dialog.dismiss());
            dialog.show();
        });

    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            List<Location> locationList = locationResult.getLocations();

            if (locationList.size() > 0) {
                location = locationList.get(locationList.size() - 1);
                currentPosition = new LatLng(location.getLatitude(), location.getLongitude());

                String markingPoint = "위도:" + location.getLatitude() + " 경도:" + location.getLongitude();
                Log.d(TAG, "onMarkingResult : " + markingPoint);


                if(tracking==0){
                    setCurrentLocation(location);
                    mCurrentLocatiion = location;
                    tracking=1;

                    if(!site1.equals("")){
                        tp1Marker=null;
                        String title = site1.split("SMFI")[0];
                        Double lat = Double.parseDouble(site1.split("SMFI")[1]);
                        Double lon = Double.parseDouble(site1.split("SMFI")[2]);
                        Log.i("site1 DATA: ",site1);
                        String markerSnippet = "위도: " + String.format("%.4f",lat) + " 경도: " + String.format("%.4f",lon) ;

                        BitmapDrawable bitmap = (BitmapDrawable) getResources().getDrawable(R.drawable.marker4);
                        Bitmap b = bitmap.getBitmap();
                        Bitmap smallMarker = Bitmap.createScaledBitmap(b,90,150,false);

                        site1Marker = mMap.addMarker(new MarkerOptions()
                                .title(title)
                                .position(new LatLng(lat,lon))
                                .snippet(markerSnippet)
                                .icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));

                        spot1_title.setText(title);
                        spot1_detail.setText("경도: "+String.format("%.4f",site1Marker.getPosition().latitude)+"\n위도: "+String.format("%.4f",site1Marker.getPosition().longitude)+"\n고도: "+"알수없음"+"\n거리: "+"알수없음"+"\n빔아크: "+"알수없음");
                        distance_tp1.setText("Site 1 설정되었습니다.");
                    }
                    else{
                        Log.i("site1 DATA: ","이전에 저장된 데이터가 없습니다.");
                        site1Marker=null;
                        if(!tp1.equals("")){
                            String title = tp1.split("SMFI")[0];
                            Double lat = Double.parseDouble(tp1.split("SMFI")[1]);
                            Double lon = Double.parseDouble(tp1.split("SMFI")[2]);
                            Log.i("tp1 DATA: ",tp1);
                            String markerSnippet = "위도: " + String.format("%.4f",lat) + " 경도: " + String.format("%.4f",lon) ;

                            BitmapDrawable bitmap = (BitmapDrawable) getResources().getDrawable(R.drawable.marker4);
                            Bitmap b = bitmap.getBitmap();
                            Bitmap smallMarker = Bitmap.createScaledBitmap(b,90,150,false);

                            tp1Marker = mMap.addMarker(new MarkerOptions()
                                    .title(title)
                                    .position(new LatLng(lat,lon))
                                    .snippet(markerSnippet)
                                    .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                                    .alpha(0.7f));
                        }
                        else{
                            tp1Marker=null;
                            Log.i("tp1 DATA: ","이전에 저장된 데이터가 없습니다.");
                        }
                    }

                    if(!site2.equals("")){
                        tp2Marker=null;
                        String title = site2.split("SMFI")[0];
                        Double lat = Double.parseDouble(site2.split("SMFI")[1]);
                        Double lon = Double.parseDouble(site2.split("SMFI")[2]);
                        Log.i("site2 DATA: ",site2);
                        String markerSnippet = "위도: " + String.format("%.4f",lat) + " 경도: " + String.format("%.4f",lon) ;

                        BitmapDrawable bitmap = (BitmapDrawable) getResources().getDrawable(R.drawable.marker3);
                        Bitmap b = bitmap.getBitmap();
                        Bitmap smallMarker = Bitmap.createScaledBitmap(b,90,150,false);

                        site2Marker = mMap.addMarker(new MarkerOptions()
                                .title(title)
                                .position(new LatLng(lat,lon))
                                .snippet(markerSnippet)
                                .icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));

                        spot2_title.setText(title);
                        spot2_detail.setText("경도: "+String.format("%.4f",site2Marker.getPosition().latitude)+"\n위도: "+String.format("%.4f",site2Marker.getPosition().longitude)+"\n고도: "+"알수없음"+"\n거리: "+"알수없음"+"\n빔아크: "+"알수없음");
                        distance_tp2.setText("Site 2 설정되었습니다.");
                    }
                    else{
                        site2Marker=null;
                        Log.i("site2 DATA: ","이전에 저장된 데이터가 없습니다.");
                        if(!tp2.equals("")){
                            String title = tp2.split("SMFI")[0];
                            Double lat = Double.parseDouble(tp2.split("SMFI")[1]);
                            Double lon = Double.parseDouble(tp2.split("SMFI")[2]);
                            Log.i("tp2 DATA: ",tp2);
                            String markerSnippet = "위도: " + String.format("%.4f",lat) + " 경도: " + String.format("%.4f",lon) ;

                            BitmapDrawable bitmap = (BitmapDrawable) getResources().getDrawable(R.drawable.marker3);
                            Bitmap b = bitmap.getBitmap();
                            Bitmap smallMarker = Bitmap.createScaledBitmap(b,90,150,false);

                            tp2Marker = mMap.addMarker(new MarkerOptions()
                                    .title(title)
                                    .position(new LatLng(lat,lon))
                                    .snippet(markerSnippet)
                                    .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                                    .alpha(0.7f));
                        }
                        else{
                            tp2Marker=null;
                            Log.i("tp2 DATA: ","이전에 저장된 데이터가 없습니다.");
                        }
                    }
                }



                Log.i("tp1 DATA: ",PreferenceManager.getString(mContext,"tp1"));
                Log.i("tp2 DATA: ",PreferenceManager.getString(mContext,"tp2"));
                Log.i("site1 DATA: ",PreferenceManager.getString(mContext,"site1"));
                Log.i("site2 DATA: ",PreferenceManager.getString(mContext,"site2"));

                // 반경범위 원만들기
                if(tp1Marker!=null && site1Marker==null){
                    if(circle1!=null){circle1.remove();}
                    CircleOptions circleOptions =  new CircleOptions().center(tp1Marker.getPosition())
                            .radius(error_range)
                            .strokeWidth(0f)
                            .fillColor(Color.parseColor("#880000ff"));
                    circle1 = mMap.addCircle(circleOptions);

                    double temp_distance=distance(tp1Marker.getPosition().latitude,tp1Marker.getPosition().longitude,location.getLatitude(),location.getLongitude(),"meter");
                    distance_tp1.setText("TP 1 까지의 거리 : "+(int)temp_distance+" (m)");

                    //alarm
                    if(alarm1==true&&temp_distance<=error_range&&site1Marker==null){
                        alarm1=false;
                        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
                        LayoutInflater inflater = getLayoutInflater();
                        View view = inflater.inflate(R.layout.dialog_alarm, null);
                        builder.setView(view);
                        final TextView text = (TextView) view.findViewById(R.id.dialogText);
                        final Button okBtn = (Button) view.findViewById(R.id.save_Btn);
                        final AlertDialog dialog = builder.create();
                        text.setText("TP 1 의"+ error_range+"m 내에 도착하였습니다.\n Site 1 를 설정해주세요.");
                        okBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                        dialog.show();
                    }
                    else if(temp_distance>error_range){
                        alarm1=true;
                    }
                }
                if(tp2Marker!=null && site2Marker==null){
                    if(circle2!=null){circle2.remove();}
                    CircleOptions circleOptions =  new CircleOptions().center(tp2Marker.getPosition())
                            .radius(error_range)
                            .strokeWidth(0f)
                            .fillColor(Color.parseColor("#880000ff"));
                    circle2 = mMap.addCircle(circleOptions);

                    double temp_distance=distance(tp2Marker.getPosition().latitude,tp2Marker.getPosition().longitude,location.getLatitude(),location.getLongitude(),"meter");
                    distance_tp2.setText("TP 2 까지의 거리 : "+(int)temp_distance+" (m)");

                    //alarm
                    if(alarm2==true&&temp_distance<=error_range&&site2Marker==null){
                        alarm2=false;
                        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
                        LayoutInflater inflater = getLayoutInflater();
                        View view = inflater.inflate(R.layout.dialog_alarm, null);
                        builder.setView(view);
                        final TextView text = (TextView) view.findViewById(R.id.dialogText);
                        final Button okBtn = (Button) view.findViewById(R.id.save_Btn);
                        final AlertDialog dialog = builder.create();
                        text.setText("TP 2 의"+ error_range+"m 내에 도착하였습니다.\n Site 2 를 설정해주세요.");
                        okBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                        dialog.show();
                    }
                    else if(temp_distance>error_range){
                        alarm2=true;
                    }
                }

                //거리,빔아크
                if(site2Marker !=null && site1Marker !=null){
                    double distance = distance(site1Marker.getPosition().latitude,site1Marker.getPosition().longitude,site2Marker.getPosition().latitude,site2Marker.getPosition().longitude,"meter");
                    distance = Double.parseDouble(String.format("%.4f",distance));
                    spot1_detail.setText("경도: "+String.format("%.4f",site1Marker.getPosition().latitude)+"\n위도: "+String.format("%.4f",site1Marker.getPosition().longitude)+"\n고도: "+"알수없음"+"\n거리: "+distance+"\n빔아크: "+String.format("%.4f",Math.PI/180*distance*spot1_atenna_angle));
                    spot2_detail.setText("경도: "+String.format("%.4f",site2Marker.getPosition().latitude)+"\n위도: "+String.format("%.4f",site2Marker.getPosition().longitude)+"\n고도: "+"알수없음"+"\n거리: "+distance+"\n빔아크: "+String.format("%.4f",Math.PI/180*distance*spot2_atenna_angle));
                }

            }

            }



    };


    private void startLocationUpdates() {

        if (!checkLocationServicesStatus()) {
            Log.d(TAG, "startLocationUpdates : call showDialogForLocationServiceSetting");
            showDialogForLocationServiceSetting();
        } else {

            int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION);

            if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED || hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "startLocationUpdates : 퍼미션 안가지고 있음");
                return;
            }

            Log.d(TAG, "startLocationUpdates : call mFusedLocationClient.requestLocationUpdates");

            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

            //내 위치 (파란색 표시)
            if (checkPermission())
                mMap.setMyLocationEnabled(true);
        }
    }


    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


    public void setCurrentLocation(Location location) {
        if (currentMarker != null) currentMarker.remove();
        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        //현재 위치 마킹 안함
        //MarkerOptions markerOptions = new MarkerOptions();
        //markerOptions.position(currentLatLng);
        //markerOptions.title(markerTitle);
        //markerOptions.snippet(markerSnippet);
        //markerOptions.draggable(true);
        //currentMarker = mMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
        mMap.moveCamera(cameraUpdate);
    }


    public void setDefaultLocation() {

        //디폴트 위치, Seoul
        LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
        String markerTitle = "위치정보 가져올 수 없음";
        String markerSnippet = "위치 퍼미션과 GPS 활성 요부 확인하세요";


        if (currentMarker != null) currentMarker.remove();

        //MarkerOptions markerOptions = new MarkerOptions();
        //markerOptions.position(DEFAULT_LOCATION);
        //markerOptions.title(markerTitle);
        //markerOptions.snippet(markerSnippet);
        //markerOptions.draggable(true);
        //markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        //currentMarker = mMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15);
        mMap.moveCamera(cameraUpdate);
    }

    //여기부터는 런타임 퍼미션 처리을 위한 메소드들
    private boolean checkPermission() {

        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        return false;

    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        super.onRequestPermissionsResult(permsRequestCode, permissions, grandResults);
        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면
            boolean check_result = true;

            // 모든 퍼미션을 허용했는지 체크합니다.
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }


            if (check_result) {
                // 퍼미션을 허용했다면 위치 업데이트를 시작합니다.
                startLocationUpdates();
            } else {

                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    // 사용자가 거부만 선택한 경우에는 앱을 다시 실행하여 허용을 선택하면 앱을 사용할 수 있습니다.
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    }).show();

                } else {

                    // "다시 묻지 않음"을 사용자가 체크하고 거부를 선택한 경우에는 설정(앱 정보)에서 퍼미션을 허용해야 앱을 사용할 수 있습니다.
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {

                            finish();
                        }
                    }).show();
                }
            }

        }
    }

    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", (dialog, id) -> {
            Intent callGPSSettingIntent
                    = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
        });
        builder.setNegativeButton("취소", (dialog, id) -> dialog.cancel());
        builder.create().show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        Log.d(TAG, "onActivityResult : GPS 활성화 되있음");
                        needRequest = true;
                        return;
                    }
                }
                break;
        }
    }


    //-- 현재위치가 아닌 특정위치로 초기설정할때 --//

    // 서울 여의도에 대한 위치 설정 (초기설정)
    //    LatLng seoul = new LatLng(37.52487, 126.92723);
    //  String markerSnippet = "위도: " + String.valueOf(seoul.latitude) + " 경도: " + String.valueOf(seoul.longitude);

    // 구글 맵에 표시할 마커에 대한 옵션 설정
    //MarkerOptions makerOptions = new MarkerOptions();
    //makerOptions
    //       .position(seoul)
    //       .title("현재 위치")
    //      .snippet(markerSnippet);

    // 마커 생성 안함 -> 현재위치 파랑으로 표시 예정
    // 마커를 생성한다.
    //mMap.addMarker(makerOptions);
    //markerMap.put("현재 위치", new double[]{seoul.latitude, seoul.longitude});

    //마커 클릭에 대한 이벤트 처리
    //mMap.setOnMarkerClickListener(this);

    //카메라를 여의도 위치로 옮긴다.
    //mMap.moveCamera(CameraUpdateFactory.newLatLng(seoul));
    //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul,16));


    @Override
    public boolean onMarkerClick(Marker marker) {


        return false;
    }

    public void selectSpot(String info, String title){

        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_place_selectspot, null);
        builder.setView(view);
        final Button spot1Btn = (Button) view.findViewById(R.id.button_dialog_spot1Btn);
        final Button spot2Btn = (Button) view.findViewById(R.id.button_dialog_spot2Btn);
        final AlertDialog dialog = builder.create();

        spot1Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    antennaHeight(info,title,0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        });

        spot2Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    antennaHeight(info,title,1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void antennaHeight(String info,String title,Integer btn) throws IOException {

        String latitude = info.split(" ")[1];
        String longitude = info.split(" ")[3];

        String url = "https://maps.googleapis.com/maps/api/elevation/json?locations="+latitude.trim()+","+longitude.trim()+"&key=AIzaSyAFpuzouXvM5n2Xrv3440Xo-sVqIu4sS64";

        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .method("GET", null)
                    .url(url)
                    .build();

            //비동기 처리 (enqueue 사용)
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    System.out.println("error + Connect Server Error is " + e.toString());
                }

                @Override
                public void onResponse(okhttp3.Call call, Response response) throws IOException {
                    System.out.println("Response Body is " + response.body().string());
                }
            });

        } catch (Exception e){
            System.err.println(e.toString());
        }


        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_place_antenna, null);
        builder.setView(view);
        final Button antenna = (Button) view.findViewById(R.id.button_dialog_antennaBtn);
        final Button cancel = (Button) view.findViewById(R.id.button_delete_Btn);
        final EditText antennaHeight = (EditText) view.findViewById(R.id.editText_dialog_antennaHeight);
        final EditText antennaAngle = (EditText) view.findViewById(R.id.editText_dialog_antennaAngle);
        final Button x33 = (Button) view.findViewById(R.id.x33);
        final Button p29 = (Button) view.findViewById(R.id.p29);
        final Button p30 = (Button) view.findViewById(R.id.p30);
        final AlertDialog dialog = builder.create();

        x33.setOnClickListener(v -> antennaAngle.setText("17"));
        p29.setOnClickListener(v -> antennaAngle.setText("6.5"));
        p30.setOnClickListener(v -> antennaAngle.setText("5"));

        cancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        antenna.setOnClickListener(v -> {

            if(antennaHeight.getText().toString().equals("")){
                Toast.makeText(MapActivity.this,"안테나 높이를 입력해주세요.",Toast.LENGTH_SHORT).show();
            }
            else if(antennaAngle.getText().toString().equals("")){
                Toast.makeText(MapActivity.this,"안테나 각도를 입력해주세요.",Toast.LENGTH_SHORT).show();
            }
            else{
                if(btn==0){
                    spot1_title.setText(title);

                }
                else{
                    spot2_title.setText(title);
                }
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}

