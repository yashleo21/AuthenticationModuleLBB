package com.emre1s.authenticationmodule.adapters;

import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.emre1s.authenticationmodule.R;
import com.emre1s.authenticationmodule.interfaces.OnProfileButtonClicked;
import com.emre1s.authenticationmodule.model.User;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

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

    class ViewHolder extends RecyclerView.ViewHolder {
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

        @BindView(R.id.rv_image_loading)
        ProgressBar imageLoading;

        ViewHolder(@NonNull View itemView) {
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
                imageLoading.setVisibility(View.VISIBLE);
                Log.d("Emre1s", "Image link not null " + imageLink);
                Picasso.get().load(imageLink).placeholder(R.drawable.profile_placeholder)
                        .resize(150, 200)
                        .centerCrop()
                        .transform(new RoundedCornersTransformation(4, 0))
                        .into(profileImagae, new Callback() {
                            @Override
                            public void onSuccess() {
                                imageLoading.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError(Exception e) {
                                imageLoading.setVisibility(View.GONE);
                            }
                        });
            } else {
                Log.d("Emre1s", "I am called.");
                profileImagae.setImageResource(R.drawable.profile_placeholder);
            }
        }
    }
}
