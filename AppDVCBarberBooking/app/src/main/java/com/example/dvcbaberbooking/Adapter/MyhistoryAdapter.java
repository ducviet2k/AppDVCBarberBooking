package com.example.dvcbaberbooking.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dvcbaberbooking.Model.BookingInformation;
import com.example.dvcbaberbooking.R;

import java.nio.Buffer;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyhistoryAdapter extends RecyclerView.Adapter<MyhistoryAdapter.MyViewHolder> {

    Context context;
    List<BookingInformation> bookingInformationList;

    public MyhistoryAdapter(Context context, List<BookingInformation> bookingInformationList) {
        this.context = context;
        this.bookingInformationList = bookingInformationList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_hisory, viewGroup, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int i) {
        holder.txt_booking_baber_text.setText(bookingInformationList.get(i)
                .getBarberName());

        holder.txt_booking_time_text.setText(bookingInformationList.get(i).getTime());
        holder.txt_salon_address.setText(bookingInformationList.get(i).getSalonAddress());
        holder.txt_salon_name.setText(bookingInformationList.get(i).getSalonName());
//        holder.txt_booking_date.setText(bookingInformationList.get(i).getTime());

    }

    @Override
    public int getItemCount() {
        return bookingInformationList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        Unbinder unbinder;

        @BindView(R.id.txt_salon_name)
        TextView txt_salon_name;

        @BindView(R.id.txt_salon_address)
        TextView txt_salon_address;

        @BindView(R.id.txt_booking_time_text)
        TextView txt_booking_time_text;

        @BindView(R.id.txt_booking_baber_text)
        TextView txt_booking_baber_text;

//        @BindView(R.id.txt_booking_date)
//        TextView txt_booking_date;




        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this, itemView);
        }
    }
}
