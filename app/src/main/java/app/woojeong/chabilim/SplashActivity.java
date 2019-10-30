package app.woojeong.chabilim;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.List;

/**
 * Created by User on 2017-09-18.
 */

public class SplashActivity extends AppCompatActivity {
    SharedPreferences pref;
    PermissionListener permissionlistener;

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            String url = "";
            Intent getIntent;
            try {
                // 카카오링크 parameter
                getIntent = getIntent();
                Uri uri = getIntent.getData();
                url = uri.getQueryParameter("url");
                Log.e("kakao_url", url);
            } catch (Exception e) {
                try {
                    // 푸시
                    getIntent = getIntent();
                    url = getIntent.getStringExtra("url");
                } catch (Exception e1) {
                }
            }
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            try {
                if (!url.equals("")) {
                    intent.putExtra("url", url);
                }
            } catch (Exception e) {

            }
            startActivity(intent);
            finish();
            return true;
        }
    });

    Handler changeImgHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            handler.sendEmptyMessageDelayed(1, 1000);
            return true;
        }
    });

    Thread tokenThread = new Thread() {
        @Override
        public void run() {
            while (true) {
                try {
                    if (!FirebaseInstanceId.getInstance().getToken().equals("")) {
                        changeImgHandler.sendEmptyMessageDelayed(1, 1000);
                        break;
                    }
                } catch (Exception e) {

                }
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isTaskRoot()
                && getIntent().hasCategory(Intent.CATEGORY_LAUNCHER)
                && Intent.ACTION_MAIN.equals(getIntent().getAction())) {
            finish();
            return;
        }
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
        pref = getSharedPreferences("pref", MODE_PRIVATE);


        permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        if (pref.getBoolean("first", false)) {
                            changeImgHandler.sendEmptyMessageDelayed(1, 1500);
                        } else {
                            tokenThread.start();
                        }
                    }
                }).start();
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
            }
        };

        if (isConnected()) {
            TedPermission.with(SplashActivity.this)
                    .setPermissionListener(permissionlistener)
                    .setDeniedMessage("권한을 허용하지 않으면\n서비스를 이용할 수 없습니다.\n설정 > 권한")
                    .setPermissions(android.Manifest.permission.CAMERA, android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.INTERNET)
                    .check();

        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("인터넷 연결을 확인 해 주세요.")
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    })
                    .setCancelable(false)
                    .create()
                    .show();
        }

    }


    //인터넷 연결상태 확인
    public boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        System.exit(0);
    }
}
