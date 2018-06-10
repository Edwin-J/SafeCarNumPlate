package minjae.safecarnumplate;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ScanActivity extends AppCompatActivity {

    private boolean CALL_PERMISSION;

    public String PHONE_NUMBER = "01040849460";
    public String CALL_TIME;

    private IntentIntegrator scanner;

    private FileOutputStream timeOutputStream;
    private FileOutputStream numOutputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED)
            CALL_PERMISSION = true;

        if (!CALL_PERMISSION)
            //make permission grant
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 1000);

        // show scan screen
        IntentIntegrator scanner = new IntentIntegrator(this);
        scanner.initiateScan();


    }

    @Override
    protected void onActivityResult(final int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // get Phone Number
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        PHONE_NUMBER = result.getContents();

        // call
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permissionResult = checkSelfPermission(Manifest.permission.CALL_PHONE);
            if (permissionResult == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(getApplicationContext(), "전화 걸기 권한을 강제로 허용합니다.", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(ScanActivity.this, new String[] { Manifest.permission.CALL_PHONE }, 1000);
            }
            else {
                ActivityCompat.requestPermissions(ScanActivity.this, new String[] { Manifest.permission.CALL_PHONE }, 1000);
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + PHONE_NUMBER));
                startActivity(intent);
                saveCallLog();
                finish();
            }
        }

        else {
            ActivityCompat.requestPermissions(ScanActivity.this, new String[] { Manifest.permission.CALL_PHONE }, 1000);
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + PHONE_NUMBER));
            startActivity(intent);
            saveCallLog();
            finish();
        }

    }

    private void saveCallLog() {
        // make internal directory
        String dirPath = getFilesDir().getAbsolutePath();
        File file = new File(dirPath);
        if (!file.exists())
            file.mkdirs();

        // save time, number information
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        CALL_TIME = simpleDateFormat.format(date);
        File file_call_time = new File(dirPath + "/log_call_time.txt");
        File file_call_num = new File(dirPath + "/log_call_num.txt");
        try {
            timeOutputStream = openFileOutput("log_call_time.txt", Context.MODE_APPEND);
            timeOutputStream.write((CALL_TIME + "\r\n").getBytes());
            timeOutputStream.close();

            numOutputStream = openFileOutput("log_call_num.txt", Context.MODE_APPEND);
            numOutputStream.write((PHONE_NUMBER + "\r\n").getBytes());
            numOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1000) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + PHONE_NUMBER));
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    startActivity(intent);
                    saveCallLog();
                    finish();
                }
            }
        }
    }
}