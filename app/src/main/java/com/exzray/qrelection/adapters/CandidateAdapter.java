package com.exzray.qrelection.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.exzray.qrelection.R;
import com.exzray.qrelection.models.Position;
import com.exzray.qrelection.models.Student;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class CandidateAdapter extends RecyclerView.Adapter<CandidateAdapter.VH> {

    // component
    private Context context;
    private String campaign_path;
    private Position position;

    // firebase
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

                                        holder.update(student);
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

    public void updateData(Position position) {
        this.position = position;
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

        private void update(Student student) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            name.setText(student.getName());
            course.setText(student.getCourse());

            Glide
                    .with(context)
                    .load(student.getImage())
                    .into(profile);
        }
    }
}
