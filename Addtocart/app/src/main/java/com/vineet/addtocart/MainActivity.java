package com.vineet.addtocart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nex3z.notificationbadge.NotificationBadge;
import com.vineet.addtocart.adapter.MyTyreAdapter;
import com.vineet.addtocart.eventbus.MyUpdateCartEvent;
import com.vineet.addtocart.listener.ICartLoadListener;
import com.vineet.addtocart.listener.ITyreLoadListener;
import com.vineet.addtocart.model.CartModel;
import com.vineet.addtocart.model.TyreModel;
import com.vineet.addtocart.utils.SpaceItemDecoration;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements ITyreLoadListener, ICartLoadListener {

    @BindView(R.id.recycler_tyre)
    RecyclerView recyclerTyre;
    @BindView(R.id.mainLayout)
    RelativeLayout mainLayout;
    @BindView(R.id.badge)
    NotificationBadge badge;
    @BindView(R.id.btnCart)
    FrameLayout btnCart;

    ITyreLoadListener tyreLoadListener;
    ICartLoadListener cartLoadListener;

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        if(EventBus.getDefault().hasSubscriberForEvent(MyUpdateCartEvent.class));
            EventBus.getDefault().removeStickyEvent(MyUpdateCartEvent.class);
            EventBus.getDefault().unregister(this);
        super.onStop();

    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onUpdateCart(MyUpdateCartEvent event){
        countCartItem();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        loadTyreFromFirebase();
        countCartItem();

    }


    public void loadTyreFromFirebase(){
        List<TyreModel> tyreModels = new ArrayList<>();
        FirebaseDatabase.getInstance()
                .getReference("Tyre")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            for(DataSnapshot tyreSnapshot:snapshot.getChildren()){
                                TyreModel tyreModel = tyreSnapshot.getValue(TyreModel.class);
                                tyreModel.setKey(tyreSnapshot.getKey());
                                tyreModels.add(tyreModel);
                            }tyreLoadListener.onTyreLoadSuccess(tyreModels);
                        }else{
                            tyreLoadListener.onTyreLoadFailed("Can't find Tyre");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        tyreLoadListener.onTyreLoadFailed(error.getMessage());

                    }
                });
    }


    public void init(){
        ButterKnife.bind(this);
        tyreLoadListener = this;
        cartLoadListener = this;

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerTyre.setLayoutManager(gridLayoutManager);
        recyclerTyre.addItemDecoration(new SpaceItemDecoration());


        btnCart.setOnClickListener(v -> startActivity(new Intent(this, CartActivity.class)));
    }

    @Override
    public void onTyreLoadSuccess(List<TyreModel> tyreModelList) {
        MyTyreAdapter adapter = new MyTyreAdapter(this, tyreModelList, cartLoadListener);
        recyclerTyre.setAdapter(adapter);
    }

    @Override
    public void onTyreLoadFailed(String message) {
        Snackbar.make(mainLayout, message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onCartLoadSuccess(List<CartModel> cartModelList) {

        int cartSum = 0;
        for(CartModel cartModel: cartModelList)
            cartSum += cartModel.getQuantity();
        badge.setNumber(cartSum);

    }

    @Override
    public void onCartLoadFailed(String message) {
        Snackbar.make(mainLayout, message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        countCartItem();
    }

    private void countCartItem() {
        List<CartModel> cartModels = new ArrayList<>();
        FirebaseDatabase
                .getInstance().getReference("Cart")
                .child("UNIQUE_USER_ID")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
for(DataSnapshot cartSnapshot:snapshot.getChildren()){
    CartModel cartModel = cartSnapshot.getValue(CartModel.class);
    cartModel.setKey(cartSnapshot.getKey());
    cartModels.add(cartModel);
}
cartLoadListener.onCartLoadSuccess(cartModels);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        cartLoadListener.onCartLoadFailed(error.getMessage());
                    }
                });
    }
}