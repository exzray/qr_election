package com.exzray.qrelection;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    // view
    private Toolbar mv_toolbar;
    private ZXingScannerView mv_scanner;

    // firebase
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        mv_toolbar = findViewById(R.id.toolbar);
        mv_scanner = findViewById(R.id.scanner);

        initUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mv_scanner.stopCamera();
    }

    @Override
    public void handleResult(Result rawResult) {
        final String uid = rawResult.getText();

        validateQq(uid);
    }

    private void initUI() {
        setActionBar(mv_toolbar);

        setTitle("Scanning Election QR Code");

        mv_scanner.setResultHandler(this);
        mv_scanner.startCamera();
    }

    private void validateQq(String uid) {
        final String UID = uid;

        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage("Waiting response from server");
        dialog.show();

        // checking qr code is valid or not
        firestore
                .document("elections/" + uid)
                .get()
                .addOnCompleteListener(this, new OnCompleteListener<DocumentSnapshot>() {

                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot snapshot = task.getResult();

                            if (snapshot != null) {
                                if (snapshot.exists()) {
                                    Intent intent = new Intent(ScannerActivity.this, MainActivity.class);
                                    intent.putExtra(getString(R.string.FIRESTORE_ELECTION_UID), UID);

                                    startActivity(intent);
                                    finish();
                                }
                            } else {
                                Toast.makeText(ScannerActivity.this, "Oopss, we cannot found this QR code on our server, Please scan again!", Toast.LENGTH_SHORT).show();
                                mv_scanner.startCamera();
                            }
                        }

                        dialog.dismiss();
                    }
                });
    }
}
