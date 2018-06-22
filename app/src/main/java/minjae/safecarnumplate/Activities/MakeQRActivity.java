package minjae.safecarnumplate.Activities;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Hashtable;

import minjae.safecarnumplate.R;

public class MakeQRActivity extends Activity implements View.OnClickListener {

    public String PHONE_NUMBER;
    public Bitmap QR_CODE = null;

    private ImageView image_qr;
    private Button btn_share;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_qr);

        image_qr = (ImageView) findViewById(R.id.result_qr);
        btn_share = (Button) findViewById(R.id.btn_share_qr);
        btn_share.setOnClickListener(this);

        Intent intent = getIntent();
        try {
            PHONE_NUMBER = intent.getExtras().getString("phone_num");
            makeQRCode(PHONE_NUMBER);
            image_qr.setImageBitmap(QR_CODE);
        } catch (NullPointerException ne) {
            SharedPreferences pref = getSharedPreferences("pref_qr", MODE_PRIVATE);
            String string_qr = pref.getString("QR_Code", "");
            Bitmap bitmap_qr = StringToBitMap(string_qr);
            Log.d("string", string_qr);
            image_qr.setImageBitmap(bitmap_qr);
        }

    }

    private void makeQRCode(String context) {

        MultiFormatWriter writer = new MultiFormatWriter();
        try {
            Hashtable hints = new Hashtable();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");

            BitMatrix bitMatrix = writer.encode(context, BarcodeFormat.QR_CODE, 1500, 1500, hints);
            BarcodeEncoder encoder = new BarcodeEncoder();
            QR_CODE = encoder.createBitmap(bitMatrix);

            // save QR Code to SharedPreferences
            String string_qr = BitmapToString(QR_CODE);
            SharedPreferences pref_qr = getSharedPreferences("pref_qr", MODE_PRIVATE);
            SharedPreferences.Editor editor_qr = pref_qr.edit();
            editor_qr.putString("QR_Code", string_qr);
            editor_qr.apply();
            // save Phone Num to SharedPreferences
            SharedPreferences pref_num = getSharedPreferences("pref_num", MODE_PRIVATE);
            SharedPreferences.Editor editor_num = pref_num.edit();
            editor_num.putString("phone_num", PHONE_NUMBER);
            editor_num.apply();

            Log.d("Make QR Code", "QR Code is successfully created!");
        } catch (WriterException e) {
            e.printStackTrace();
        }

    }

    private Bitmap StringToBitMap(String string) {
        try {
            byte[] bytes = android.util.Base64.decode(string, android.util.Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }

    private String BitmapToString(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        byte[] bytes = outputStream.toByteArray();
        String string = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT);
        return string;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_share_qr)
            ShareQR();
    }

    private void ShareQR() {
        File dirName = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/Download");
        if (!dirName.exists()){
            dirName.mkdir();
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/*");
            String dataUrl = MediaStore.Images.Media.insertImage(getContentResolver(), QR_CODE, "QR Code", "");
            intent.putExtra(Intent.EXTRA_STREAM, dataUrl);
            startActivity(Intent.createChooser(intent, "공유하기"));
            finish();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, 44);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 44) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                    == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("image/jpg");
                String dataUrl = MediaStore.Images.Media.insertImage(getContentResolver(), QR_CODE, "QR Code", "");
                intent.putExtra(Intent.EXTRA_STREAM, dataUrl);
                startActivity(Intent.createChooser(intent, "공유하기"));
            } else {
                Toast.makeText(getApplicationContext(), R.string.permission_denied, Toast.LENGTH_LONG).show();
            }


        }
    }
}
