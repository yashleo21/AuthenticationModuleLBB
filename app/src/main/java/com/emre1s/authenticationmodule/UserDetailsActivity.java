package com.emre1s.authenticationmodule;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.ProgressBar;

import com.emre1s.authenticationmodule.adapters.UserDetailsAdapter;
import com.emre1s.authenticationmodule.interfaces.OnProfileButtonClicked;
import com.emre1s.authenticationmodule.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.emre1s.authenticationmodule.LoginActivity.USER_ID_KEY;

public class UserDetailsActivity extends AppCompatActivity {

    private static final int GALLERY_REQUEST_CODE = 1;
    private FirebaseFirestore db;
    private String userId;

    @BindView(R.id.rv_user_details)
    RecyclerView userDetailsRecycler;

    @BindView(R.id.pb_user_detail)
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);
        ButterKnife.bind(this);

        db = FirebaseFirestore.getInstance();
        Intent intent = getIntent();
        if (intent != null) {
            userId = intent.getStringExtra(USER_ID_KEY);
        }
        progressBar.setVisibility(View.VISIBLE);
        retrieveUserData(userId);
    }

    private void retrieveUserData(String userId) {
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Log.d("Emre1s", "Data retrieval success");
                        SparseArray<User> userSparseArray = new SparseArray<>();
                        userSparseArray.append(0, documentSnapshot.toObject(User.class));
                        UserDetailsAdapter userDetailsAdapter =
                                new UserDetailsAdapter(userSparseArray, new OnProfileButtonClicked() {
                                    @Override
                                    public void imageData() {
                                        pickFromGallery();
                                    }
                                });

                        userDetailsRecycler.setLayoutManager(new
                                LinearLayoutManager(UserDetailsActivity.this));
                        userDetailsRecycler.setAdapter(userDetailsAdapter);

                        progressBar.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Emre1s", "Data retrieval failure " + e.getMessage());
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode){
                case GALLERY_REQUEST_CODE:
                    //data.getData returns the content URI for the selected Image
                    Uri selectedImage = data.getData();
                    Log.d("Emre1s", "Image:  " + selectedImage.toString());

                    String[] filePathColumn = { MediaStore.Images.Media.DATA };
                    // Get the cursor
                    Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    // Move to first row
                    cursor.moveToFirst();
                    //Get the column index of MediaStore.Images.Media.DATA
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    //Gets the String value in the column
                    String imgDecodableString = cursor.getString(columnIndex);
                    cursor.close();
                    // Set the Image in ImageView after decoding the String
                    //imageView.setImageBitmap(BitmapFactory.decodeFile(imgDecodableString));
                    break;
            }
        }
    }

    private void pickFromGallery(){
        //Create an Intent with action as ACTION_PICK
        Intent intent=new Intent(Intent.ACTION_PICK);
        // Sets the type as image/*. This ensures only components of type image are selected
        intent.setType("image/*");
        //We pass an extra array with the accepted mime types. This will ensure only components with these MIME types as targeted.
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
        // Launching the Intent 
        startActivityForResult(intent,GALLERY_REQUEST_CODE);
    }
}
