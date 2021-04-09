package com.example.my_message.Notification;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAA1WBg5CM:APA91bGeFex4qr1L-kwAjBM4Rv1OZHZAgoRFGyWf8gBFJeCjXtNhDO1-ebwvt0Q9GrDvzCA0SMB6I-IyvctoKLMSEtC3GfGclypwt4mLr4nvO5HO2JA6xDAh-gzDYjWEMdY8yrLZhIDx"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);

}
