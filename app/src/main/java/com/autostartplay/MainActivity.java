package com.autostartplay;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback{
    CountDownTimer timer;
    TextView textViewFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView textViewTimeout = findViewById(R.id.Timeout);
        TextView textViewPermission = findViewById(R.id.Permission);
        textViewFilePath = findViewById(R.id.FilePath);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_BOOT_COMPLETED) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_BOOT_COMPLETED}, 0);
            Toast.makeText(this, "receive Boot Completed DENIED", Toast.LENGTH_SHORT).show();
        }
        if (!Settings.canDrawOverlays(this)) {
            textViewPermission.setText("System alert window DENIED - won't auto start");
        }

        // if file exist, wait 5s then open it:
        //to retrieve it back
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        String your_value = prefs.getString("FilePath", null);
        textViewFilePath.setText(your_value);
        if (your_value != null) {
            timer = new CountDownTimer(6000, 1000) {
                public void onTick(long millisUntilFinished) {
                    textViewTimeout.setText(String.format("%d", millisUntilFinished / 1000));
                }

                public void onFinish() {
                    openApp(Uri.parse(your_value));
                }
            }.start();
        } else {
            selectFile();
        }
    }

    //if click on button stop timer and open file
    public void newFile(View view) {
        timer.cancel();
        selectFile();
    }

    public void selectFile(){
        // launch explorer to select file
        mGetContent.launch("*/*");
    }

    // GetContent creates an ActivityResultLauncher<String> to allow you to pass
    // in the mime type you'd like to allow the user to select
    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    // save path for next start
                    SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
                    editor.putString("FilePath", uri.toString());
                    editor.apply();
                    // display
                    textViewFilePath.setText(uri.toString());
                    //open file
                    openApp(uri);
                }
            });

    public void openApp(Uri selectedFile) {
        Intent videoPlay = new Intent(Intent.ACTION_VIEW);
        videoPlay.setDataAndType(selectedFile, "video/*");
        startActivity(videoPlay);
    }
}
