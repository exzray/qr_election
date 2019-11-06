package com.exzray.qrelection;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.exzray.qrelection.adapters.CandidateAdapter;
import com.exzray.qrelection.models.Campaign;
import com.exzray.qrelection.models.Position;
import com.exzray.qrelection.models.Student;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import java.util.List;

public class VoteActivity extends AppCompatActivity {

    // var
    private String campaign_path = "";

    // component
    private CandidateAdapter mc_adapter;
    private GridLayoutManager mc_manager;
    private ListenerRegistration mc_campaignListener;
    private ListenerRegistration mc_positionListener;

    // view
    private Toolbar mv_toolbar;
    private TextView mv_title;
    private RecyclerView mv_recycler;


    // firebase
    private FirebaseAuth fb_auth = FirebaseAuth.getInstance();
    private FirebaseFirestore fb_firestore = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote);

        mc_manager = new GridLayoutManager(this, 2);
        mc_adapter = new CandidateAdapter();

        mv_toolbar = findViewById(R.id.toolbar);
        mv_title = findViewById(R.id.text_title);
        mv_recycler = findViewById(R.id.recycler);

        initUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mc_campaignListener != null) mc_campaignListener.remove();
        if (mc_positionListener != null) mc_positionListener.remove();
    }

    private void initUI() {
        campaign_path = getIntent().getStringExtra(getString(R.string.FIRESTORE_CAMPAIGN_PATH));

        setActionBar(mv_toolbar);

        mv_recycler.setAdapter(mc_adapter);
        mv_recycler.setLayoutManager(mc_manager);

        doJob();
    }

    private void doJob() {
        if (!campaign_path.isEmpty()) {

            // code start here
            listenToCampaign();
            listenToPosition();
        } else onBackPressed();
    }

    private void listenToCampaign() {
        mc_campaignListener = fb_firestore
                .document(campaign_path)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if (documentSnapshot != null) {
                            Campaign campaign = documentSnapshot.toObject(Campaign.class);

                            if (campaign != null) {
                                setTitle(campaign.getTitle());
                            }
                        }
                    }
                });
    }

    private void listenToPosition() {
        mc_positionListener = fb_firestore
                .collection(campaign_path + "/positions")
                .orderBy("arrangement")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {

                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                        boolean status = false;

                        if (queryDocumentSnapshots != null) {
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                            for (DocumentSnapshot snapshot : list) {
                                status = isVoted(snapshot);

                                if (status) break;
                            }

                            if (!status) {
                                Toast.makeText(VoteActivity.this, "You cannot vote for this task anymore", Toast.LENGTH_SHORT).show();
                                onBackPressed();
                                finish();
                            }
                        }
                    }
                });
    }

    private boolean isVoted(final DocumentSnapshot snapshot) {
        final FirebaseUser user = fb_auth.getCurrentUser();
        Position position = snapshot.toObject(Position.class);

        assert user != null;

        if (position != null) {

            if (!position.getVotes().containsKey(user.getUid())) {

                mv_title.setText(position.getTitle());
                mc_adapter.setOnClickItem(new CandidateAdapter.OnClickItem() {

                    @Override
                    public void voteCandidate(final String uid, final Student student) {
                        final DocumentReference ref = snapshot.getReference();

                        new AlertDialog
                                .Builder(VoteActivity.this)
                                .setMessage("Are you sure to vote for " + student.getName())
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        fb_firestore.runTransaction(new Transaction.Function<Object>() {

                                            @Nullable
                                            @Override
                                            public Object apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                                                final DocumentSnapshot read = transaction.get(ref);
                                                final Position position1 = read.toObject(Position.class);

                                                if (position1 != null) {

                                                    position1.getVotes().put(user.getUid(), uid);
                                                    transaction.set(ref, position1);
                                                }

                                                return null;
                                            }
                                        });
                                    }
                                })
                                .setNegativeButton("Not sure", null)
                                .show();
                    }
                });
                mc_adapter.updateList(position.getCandidates());

                return true;
            }
        }

        return false;
    }
}
