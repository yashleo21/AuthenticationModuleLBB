package com.emre1s.authenticationmodule;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.emre1s.authenticationmodule.adapters.UserDetailsAdapter;
import com.emre1s.authenticationmodule.interfaces.OnImagePickerClicked;
import com.emre1s.authenticationmodule.interfaces.OnProfileButtonClicked;
import com.emre1s.authenticationmodule.model.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.emre1s.authenticationmodule.ImagePickerSheet.REQUEST_GALLERY_CAPTURE;
import static com.emre1s.authenticationmodule.ImagePickerSheet.REQUEST_IMAGE_CAPTURE;
import static com.emre1s.authenticationmodule.LoginActivity.USER_ID_KEY;

public class UserDetailsActivity extends AppCompatActivity implements OnProfileButtonClicked, OnImagePickerClicked {

    @BindView(R.id.rv_user_details)
    RecyclerView userDetailsRecycler;
    @BindView(R.id.pb_user_detail)
    ProgressBar progressBar;
    @BindView(R.id.btn_logout)
    MaterialButton logoutButton;
    private FirebaseFirestore db;
    private String userId;
    private User user;
    private SparseArray<User> userSparseArray = new SparseArray<>();
    private UserDetailsAdapter userDetailsAdapter;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);
        ButterKnife.bind(this);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference().child("userimages");

        Intent intent = getIntent();
        if (intent != null) {
            userId = intent.getStringExtra(USER_ID_KEY);
        }
        progressBar.setVisibility(View.VISIBLE);
        retrieveUserData(userId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
                startActivity(new Intent(UserDetailsActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        logoutButton.setOnClickListener(null);
    }

    private void retrieveUserData(String userId) {
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Log.d("Emre1s", "Data retrieval success");
                        user = documentSnapshot.toObject(User.class);
                        userSparseArray.append(0, user);
                        userDetailsAdapter =
                                new UserDetailsAdapter(userSparseArray,
                                        UserDetailsActivity.this);

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
            switch (requestCode) {
                case REQUEST_IMAGE_CAPTURE: {
                    progressBar.setVisibility(View.VISIBLE);
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    uploadProfileImage(imageBitmap);
                    break;
                }

                case REQUEST_GALLERY_CAPTURE:
                    progressBar.setVisibility(View.VISIBLE);
                    Uri selectedImage = data.getData();
                    Log.d("Emre1s", "Image:  " + selectedImage.toString());

                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    // Get the cursor
                    Cursor cursor = getContentResolver().query(selectedImage,
                            filePathColumn, null, null, null);
                    // Move to first row
                    cursor.moveToFirst();
                    //Get the column index of MediaStore.Images.Media.DATA
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    //Gets the String value in the column
                    String imgDecodableString = cursor.getString(columnIndex);
                    cursor.close();
                    user.setImageUrl(imgDecodableString);
                    userDetailsAdapter.notifyDataSetChanged();
                    Log.d("Emre1s", "Cursor link: " + imgDecodableString);
                    Bitmap image = BitmapFactory.decodeFile(imgDecodableString);
                    uploadProfileImage(image);
                    break;
            }
        } else {
            Log.d("Emre1s", "Result not ok.");
        }
    }

    private void uploadProfileImage(Bitmap imageBitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageData = baos.toByteArray();

        UploadTask uploadTask = storageReference.child(userId + ".jpg")
                .putBytes(imageData);

        Task<Uri> urlTask =
                uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            Log.d("Emre1s", "Exception? task unsuccessful");
                            throw new Exception();
                        }
                        progressBar.setVisibility(View.GONE);
                        return storageReference.child(userId + ".jpg").getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            Log.d("Emre1s", "Download url: " + downloadUri);
                            user.setImageUrl(downloadUri.toString());
                            userDetailsAdapter.notifyDataSetChanged();
                            Executors.newSingleThreadExecutor().execute(new Runnable() {
                                @Override
                                public void run() {
                                    updateUserData();
                                }
                            });

                        } else {
                            Log.d("Emre1s", "No download url");
                        }
                    }
                });
    }

    private void updateUserData() {
        db.collection("users")
                .document(userId)
                .set(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("Emre1s", "Completed. Succ");
                        } else {
                            Log.d("Emre1s", "Unsuccessful");
                        }
                    }
                });
    }

    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

        startActivityForResult(intent, REQUEST_GALLERY_CAPTURE);
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
    }

    @Override
    public void imageData() {
        ImagePickerSheet imagePickerSheet = new ImagePickerSheet();
        imagePickerSheet.show(getSupportFragmentManager(), ImagePickerSheet.TAG);
    }

    @Override
    public void pickerType(int pickerId) {
        switch (pickerId) {
            case REQUEST_IMAGE_CAPTURE: {
                Log.d("Emre1s", "Camera capture called");
                Dexter.withActivity(this)
                        .withPermissions(
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE)
                        .withListener(new MultiplePermissionsListener() {
                            @Override
                            public void onPermissionsChecked(MultiplePermissionsReport report) {
                                Log.d("Emre1s", "Onclicked perm check");
                                if (report.areAllPermissionsGranted()) {
                                    dispatchCameraIntent();
                                }
                                for (int i = 0; i < report.getDeniedPermissionResponses().size(); i++) {
                                    Log.d("Permission denied",
                                            report.getDeniedPermissionResponses()
                                                    .get(i).getPermissionName());
                                }
                                if (report.isAnyPermissionPermanentlyDenied()) {
                                    Intent intent =
                                            new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                    Uri.fromParts("package",
                                                            getPackageName(),
                                                            null));
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                }
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                                token.continuePermissionRequest();
                            }
                        }).check();
                break;
            }
            case REQUEST_GALLERY_CAPTURE: {
                Dexter.withActivity(this)
                        .withPermissions(
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE)
                        .withListener(new MultiplePermissionsListener() {
                            @Override
                            public void onPermissionsChecked(MultiplePermissionsReport report) {
                                Log.d("Emre1s", "Onclicked perm check");
                                if (report.areAllPermissionsGranted()) {
                                    pickFromGallery();
                                }
                                for (int i = 0; i < report.getDeniedPermissionResponses().size(); i++) {
                                    Log.d("Permission denied",
                                            report.getDeniedPermissionResponses()
                                                    .get(i).getPermissionName());
                                }
                                if (report.isAnyPermissionPermanentlyDenied()) {
                                    Intent intent =
                                            new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                    Uri.fromParts("package",
                                                            getPackageName(),
                                                            null));
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                }
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                                token.continuePermissionRequest();
                            }
                        }).check();
                break;
            }
        }
    }

    private void dispatchCameraIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

}
