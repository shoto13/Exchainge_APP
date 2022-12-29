package com.journey13.exchainge.Fragments;

import com.journey13.exchainge.Notifications.Response;
import com.journey13.exchainge.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAA3kNJONY:APA91bHQshN319ko3IfwQtjTdGETX7j6pAqQ8gC5M3wOCNULj4GY1WJ6RivHcR35sU_DCM5Tr5nSKTx6uuGNBUvCmVRIlAYyPG4cdZvg7gg5Nwo5GSolv8L99Gq0YRIjISIh7FXBWDWg"
            }


    )

    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);

}
