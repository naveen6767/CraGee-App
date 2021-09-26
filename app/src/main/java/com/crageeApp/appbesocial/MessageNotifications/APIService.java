package com.crageeApp.appbesocial.MessageNotifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAyUp6GFA:APA91bHOq2dDYmuRqRmK-2gsEEQ6dD1a8nln62NeFPe8gofMydDHs1UetjmCap-thR4v_KW0jKf85ipXu723dQFWwEx7xYhxJ9rKUtFrNhYyXVyYG0wC7ycL1ljOv0EA5sR238zSmRNX"
    })

    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);

}
