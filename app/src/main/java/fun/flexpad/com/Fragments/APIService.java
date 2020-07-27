package fun.flexpad.com.Fragments;

import fun.flexpad.com.Notifications.MyResponse;
import fun.flexpad.com.Notifications.Sender;



import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAt1GzHho:APA91bFKcMkS3vXGlahYMcycfbFfKIxKZMlSofKVp3WdKL_DKIxOmsU5cSWsFpPgp9fH8ZNcSm3HbMjUat_8dtBaWplfvN6giy3hnoWNgxwHaWMiLP8nB79rPCz4XWO6gDRInVQPRLRv"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
