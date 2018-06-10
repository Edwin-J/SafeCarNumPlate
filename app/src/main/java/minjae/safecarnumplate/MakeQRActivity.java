package minjae.safecarnumplate;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.Hashtable;

public class MakeQRActivity extends AppCompatActivity {

    public String PHONE_NUMBER;

    public Bitmap QR_CODE = null;

    private ImageView image_qr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_qr);

        image_qr = (ImageView) findViewById(R.id.result_qr);

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

        } catch (WriterException e) {
            e.printStackTrace();
        }

    }
}
