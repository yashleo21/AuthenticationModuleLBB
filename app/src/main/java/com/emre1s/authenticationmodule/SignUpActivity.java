package com.emre1s.authenticationmodule;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.emre1s.authenticationmodule.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.emre1s.authenticationmodule.LoginActivity.USER_ID_KEY;

public class SignUpActivity extends AppCompatActivity {
    public static final int PHONE_NUMBER_LENGTH = 10;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @BindView(R.id.pb_loading)
    ProgressBar loadingProgressBar;

    @BindView(R.id.btn_signup)
    MaterialButton signUpButton;

    @BindView(R.id.et_signup_email)
    TextInputEditText signUpEmail;
    @BindView(R.id.tl_signup_email)
    TextInputLayout signUpEmailLayout;

    @BindView(R.id.et_signup_password)
    TextInputEditText signUpPassword;

    @BindView(R.id.et_signup_phone)
    TextInputEditText signUpPhone;
    @BindView(R.id.tl_signup_phone)
    TextInputLayout signUpPhoneLayout;

    @BindView(R.id.et_signup_name)
    TextInputEditText signUpName;
    @BindView(R.id.tl_signup_fullname)
    TextInputLayout signUpNameLayout;

    @BindView(R.id.et_signup_confirm_password)
    TextInputEditText signUpPasswordConfirm;
    @BindView(R.id.tl_signup_confirm_password)
    TextInputLayout signUpConfirmPasswordLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        InputFilter[] filterArray = new InputFilter[1];
        filterArray[0] = new InputFilter.LengthFilter(PHONE_NUMBER_LENGTH);
        signUpPhone.setFilters(filterArray);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerListeners();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterListeners();
    }

    private void registerListeners() {
        signUpButton.setOnClickListener(this::signUpClickListener);
        signUpName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d("Emre1s", "Text changed " + charSequence.toString() + i + i1 + i2);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!isValidName(editable.toString())) {
                    signUpNameLayout.setErrorEnabled(true);
                    signUpNameLayout.setError("Incorrect name");
                } else {
                    signUpNameLayout.setErrorEnabled(false);
                }
            }
        });
        signUpEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().isEmpty()) {
                    signUpEmailLayout.setErrorEnabled(false);
                    return;
                }
                if (!isValidEmail(editable.toString())) {
                    signUpEmailLayout.setErrorEnabled(true);
                    signUpEmailLayout.setError("Please enter valid email");
                } else {
                    signUpEmailLayout.setErrorEnabled(false);
                }
            }
        });
        signUpPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!isValidPhone(editable.toString())) {
                    signUpPhoneLayout.setErrorEnabled(true);
                    signUpPhoneLayout.setError("Invalid phone number");
                } else {
                    signUpPhoneLayout.setErrorEnabled(false);
                }
            }
        });
        signUpPasswordConfirm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!arePasswordsSame(editable.toString())) {
                    signUpConfirmPasswordLayout.setErrorEnabled(true);
                    signUpConfirmPasswordLayout.setError("Passwords do not match");
                } else {
                    signUpConfirmPasswordLayout.setErrorEnabled(false);
                }
            }
        });
    }

    private void unregisterListeners() {
        signUpButton.setOnClickListener(null);
        signUpName.addTextChangedListener(null);
        signUpEmail.addTextChangedListener(null);
        signUpPhone.addTextChangedListener(null);
        signUpPasswordConfirm.addTextChangedListener(null);
    }

    private void signUpClickListener(View view) {
        signUpButton.setEnabled(false);
        if (finalValidation()) {
            Log.d("Emre1s", "Check successfull. Launch");
            loadingProgressBar.setVisibility(View.VISIBLE);
            signUpUser();
        } else {
            Log.d("Emre1s", "Check unsuccessful");
            Toast.makeText(this, "Please recheck the entries",
                    Toast.LENGTH_SHORT).show();
            signUpButton.setEnabled(true);
        }
    }

    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
        }
    }

    private boolean isValidName(String name) {
        return !name.equals("") && name.matches("^[a-zA-Z ]*$");
    }

    private boolean isValidPhone(String phone) {
        if (phone == null) {
            return false;
        }
        return !phone.equals("") && phone.matches("^[0-9]*$");
    }

    private boolean arePasswordsSame(String password) {
        return password.equals(signUpPassword.getText().toString());
    }

    private boolean finalValidation() {
        return isValidEmail(signUpEmail.getText().toString()) &&
                isValidName(signUpName.getText().toString()) &&
                arePasswordsSame(signUpPasswordConfirm.getText().toString()) &&
                isValidPhone(signUpPhone.getText().toString());

    }

    private User createUser() {
        User user = new User();
        user.setEmail(signUpEmail.getText().toString());
        user.setNumber(signUpPhone.getText().toString());
        user.setName(signUpName.getText().toString());
        return user;
    }

    private void signUpUser() {
        mAuth.createUserWithEmailAndPassword(Objects.requireNonNull(signUpEmail.getText()).toString(),
                Objects.requireNonNull(signUpPassword.getText()).toString())
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        loadingProgressBar.setVisibility(View.GONE);
                        signUpButton.setEnabled(true);
                        User user = createUser();
                        //Todo navigate
                        Intent toUserDetailActivity =
                                new Intent(SignUpActivity.this,
                                        UserDetailsActivity.class);
                        toUserDetailActivity.putExtra(USER_ID_KEY, authResult.getUser().getUid());
                        Executors.newSingleThreadExecutor().execute(new Runnable() {
                            @Override
                            public void run() {
                                db.collection("users")
                                        .document(authResult.getUser().getUid())
                                        .set(user)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d("Emre1s", "Success Document made");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d("Emre1s", "Failure. Doc not made");
                                            }
                                        });
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Emre1s", "Sign up failure. Error: " + e.getMessage());
                        loadingProgressBar.setVisibility(View.GONE);
                        signUpButton.setEnabled(true);
                        new MaterialAlertDialogBuilder(SignUpActivity.this)
                                .setTitle("Error")
                                .setMessage(e.getMessage())
                                .setNeutralButton("Okay", null)
                                .create().show();
                    }
                });
    }

}
