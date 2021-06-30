package com.example.smfi_manage;

import com.google.gson.annotations.SerializedName;
import java.util.HashMap;
import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface RetrofitAPI {
    public static final String API_URL ="";

    @FormUrlEncoded
    @POST("sdf")
    Call<Post> exampleAPI(@FieldMap HashMap<String,Object> param);

}