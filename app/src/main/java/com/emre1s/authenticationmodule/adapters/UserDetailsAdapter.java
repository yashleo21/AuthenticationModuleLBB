package com.emre1s.authenticationmodule.adapters;

import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.emre1s.authenticationmodule.R;
import com.emre1s.authenticationmodule.interfaces.OnProfileButtonClicked;
import com.emre1s.authenticationmodule.model.User;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserDetailsAdapter extends RecyclerView.Adapter<UserDetailsAdapter.ViewHolder> {

    private SparseArray<User> users;
    private OnProfileButtonClicked onProfileButtonClicked;

    public UserDetailsAdapter(SparseArray<User> users, OnProfileButtonClicked onProfileButtonClicked) {
        this.users = users;
        this.onProfileButtonClicked = onProfileButtonClicked;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_user_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_name)
        TextView name;

        @BindView(R.id.tv_email)
        TextView email;

        @BindView(R.id.tv_mobile)
        TextView mobile;

        @BindView(R.id.iv_profile)
        ImageView profileImagae;

        @BindView(R.id.iv_add_click)
        ImageView addButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onProfileButtonClicked.imageData();
                }
            });
        }
        private void bind(User user) {
            name.setText(user.getName());
            email.setText(user.getEmail());
            mobile.setText(user.getNumber());
            String imageLink = user.getImageUrl();
            if (imageLink != null) {
                Picasso.get().load(imageLink).placeholder(R.drawable.profile_placeholder)
                        .into(profileImagae);
            } else {
                profileImagae.setImageResource(R.drawable.profile_placeholder);
            }
        }
    }
}
