package com.vineet.addtocart.adapter;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.w3c.dom.Text;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vineet.addtocart.R;
import com.vineet.addtocart.eventbus.MyUpdateCartEvent;
import com.vineet.addtocart.listener.ICartLoadListener;
import com.vineet.addtocart.listener.IRecyclerViewClickListener;
import com.vineet.addtocart.model.CartModel;
import com.vineet.addtocart.model.TyreModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyTyreAdapter extends RecyclerView.Adapter<MyTyreAdapter.MyTyreViewHolder> {

private Context context;
private List<TyreModel> tyreModelList;
private ICartLoadListener iCartLoadListener;

    public MyTyreAdapter(Context context, List<TyreModel> tyreModelList, ICartLoadListener iCartLoadListener) {
        this.context = context;
        this.tyreModelList = tyreModelList;
        this.iCartLoadListener = iCartLoadListener;
    }

    public MyTyreAdapter(Context context, List<TyreModel> tyreModelList) {
        this.context = context;
        this.tyreModelList = tyreModelList;
    }

    @NonNull
    @Override
    public MyTyreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyTyreViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.layout_tyre_item, parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyTyreViewHolder holder, int position) {
        Glide.with(context)
                .load(tyreModelList.get(position).getImage())
                .into(holder.imageView);
        holder.txtPrice.setText(new StringBuilder("Rs. ").append(tyreModelList.get(position).getPrice()));
        holder.txtName.setText(new StringBuilder().append(tyreModelList.get(position).getName()));

        holder.setListener((view, adapterPosition) -> {
            addToCart(tyreModelList.get(position));

        });

    }

    private void addToCart(TyreModel tyreModel) {
        DatabaseReference userCart = FirebaseDatabase
                .getInstance()
                .getReference("Cart")
                .child("UNIQUE_USER_ID");

        userCart.child(tyreModel.getKey())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            CartModel cartModel = snapshot.getValue(CartModel.class);
                            cartModel.setQuantity(cartModel.getQuantity() + 1);
                            Map<String, Object> updateData = new HashMap<>();
                            updateData.put("quantity", cartModel.getQuantity());
                            updateData.put("totalPrice", cartModel.getQuantity()*Float.parseFloat(cartModel.getPrice()));


                            userCart.child(tyreModel.getKey())
                                    .updateChildren(updateData)
                                    .addOnSuccessListener(unused -> {
                                        iCartLoadListener.onCartLoadFailed("Add To Cart Success");

                                    })
                                    .addOnFailureListener(e -> iCartLoadListener.onCartLoadFailed(e.getMessage()));
                        }
                        else{
                            CartModel cartModel = new CartModel();
                            cartModel.setName(tyreModel.getName());
                            cartModel.setImage(tyreModel.getImage());
                            cartModel.setKey(tyreModel.getKey());
                            cartModel.setPrice(tyreModel.getPrice());
                            cartModel.setQuantity(1);
                            cartModel.setTotalPrice(Float.parseFloat(tyreModel.getPrice()));

                            userCart.child(tyreModel.getKey())
                                    .setValue(cartModel)
                                    .addOnSuccessListener(unused -> {
                                        iCartLoadListener.onCartLoadFailed("Add To Cart Success");

                                    })
                                    .addOnFailureListener(e -> iCartLoadListener.onCartLoadFailed(e.getMessage()));
                        }
                        EventBus.getDefault().postSticky(new MyUpdateCartEvent());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
iCartLoadListener.onCartLoadFailed(error.getMessage());
                    }
                });
    }

    @Override
    public int getItemCount() {
        return tyreModelList.size();
    }

    public static class MyTyreViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.imageView)
        ImageView imageView;
        @BindView(R.id.txtName)
        TextView txtName;
        @BindView(R.id.txtPrice)
        TextView txtPrice;

        IRecyclerViewClickListener listener;

        public void setListener(IRecyclerViewClickListener listener) {
            this.listener = listener;
        }

        public MyTyreViewHolder(@NonNull View itemView) {
            super(itemView);
            Unbinder unbinder = ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listener.onRecyclerClick(view, getAdapterPosition());
        }
    }
}
