package com.emre1s.authenticationmodule;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {

    public static final String USER_ID_KEY = "user-id";
    @BindView(R.id.tv_sign_up)
    TextView signUpText;

    @BindView(R.id.et_email)
    TextInputEditText email;
    @BindView(R.id.tl_email)
    TextInputLayout emailLayout;

    @BindView(R.id.et_password)
    TextInputEditText password;

    @BindView(R.id.btn_login)
    MaterialButton loginButton;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startDetailActivity(currentUser);
        }
    }

    private void startDetailActivity(FirebaseUser currentUser) {
        Intent intent = new Intent(LoginActivity.this, UserDetailsActivity.class);
        intent.putExtra(USER_ID_KEY, currentUser.getUid());
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        signUpText.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            finish();
        });
        loginButton.setOnClickListener(this::handleLoginButton);
        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!SignUpActivity.isValidEmail(editable.toString())) {
                    emailLayout.setErrorEnabled(true);
                    emailLayout.setError("Invalid email address");
                } else {
                    emailLayout.setErrorEnabled(false);
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        signUpText.setOnClickListener(null);
    }

    private void handleLoginButton(View view) {
        if (SignUpActivity.isValidEmail(Objects.requireNonNull(email.getText()).toString()) &&
        !Objects.requireNonNull(password.getText()).toString().isEmpty()) {
            loginButton.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);
            mAuth.signInWithEmailAndPassword(email.getText().toString(),
                    Objects.requireNonNull(password.getText()).toString())
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            loginButton.setEnabled(true);
                            Log.d("Emre1s", "Login successful LoginActivity");
                            progressBar.setVisibility(View.GONE);
                            startDetailActivity(authResult.getUser());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            loginButton.setEnabled(true);
                            progressBar.setVisibility(View.GONE);
                            Log.d("Emre1s", "Login unsuccessful LoginActivity");
                            new MaterialAlertDialogBuilder(LoginActivity.this)
                                    .setTitle("Login unsuccessful")
                                    .setMessage(e.getMessage())
                                    .create().show();
                        }
                    });
        } else {
            Toast.makeText(this, "Please recheck your entries", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean passwordCheck(String password) {
        if (password.trim().isEmpty()) {
            return false;
        }
        return true;
    }
}
