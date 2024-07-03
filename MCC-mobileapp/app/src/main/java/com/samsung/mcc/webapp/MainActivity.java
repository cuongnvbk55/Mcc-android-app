package com.samsung.mcc.webapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.strictmode.ContentUriWithoutPermissionViolation;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private final int PERMISSION_CAMERA_REQUEST_CODE = 1000;
    private final String PREF_WEBVIEW_URL = "pref_webview_url";
    private WebView webView;

    private AlertDialog alertDialog;

    private MyPreferences myPreferences;

    private static final String TAG = MainActivity.class.getSimpleName();

    @SuppressLint({"SetJavaScriptEnabled", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = (WebView) findViewById(R.id.os_view);
        myPreferences = MyPreferences.getInstance(this);
        showDialogPermissionRequest();

    }


    private void initWebview() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowContentAccess(true);
        //webSettings.setAllowFileAccess(true);
        //webSettings.setMediaPlaybackRequiresUserGesture(true);
        webSettings.setMixedContentMode(0);

        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        webView.setWebViewClient(new Callback());
        webView.loadUrl(myPreferences.getStr(PREF_WEBVIEW_URL, ""));
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                request.grant(request.getResources());
            }
        });
    }

    private void showDialogConfigUrl() {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        String oldUrl = myPreferences.getStr(PREF_WEBVIEW_URL, "");
        input.setText(oldUrl);

        alertDialog = new AlertDialog.Builder(this)
                .setTitle("Config")
                .setMessage("Vui lòng cài đặt Url của server.")
                .setCancelable(false)
                .setView(input)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String newUrl = input.getText().toString().trim();
                        myPreferences.setStr(PREF_WEBVIEW_URL, newUrl);
                        initWebview();
                    }
                })
                .show();
    }

    private void showDialogPermissionRequest() {
        if (!checkPermission()) {
            alertDialog = new AlertDialog.Builder(this)
                    .setTitle("Permission")
                    .setMessage("MCC Scanner cần quyền truy cập Camera, vui lòng cấp quyền.")
                    .setCancelable(false)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA_REQUEST_CODE);
                        }
                    })
                    .show();
        } else {
            showWebview();
        }
    }

    /*-- callback reporting if error occurs --*/
    public class Callback extends WebViewClient {
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Toast.makeText(getApplicationContext(), String.format("Failed: %s", description), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CAMERA_REQUEST_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                showDialogPermissionRequest();
            } else {
                showWebview();
            }
        }
    }

    private void showWebview() {
        if (checkWebviewUrl()) {
            initWebview();
        } else {
            showDialogConfigUrl();
        }
    }


    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= 23 && (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
            return false;
        } else {
            return true;
        }
    }

    private boolean checkWebviewUrl() {
        String url = myPreferences.getStr(PREF_WEBVIEW_URL, "");
        if (url == null || url.length() == 0) {
            return false;
        }
        return true;
    }

    /*-- back/down key handling --*/
    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (webView.canGoBack()) {
                    webView.goBack();
                } else {
                    finish();
                }
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        webView.destroy();
        if (alertDialog != null)
            alertDialog.dismiss();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_setting) {
            showDialogConfigUrl();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return super.onCreateOptionsMenu(menu);
    }
}
