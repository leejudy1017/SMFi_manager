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
    TextView spot1_title;
    TextView spot2_title;
    Button spot1_btn;
    Button spot2_btn;
    GoogleMap mMap;

    Handler handler = new Handler();
    float lat,lng;
    String[] lats ;
    String[] lngs;
    int select;

    String spot1_latitude="";
    String spot1_longitude="";
    String spot1_antennaHeight="";

    String spot2_latitude="";
    String spot2_longitude="";
    String spot2_antennaHeight="";

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

        searchView=findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchText = query;
                Log.i("search key: ",searchText);

                String urlStr= "https://maps.googleapis.com/maps/api/geocode/json?address="+searchText+"&key=AIzaSyBGAkuLBAguuSvJo6SOS5kLcIJFJCLthro&language=en";
                ConnectThread thread = new ConnectThread(urlStr);
                thread.start();

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
                    Toast.makeText(MapActivity.this,"마커를 눌러 SPOT 1 을 설정해주세요.",Toast.LENGTH_SHORT).show();
                    //spot1_btn.setEnabled(false);
                }
                else{
                    antennaHeight("위도: " + spot1_latitude + " 경도: " + spot1_longitude,spot1_title.getText().toString(),0);
                }
                break;

            case R.id.spot2_detail:
                if(spot2_latitude==""){
                    Toast.makeText(MapActivity.this,"마커를 눌러 SPOT 2 을 설정해주세요.",Toast.LENGTH_SHORT).show();
                    //spot2_btn.setEnabled(false);
                }
                else{
                    antennaHeight("위도: " + spot2_latitude + " 경도: " + spot2_longitude,spot2_title.getText().toString(),1);
                }
                break;

        }
    }

    class ConnectThread extends Thread{
        String urlStr;
        public ConnectThread(String inStr){
            urlStr=inStr;
        }
        public void run(){
            try{
                final String output = request(urlStr);
                handler.post(new Runnable(){
                    @Override
                    public void run() {
                        findLatLng(output);
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private String request(String urlStr){
        StringBuilder output = new StringBuilder();
        try{
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            if(conn!=null){
                conn.setConnectTimeout(10000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestProperty("Accept-Charset","UTF-8");

                int resCode = conn.getResponseCode();

                Log.d("resCode",String.valueOf(resCode));
                if(resCode==HttpURLConnection.HTTP_OK){
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));

                    String line = null;
                    while(true){
                        line = reader.readLine();
                        if(line==null){
                            break;
                        }
                        output.append(line+"\n");
                    }
                    reader.close();
                    conn.disconnect();
                }
            }

        } catch (MalformedURLException e) {
            Log.e("SampleHTTP","Exception in processing response",e);
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output.toString();
    }

    private void findLatLng(String output){
        Log.d("output",output);
        try{
            JSONObject jsonObject = new JSONObject(output);
            String status = jsonObject.getString("status");
            String condition= status.trim();

            if(condition.equals("OK")){
                JSONArray jsonResultsArray = new JSONArray(jsonObject.getString("results"));
                int jsonResultsLength= jsonResultsArray.length();
                if(jsonResultsLength>5){
                    Toast.makeText(MapActivity.this,"검색된 값이 너무 많습니다.",Toast.LENGTH_LONG).show();
                }else if(jsonResultsLength>1){
                    String addresses[] = new String[jsonResultsLength];
                    lats = new String[jsonResultsLength];
                    lngs=new String[jsonResultsLength];

                    for (int i =0;i<jsonResultsLength;i++){
                        String address = jsonResultsArray.getJSONObject(i).getString("formatted_address");
                        JSONObject geoObject = new JSONObject(jsonResultsArray.getJSONObject(i).getString("geometry"));
                        JSONObject locObject = new JSONObject(geoObject.getString("location"));
                        String lat = locObject.getString("lat");
                        String lng = locObject.getString("lng");

                        addresses[i] = address;
                        lats[i] = lat;
                        lngs[i] = lng;
                    }

                    AlertDialog.Builder ab= new AlertDialog.Builder(MapActivity.this);
                    ab.setTitle("아래에서 해당 주소를 선택하세요.");
                    ab.setSingleChoiceItems(addresses, select, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            select = which;
                        }
                    }).setPositiveButton("선택", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(MapActivity.this,"위도: "+ lats[select]+"경도: "+lngs[select],Toast.LENGTH_SHORT).show();
                        }
                    }).setNegativeButton("취소",null);
                    ab.show();
                } else if(jsonResultsLength ==1){
                    JSONObject geoObject = new JSONObject(jsonResultsArray.getJSONObject(0).getString("geometry"));
                    JSONObject locObject = new JSONObject(geoObject.getString("location"));
                    String lat = locObject.getString("lat");
                    String lng =locObject.getString("lng");
                }
            } else{
                Toast.makeText(MapActivity.this,"해당 조회 결과 값이 없습니다.",Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        // 구글 맵 객체를 불러온다.
        mMap = googleMap;

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
                String info = marker.getSnippet();
                String title = marker.getTitle();
                selectSpot(info,title);
                dialog.dismiss();
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
        final AlertDialog dialog = builder.create();

        antenna.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(btn==0){
                    spot1_title.setText(title);
                    spot1_latitude = latitude;
                    spot1_longitude =longitude;
                    spot1_antennaHeight = antennaHeight.getText().toString();
                    spot1_detail.setText("위도: "+latitude+"\n경도: "+longitude+"\n고도: "+"\n안테나 높이: " + antennaHeight.getText().toString());
                }
                else{
                    spot2_title.setText(title);
                    spot2_latitude = latitude;
                    spot2_longitude =longitude;
                    spot2_antennaHeight = antennaHeight.getText().toString();
                    spot2_detail.setText("위도: "+latitude+"\n경도: "+longitude+"\n고도: "+"\n안테나 높이: " + antennaHeight.getText().toString());
                }
                dialog.dismiss();
            }
        });


        dialog.show();
    }
}
