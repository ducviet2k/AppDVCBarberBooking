package com.example.dvcbaberbooking;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dvcbaberbooking.Adapter.MyCartAdapter;
import com.example.dvcbaberbooking.Common.Common;
import com.example.dvcbaberbooking.Database.CartDataSource;
import com.example.dvcbaberbooking.Database.CartDatabase;
import com.example.dvcbaberbooking.Database.LocalCartDataSource;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
<<<<<<< HEAD

public class CartActivity extends AppCompatActivity implements ICartItemLoadListener, ICartItemUpdateListener, ISumCartListener {
=======
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class CartActivity extends AppCompatActivity {

    MyCartAdapter adapter;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    CartDataSource cartDataSource;
    //    CartDatabase cartDatabase;
>>>>>>> 16f49b7e73952a3ec410370f0ae495f17a09a276
    @BindView(R.id.recycler_cart)
    RecyclerView recycler_cart;

    @BindView(R.id.txt_total_price)
    TextView txt_total_price;

    @BindView(R.id.btn_clear_cart)
    Button btn_clear_cart;
<<<<<<< HEAD
    //xoa dich vu
    @OnClick(R.id.btn_clear_cart)
    void clearCart() {
        DatabaseUtils.clearCart(cartDatabase);

        //update adapter
        DatabaseUtils.getAllCart(cartDatabase, this);
=======

    //xoa dich vu
    @OnClick(R.id.btn_clear_cart)
    void clearCart() {
        //DatabaseUtils.clearCart(cartDatabase);
        cartDataSource.clearCart(Common.currentUser.getPhoneNumber())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Integer integer) {
                        compositeDisposable.add(
                                cartDataSource.getAllItemFromCart(Common.currentUser.getPhoneNumber())
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(cartItems -> {
                                            cartDataSource.sumPrice(Common.currentUser.getPhoneNumber())
                                                    .subscribeOn(Schedulers.io())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe(new SingleObserver<Long>() {
                                                        @Override
                                                        public void onSubscribe(Disposable d) {

                                                        }

                                                        @Override
                                                        public void onSuccess(Long aLong) {
                                                            txt_total_price.setText(new StringBuilder("$").append(aLong));
                                                        }

                                                        @Override
                                                        public void onError(Throwable e) {
                                                            Toast.makeText(CartActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        }, throwable -> {
                                            Toast.makeText(CartActivity.this, "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                        })
                        );
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(CartActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        //update adapter
        // DatabaseUtils.getAllCart(cartDatabase, this);
        getAllCart();
>>>>>>> 16f49b7e73952a3ec410370f0ae495f17a09a276
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
<<<<<<< HEAD
        cartDatabase = CartDatabase.getInstance(this);
=======
>>>>>>> 16f49b7e73952a3ec410370f0ae495f17a09a276
        ButterKnife.bind(CartActivity.this);


        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(this).cartDAO());
        //DatabaseUtils.getAllCart(cartDatabase, this);
        getAllCart();

        //View
        recycler_cart.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recycler_cart.setLayoutManager(linearLayoutManager);
        recycler_cart.addItemDecoration(new DividerItemDecoration(this, linearLayoutManager.getOrientation()));
    }

<<<<<<< HEAD
    @Override
    public void onGetAllItemFromCartSuccess(List<CartItem> cartItemList) {
        //lay tat du lieu tu data va gan vao recylerview

        MyCartAdapter adapter = new MyCartAdapter(this, cartItemList, this);
        recycler_cart.setAdapter(adapter);
=======
    private void getAllCart() {
        compositeDisposable.add(cartDataSource.getAllItemFromCart(Common.currentUser.getPhoneNumber())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(cartItems -> {

                    adapter = new MyCartAdapter(this, cartItems);
                    recycler_cart.setAdapter(adapter);
                    cartDataSource.sumPrice(Common.currentUser.getPhoneNumber())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new SingleObserver<Long>() {
                                @Override
                                public void onSubscribe(Disposable d) {

                                }

                                @Override
                                public void onSuccess(Long aLong) {
                                    txt_total_price.setText(new StringBuilder("$").append(aLong));
                                }

                                @Override
                                public void onError(Throwable e) {
                                    if (e.getMessage().contains("Query returned empty"))
                                        txt_total_price.setText("");
                                    else
                                        Toast.makeText(CartActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                                }
                            });
                }, throwable -> {
                    Toast.makeText(this, "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }));
>>>>>>> 16f49b7e73952a3ec410370f0ae495f17a09a276
    }
//
//    @Override
//    public void onGetAllItemFromCartSuccess(List<CartItem> cartItemList) {
//        //lay tat du lieu tu data va gan vao recylerview
//
//
//    }


<<<<<<< HEAD
    @Override
    public void onCartItemUpdateSuccess() {
        DatabaseUtils.sumCart(cartDatabase, this);
=======
//    @Override
//    public void onCartItemUpdateSuccess() {
//        DatabaseUtils.sumCart(cartDatabase, this);
//
//    }
>>>>>>> 16f49b7e73952a3ec410370f0ae495f17a09a276


    @Override
    protected void onDestroy() {
        if (adapter != null)
            adapter.onDestroy();
        compositeDisposable.clear();
        super.onDestroy();
    }
}