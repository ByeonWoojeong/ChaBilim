package app.woojeong.chabilim;


import android.app.Activity;
import android.content.SharedPreferences;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class FirebaseInstanceIDService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        String token = FirebaseInstanceId.getInstance().getToken();
        SharedPreferences prefToken = FirebaseInstanceIDService.this.getSharedPreferences("prefToken", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefToken.edit();
        editor.clear();
        editor.putString("Token", token);
        editor.commit();
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("Token", token)
                .add("package","chabilim")
                .build();
        Request request = new Request.Builder()
                .url("http://push.globalhumanism.kr/push/?type=action&value=reg")
                .post(body)
                .build();
        try {
            client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


