package minjae.safecarnumplate;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.zxing.client.android.Intents;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ListView list_call;
    private CallLogAdapter adapter;

    private Toolbar toolbar;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;

    private FileInputStream timeInputStream;
    private FileInputStream numInputStream;

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

        adapter = new CallLogAdapter();
        list_call = (ListView) findViewById(R.id.list_call);
        takeCallLog();
        list_call.setAdapter(adapter);

        Intent intent = new Intent(this, ScanActivity.class);
        startActivity(intent);

    }

    private void takeCallLog() {
        String dirPath = getFilesDir().getAbsolutePath();
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
                adapter.addLog(ContextCompat.getDrawable(this, R.mipmap.ic_launcher_round), call_num, call_time);
                call_time = timeReader.readLine();
                call_num = numReader.readLine();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_scan) {
            Intent intent = new Intent(getApplicationContext(), ScanActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_make) {
            makeQRDialog();
        } else if (id == R.id.nav_log) {

        } else if (id == R.id.nav_share) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/png");
            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("C:\\Users\\Minjae\\Desktop\\SafeCarNumPlate\\app\\src\\main\\res\\drawable\\ic_launcher_background.xml"));
            intent.setPackage("com.kakao.talk");
        } else if (id == R.id.nav_send) {

        }

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void makeQRDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("QR코드 생성하기");
        builder.setMessage("전화번호를 입력하세요.");
        final EditText editText = new EditText(MainActivity.this);
        builder.setView(editText);
        builder.setPositiveButton("생성!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(getApplicationContext(), MakeQRActivity.class);
                intent.putExtra("phone_num", editText.getText().toString());
                startActivity(intent);
            }
        });
        builder.setNegativeButton("취소", null);
        builder.show();
    }
}
