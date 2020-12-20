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
                    "Content-Type: application/json",
                    "Authorization: key=AAAAVEr7ECE:APA91bE30hQkkEd7YBDi8iTOO3YSxqD2Gg8INys3xrxFIFBK6KmZDlX8I6c9CdpnjDYrmA0N-Ap06_Qd6FFsEy6OwFcl3LsjgV2IKVl2Q9-5KpqETf_-avow2RLhSGRj9RCCVWzQc4yR"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
