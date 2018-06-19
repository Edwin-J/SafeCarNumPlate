package minjae.safecarnumplate.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Hashtable;

import minjae.safecarnumplate.R;

public class MakeQRActivity extends AppCompatActivity implements View.OnClickListener {

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
        PHONE_NUMBER = intent.getExtras().getString("phone_num");

        makeQRCode(PHONE_NUMBER);
        image_qr.setImageBitmap(QR_CODE);
    }

    private void makeQRCode(String context) {

        MultiFormatWriter writer = new MultiFormatWriter();
        try {
            Hashtable hints = new Hashtable();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");

            BitMatrix bitMatrix = writer.encode(context, BarcodeFormat.QR_CODE, 300, 300, hints);
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

        } catch (WriterException e) {
            e.printStackTrace();
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
        if (v == R.id.btn_share_qr) {
            String qrPath = MediaStore.Images.Media.insertImage(getContentResolver(), QR_CODE, "qr_Code", null);
            Uri qrUri = Uri.parse(qrPath);
            // share QR Code
            Intent intent = new intent(Intent.ACTION_SEND);
            intent.setType("image/png");
            intent.putExtra(Intent.EXTRA_STREAM, qrUri);
            startActivity(Intent.createChooser(intent, "공유하기!"));
        }
    }
}
