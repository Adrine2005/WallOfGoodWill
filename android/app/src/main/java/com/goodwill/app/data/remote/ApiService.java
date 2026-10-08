package com.goodwill.app.data.remote;

import com.goodwill.app.data.remote.dto.AuthResponse;
import com.goodwill.app.data.remote.dto.CreateListingRequest;
import com.goodwill.app.data.remote.dto.ListingDto;
import com.goodwill.app.data.remote.dto.RegisterRequest;
import com.goodwill.app.data.remote.dto.UpdateStatusRequest;
import com.goodwill.app.data.remote.dto.UserDto;
import com.goodwill.app.data.remote.dto.VerifyQrRequest;

import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @POST("v1/auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @POST("v1/auth/verify-qr")
    Call<AuthResponse> verifyQr(@Body VerifyQrRequest request);

    @GET("v1/auth/me")
    Call<UserDto> getProfile();

    @GET("health")
    Call<Map<String, Object>> getHealth();

    // Listings endpoints
    @GET("v1/listings")
    Call<List<ListingDto>> getListings(
            @Query("category") String category,
            @Query("type") String type,
            @Query("hostelBlock") String hostelBlock,
            @Query("search") String search
    );

    @POST("v1/listings")
    Call<ListingDto> createListing(@Body CreateListingRequest request);

    @GET("v1/listings/{id}")
    Call<ListingDto> getListingDetail(@Path("id") String id);

    @GET("v1/listings/my")
    Call<List<ListingDto>> getMyListings();

    @PUT("v1/listings/{id}/status")
    Call<Map<String, String>> updateListingStatus(
            @Path("id") String id,
            @Body UpdateStatusRequest request
    );

    @DELETE("v1/listings/{id}")
    Call<Map<String, String>> deleteListing(@Path("id") String id);
}
