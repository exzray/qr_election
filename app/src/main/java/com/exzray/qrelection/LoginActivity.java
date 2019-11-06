package com.exzray.qrelection;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.exzray.qrelection.models.Student;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class LoginActivity extends AppCompatActivity {

    // component
    private ProgressDialog mc_progress;

    // view
    private ImageView mv_logo;
    private EditText mv_matrik, mv_password;
    private Button mv_signin;

    // firebase
    private FirebaseAuth fb_auth = FirebaseAuth.getInstance();
    private FirebaseFirestore fb_firestore = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);

        mc_progress = new ProgressDialog(this);

        mv_logo = findViewById(R.id.image_logo);
        mv_matrik = findViewById(R.id.edit_matrik);
        mv_password = findViewById(R.id.edit_password);
        mv_signin = findViewById(R.id.button_signin);

        initUI();
    }

    @Override
    protected void onStart() {
        super.onStart();

        updateUI(fb_auth.getCurrentUser());
    }

    private void initUI() {
        loadLogoImage();
        setOnClickSignin();

        mc_progress.setCancelable(false);
        mc_progress.setMessage("Sign in to your student account");
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(this, ScannerActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void loadLogoImage() {
        Glide
                .with(this)
                .load(getString(R.string.url_logo))
                .into(mv_logo);
    }

    private void setOnClickSignin() {
        mv_signin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                final String matrik = mv_matrik.getText().toString().trim();
                final String password = mv_password.getText().toString().trim();

                if (validateLogin(matrik, password)) {

                    mc_progress.show();

                    fb_firestore
                            .collection("students")
                            .whereEqualTo("matrik_id", matrik)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                    if (task.isSuccessful()) {

                                        QuerySnapshot snapshots = task.getResult();

                                        if (snapshots != null) {

                                            if (snapshots.size() == 1) {

                                                List<DocumentSnapshot> list = snapshots.getDocuments();

                                                Student student = list.get(0).toObject(Student.class);

                                                if (student != null) {

                                                    final String email = student.getEmail();

                                                    doLogin(email, password);
                                                }
                                            }
                                        }
                                    }
                                    else {
                                        mc_progress.dismiss();

                                        assert task.getException() != null;
                                        Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
    }

    private boolean validateLogin(String matrik, String password) {

        if (matrik.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all field", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password too short", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void doLogin(String email, String password){

        fb_auth
                .signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        mc_progress.dismiss();

                        if (task.isSuccessful()){
                            if (task.getResult() != null) {
                                updateUI(task.getResult().getUser());
                            }
                        }
                        else {
                            assert task.getException() != null;
                            Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
