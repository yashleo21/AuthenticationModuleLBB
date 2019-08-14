package com.emre1s.authenticationmodule;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.emre1s.authenticationmodule.interfaces.OnImagePickerClicked;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ImagePickerSheet extends BottomSheetDialogFragment {

    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int REQUEST_GALLERY_CAPTURE = 2;
    public static String TAG = "ImagePickerSheet";
    @BindView(R.id.tv_camera)
    TextView camera;
    @BindView(R.id.tv_gallery)
    TextView gallery;
    private OnImagePickerClicked onImagePickerClicked;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            onImagePickerClicked = (OnImagePickerClicked) context;
        } catch (Exception e) {
            Log.d("Emre1s", "Activity does not implement the interface");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.modal_bottom_sheet, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                onImagePickerClicked.pickerType(REQUEST_IMAGE_CAPTURE);
                Log.d("Emre1s", "Onclicked");
            }
        });

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                onImagePickerClicked.pickerType(REQUEST_GALLERY_CAPTURE);
                Log.d("Emre1s", "Onclicked");
            }
        });

    }
}
