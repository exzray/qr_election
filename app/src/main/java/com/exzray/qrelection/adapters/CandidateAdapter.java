package com.exzray.qrelection.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.exzray.qrelection.R;
import com.exzray.qrelection.models.Position;
import com.exzray.qrelection.models.Student;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

public class CandidateAdapter extends RecyclerView.Adapter<CandidateAdapter.VH> {

    // component
    private Context context;
    private String campaign_path;
    private Position position;
    private String position_uid;

    // firebase
    private FirebaseAuth fb_auth = FirebaseAuth.getInstance();
    private FirebaseUser fb_user = fb_auth.getCurrentUser();
    private FirebaseFirestore fb_firestore = FirebaseFirestore.getInstance();


    public CandidateAdapter(Context context, String campaign_path) {
        this.context = context;
        this.campaign_path = campaign_path;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_candidate, parent, false);

        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final VH holder, int index) {

        if (position != null) {
            String str_candidateID = "students/" + position.getCandidates().get(index);

            fb_firestore
                    .document(str_candidateID)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                            if (task.isSuccessful()) {
                                DocumentSnapshot snapshot = task.getResult();

                                if (snapshot != null) {

                                    final Student student = snapshot.toObject(Student.class);

                                    if (student != null) {

                                        holder.update(student, snapshot.getId());
                                    }
                                }
                            }
                        }
                    });
        }
    }

    @Override
    public int getItemCount() {
        return position != null ? position.getCandidates().size() : 0;
    }

    public void updateData(Position position, String position_uid) {
        this.position = position;
        this.position_uid = position_uid;
        notifyDataSetChanged();
    }


    class VH extends RecyclerView.ViewHolder {

        private ImageView profile;
        private TextView name, course;

        private VH(@NonNull View itemView) {
            super(itemView);

            profile = itemView.findViewById(R.id.image_profile);
            name = itemView.findViewById(R.id.text_name);
            course = itemView.findViewById(R.id.text_course);
        }

        private void update(Student student, String uid) {

            itemView.setOnClickListener(new ItemOnClick(uid, student));

            name.setText(student.getName());
            course.setText(student.getCourse());

            Glide
                    .with(context)
                    .load(student.getImage())
                    .into(profile);
        }
    }

    class ItemOnClick implements OnClickListener {

        private String uid;
        private Student student;

        private ItemOnClick(String uid, Student student) {
            this.uid = uid;
            this.student = student;
        }

        @Override
        public void onClick(View v) {
            final DocumentReference position_ref = fb_firestore.document(campaign_path + "/positions/" + position_uid);

            // create popup dialog asking confirmation to student
            new AlertDialog
                    .Builder(context)
                    .setMessage("Are you sure to vote " + student.getName() + "?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            fb_firestore.runTransaction(new Transaction.Function<Object>() {

                                @Nullable
                                @Override
                                public Object apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

                                    DocumentSnapshot snapshot = transaction.get(position_ref);

                                    Position update_position = snapshot.toObject(Position.class);

                                    if (update_position != null){
                                        update_position.getVotes().put(fb_user.getUid(), uid);

                                        transaction.set(position_ref, update_position);
                                    }

                                    return null;
                                }
                            });
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        }
    }
}
