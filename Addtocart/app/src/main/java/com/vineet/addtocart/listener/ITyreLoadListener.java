package com.vineet.addtocart.listener;

import com.vineet.addtocart.model.TyreModel;

import java.util.List;

public interface ITyreLoadListener {
    void onTyreLoadSuccess(List<TyreModel> tyreModelList);
    void onTyreLoadFailed(String message);

}
