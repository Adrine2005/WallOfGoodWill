package com.goodwill.app.data.remote;

import com.goodwill.app.data.remote.dto.AuthResponse;
import com.goodwill.app.data.remote.dto.ChatConversationDto;
import com.goodwill.app.data.remote.dto.ChatMessageDto;
import com.goodwill.app.data.remote.dto.CreateConversationRequest;
import com.goodwill.app.data.remote.dto.CreateDonationRequest;
import com.goodwill.app.data.remote.dto.CreateListingRequest;
import com.goodwill.app.data.remote.dto.CreateWishlistRequest;
import com.goodwill.app.data.remote.dto.DonationRequestDto;
import com.goodwill.app.data.remote.dto.EditListingRequest;
import com.goodwill.app.data.remote.dto.ListingDto;
import com.goodwill.app.data.remote.dto.LoginRequest;
import com.goodwill.app.data.remote.dto.NotificationDto;
import com.goodwill.app.data.remote.dto.RegisterRequest;
import com.goodwill.app.data.remote.dto.SendMessageRequest;
import com.goodwill.app.data.remote.dto.UnreadCountDto;
import com.goodwill.app.data.remote.dto.UpdateStatusRequest;
import com.goodwill.app.data.remote.dto.UserDto;
import com.goodwill.app.data.remote.dto.VerifyQrRequest;
import com.goodwill.app.data.remote.dto.LeaderboardEntryDto;
import com.goodwill.app.data.remote.dto.ReportDto;
import com.goodwill.app.data.remote.dto.ReportListingRequest;
import com.goodwill.app.data.remote.dto.UploadResponseDto;
import com.goodwill.app.data.remote.dto.UserPointsDto;
import com.goodwill.app.data.remote.dto.WishlistItemDto;

import java.util.List;
import java.util.Map;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @Multipart
    @POST("v1/upload")
    Call<UploadResponseDto> uploadImage(@Part MultipartBody.Part file);

    @POST("v1/auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @POST("v1/auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

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

    @PATCH("v1/listings/{id}")
    Call<ListingDto> editListing(
            @Path("id") String id,
            @Body EditListingRequest request
    );

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

    // Donation Requests endpoints
    @POST("v1/listings/{id}/requests")
    Call<DonationRequestDto> createDonationRequest(
            @Path("id") String listingId,
            @Body CreateDonationRequest request
    );

    @GET("v1/listings/{id}/requests")
    Call<List<DonationRequestDto>> getListingRequests(@Path("id") String listingId);

    @GET("v1/requests/incoming")
    Call<List<DonationRequestDto>> getIncomingRequests();

    @POST("v1/requests/{id}/accept")
    Call<DonationRequestDto> acceptRequest(@Path("id") String requestId);

    @POST("v1/requests/{id}/decline")
    Call<DonationRequestDto> declineRequest(@Path("id") String requestId);

    // Notifications endpoints
    @GET("v1/notifications")
    Call<List<NotificationDto>> getNotifications();

    @GET("v1/notifications/unread-count")
    Call<UnreadCountDto> getUnreadCount();

    @POST("v1/notifications/{id}/read")
    Call<NotificationDto> markNotificationAsRead(@Path("id") String id);

    @POST("v1/notifications/read-all")
    Call<Map<String, Integer>> markAllNotificationsAsRead();

    // Chat endpoints
    @POST("v1/chat/conversations")
    Call<ChatConversationDto> createOrGetConversation(@Body CreateConversationRequest request);

    @GET("v1/chat/conversations")
    Call<List<ChatConversationDto>> getConversations();

    @GET("v1/chat/conversations/{id}")
    Call<ChatConversationDto> getConversationDetail(@Path("id") String conversationId);

    @GET("v1/chat/conversations/{id}/messages")
    Call<List<ChatMessageDto>> getMessages(@Path("id") String conversationId);

    @POST("v1/chat/conversations/{id}/messages")
    Call<ChatMessageDto> sendMessage(
            @Path("id") String conversationId,
            @Body SendMessageRequest request
    );

    // Wishlist endpoints
    @POST("v1/wishlist")
    Call<WishlistItemDto> createWishlistItem(@Body CreateWishlistRequest request);

    @GET("v1/wishlist")
    Call<List<WishlistItemDto>> getWishlistItems(
            @Query("category") String category,
            @Query("search") String search
    );

    @GET("v1/wishlist/my")
    Call<List<WishlistItemDto>> getMyWishlistItems();

    @GET("v1/wishlist/{id}")
    Call<WishlistItemDto> getWishlistItemDetail(@Path("id") String id);

    @DELETE("v1/wishlist/{id}")
    Call<Map<String, String>> deleteWishlistItem(@Path("id") String id);

    // Goodwill Points & Badges endpoints
    @GET("v1/points/me")
    Call<UserPointsDto> getMyPoints();

    @GET("v1/points/leaderboard")
    Call<List<LeaderboardEntryDto>> getLeaderboard();

    // Listing Moderation & Reporting endpoints
    @POST("v1/listings/{id}/reports")
    Call<ReportDto> reportListing(
            @Path("id") String listingId,
            @Body ReportListingRequest request
    );

    @GET("v1/listings/{id}/reports")
    Call<List<ReportDto>> getListingReports(@Path("id") String listingId);
}
