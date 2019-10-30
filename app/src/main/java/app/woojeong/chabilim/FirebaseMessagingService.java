package app.woojeong.chabilim;

/**
 * Created by YJhong on 2018-3-08.
 */

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;

import java.net.URL;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    Bitmap bigPicture;
    String nickname, type = "", text = "", image = "";

    // [START receive_message]
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.e("push", "push");
        Log.e("push", remoteMessage.getData().toString());
        nickname = "ChaBilim";
        type = "";
        text = "";
        image = "";
        type += remoteMessage.getData().get("type");
        image += remoteMessage.getData().get("image");
        text += remoteMessage.getData().get("text");
        if (type.equals("image")) {
            //img 추가-메세지와 이미지를 인수로 지정
            sendNotificationImg(text, image);
        } else {
            sendNotification(text);
        }
    }

    //messageBody를 핸드폰 상단 화면에 띄움
    //메세지만
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void sendNotification(String messageBody) {
        Uri defaultSoundUri;
        defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Intent intent = new Intent(this, SplashActivity.class);
        int idx = (int) System.currentTimeMillis();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, idx /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder notificationBuilder;
        notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentTitle(nickname)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .setBigContentTitle(nickname)
                        .bigText(messageBody))
                .setContentText(messageBody)
                .setSound(defaultSoundUri)
//                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }


    //이미지도
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void sendNotificationImg(String messageBody, String myimgurl) {
        Uri defaultSoundUri;

        defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Intent intent = new Intent(this, SplashActivity.class);
        int idx = (int) System.currentTimeMillis();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, idx /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);
        //이미지 온라인 링크를 가져와 비트맵으로 바꾼다.
        try {
            URL url = new URL(myimgurl);
            bigPicture = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentTitle(nickname)
                .setContentText("알림을 확인해 주세요.")
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigPictureStyle()
                        .bigPicture(bigPicture)
                        .setBigContentTitle(nickname)
                        .setSummaryText(messageBody))
                .setSound(defaultSoundUri)
//                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
        //.notify(noti아이디(쌓이게 하려면 매번 다른값을, 지워지게하려면 0으로 고정,-)
    }
}
