package minjae.safecarnumplate.Activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Base64;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import minjae.safecarnumplate.CallLog.CallLogAdapter;
import minjae.safecarnumplate.R;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener, View.OnClickListener {

    public String PHONE_NUMBER;

    private ListView list_call;
    private CallLogAdapter adapter;

    private Toolbar toolbar;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;

    private File file_call_time;
    private File file_call_num;
    private FileInputStream timeInputStream;
    private FileInputStream numInputStream;

    private ImageView menu_qr;
    private TextView menu_num;

    private Bitmap QR_CODE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        menu_qr = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.menu_qr);
        menu_qr.setOnClickListener(this);
        menu_num = (TextView) navigationView.getHeaderView(0).findViewById(R.id.menu_num);

        try {
            setMenuImage();
            setMenuNum();
        } catch (NullPointerException ne) {
            Log.d("NullPointer Error", "NullPointerException");
        }


        adapter = new CallLogAdapter();
        list_call = (ListView) findViewById(R.id.list_call);
        takeCallLog();
        list_call.setAdapter(adapter);
        list_call.setOnItemClickListener(this);
        list_call.setOnItemLongClickListener(this);

        Intent intent = new Intent(this, ScanActivity.class);
        //startActivity(intent);

    }

    // set menu after created QR
    @Override
    protected void onPostResume() {
        super.onPostResume();
        setMenuImage();
        setMenuNum();
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(getApplicationContext(), MakeQRActivity.class);
        startActivity(intent);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            PHONE_NUMBER = adapter.getNum(position);
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                    == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + PHONE_NUMBER));
                startActivity(intent);
                saveCallLog();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[] { Manifest.permission.CALL_PHONE }, 44);
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "알 수 없는 전화번호입니다.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("기록 삭제")
                .setMessage("해당 통화 기록을 삭제하시겠습니까?")
                .setNegativeButton("취소", null)
                .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeCallLog(position);
                        dialog.dismiss();
                    }
                })
                .show();

        return false;
    }

    private void removeCallLog(int position) {
        // remove from adapter
        adapter.delLog(position);
        adapter.notifyDataSetChanged();

        // remove from log_call txt files - not completed
        FileOutputStream numOutputStream;
        FileOutputStream timeOutputStream;
        FileInputStream numInputStream;
        FileInputStream timeInputStream;

        String logs_num = "";
        String logs_time = "";

        try {
            timeInputStream = openFileInput("log_call_time.txt");
            numInputStream = openFileInput("log_call_num.txt");
            BufferedReader timeReader = new BufferedReader(new InputStreamReader(timeInputStream));
            BufferedReader numReader = new BufferedReader(new InputStreamReader(numInputStream));


            String line_num;
            String line_time;
            for (int i = 0; i < position; i++) {
                line_num = numReader.readLine();
                line_time = timeReader.readLine();
                logs_num += (line_num + "\r\n");
                logs_time += (line_time + "\r\n");
            }

            while ( ( ( line_num = numReader.readLine() ) != null) && ( ( line_time = timeReader.readLine() ) != null) ) {
                logs_num += (line_num + "\r\n");
                logs_time += (line_time + "\r\n");
            }

            numOutputStream = openFileOutput("log_call_num.txt", Context.MODE_PRIVATE);
            numOutputStream.write((logs_num).getBytes());
            numOutputStream.close();

            timeOutputStream = openFileOutput("log_call_time.txt", Context.MODE_PRIVATE);
            timeOutputStream.write((logs_time).getBytes());
            timeOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void takeCallLog() {
        String call_time;
        String call_num;
        try {
            timeInputStream = openFileInput("log_call_time.txt");
            numInputStream = openFileInput("log_call_num.txt");
            BufferedReader timeReader = new BufferedReader(new InputStreamReader(timeInputStream));
            BufferedReader numReader = new BufferedReader(new InputStreamReader(numInputStream));
            call_time = timeReader.readLine();
            call_num = numReader.readLine();
            while (call_time != null || call_num != null) {
                // add to list
                adapter.addLog(
                        ContextCompat.getDrawable(this, R.drawable.ic_scan_qr),
                        call_num, call_time
                );
                call_time = timeReader.readLine();
                call_num = numReader.readLine();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void makeQRDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("QR코드 생성하기");
        builder.setMessage("전화번호를 입력하세요.");

        final EditText editText = new EditText(MainActivity.this);
        // set inputType="number"
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        // set maxLength="12"
        InputFilter[] inputFilters = new InputFilter[1];
        inputFilters[0] = new InputFilter.LengthFilter(12);
        editText.setFilters(inputFilters);
        // set multiLine="true"
        editText.setSingleLine();

        builder.setView(editText)
                .setPositiveButton("생성!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(getApplicationContext(), MakeQRActivity.class);
                        intent.putExtra("phone_num", editText.getText().toString());
                        startActivity(intent);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void setMenuNum() {
        SharedPreferences pref = getSharedPreferences("pref_num", MODE_PRIVATE);
        String string_num = pref.getString("phone_num", "");
        Log.d("num", string_num);
        menu_num.setText(string_num);
    }

    private void setMenuImage() {
        SharedPreferences pref = getSharedPreferences("pref_qr", MODE_PRIVATE);
        String string_qr = pref.getString("QR_Code", "");
        QR_CODE = StringToBitMap(string_qr);
        Log.d("string", string_qr);
        menu_qr.setImageBitmap(QR_CODE);
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
        String CALL_TIME = simpleDateFormat.format(date);
        File file_call_time = new File(dirPath + "/log_call_time.txt");
        File file_call_num = new File(dirPath + "/log_call_num.txt");
        try {
            FileOutputStream timeOutputStream = openFileOutput("log_call_time.txt", Context.MODE_APPEND);
            timeOutputStream.write((CALL_TIME + "\r\n").getBytes());
            timeOutputStream.close();

            FileOutputStream numOutputStream = openFileOutput("log_call_num.txt", Context.MODE_APPEND);
            numOutputStream.write((PHONE_NUMBER + "\r\n").getBytes());
            numOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Bitmap StringToBitMap(String string) {
        try {
            byte[] bytes = Base64.decode(string, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // refresh list
        if (id == R.id.menu_refresh) {
            adapter.clear();
            takeCallLog();
            adapter.notifyDataSetChanged();
        } else if (id == R.id.menu_clear) {
//            adapter.clear();
//            file_call_time.delete();
//            file_call_num.delete();
//            takeCallLog();
//            adapter.notifyDataSetChanged();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_scan) {
            Intent intent = new Intent(getApplicationContext(), ScanActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_make) {
            makeQRDialog();
        } else if (id == R.id.nav_share) {
            // share app
        } else if (id == R.id.nav_send) {
            ShareQR();
        }

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void ShareQR() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/*");
            Log.e("TAG", "ShareQR: " + (QR_CODE == null));
            String dataUrl = MediaStore.Images.Media.insertImage(getContentResolver(), QR_CODE, "QR Code", "");
            intent.putExtra(Intent.EXTRA_STREAM, dataUrl);
            startActivity(Intent.createChooser(intent, "공유하기"));
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
                intent.setType("image/*");

                String dataUrl = MediaStore.Images.Media.insertImage(getContentResolver(), QR_CODE, "QR Code", "");
                intent.putExtra(Intent.EXTRA_STREAM, dataUrl);
                startActivity(Intent.createChooser(intent, "공유하기"));
            } else {
                Toast.makeText(getApplicationContext(), R.string.permission_denied, Toast.LENGTH_LONG).show();
            }
        }
    }
}
