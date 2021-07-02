package com.example.smfi_manage;

import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Retrofit;

public class MapActivity  extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    SearchView searchView;
    String searchText;
    TextView spot1_detail;
    TextView spot2_detail;
    Button spot1_title;
    Button spot2_title;
    Button spot1_btn;
    Button spot2_btn;
    GoogleMap mMap;
    private Geocoder geocoder;

    Handler handler = new Handler();
    String[] lats ;
    String[] lngs;
    int select;

    String spot1_latitude="";
    String spot1_longitude="";
    String spot1_antennaHeight="";
    String spot1_antennaAngle="";

    String spot2_latitude="";
    String spot2_longitude="";
    String spot2_antennaHeight="";
    String spot2_antennaAngle="";

    public MapActivity() throws IOException {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        //구글 맵 호출
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this); //getMapAsync must be called on the main thread.

        spot1_detail = findViewById(R.id.spot1_detail);
        spot2_detail = findViewById(R.id.spot2_detail);
        spot1_title = findViewById(R.id.spot1);
        spot2_title = findViewById(R.id.spot2);
        spot1_btn = findViewById(R.id.spot1_detail);
        spot2_btn = findViewById(R.id.spot2_detail);

        spot1_title.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(spot1_latitude==""){
                    Toast.makeText(MapActivity.this,"마커를 먼저 설정해주세요.",Toast.LENGTH_SHORT).show();
                }
                else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
                    LayoutInflater inflater = getLayoutInflater();
                    View view = inflater.inflate(R.layout.dialog_marker_delete, null);
                    builder.setView(view);
                    final Button deleteBtn = (Button) view.findViewById(R.id.button_dialog_deleteBtn);
                    final Button confirmBtn = (Button) view.findViewById(R.id.button_dialog_Btn);
                    final AlertDialog dialog = builder.create();

                    deleteBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            spot1_title.setText("SPOT 1");
                            spot1_latitude="";
                            spot1_longitude="";
                            spot1_detail.setText("");

                            dialog.dismiss();
                        }
                    });

                    confirmBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    dialog.show();

                }
                return false;
            }
        });

        spot2_title.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(spot2_latitude==""){
                    Toast.makeText(MapActivity.this,"마커를 먼저 설정해주세요.",Toast.LENGTH_SHORT).show();
                }
                else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
                    LayoutInflater inflater = getLayoutInflater();
                    View view = inflater.inflate(R.layout.dialog_marker_delete, null);
                    builder.setView(view);
                    final Button deleteBtn = (Button) view.findViewById(R.id.button_dialog_deleteBtn);
                    final Button confirmBtn = (Button) view.findViewById(R.id.button_dialog_Btn);
                    final AlertDialog dialog = builder.create();

                    deleteBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            spot2_title.setText("SPOT 2");
                            spot2_latitude="";
                            spot2_longitude="";
                            spot2_detail.setText("");

                            dialog.dismiss();
                        }
                    });

                    confirmBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    dialog.show();

                }
                return false;
            }
        });

        //검색기능
        searchView=findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchText = query;
                Log.i("search key: ",searchText);
                List<Address> addressList = null;

                try {
                    // editText에 입력한 텍스트(주소, 지역, 장소 등)을 지오 코딩을 이용해 변환
                    addressList = geocoder.getFromLocationName(
                            searchText, // 주소
                            10); // 최대 검색 결과 개수
                }
                catch (IOException e) {
                    e.printStackTrace();
                }

                if(addressList == null){
                    Toast.makeText(MapActivity.this,"일치하는 정보가 없습니다.",Toast.LENGTH_SHORT).show();
                }
                else{
                    System.out.println(addressList.get(0).toString());
                    // 콤마를 기준으로 split
                    String []splitStr = addressList.get(0).toString().split(",");

                    String latitude="";
                    String longitude="";

                    for (int i=0;i<splitStr.length;i++) {
                        if (splitStr[i].contains("latitude")) {
                            latitude = splitStr[i].split("=")[1];
                            Log.i("latitude: ", latitude);
                        }
                        if (splitStr[i].contains("longitude")) {
                            longitude = splitStr[i].split("=")[1];
                            Log.i("longitude: ", longitude);
                        }
                    }

                    // 좌표(위도, 경도) 생성
                    LatLng point = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
                    String markerSnippet = "위도: " + latitude + " 경도: " + longitude;
                    // 마커 생성
                    MarkerOptions mOptions2 = new MarkerOptions();
                    mOptions2.title(searchText);
                    mOptions2.snippet(markerSnippet);
                    mOptions2.position(point);
                    // 마커 추가
                    mMap.addMarker(mOptions2);
                    // 해당 좌표로 화면 줌
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point,15));
                }

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    public void onClick(View v) throws IOException {

        switch (v.getId()){
            case R.id.spot1_detail:

                if(spot1_latitude==""){
                    Toast.makeText(MapActivity.this,"마커를 먼저 설정해주세요.",Toast.LENGTH_SHORT).show();
                    //spot1_btn.setEnabled(false);
                }
                else{
                    antennaHeight("위도: " + spot1_latitude + " 경도: " + spot1_longitude,spot1_title.getText().toString(),0);
                }
                break;

            case R.id.spot2_detail:
                if(spot2_latitude==""){
                    Toast.makeText(MapActivity.this,"마커를 먼저 설정해주세요.",Toast.LENGTH_SHORT).show();
                    //spot2_btn.setEnabled(false);
                }
                else{
                    antennaHeight("위도: " + spot2_latitude + " 경도: " + spot2_longitude,spot2_title.getText().toString(),1);
                }
                break;

            case R.id.spot1:
                if(spot1_latitude==""){
                    Toast.makeText(MapActivity.this,"마커를 먼저 설정해주세요.",Toast.LENGTH_SHORT).show();
                    //spot1_btn.setEnabled(false);
                }
                else{
                    LatLng latLng = new LatLng(Double.parseDouble(spot1_latitude), Double.parseDouble(spot1_longitude));
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                }
                break;

            case R.id.spot2:
                if(spot2_latitude==""){
                    Toast.makeText(MapActivity.this,"마커를 먼저 설정해주세요.",Toast.LENGTH_SHORT).show();
                    //spot1_btn.setEnabled(false);
                }
                else{
                    LatLng latLng = new LatLng(Double.parseDouble(spot2_latitude), Double.parseDouble(spot2_longitude));
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                }
                break;

            case R.id.plus_btn:

                AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                View view = inflater.inflate(R.layout.dialog_place_add, null);
                builder.setView(view);
                final Button button_submit = (Button) view.findViewById(R.id.button_dialog_placeInfo);
                final EditText editText_placeTitle = (EditText) view.findViewById(R.id.editText_dialog_placeTitle);
                final EditText editText_latitude = (EditText) view.findViewById(R.id.editText_dialog_latitude);
                final EditText editText_longitude = (EditText) view.findViewById(R.id.editText_dialog_longitude);

                final AlertDialog dialog = builder.create();
                button_submit.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {

                        if(editText_placeTitle.getText().toString().equals("")){
                            Toast.makeText(MapActivity.this,"장소 이름을 입력해주세요.",Toast.LENGTH_SHORT).show();
                        }
                        else if(editText_latitude.getText().toString().equals("")){
                            Toast.makeText(MapActivity.this,"위도를 입력해주세요.",Toast.LENGTH_SHORT).show();
                        }
                        else if(editText_longitude.getText().toString().equals("")){
                            Toast.makeText(MapActivity.this,"경도를 입력해주세요.",Toast.LENGTH_SHORT).show();
                        }
                        else {
                            String string_placeTitle = editText_placeTitle.getText().toString();
                            String markerSnippet = "위도: " + editText_latitude.getText().toString() + " 경도: " + editText_longitude.getText().toString();

                            //맵을 클릭시 현재 위치에 마커 추가
                            LatLng latLng = new LatLng(Double.parseDouble(editText_latitude.getText().toString()), Double.parseDouble(editText_longitude.getText().toString()));
                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(latLng);
                            markerOptions.title(string_placeTitle);
                            markerOptions.snippet(markerSnippet);
                            mMap.addMarker(markerOptions);
                            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                            dialog.dismiss();
                        }
                    }
                });
                dialog.show();
                break;

            case R.id.calculate:

                if(spot1_latitude==""||spot2_latitude==""){
                    Toast.makeText(MapActivity.this,"SPOT1과 SPOT2를 모두 설정해주세요.",Toast.LENGTH_SHORT).show();
                }
                else{
                AlertDialog.Builder builder2 = new AlertDialog.Builder(MapActivity.this);
                LayoutInflater inflater2 = getLayoutInflater();
                View view2 = inflater2.inflate(R.layout.dialog_calculate, null);
                builder2.setView(view2);
                final Button submit_Btn = (Button) view2.findViewById(R.id.submit_Btn);
                final TextView spot1_to_spot2 = (TextView) view2.findViewById(R.id.spot1_to_spot2);

                double distance = distance(Double.parseDouble(spot1_latitude),Double.parseDouble(spot1_longitude),Double.parseDouble(spot2_latitude),Double.parseDouble(spot2_longitude),"meter");
                Log.i("distance", String.valueOf(distance));
                double temp1 = 2*distance*Math.tan((Double.parseDouble(spot1_antennaAngle)/2*180/Math.PI));
                spot1_to_spot2.setText(temp1+" m");

                final AlertDialog dialog2 = builder2.create();
                submit_Btn.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        dialog2.dismiss();
                    }
                });
                dialog2.show();
                }
                break;

            case R.id.calculate2:
                AlertDialog.Builder builder2 = new AlertDialog.Builder(MapActivity.this);
                LayoutInflater inflater2 = getLayoutInflater();
                View view2 = inflater2.inflate(R.layout.dialog_calculate2, null);
                builder2.setView(view2);
                final Button calculate_Btn = (Button) view2.findViewById(R.id.button_dialog_calculateBtn);
                final Button cancel_Btn = (Button) view2.findViewById(R.id.button_delete_Btn);
                final EditText edit_distance =(EditText) view2.findViewById(R.id.editText_dialog_distance);
                final EditText edit_angle =(EditText) view2.findViewById(R.id.editText_dialog_antennaAngle);
                final TextView distance = (TextView) view2.findViewById(R.id.distance);
                final Button x33 = (Button) view2.findViewById(R.id.x33);
                final Button p29 = (Button) view2.findViewById(R.id.p29);
                final Button p30 = (Button) view2.findViewById(R.id.p30);

                final AlertDialog dialog2 = builder2.create();

                x33.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        edit_angle.setText("17");
                    }
                });
                p29.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        edit_angle.setText("6.5");
                    }
                });
                p30.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        edit_angle.setText("5");
                    }
                });

                calculate_Btn.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if(edit_distance.getText().toString().equals("")){
                            Toast.makeText(MapActivity.this,"거리 값을 입력해주세요.",Toast.LENGTH_SHORT).show();
                        }
                        else if(edit_angle.getText().toString().equals("")){
                            Toast.makeText(MapActivity.this,"안테나 각도를 입력해주세요.",Toast.LENGTH_SHORT).show();
                        }
                        else{
                            double temp2 = Math.tan((Double.parseDouble(edit_angle.getText().toString())/2*Math.PI/180));
                            double temp1 = 2*Double.parseDouble(edit_distance.getText().toString())*temp2;
                            Toast.makeText(MapActivity.this,"tan: "+temp2,Toast.LENGTH_SHORT).show();
                            distance.setText(temp1+" m");
                        }
                    }
                });
                cancel_Btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog2.dismiss();
                    }
                });
                dialog2.show();
                break;

        }
    }


    private static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {

        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));

        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;

        if (unit == "kilometer") {
            dist = dist * 1.609344;
        } else if(unit == "meter"){
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

        // 서울 여의도에 대한 위치 설정 (초기설정)
        LatLng seoul = new LatLng(37.52487, 126.92723);
        String markerSnippet = "위도: " + String.valueOf(seoul.latitude) + " 경도: " + String.valueOf(seoul.longitude);

        // 구글 맵에 표시할 마커에 대한 옵션 설정
        MarkerOptions makerOptions = new MarkerOptions();
        makerOptions
                .position(seoul)
                .title("현재 위치")
                .snippet(markerSnippet);

        // 마커를 생성한다.
        mMap.addMarker(makerOptions);

        //마커 클릭에 대한 이벤트 처리
        mMap.setOnMarkerClickListener(this);

        //카메라를 여의도 위치로 옮긴다.
        mMap.moveCamera(CameraUpdateFactory.newLatLng(seoul));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul,16));

        //맵 터치 이벤트
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            @Override
            public void onMapLongClick(LatLng latLng) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                View view = inflater.inflate(R.layout.dialog_place_info, null);
                builder.setView(view);
                final Button button_submit = (Button) view.findViewById(R.id.button_dialog_placeInfo);
                final EditText editText_placeTitle = (EditText) view.findViewById(R.id.editText_dialog_placeTitle);

                final AlertDialog dialog = builder.create();
                button_submit.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        String string_placeTitle = editText_placeTitle.getText().toString();
                        String markerSnippet = "위도: " + String.valueOf(latLng.latitude) + " 경도: " + String.valueOf(latLng.longitude);

                        //맵을 클릭시 현재 위치에 마커 추가
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(latLng);
                        markerOptions.title(string_placeTitle);
                        markerOptions.snippet(markerSnippet);
                        googleMap.addMarker(markerOptions);
                        dialog.dismiss();
                    }
                });
                dialog.show();

            }
        });

        //정보창 클릭 이벤트
        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                View view = inflater.inflate(R.layout.dialog_place_delete, null);
                builder.setView(view);
                final Button deleteBtn = (Button) view.findViewById(R.id.button_dialog_deleteBtn);
                final Button confirmBtn = (Button) view.findViewById(R.id.button_dialog_Btn);
                final AlertDialog dialog = builder.create();

                deleteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        marker.remove();

                        if(marker.getTitle().equals(spot1_title.getText().toString())){
                            spot1_title.setText("SPOT 1");
                            spot1_latitude="";
                            spot1_longitude="";
                            spot1_detail.setText("");
                        }
                        if(marker.getTitle().equals(spot2_title.getText().toString())){
                            spot2_title.setText("SPOT 2");
                            spot2_latitude="";
                            spot2_longitude="";
                            spot2_detail.setText("");
                        }

                        dialog.dismiss();
                    }
                });

                confirmBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });

    }


    @Override
    public boolean onMarkerClick(Marker marker) {

        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_place_spot, null);
        builder.setView(view);
        final Button cancelBtn = (Button) view.findViewById(R.id.button_dialog_cancelBtn);
        final Button spotBtn = (Button) view.findViewById(R.id.button_dialog_spotBtn);
        final AlertDialog dialog = builder.create();

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        spotBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(marker.getTitle().equals(spot1_title.getText().toString()) || marker.getTitle().equals(spot2_title.getText().toString())){
                    Toast.makeText(MapActivity.this,"이미 SPOT 으로 설정된 마커입니다.",Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }
                else{
                    String info = marker.getSnippet();
                    String title = marker.getTitle();
                    selectSpot(info,title);
                    dialog.dismiss();
                }
            }
        });

        dialog.show();
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
        final EditText antennaHeight = (EditText) view.findViewById(R.id.editText_dialog_antennaHeight);
        final EditText antennaAngle = (EditText) view.findViewById(R.id.editText_dialog_antennaAngle);
        final Button x33 = (Button) view.findViewById(R.id.x33);
        final Button p29 = (Button) view.findViewById(R.id.p29);
        final Button p30 = (Button) view.findViewById(R.id.p30);
        final AlertDialog dialog = builder.create();

        x33.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                antennaAngle.setText("17");
            }
        });
        p29.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                antennaAngle.setText("6.5");
            }
        });
        p30.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                antennaAngle.setText("5");
            }
        });

        antenna.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(antennaHeight.getText().toString().equals("")){
                    Toast.makeText(MapActivity.this,"안테나 높이를 입력해주세요.",Toast.LENGTH_SHORT).show();
                }
                else if(antennaAngle.getText().toString().equals("")){
                    Toast.makeText(MapActivity.this,"안테나 각도를 입력해주세요.",Toast.LENGTH_SHORT).show();
                }
                else{
                    if(btn==0){
                        spot1_title.setText(title);
                        spot1_latitude = latitude;
                        spot1_longitude =longitude;
                        spot1_antennaHeight = antennaHeight.getText().toString();
                        spot1_antennaAngle = antennaAngle.getText().toString();
                        spot1_detail.setText("위도: "+spot1_latitude+"\n경도: "+spot1_longitude+"\n고도: "+"\n안테나 높이: " + spot1_antennaHeight+" m \n안테나 각도: " + spot1_antennaAngle+"°");
                    }
                    else{
                        spot2_title.setText(title);
                        spot2_latitude = latitude;
                        spot2_longitude =longitude;
                        spot2_antennaHeight = antennaHeight.getText().toString();
                        spot2_antennaAngle = antennaAngle.getText().toString();
                        spot2_detail.setText("위도: "+spot2_latitude+"\n경도: "+spot2_longitude+"\n고도: "+"\n안테나 높이: " + spot2_antennaHeight+" m \n안테나 각도: " + spot2_antennaAngle+"°");
                    }
                    dialog.dismiss();
                }
            }
        });


        dialog.show();
    }
}
