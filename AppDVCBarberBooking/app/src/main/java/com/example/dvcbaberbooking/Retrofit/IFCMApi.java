package com.example.dvcbaberbooking.Retrofit;

import com.example.dvcbaberbooking.Model.FCMResponse;
import com.example.dvcbaberbooking.Model.FCMSenData;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMApi {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAA2fmOGSU:APA91bGEarV4wpb950D5feIWyilTZUH-DuD_2DrvGom7buHl3-qHozjMxoi6uphdZOXCn8FdOSNwZikDisVEAnhKBVcS61zlRvAGqEmDAk8uV22lMt1v_Gj02fvi0K5OyQPZXOg1d2W_"
    })
    @POST("fcm/send")
    Observable<FCMResponse> senNotification(@Body FCMSenData body);
}
