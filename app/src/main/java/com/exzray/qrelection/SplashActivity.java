package com.exzray.qrelection;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

public class SplashActivity extends AppCompatActivity {

    // view
    private ImageView mv_logo;
    private Button mv_scan;

    // firebase
    private FirebaseAuth fb_auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        mv_logo = findViewById(R.id.image_logo);
        mv_scan = findViewById(R.id.button_scan);

        initUI();
    }

    private void initUI() {
        loadLogoImage();
        setButtonScan();
    }

    private void loadLogoImage() {
        Glide
                .with(this)
                .load(getString(R.string.url_logo))
                .into(mv_logo);
    }

    private void setButtonScan() {
        mv_scan.setOnClickListener(new onClickScan());
    }

    private class onClickScan implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Dexter
                    .withActivity(SplashActivity.this)
                    .withPermission(Manifest.permission.CAMERA)
                    .withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse response) {

                            FirebaseUser user = fb_auth.getCurrentUser();

                            if (user != null){
                                Intent intent = new Intent(SplashActivity.this, ScannerActivity.class);
                                startActivity(intent);
                                finish();
                            }
                            else {
                                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                                startActivity(intent);
                            }
                        }

                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse response) {
                            Toast.makeText(SplashActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                        }
                    })
                    .check();
        }
    }
}
