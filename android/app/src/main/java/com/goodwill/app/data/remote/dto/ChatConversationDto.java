package com.goodwill.app.data.remote.dto;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class ChatConversationDto implements Serializable {

    @SerializedName("id")
    private String id;

    @SerializedName("listingId")
    private String listingId;

    @SerializedName("listingTitle")
    private String listingTitle;

    @SerializedName("buyerId")
    private String buyerId;

    @SerializedName("buyerName")
    private String buyerName;

    @SerializedName("buyerEmail")
    private String buyerEmail;

    @SerializedName("sellerId")
    private String sellerId;

    @SerializedName("sellerName")
    private String sellerName;

    @SerializedName("sellerEmail")
    private String sellerEmail;

    @SerializedName("lastMessageText")
    private String lastMessageText;

    @SerializedName("lastMessageAt")
    private String lastMessageAt;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

    public ChatConversationDto() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getListingId() {
        return listingId;
    }

    public void setListingId(String listingId) {
        this.listingId = listingId;
    }

    public String getListingTitle() {
        return listingTitle;
    }

    public void setListingTitle(String listingTitle) {
        this.listingTitle = listingTitle;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public String getBuyerEmail() {
        return buyerEmail;
    }

    public void setBuyerEmail(String buyerEmail) {
        this.buyerEmail = buyerEmail;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getSellerEmail() {
        return sellerEmail;
    }

    public void setSellerEmail(String sellerEmail) {
        this.sellerEmail = sellerEmail;
    }

    public String getLastMessageText() {
        return lastMessageText;
    }

    public void setLastMessageText(String lastMessageText) {
        this.lastMessageText = lastMessageText;
    }

    public String getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(String lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getOtherPartyName(String myUid) {
        if (myUid != null && myUid.equals(buyerId)) {
            return (sellerName != null && !sellerName.isEmpty()) ? sellerName : "Seller";
        }
        return (buyerName != null && !buyerName.isEmpty()) ? buyerName : "Buyer";
    }
}
