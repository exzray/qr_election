package com.exzray.qrelection.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.exzray.qrelection.R;
import com.exzray.qrelection.VoteActivity;
import com.exzray.qrelection.models.Campaign;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CampaignAdapter extends RecyclerView.Adapter<CampaignAdapter.VH> {

    private List<DocumentSnapshot> list = new ArrayList<>();

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_campaign, parent, false);

        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final VH holder, int position) {
        final DocumentSnapshot snapshot = list.get(position);
        final Campaign campaign = snapshot.toObject(Campaign.class);
        final Context context = holder.itemView.getContext();

        assert campaign != null;

        Glide
                .with(context)
                .load(campaign.getImage())
                .into(holder.image);

        holder.title.setText(campaign.getTitle());

        // go to vote page when click to this item
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String campaign_path = snapshot.getReference().getPath();

                Intent intent = new Intent(context, VoteActivity.class);
                intent.putExtra(context.getString(R.string.FIRESTORE_CAMPAIGN_PATH), campaign_path);

                context.startActivity(intent);
            }
        });

        // show description message when long click to this item
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(context, campaign.getDescription(), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void updateList(List<DocumentSnapshot> updated) {
        list = updated;
        notifyDataSetChanged();
    }

    class VH extends RecyclerView.ViewHolder {

        ImageView image;
        TextView title;

        private VH(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.image);
            title = itemView.findViewById(R.id.title);
        }
    }
}
