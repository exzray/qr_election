package com.exzray.qrelection.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.exzray.qrelection.R;
import com.exzray.qrelection.VoteActivity;
import com.exzray.qrelection.models.Student;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class CandidateAdapter extends RecyclerView.Adapter<CandidateAdapter.VH> {

    // component
    private OnClickItem onClickItem;
    private List<String> mc_list = new ArrayList<>();

    // firebase
    private FirebaseFirestore fb_firestore = FirebaseFirestore.getInstance();

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_candidate, parent, false);

        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final VH holder, int position) {

        if (holder.itemView.getContext() instanceof VoteActivity) {
            final VoteActivity activity = (VoteActivity) holder.itemView.getContext();
            final String candidate_uid = mc_list.get(position);

            DocumentReference reference = fb_firestore
                    .document("students/" + candidate_uid);

            reference
                    .get()
                    .addOnCompleteListener(activity, new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                            if (task.isSuccessful()) {
                                DocumentSnapshot snapshot = task.getResult();

                                if (snapshot != null) {

                                    final Student student = snapshot.toObject(Student.class);

                                    if (student != null) {

                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                if (onClickItem != null) onClickItem.voteCandidate(candidate_uid, student);
                                            }
                                        });

                                        holder.name.setText(student.getName());
                                        holder.course.setText(student.getCourse());

                                        Glide
                                                .with(activity)
                                                .load(student.getImage())
                                                .into(holder.profile);
                                    }
                                }
                            }
                        }
                    });
        }
    }

    @Override
    public int getItemCount() {
        return mc_list.size();
    }

    public void updateList(List<String> updated) {
        mc_list = updated;
        notifyDataSetChanged();
    }

    public void setOnClickItem(OnClickItem clickItem){
        onClickItem = clickItem;
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
    }

    public interface OnClickItem {
        void voteCandidate(String uid, Student student);
    }
}
