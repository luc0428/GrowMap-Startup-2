package com.example.growmapapp.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface CardApiService {
    @GET("cards")
    Call<List<CardDto>> getCards();

    @POST("cards")
    Call<CardDto> createCard(@Body CardDto card);

    @PUT("cards/{id}")
    Call<CardDto> updateCard(@Path("id") Long id, @Body CardDto card);
}
