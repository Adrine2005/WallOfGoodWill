package com.goodwill.app.ui.listings;

import androidx.annotation.DrawableRes;
import com.goodwill.app.R;

public class CategoryPlaceholderHelper {

    @DrawableRes
    public static int getPlaceholderForCategory(String category) {
        if (category == null) {
            return R.drawable.ic_placeholder_general;
        }

        String upper = category.trim().toUpperCase();
        switch (upper) {
            case "BOOKS":
                return R.drawable.ic_placeholder_books;
            case "ELECTRONICS":
                return R.drawable.ic_placeholder_electronics;
            case "CYCLES":
                return R.drawable.ic_placeholder_cycles;
            case "STATIONERY":
                return R.drawable.ic_placeholder_stationery;
            case "KITCHENWARE":
                return R.drawable.ic_placeholder_kitchenware;
            case "FURNITURE":
                return R.drawable.ic_placeholder_furniture;
            default:
                return R.drawable.ic_placeholder_general;
        }
    }
}
