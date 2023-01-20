package com.vineet.addtocart.listener;

import com.vineet.addtocart.model.CartModel;

import java.util.List;

public interface ICartLoadListener {
    void onCartLoadSuccess(List<CartModel> cartModelList);
    void onCartLoadFailed(String message);

}
