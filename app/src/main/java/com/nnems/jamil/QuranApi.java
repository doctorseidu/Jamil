package com.nnems.jamil;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface QuranApi {

    String BASE_URL = "https://api.alquran.cloud/v1/";

    @GET("ayah/{ayat}/{translation}")
    Call<ApiData> getAyatEnglishText(
            @Path("ayat") String ayat,
            @Path("translation")  String translation);

    @GET("ayah/{ayat}")
    Call<ApiData> getAyatArabicText(@Path("ayat") String ayat);

    @GET("ayah/{ayat}/{reciter}")
    Call<ApiData> getAudioData(
            @Path("ayat") String ayat ,
            @Path("reciter") String reciter);

}
