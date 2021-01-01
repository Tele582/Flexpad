package fun.flexpad.com.Notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import fun.flexpad.com.MessageActivity;
import fun.flexpad.com.R;
import fun.flexpad.com.RoomChatActivity;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String notificationType = remoteMessage.getData().get("notificationType");

        SharedPreferences preferences = getSharedPreferences("PREFS", MODE_PRIVATE);
        String currentUser = preferences.getString("currentuser", "none");

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

//        if (notificationType == "userNotification" ) {
//            String sented = remoteMessage.getData().get("sented");
//            String user = remoteMessage.getData().get("user");
//            if (firebaseUser != null && sented != null && sented.equals(firebaseUser.getUid())) {
//                if (!currentUser.equals(user)) {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                        sendOreoNotification(remoteMessage);
//                    } else {
//                        sendNotification(remoteMessage);
//                    }
//                }
//            }
//        } else if (notificationType == "roomNotification" ) {

//            String room = remoteMessage.getData().get("room");
////            String room = "-MOy_RBHYl6toX9sHCk6";
//            String roomTitle = remoteMessage.getData().get("roomTitle");
//            String sented = remoteMessage.getData().get("sented");
//            String user = remoteMessage.getData().get("user");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                sendRoomOreoNotification(remoteMessage);
            } else {
                sendRoomNotification(remoteMessage);
            }
//        }
//        else {
////            String room = remoteMessage.getData().get("room");
//            String room = "-MOy_RBHYl6toX9sHCk6";
//            String roomTitle = remoteMessage.getData().get("roomTitle");
//            String sented = remoteMessage.getData().get("sented");
////            String user = remoteMessage.getData().get("user");
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                sendRoomOreoNotification(remoteMessage);
//            } else {
//                sendRoomNotification(remoteMessage);
//            }
//        }
    }

    private void sendOreoNotification(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int j = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, MessageActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("userid", user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        OreoNotification oreoNotification = new OreoNotification(this);
        Notification.Builder builder = oreoNotification.getOreoNotification(title, body, pendingIntent,
                defaultSound, icon);

        int i = 0;
        if (j > 0){
            i = j;
        }

        oreoNotification.getManager().notify(i, builder.build());

    }

    private void sendNotification(RemoteMessage remoteMessage) {

        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int j = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, MessageActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("userid", user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.flexpad_fourth_actual_icon_foreground)
//                .setSmallIcon(Integer.parseInt(icon))
                .setWhen(System.currentTimeMillis())
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defaultSound)
                .setContentIntent(pendingIntent);
        NotificationManager noti = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        int i = 0;
        if (j > 0){
            i = j;
        }

        noti.notify(i, builder.build());
    }

    private void sendRoomOreoNotification(RemoteMessage remoteMessage) {
//        String user = remoteMessage.getData().get("user");
        String room = remoteMessage.getData().get("room");
//        String room = "-MOy_RBHYl6toX9sHCk6";
        String roomTitle = remoteMessage.getData().get("roomTitle");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        int j = Integer.parseInt(room.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, RoomChatActivity.class);
        Bundle bundle = new Bundle();
//        bundle.putString("userid", user);
        bundle.putString("Room_ID", room);
        bundle.putString("Room_Name", roomTitle);

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        OreoNotification oreoNotification = new OreoNotification(this);
        Notification.Builder builder = oreoNotification.getOreoNotification(title, body, pendingIntent,
                defaultSound, icon);

        int i = 0;
        if (j > 0){
            i = j;
        }

        oreoNotification.getManager().notify(i, builder.build());

    }

    private void sendRoomNotification(RemoteMessage remoteMessage) {

//        String user = remoteMessage.getData().get("user");
        String room = remoteMessage.getData().get("room");
//        String room = "-MOy_RBHYl6toX9sHCk6";
        String roomTitle = remoteMessage.getData().get("roomTitle");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int j = Integer.parseInt(room.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, RoomChatActivity.class);
        Bundle bundle = new Bundle();
//        bundle.putString("userid", user);
        bundle.putString("Room_ID", room);
        bundle.putString("Room_Name", roomTitle);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.flexpad_fourth_actual_icon_foreground)
//                .setSmallIcon(Integer.parseInt(icon))
                .setWhen(System.currentTimeMillis())
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defaultSound)
                .setContentIntent(pendingIntent);
        NotificationManager noti = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        int i = 0;
        if (j > 0){
            i = j;
        }

        noti.notify(i, builder.build());
    }
}
