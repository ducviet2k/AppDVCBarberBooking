package com.example.dvcbaberbooking.Fragments;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;

import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.dvcbaberbooking.Common.Common;
import com.example.dvcbaberbooking.Database.CartDatabase;
import com.example.dvcbaberbooking.Database.CartItem;
import com.example.dvcbaberbooking.Database.DatabaseUtils;
import com.example.dvcbaberbooking.HomeActivity;
import com.example.dvcbaberbooking.Interface.ICartItemLoadListener;
import com.example.dvcbaberbooking.MainActivity;
import com.example.dvcbaberbooking.Model.BookingInformation;
import com.example.dvcbaberbooking.Model.EventBus.ConfirmBookingEvent;
import com.example.dvcbaberbooking.Model.FCMResponse;
import com.example.dvcbaberbooking.Model.FCMSenData;
import com.example.dvcbaberbooking.Model.MyNotification;
import com.example.dvcbaberbooking.Model.MyToken;
import com.example.dvcbaberbooking.R;
import com.example.dvcbaberbooking.Retrofit.IFCMApi;
import com.example.dvcbaberbooking.Retrofit.RetrofitClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class BookingStep4Fragment extends Fragment implements ICartItemLoadListener {
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    SimpleDateFormat simpleDateFormat;

    Unbinder unbinder;
    AlertDialog dialog;
    IFCMApi ifcmApi;
    @BindView(R.id.txt_booking_baber_text)
    TextView txt_booking_baber_text;
    @BindView(R.id.txt_booking_time_text)
    TextView txt_booking_time_text;
    @BindView(R.id.txt_salon_address)
    TextView txt_salon_address;
    @BindView(R.id.txt_salon_name)
    TextView txt_salon_name;
    @BindView(R.id.txt_salon_open_hours)
    TextView txt_salon_open_hours;
    @BindView(R.id.txt_salon_phone)
    TextView txt_salon_phone;
    @BindView(R.id.txt_salon_website)
    TextView txt_salon_website;

    @OnClick(R.id.btn_confirm)
        //tao man booking
    void confirmBooking() {
        dialog.show();
        DatabaseUtils.getAllCart(CartDatabase.getInstance(getContext()),
                this);
    }


    private void addToUserBooking(BookingInformation bookingInformation) {

        final CollectionReference userBooking = FirebaseFirestore.getInstance()
                .collection("User")
                .document(Common.currentUser.getPhoneNumber())
                .collection("Booking");

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        Timestamp timestamp = new Timestamp(calendar.getTime());

        userBooking.whereGreaterThanOrEqualTo("timestamp", timestamp)
                .whereEqualTo("done", false)
                .limit(1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.getResult().isEmpty()) {
                            userBooking.document()
                                    .set(bookingInformation)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            MyNotification myNotification = new MyNotification();
                                            myNotification.setUid(UUID.randomUUID().toString());
                                            myNotification.setTitle("New Booking");
                                            myNotification.setConten("You have a new appoiment for customer hair care with " + Common.currentUser.getName());
                                            myNotification.setRead(false);
                                            myNotification.setServerTimestamp(FieldValue.serverTimestamp());

                                            FirebaseFirestore.getInstance()
                                                    .collection("AllSalon")
                                                    .document(Common.city)
                                                    .collection("Branch")
                                                    .document(Common.currentSalon.getSalonId())
                                                    .collection("Barbers")
                                                    .document(Common.currentBarber.getBarberId())
                                                    .collection("Notifications")
                                                    .document(myNotification.getUid())
                                                    .set(myNotification)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            FirebaseFirestore.getInstance().collection("Tokens")
                                                                    .whereEqualTo("userPhone", Common.currentBarber.getUsername())
                                                                    .limit(1)
                                                                    .get()
                                                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                            if (task.isSuccessful() && task.getResult().size() > 0) {
                                                                                MyToken myToken = new MyToken();
                                                                                for (DocumentSnapshot tokenSnapshot : task.getResult())
                                                                                    myToken = tokenSnapshot.toObject(MyToken.class);

                                                                                FCMSenData sendRequest = new FCMSenData();
                                                                                Map<String, String> dataSend = new HashMap<>();
                                                                                dataSend.put(Common.TITLE_KEY, "New Booking");
                                                                                dataSend.put(Common.CONTENT_KEY, "You have new booking from user" + Common.currentUser.getName());
                                                                                ///////////////////////////
                                                                                sendRequest.setTo(myToken.getToken());
                                                                                sendRequest.setData(dataSend);
                                                                                compositeDisposable.add(ifcmApi.senNotification(sendRequest)
                                                                                        .subscribeOn(Schedulers.io())
                                                                                        .observeOn(AndroidSchedulers.mainThread())
                                                                                        .subscribe(new Consumer<FCMResponse>() {
                                                                                            @Override
                                                                                            public void accept(FCMResponse fcmResponse) throws Exception {
                                                                                                if (dialog.isShowing())
                                                                                                dialog.dismiss();
                                                                                                addToCalendar(Common.bookingDate,
                                                                                                        Common.convertTimeSlotToString(Common.currentTimeSlot));
                                                                                                resestStaticData();
                                                                                                getActivity().finish();
                                                                                                Toast.makeText(getContext(), "Success!", Toast.LENGTH_SHORT).show();


                                                                                            }
                                                                                        }, new Consumer<Throwable>() {
                                                                                            @Override
                                                                                            public void accept(Throwable throwable) throws Exception {
                                                                                                if (dialog.isShowing())
                                                                                                    dialog.dismiss();
                                                                                                    addToCalendar(Common.bookingDate,
                                                                                                        Common.convertTimeSlotToString(Common.currentTimeSlot));
                                                                                                resestStaticData();
                                                                                                getActivity().finish();

                                                                                            }
                                                                                        }));

                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    });


                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    if (dialog.isShowing())
                                        dialog.dismiss();
                                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            if (dialog.isShowing())
                            dialog.dismiss();
                            resestStaticData();
                            getActivity().finish();
                            Toast.makeText(getContext(), "Success!", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }


////                                        FirebaseFirestore.getInstance()
////                                                .collection("AllSalon")
////                                                .document(Common.city)
////                                                .collection("Branch")
////                                                .document(Common.currentSalon.getSalonId())
////                                                .collection("Barbers")
////                                                .document(Common.currentBarber.getBarberId())
////                                                .collection("Notifications")
////                                                .document(myNotification.getUid())
////                                                .set(myNotification)
////                                                .addOnSuccessListener(aVoid1 -> {
////
////                                                    FirebaseFirestore.getInstance()
////                                                            .collection("Tokens")
////                                                            .whereEqualTo("userPhone", Common.currentBarber.getUsername())
////                                                            .limit(1)
////                                                            .get()
////                                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
////                                                                @Override
////                                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
////                                                                    if (task.isSuccessful() && task.getResult().size() > 0) {
////                                                                        MyToken myToken = new MyToken();
////                                                                        for (DocumentSnapshot tokensSnapshot : task.getResult())
////                                                                            myToken = tokensSnapshot.toObject(MyToken.class);
////                                                                        //create data
////                                                                        FCMSenData sendRequest = new FCMSenData();
////                                                                        Map<String, String> dataSend = new HashMap<>();
////                                                                        dataSend.put(Common.TITLE_KEY, "Thông ");
////                                                                        dataSend.put(Common.CONTENT_KEY, "Bạn có thông báo mới từ khách " + Common.currentUser.getName());
////                                                                        sendRequest.setTo(myToken.getToken());
////                                                                        sendRequest.setData(dataSend);
////
////                                                                        compositeDisposable.add(ifcmApi.senNotification(sendRequest)
////                                                                                .subscribeOn(Schedulers.io())
////                                                                                .observeOn(AndroidSchedulers.mainThread())
////                                                                                .subscribe(new Consumer<FCMResponse>() {
////                                                                                    @Override
////                                                                                    public void accept(FCMResponse fcmResponse) throws Exception {
////
////                                                                                        if (dialog.isShowing())
////                                                                                            dialog.dismiss();
////
////                                                                                        addToCalendar(Common.bookingDate,
////                                                                                                Common.convertTimeSlotToString(Common.currentTimeSlot));
////                                                                                        resestStaticData();
////                                                                                        getActivity().finish();
////                                                                                        Toast.makeText(getContext(), "Đặt lịch thành công !", Toast.LENGTH_SHORT).show();
////
////                                                                                    }
////                                                                                }, new Consumer<Throwable>() {
////                                                                                    @Override
////                                                                                    public void accept(Throwable throwable) throws Exception {
////                                                                                        Log.d("NOTIFICATION_ERROR", throwable.getMessage());
////
////                                                                                        addToCalendar(Common.bookingDate,
////                                                                                                Common.convertTimeSlotToString(Common.currentTimeSlot));
////                                                                                        resestStaticData();
////                                                                                        getActivity().finish();
////                                                                                        dialog.dismiss();
////
////                                                                                    }
////                                                                                }));
////                                                                    }
////                                                                }
////                                                            });
////
////                                                });
////
////
////                                    }
////                                })
//                                .addOnFailureListener(new OnFailureListener() {
//                                    @Override
//                                    public void onFailure(@NonNull Exception e) {
//
//                                        if (dialog.isShowing())
//                                            dialog.dismiss();
//                                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
//                                    }
//                                });
//                    } else {
//                        if (dialog.isShowing())
//                            dialog.dismiss();
//                        resestStaticData();
//                        getActivity().finish();
//                        Toast.makeText(getContext(), "Success!", Toast.LENGTH_SHORT).show();
//
//                    }
//                });
//    }
//

    private void addToCalendar(Calendar bookingDate, String startDate) {

        String startTime = Common.convertTimeSlotToString(Common.currentTimeSlot);
        String[] convertTime = startTime.split("-");
        String[] startTimeConvert = convertTime[0].split(":");
        int startHourInt = Integer.parseInt(startTimeConvert[0].trim());
        int startMinInt = Integer.parseInt(startTimeConvert[1].trim());

        String[] endTimeConvert = convertTime[1].split(":");
        int endHourInt = Integer.parseInt(endTimeConvert[0].trim());
        int endMinInt = Integer.parseInt(endTimeConvert[1].trim());

        Calendar startEvent = Calendar.getInstance();
        startEvent.setTimeInMillis(bookingDate.getTimeInMillis());
        startEvent.set(Calendar.HOUR_OF_DAY, startHourInt);
        startEvent.set(Calendar.MINUTE, startMinInt);

        Calendar endEvent = Calendar.getInstance();
        endEvent.setTimeInMillis(bookingDate.getTimeInMillis());
        endEvent.set(Calendar.HOUR_OF_DAY, endHourInt);
        endEvent.set(Calendar.MINUTE, endMinInt);

        SimpleDateFormat calendarFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        String startEventTime = calendarFormat.format(startEvent.getTime());
        String endEventTime = calendarFormat.format(endEvent.getTime());


        addToDeviceCalendar(startEventTime, endEventTime, "Haircut Booking",
                new StringBuilder("Haircut from")
                        .append(startTime)
                        .append(" with ")
                        .append(Common.currentBarber.getName())
                        .append(" at ")
                        .append(Common.currentSalon.getName()).toString(),
                new StringBuilder("Address: ").append(Common.currentSalon.getAddress()).toString());

    }

    private void addToDeviceCalendar(String startEventTime, String endEventTime, String title, String description, String location) {

        SimpleDateFormat calendarDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");

        try {

            Date start = calendarDateFormat.parse(startEventTime);
            Date end = calendarDateFormat.parse(endEventTime);
            ContentValues event = new ContentValues();
            event.put(CalendarContract.Events.CALENDAR_ID, getCalendar(getContext()));
            event.put(CalendarContract.Events.TITLE, title);
            event.put(CalendarContract.Events.DESCRIPTION, String.valueOf(description));
            event.put(CalendarContract.Events.EVENT_LOCATION, String.valueOf(location));


            event.put(CalendarContract.Events.DTSTART, start.getTime());
            event.put(CalendarContract.Events.DTEND, end.getTime());
            event.put(CalendarContract.Events.ALL_DAY, 0);
            event.put(CalendarContract.Events.HAS_ALARM, 1);

            String timeZone = TimeZone.getDefault().getID();
            event.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone);

            Uri calendar;
            if (Build.VERSION.SDK_INT >= 8)
                calendar = Uri.parse("content://com.android.calendar/events");
            else {
                calendar = Uri.parse("content://calendar/events");
            }
            Uri uri_save = getActivity().getContentResolver().insert(calendar, event);

            Paper.init(getActivity());
            Paper.book().write(Common.EVENT_URI_CACHE, uri_save.toString());


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getCalendar(Context context) {

        String gmailIdCalendar = "";
        String projection[] = {"_id", "calendar_displayName"};
        Uri calendar = Uri.parse("content://com.android.calendar/calendars");
        ContentResolver contentResolver = context.getContentResolver();
        Cursor managedCursor = contentResolver.query(calendar, projection, null, null, null);
        if (managedCursor.moveToFirst()) {
            String callname;
            int NameCol = managedCursor.getColumnIndex(projection[1]);
            int idCol = managedCursor.getColumnIndex(projection[0]);
            do {
                callname = managedCursor.getString(NameCol);
                if (callname.contains("@gmail.com")) {
                    gmailIdCalendar = managedCursor.getString(idCol);
                }
            } while (managedCursor.moveToNext());
            managedCursor.close();
        }
        return gmailIdCalendar;
    }

    private void resestStaticData() {
        Common.step = 0;
        Common.currentTimeSlot = -1;
        Common.currentSalon = null;
        Common.currentBarber = null;
        Common.bookingDate.add(Calendar.DATE, 0);


    }

//===========================>
//EvenBus

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();

    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void setDataBooking(ConfirmBookingEvent event) {
        if (event.isConfirm()) {
            setData();
        }
    }


//===============================


    private void setData() {
        txt_booking_baber_text.setText(Common.currentBarber.getName());
        txt_booking_time_text.setText(new StringBuilder(Common.convertTimeSlotToString(Common.currentTimeSlot))
                .append(" at ")
                .append(simpleDateFormat.format(Common.bookingDate.getTime())));
        txt_salon_address.setText(Common.currentSalon.getAddress());
        txt_salon_website.setText(Common.currentSalon.getWebsite());
        txt_salon_name.setText(Common.currentSalon.getName());
        txt_salon_open_hours.setText(Common.currentSalon.getOpenHours());
    }

    static BookingStep4Fragment instance;


    public static BookingStep4Fragment getInstance() {
        if (instance == null)
            instance = new BookingStep4Fragment();
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ifcmApi = RetrofitClient.getInstance().create(IFCMApi.class);
        //apply format for date display on confics
        simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");


       // dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
       dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();

    }

    @Override
    public void onDestroy() {

        compositeDisposable.clear();
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View itemView = inflater.inflate(R.layout.fragment_booking_step_four, container, false);
        unbinder = ButterKnife.bind(this, itemView);
        return itemView;
    }


    @Override
    public void onGetAllItemFromCartSuccess(List<CartItem> cartItemList) {

        String startTime = Common.convertTimeSlotToString(Common.currentTimeSlot);
        String[] convertTime = startTime.split("-");
        String[] startTimeConvert = convertTime[0].split(":");
        int startHourInt = Integer.parseInt(startTimeConvert[0].trim());
        int startMinInt = Integer.parseInt(startTimeConvert[1].trim());

        Calendar bookingDateWithourHouse = Calendar.getInstance();
        bookingDateWithourHouse.setTimeInMillis(Common.bookingDate.getTimeInMillis());
        bookingDateWithourHouse.set(Calendar.HOUR_OF_DAY, startHourInt);
        bookingDateWithourHouse.set(Calendar.MINUTE, startMinInt);
        Timestamp timestamp = new Timestamp(bookingDateWithourHouse.getTime());

        BookingInformation bookingInformation = new BookingInformation();

        bookingInformation.setCityBook(Common.city);
        bookingInformation.setTimestamp(timestamp);

        bookingInformation.setDone(false);
        bookingInformation.setBarberId(Common.currentBarber.getBarberId());
        bookingInformation.setBarberName(Common.currentBarber.getName());
        bookingInformation.setCustomerName(Common.currentUser.getName());
        bookingInformation.setCustomerPhone(Common.currentUser.getPhoneNumber());
        bookingInformation.setSalonId(Common.currentSalon.getSalonId());
        bookingInformation.setSalonAddress(Common.currentSalon.getAddress());
        bookingInformation.setSalonName(Common.currentSalon.getName());
        bookingInformation.setTime(new StringBuilder(Common.convertTimeSlotToString(Common.currentTimeSlot))
                .append(" at ")
                .append(simpleDateFormat.format(bookingDateWithourHouse.getTime())).toString());
        bookingInformation.setSlot(Long.valueOf(Common.currentTimeSlot));
        bookingInformation.setCartItemList(cartItemList);//add card item list to booking information

        //submit to banner Document

        DocumentReference bookingdate = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.city)
                .collection("Branch")
                .document(Common.currentSalon.getSalonId())
                .collection("Barbers")
                .document(Common.currentBarber.getBarberId())
                .collection(Common.simpleDateFormat.format(Common.bookingDate.getTime()))
                .document(String.valueOf(Common.currentTimeSlot));

        //ghi du lieu

        bookingdate.set(bookingInformation).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                DatabaseUtils.clearCart(CartDatabase.getInstance(getContext()));
                addToUserBooking(bookingInformation);


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
