package io.github.aosn.ichimatsu;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback{
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int PERMISSION_REQUEST_CAMERA = 0;
    private View mLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mLayout = findViewById(R.id.content_main);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "他のアクテビティを立ち上げるよ！", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                Log.d(TAG, "start ");
                showCameraPreview();
            }
        });
    }

    private void showCameraPreview() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            // カメラ周りのパーミッションを持ってたらそのまま実行
            Snackbar.make(mLayout,
                    "Camera permission is available. Starting preview.",
                    Snackbar.LENGTH_SHORT).show();
            startCamera();
        } else {
            // パーミッションを持ってなかったら、取得するダイアログを表示
            requestCameraPermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            // Request for camera permission.
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted. Start camera preview Activity.
                Snackbar.make(mLayout, "Camera permission was granted. Starting preview.",
                        Snackbar.LENGTH_SHORT)
                        .show();
                startCamera();
            } else {
                // Permission request was denied.
                Snackbar.make(mLayout, "Camera permission request was denied.",
                        Snackbar.LENGTH_SHORT)
                        .show();
            }
        }
    }


    private void startCamera() {
        new IntentIntegrator(MainActivity.this).initiateScan();
   }

    /**
     * カメラのパーミッション要求箇所
     */
    private void requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            Snackbar.make(mLayout, "Camera access is required to display the camera preview.",
                    Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.CAMERA},
                            PERMISSION_REQUEST_CAMERA);
                }
            }).show();

        } else {
            Snackbar.make(mLayout,
                    "Permission is not available. Requesting camera permission.",
                    Snackbar.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CAMERA);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Zebra clossing のIntent が帰ってきた場合に読み込まれるメソッド

        Log.d(TAG, "onActivityResult Start");
        Log.d(TAG, "requestCode: " + requestCode + " resultCode: " + resultCode + " data: " + data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (intentResult == null) {
            Log.d(TAG, "Weird");
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }

        if (intentResult.getContents() == null) {
            // 戻るボタンをタップした場合
            Log.d(TAG, "Cancelled Scan");

        } else {
            // カメラで読み取った情報をラベルに表示
            Log.d(TAG, "Scanned");
            TextView textViewScannedData = (TextView) findViewById(R.id.textViewScannedData);
            textViewScannedData.setText(intentResult.getContents());

            String isbn_number = intentResult.getContents();
            openAddBookInBrowser(isbn_number);
        }
    }

    private void openAddBookInBrowser(String isbn_number) {
        // TODO URLをプロパティ化
        Uri uri = Uri.parse("http://vote.tasktoys.com/#!add-book/"+isbn_number);
        Intent i = new Intent(Intent.ACTION_VIEW,uri);
        startActivity(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        Log.d(TAG,"create");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Log.d(TAG,"select");

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
