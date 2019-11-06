package com.exzray.qrelection;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.exzray.qrelection.adapters.CampaignAdapter;
import com.exzray.qrelection.models.Election;
import com.exzray.qrelection.models.Student;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;

public class MainActivity extends AppCompatActivity {

    // component
    private String mc_uid;
    private CampaignAdapter mc_adapter;
    private LinearLayoutManager mc_layout_manager;
    private ListenerRegistration electionListener;
    private ListenerRegistration campaignListener;

    // view
    private Toolbar mv_toolbar;
    private TextView mv_date, mv_vote;
    private RecyclerView mv_recycler;

    // firebase
    FirebaseAuth fb_auth = FirebaseAuth.getInstance();
    FirebaseFirestore fb_firestore = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mv_toolbar = findViewById(R.id.toolbar);
        mv_date = findViewById(R.id.text_date);
        mv_vote = findViewById(R.id.text_vote);
        mv_recycler = findViewById(R.id.recycler);

        mc_uid = getIntent().getStringExtra(getString(R.string.FIRESTORE_ELECTION_UID));
        mc_adapter = new CampaignAdapter();
        mc_layout_manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        initUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (electionListener != null) electionListener.remove();
        if (campaignListener != null) campaignListener.remove();
    }

    private void initUI() {
        setActionBar(mv_toolbar);
        setTitle(mc_uid);

        mv_recycler.setAdapter(mc_adapter);
        mv_recycler.setLayoutManager(mc_layout_manager);

        listenToElection();
        listenToCampaign();
        getStudentInfo();
    }

    private void listenToElection() {
        electionListener = fb_firestore
                .document("elections/" + mc_uid)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                        if (documentSnapshot != null) {

                            Election election = documentSnapshot.toObject(Election.class);

                            if (election != null) {
                                String date = "Date: " + DateFormat.getDateInstance(DateFormat.MEDIUM).format(election.getStart());
                                String vote = "Votes: " + election.getVotes();

                                mv_date.setText(date);
                                mv_vote.setText(vote);
                            }
                        }
                    }
                });
    }

    private void listenToCampaign() {
        campaignListener = fb_firestore
                .collection("elections/" + mc_uid + "/campaigns")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                        if (queryDocumentSnapshots != null) {
                            mc_adapter.updateList(queryDocumentSnapshots.getDocuments());
                        }
                    }
                });
    }

    private void getStudentInfo() {
        FirebaseUser user = fb_auth.getCurrentUser();

        if (user != null) {
            fb_firestore
                    .document("students/" + user.getUid())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                            if (task.isSuccessful()) {

                                DocumentSnapshot snapshot = task.getResult();

                                if (snapshot != null) {
                                    Student student = snapshot.toObject(Student.class);

                                    if (student != null){
                                        setTitle("Selamat datang, " + student.getName());
                                    }
                                }

                            } else {
                                assert task.getException() != null;
                                Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}
