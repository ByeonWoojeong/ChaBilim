package app.woojeong.chabilim;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import java.io.File;

import app.woojeong.chabilim.R;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    long backKeyPressedTime = 0;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    String token;
    WebView webView;
    WebView childView;
    ValueCallback mFilePathCallback;
    Uri uri;
    int childcnt = 0;

    FrameLayout back_con, home_con, share_con, copy_con;
    ImageView back, home, share, copy;
    TwoBtnDialog twoBtnDialog;
    AddImageDialog addImageDialog;


    public ValueCallback<Uri> filePathCallbackNormal;
    public ValueCallback<Uri[]> filePathCallbackLollipop;
    public final static int FILECHOOSER_NORMAL_REQ_CODE = 2001;
    public final static int FILECHOOSER_LOLLIPOP_REQ_CODE = 2002;
    private Uri cameraImageUri = null;


    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = getSharedPreferences("pref", MODE_PRIVATE);
        editor = preferences.edit();

        token = FirebaseInstanceId.getInstance().getToken();

        back_con = findViewById(R.id.back_con);
        home_con = findViewById(R.id.home_con);
        share_con = findViewById(R.id.share_con);
        copy_con = findViewById(R.id.copy_con);

        back = findViewById(R.id.back);
        home = findViewById(R.id.home);
        share = findViewById(R.id.share);
        copy = findViewById(R.id.copy);

        webView = (WebView) findViewById(R.id.webview);
        webView.getSettings().setDatabaseEnabled(true);
        File dir = getCacheDir();
        if (!dir.exists()) {
            dir.mkdirs();
        }

        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);

        webView.getSettings().setAppCachePath(dir.getPath());
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setSupportZoom(false);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSupportMultipleWindows(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);

        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowContentAccess(true);


        webView.setWebViewClient(new MyWebViewClient());
        webView.setWebChromeClient(new MyWebChromeClient());


        Intent intent = getIntent();
        String url = intent.getStringExtra("URL");
        if (url == null) {
            webView.loadUrl(getString(R.string.url));  //원하는 사이트의 주소
        } else {
            webView.loadUrl(url);
        }

        share_con.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share.callOnClick();
            }
        });
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String subject = "차빌림";
                String text = webView.getUrl();

                Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                intent.putExtra(Intent.EXTRA_TEXT, text);
                Intent chooser = Intent.createChooser(intent, "차빌림 공유하기");
                startActivity(chooser);
            }
        });

        back_con.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back.callOnClick();
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 뒤로가기
                if (webView.canGoBack()) {
                    webView.goBack();
                }
            }
        });
        home_con.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                home.callOnClick();
            }
        });
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.loadUrl(getString(R.string.url));
            }
        });

        copy_con.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copy.callOnClick();
            }
        });
        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean isMessage = false;
                isMessage = preferences.getBoolean("message", false);
                if (isMessage) {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("url", webView.getUrl());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(MainActivity.this, "링크를 복사하였습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    twoBtnDialog = new TwoBtnDialog(MainActivity.this, "링크를 복사하였습니다.");
                    twoBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    twoBtnDialog.setCancelable(false);
                    twoBtnDialog.show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
//            mFilePathCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
//            mFilePathCallback = null;
//        } else {
//            mFilePathCallback.onReceiveValue(null);
//        }



//        switch (requestCode) {
//            case 111:
//                if (resultCode == RESULT_CANCELED) {
//                    mFilePathCallback.onReceiveValue(null);
//                } else {
//
//                    if (Build.VERSION.SDK_INT < 21) {
//                        uri = data.getData();
//                    }
//                    mFilePathCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
//                    mFilePathCallback = null;
//                }
//                break;
//            case 222:
//                if (resultCode == RESULT_CANCELED) {
//                    mFilePathCallback.onReceiveValue(null);
//                } else {
//                    mFilePathCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
//                    mFilePathCallback = null;
//                }
//                break;
//            default:
//                break;
//        }





        switch (requestCode)
        {
            case FILECHOOSER_NORMAL_REQ_CODE:
                if (resultCode == RESULT_OK)
                {
                    if (filePathCallbackNormal == null) return;
                    Uri result = (data == null || resultCode != RESULT_OK) ? null : data.getData();
                    //  onReceiveValue 로 파일을 전송한다.
                    filePathCallbackNormal.onReceiveValue(result);
                    filePathCallbackNormal = null;
                }
                break;
            case FILECHOOSER_LOLLIPOP_REQ_CODE:
                if (resultCode == RESULT_OK)
                {
                    if (filePathCallbackLollipop == null) return;
                    if (data == null)
                        data = new Intent();
                    if (data.getData() == null)
                        data.setData(cameraImageUri);

                    filePathCallbackLollipop.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
                    filePathCallbackLollipop = null;
                }
                else
                {
                    if (filePathCallbackLollipop != null)
                    {   //  resultCode에 RESULT_OK가 들어오지 않으면 null 처리하지 한다.(이렇게 하지 않으면 다음부터 input 태그를 클릭해도 반응하지 않음)
                        filePathCallbackLollipop.onReceiveValue(null);
                        filePathCallbackLollipop = null;
                    }

                    if (filePathCallbackNormal != null)
                    {
                        filePathCallbackNormal.onReceiveValue(null);
                        filePathCallbackNormal = null;
                    }
                }
                break;
            default:

                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (childcnt > 0) {
            childcnt = 0;
            webView.removeAllViews();
        } else if (webView.canGoBack()) {
            webView.goBack();
        } else {
            if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
                backKeyPressedTime = System.currentTimeMillis();
                Toast.makeText(MainActivity.this, "\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
            } else if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
                setResult(999);
                finish();
            }
        }
    }

    class MyWebViewClient extends WebViewClient {

        public static final String INTENT_PROTOCOL_START = "intent:";
        public static final String INTENT_PROTOCOL_INTENT = "#Intent;";
        public static final String INTENT_PROTOCOL_END = ";end;";
        public static final String GOOGLE_PLAY_STORE_PREFIX = "market://details?id=";

        public boolean doFallback(WebView view, Intent parsedIntent) {
            Log.i(TAG, " doFallback ");
            if (parsedIntent == null) {
                return false;
            }
            String fallbackUrl = parsedIntent.getStringExtra("browser_fallback_url");
            if (fallbackUrl != null) {
                view.loadUrl(fallbackUrl);
                return true;
            }

            final String packageName = parsedIntent.getPackage();
            if (packageName != null) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("설치 후 사용하시겠습니까?")
                        .setCancelable(false)
                        .setPositiveButton("네", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();

                return true;
            }
            return false;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Log.i(TAG, " onPageStarted");
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Log.i(TAG, " onPageFinished");
        }


        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.i(TAG, " shouldOverrideUrlLoading 1 " + url);

            if (url.startsWith("sms:")) {
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
                startActivity(intent);
            } else if (url.startsWith("http://") || url.startsWith("https://")) {
                return false;
            } else if (url.startsWith("intent:")) {
                Log.i(TAG, " intent");
                final int customUrlStartIndex = INTENT_PROTOCOL_START.length();
                final int customUrlEndIndex = url.indexOf(INTENT_PROTOCOL_INTENT);
                if (customUrlEndIndex < 0) {
                    return false;
                } else {
                    final String customUrl = url.substring(customUrlStartIndex, customUrlEndIndex);
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(customUrl)));
                    } catch (ActivityNotFoundException e) {
                        final int packageStartIndex = customUrlEndIndex + INTENT_PROTOCOL_INTENT.length();
                        final int packageEndIndex = url.indexOf(INTENT_PROTOCOL_END);

                        final String packageName = url.substring(packageStartIndex, packageEndIndex < 0 ? url.length() : packageEndIndex);
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(GOOGLE_PLAY_STORE_PREFIX + packageName)));
                    }
                    return true;
                }
            } else {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    if (url.endsWith(".mp4") || url.endsWith(".swf")) {
                        intent.setDataAndType(Uri.parse(url), "video/*");
                    }
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                    if (url.startsWith("intent:") && webView.canGoBack()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            Intent intent = null;
                            try {
                                intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                                startActivity(intent);
                            } catch (Exception e1) {
                                e.printStackTrace();
                                if (webView.canGoBack()) {
                                    webView.clearHistory();
                                }
                                return doFallback(view, intent);
                            }
                        } else {
                            Intent intent = null;
                            try {
                                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                startActivity(intent);
                            } catch (ActivityNotFoundException e2) {
                                e.printStackTrace();
                                if (webView.canGoBack()) {
                                    webView.clearHistory();
                                }
                                return doFallback(view, intent);
                            }
                        }
                        return true;
                    } else if (url.startsWith("market://details?id=")) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                    }
                }
            }
            return true;
        }
    }

    class MyWebChromeClient extends WebChromeClient {


        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback filePathCallback, FileChooserParams fileChooserParams) {
            Log.i(TAG, " onShowFileChooser");
            mFilePathCallback = filePathCallback;
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(intent, 0);

            return true;
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("알림")
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok,
                            new AlertDialog.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    result.confirm();
                                }
                            })
                    .setCancelable(false)
                    .create()
                    .show();
            return true;
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("알림")
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    result.confirm();
                                }
                            })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    result.cancel();
                                }
                            })
                    .setCancelable(false)
                    .create()
                    .show();
            return true;
        }

        @Override
        public boolean onCreateWindow(WebView view, final boolean dialog, final boolean userGesture, final Message resultMsg) {
            Log.i(TAG, " onCreateWindow");

            childView = new WebView(MainActivity.this);
            childView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            WebSettings webSettings = childView.getSettings();
            webSettings.setDomStorageEnabled(true);
            webSettings.setAllowFileAccess(true);
            webSettings.setAllowContentAccess(true);
            webSettings.setLoadWithOverviewMode(true);

            webSettings.setJavaScriptEnabled(true);
            webSettings.setSupportMultipleWindows(true);
            webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

            webSettings.setDefaultZoom(WebSettings.ZoomDensity.FAR);
            webSettings.setUseWideViewPort(true);
            webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
            childView.setWebChromeClient(new WebChromeClient() {

                @Override
                public void onCloseWindow(WebView window) {
                    Log.i(TAG, " onCloseWindow");

                    window.setVisibility(View.GONE);
                    childcnt--;
                    webView.removeView(window);
                }

                @Override
                public boolean onShowFileChooser(WebView webView, ValueCallback filePathCallback, FileChooserParams fileChooserParams) {
                    Log.i(TAG, " ");
//                    mFilePathCallback =onShowFileChooser filePathCallback;
//                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                    intent.addCategory(Intent.CATEGORY_OPENABLE);
//                    intent.setType("image/*");
//                    startActivityForResult(intent, 0);

//                    addImageDialog = new AddImageDialog(MainActivity.this, filePathCallback);
//                    addImageDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//                    addImageDialog.setCancelable(false);
//                    addImageDialog.show();
//                    return true;

                    // Callback 초기화 (중요!)
                    if (filePathCallbackLollipop != null) {
                        filePathCallbackLollipop.onReceiveValue(null);
                        filePathCallbackLollipop = null;
                    }
                    filePathCallbackLollipop = filePathCallback;

                    boolean isCapture = fileChooserParams.isCaptureEnabled();
                    runCamera(isCapture);
                    return true;
                }
            });
            childView.setWebViewClient(new WebViewClient() {

                public boolean doFallback(WebView view, Intent parsedIntent) {
                    if (parsedIntent == null) {
                        return false;
                    }
                    String fallbackUrl = parsedIntent.getStringExtra("browser_fallback_url");
                    if (fallbackUrl != null) {
                        view.loadUrl(fallbackUrl);
                        return true;
                    }

                    final String packageName = parsedIntent.getPackage();
                    if (packageName != null) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setMessage("설치 후 사용하시겠습니까?")
                                .setCancelable(false)
                                .setPositiveButton("네", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
                                    }
                                })
                                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();

                        return true;
                    }
                    return false;
                }

                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, final String url) {
                    Log.i(TAG, " shouldOverrideUrlLoading 2 " + url);

                    if (url.startsWith(getString(R.string.url))) {
                        Log.i(TAG, " 1");
                        webView.removeAllViews();
                        childcnt++;
                        webView.addView(childView);
                        childView.loadUrl(url);
                        Handler delayHandler = new Handler();
                        delayHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(webView.getWindowToken(), 0);
                                webView.setScrollY(0);
                            }
                        }, 500);
                    } else if (url.startsWith("http://") || url.startsWith("https://")) {
                        Log.i(TAG, " 2");

                        if (url.startsWith("https://play.google.com/store/apps/details?")) {
                            Log.i(TAG, " 2-1");
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            startActivity(intent);
//                        return false;
                        } else if (url.contains("twitter") || url.contains("kakao") || url.contains("naver") || url.contains("facebook") || url.contains("band")) {

                            Log.i(TAG, " 2-2");
                            Intent intent = new Intent(MainActivity.this, LinkActivity.class);
                            intent.putExtra("link", url);
                            startActivity(intent);
                        } else {
                            Log.i(TAG, " 2-3");
                            Intent intent = new Intent(MainActivity.this, LinkActivity.class);
                            intent.putExtra("link", url);
                            startActivity(intent);
                        }
                    } else {
                        Log.i(TAG, "  3 " + url);
                    }
                    return true;
                }
            });

            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(childView);
            resultMsg.sendToTarget();
            return true;
        }
    }

    // 카메라 기능 구현
    private void runCamera(boolean _isCapture) {
        Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        File path = getFilesDir();
        File file = new File(path, "chabilim.png"); // sample.png 는 카메라로 찍었을 때 저장될 파일명이므로 사용자 마음대로
        // File 객체의 URI 를 얻는다.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            String strpa = getApplicationContext().getPackageName();
            cameraImageUri = FileProvider.getUriForFile(this, strpa + ".fileprovider", file);
        } else {
            cameraImageUri = Uri.fromFile(file);
        }
        intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);

        if (!_isCapture) { // 선택팝업 카메라, 갤러리 둘다 띄우고 싶을 때
            Intent pickIntent = new Intent(Intent.ACTION_PICK);
            pickIntent.setType(MediaStore.Images.Media.CONTENT_TYPE);
            pickIntent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            String pickTitle = "사진 가져올 방법을 선택하세요.";
            Intent chooserIntent = Intent.createChooser(pickIntent, pickTitle);

            // 카메라 intent 포함시키기..
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Parcelable[]{intentCamera});
            startActivityForResult(chooserIntent, FILECHOOSER_LOLLIPOP_REQ_CODE);
        } else {// 바로 카메라 실행..
            startActivityForResult(intentCamera, FILECHOOSER_LOLLIPOP_REQ_CODE);
        }
    }


    public class TwoBtnDialog extends Dialog {
        TwoBtnDialog twoBtnDialog = this;
        Context context;

        public TwoBtnDialog(final Context context, final String content) {
            super(context);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.dialog_two_btn);
            getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            this.context = context;
            TextView title1 = (TextView) findViewById(R.id.title1);
            TextView btn1 = (TextView) findViewById(R.id.btn1);
            TextView btn2 = (TextView) findViewById(R.id.btn2);
            title1.setText(content);
            btn1.setText("다음부터 이 메세지를\n표시하지 않음");
            btn2.setText("확인");
            btn1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editor.putBoolean("message", true);
                    editor.commit();

                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("url", webView.getUrl());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(MainActivity.this, "링크를 복사하였습니다.", Toast.LENGTH_SHORT).show();

                    twoBtnDialog.dismiss();
                }
            });
            btn2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("url", webView.getUrl());
                    clipboard.setPrimaryClip(clip);
                    twoBtnDialog.dismiss();
                }
            });
        }
    }

    public class AddImageDialog extends Dialog {
        AddImageDialog addImageDialog = this;
        Context context;

        public AddImageDialog(final Context context, final ValueCallback valueCallback) {
            super(context);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.dialog_add_img);
            getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            this.context = context;
            TextView title1 = (TextView) findViewById(R.id.title1);
            TextView btn1 = (TextView) findViewById(R.id.btn1);
            TextView btn2 = (TextView) findViewById(R.id.btn2);
            btn1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //카메라
                    addImageDialog.dismiss();

                    mFilePathCallback = valueCallback;
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, 111);


                }
            });
            btn2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //갤러리
                    addImageDialog.dismiss();
                    mFilePathCallback = valueCallback;
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    startActivityForResult(intent, 222);
                }
            });
        }
    }
}
