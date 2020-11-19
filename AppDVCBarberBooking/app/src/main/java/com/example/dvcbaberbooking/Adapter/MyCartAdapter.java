package com.example.dvcbaberbooking.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dvcbaberbooking.Database.CartDataSource;
import com.example.dvcbaberbooking.Database.CartDatabase;
import com.example.dvcbaberbooking.Database.CartItem;
import com.example.dvcbaberbooking.Database.LocalCartDataSource;
import com.example.dvcbaberbooking.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MyCartAdapter extends RecyclerView.Adapter<MyCartAdapter.MyViewHolder> {
    Context context;
    List<CartItem> cartItemList;
    CartDatabase cartDatabase;
    CartDataSource cartDataSource;
    CompositeDisposable compositeDisposable;
//    ICartItemUpdateListener iCartItemUpdateListener;

    public void onDestroy(){
        compositeDisposable.clear();
    }

    public MyCartAdapter(Context context, List<CartItem> cartItemList) {
        this.context = context;
        this.cartItemList = cartItemList;
//        this.iCartItemUpdateListener = iCartItemUpdateListener;
        this.cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(context).cartDAO());
        this.compositeDisposable = new CompositeDisposable();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(
                R.layout.layout_cart_item, viewGroup, false
        );
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int i) {
        Picasso.get().load(cartItemList.get(i).getProductImage()).into(holder.img_product);
        holder.txt_cart_name.setText(new StringBuilder(cartItemList.get(i).getProductName()));
        holder.txt_cart_price.setText(new StringBuilder("$").append(cartItemList.get(i).getProductPrice()));
        holder.txt_quantity.setText(new StringBuilder(String.valueOf(cartItemList.get(i).getProductQuantity())));


        //su kien

        holder.setListener(new IImageButtonListener() {
            @Override
            public void onImageButtonClick(View view, int pos, boolean isDecrease) {
                if (isDecrease) {
                    if (cartItemList.get(pos).getProductQuantity() > 1) {
                        cartItemList.get(pos)
                                .setProductQuantity(cartItemList
                                        .get(pos)
                                        .getProductQuantity() - 1);
                        cartDataSource.update(cartItemList.get(pos))
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new SingleObserver<Integer>() {
                                    @Override
                                    public void onSubscribe(Disposable d) {

                                    }

                                    @Override
                                    public void onSuccess(Integer integer) {
                                        holder.txt_quantity.setText(new StringBuilder(String.valueOf(cartItemList.get(i).getProductQuantity())));

                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                       // DatabaseUtils.updateCart(cartDatabase, cartItemList.get(pos));

                    } else if (cartItemList.get(pos).getProductQuantity()==1)//delete Cart
                    {
                       // DatabaseUtils.deleteCart(cartDatabase,cartItemList.get(pos));
                        cartDataSource.delete(cartItemList.get(pos))
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new SingleObserver<Integer>() {
                                    @Override
                                    public void onSubscribe(Disposable d) {

                                    }

                                    @Override
                                    public void onSuccess(Integer integer) {
                                        cartItemList.remove(pos);
                                        notifyItemRemoved(pos);
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                    }
                } else {
                    if (cartItemList.get(pos).getProductQuantity() < 99) {
                        cartItemList.get(pos)
                                .setProductQuantity(cartItemList
                                        .get(pos)
                                        .getProductQuantity() + 1);

                       // DatabaseUtils.updateCart(cartDatabase, cartItemList.get(pos));
                        cartDataSource.update(cartItemList.get(pos))
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new SingleObserver<Integer>() {
                                    @Override
                                    public void onSubscribe(Disposable d) {

                                    }

                                    @Override
                                    public void onSuccess(Integer integer) {
                                        holder.txt_quantity.setText(new StringBuilder(String.valueOf(cartItemList.get(i).getProductQuantity())));

                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });


                    }
                }
//                iCartItemUpdateListener.onCartItemUpdateSuccess();
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    interface IImageButtonListener {
        void onImageButtonClick(View view, int pos, boolean isDecrease);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView txt_cart_name, txt_cart_price, txt_quantity;
        ImageView img_decrease, img_increase, img_product;

        IImageButtonListener listener;

        public void setListener(IImageButtonListener listener) {
            this.listener = listener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            img_product = (ImageView) itemView.findViewById(R.id.cart_image);
            txt_cart_name = (TextView) itemView.findViewById(R.id.txt_cart_name);
            txt_cart_price = (TextView) itemView.findViewById(R.id.txt_cart_price);
            img_decrease = (ImageView) itemView.findViewById(R.id.img_decrease);
            txt_quantity = (TextView) itemView.findViewById(R.id.txt_cart_quantity);
            img_increase = (ImageView) itemView.findViewById(R.id.img_increase);

            //su kien

            img_decrease.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onImageButtonClick(view, getAdapterPosition(), true);
                }
            });

            img_increase.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onImageButtonClick(view, getAdapterPosition(), false);
                }
            });

        }
    }
}
