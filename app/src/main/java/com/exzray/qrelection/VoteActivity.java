package com.exzray.qrelection;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.exzray.qrelection.adapters.CandidateAdapter;
import com.exzray.qrelection.models.Campaign;
import com.exzray.qrelection.models.Position;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class VoteActivity extends AppCompatActivity {

    // component
    private String campaign_path;
    private CandidateAdapter mc_adapter;
    private GridLayoutManager mc_manager;
    private ListenerRegistration mc_campaignListener;
    private ListenerRegistration mc_positionListener;

    // view
    private Toolbar mv_toolbar;
    private TextView mv_title, mv_info;
    private View mv_container1, mv_container2;
    private RecyclerView mv_recycler;

    // firebase
    private FirebaseAuth fb_auth = FirebaseAuth.getInstance();
    private FirebaseFirestore fb_firestore = FirebaseFirestore.getInstance();
    private FirebaseUser fb_user = fb_auth.getCurrentUser();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote);

        mv_toolbar = findViewById(R.id.toolbar);
        mv_container1 = findViewById(R.id.container_1);
        mv_container2 = findViewById(R.id.container_2);
        mv_title = findViewById(R.id.text_title);
        mv_recycler = findViewById(R.id.recycler);
        mv_info = findViewById(R.id.text_info);

        // important component
        mc_manager = new GridLayoutManager(this, 2);
        campaign_path = getIntent().getStringExtra(getString(R.string.FIRESTORE_CAMPAIGN_PATH));

        if (campaign_path != null && !campaign_path.isEmpty()) {
            mc_adapter = new CandidateAdapter(this, campaign_path);

            initUI();

        } else onBackPressed();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mc_campaignListener != null) mc_campaignListener.remove();
        if (mc_positionListener != null) mc_positionListener.remove();
    }

    private void initUI() {
        setActionBar(mv_toolbar);

        mv_recycler.setAdapter(mc_adapter);
        mv_recycler.setLayoutManager(mc_manager);

        listenToCampaign();
        listenToPosition();
    }

    private void listenToCampaign() {
        mc_campaignListener = fb_firestore
                .document(campaign_path)
                .addSnapshotListener(new CampaignListener());
    }

    private void listenToPosition() {
        mc_positionListener = fb_firestore
                .collection(campaign_path + "/positions")
                .orderBy("arrangement", Query.Direction.ASCENDING)
                .addSnapshotListener(new PositionListener());
    }

    private void filterPosition(List<DocumentSnapshot> list) {

        if (fb_user != null) {
            int totalPosition = list.size();
            String user_uid = fb_user.getUid();

            for (DocumentSnapshot snapshot : list) {

                Position position = snapshot.toObject(Position.class);

                // loop stopper
                if (position != null) {

                    // if user not yet vote for this position
                    if (!position.getVotes().containsKey(user_uid)) {
                        mv_container1.setVisibility(View.VISIBLE);
                        mv_container2.setVisibility(View.GONE);

                        mc_adapter.updateData(position, snapshot.getId());

                        mv_title.setText(position.getTitle());

                        return;
                    }
                }
            }

            // when no position available for student to vote
            mv_container1.setVisibility(View.GONE);
            mv_container2.setVisibility(View.VISIBLE);

            if (totalPosition != 0) mv_info.setText(R.string.text_position_finish);
            else mv_info.setText(R.string.text_position_zero);
        }
    }

    class CampaignListener implements EventListener<DocumentSnapshot> {

        @Override
        public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
            if (snapshot != null) {
                Campaign campaign = snapshot.toObject(Campaign.class);

                if (campaign != null) {
                    setTitle(campaign.getTitle());
                }
            }
        }
    }

    class PositionListener implements EventListener<QuerySnapshot> {

        @Override
        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
            if (queryDocumentSnapshots != null) {

                filterPosition(queryDocumentSnapshots.getDocuments());
            }
        }
    }
}
