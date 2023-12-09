package com.app.duolingo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.app.duolingo.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText signupEmail, signupPassword;
    private Button signupButton;
    private TextView loginRedirectText;
    private ProgressBar loadingProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        auth = FirebaseAuth.getInstance();
        signupEmail = findViewById(R.id.signup_email);
        signupPassword = findViewById(R.id.signup_password);
        signupButton = findViewById(R.id.signup_button);
        loginRedirectText = findViewById(R.id.loginRedirectText);
        loadingProgressBar = findViewById(R.id.loading);

        if (savedInstanceState != null) {
            // Restore the saved email and password entered by the user
            signupEmail.setText(savedInstanceState.getString("savedEmail",""));
            signupPassword.setText(savedInstanceState.getString("savedPassword",""));

            // Restore the visibility state of the ProgressBar and the login Button
            loadingProgressBar.setVisibility(savedInstanceState.getInt("progressBarVisibility", View.GONE));
            signupButton.setVisibility(savedInstanceState.getInt("loginButtonVisibility", View.VISIBLE));
        }

        signupPassword.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, android.view.MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (signupPassword.getRight() - signupPassword.getCompoundDrawables()[2].getBounds().width())) {
                        if (signupPassword.getTransformationMethod() == PasswordTransformationMethod.getInstance()) {
                            signupPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                            signupPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_password_24, 0, R.drawable.baseline_visibility_off_24, 0);
                        } else {
                            signupPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                            signupPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_password_24, 0, R.drawable.baseline_visibility_24, 0);
                        }
                        return true;
                    }
                }
                return false;
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signupButton.setVisibility(View.GONE);
                loadingProgressBar.setVisibility(View.VISIBLE);

                String user = signupEmail.getText().toString().trim();
                String pass = signupPassword.getText().toString().trim();

                if (user.isEmpty()){
                    signupEmail.setError("Email cannot be empty");
                }
                if (pass.isEmpty()){
                    signupPassword.setError("Password cannot be empty");
                } else{
                    auth.createUserWithEmailAndPassword(user, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                loadingProgressBar.setVisibility(View.GONE);
                                signupButton.setVisibility(View.VISIBLE);
                                Toast.makeText(SignUpActivity.this, "SignUp Successful", Toast.LENGTH_SHORT).show();

                                FirebaseUser firebaseUser = task.getResult().getUser();
                                String userId = firebaseUser.getUid();

                                // Create a new User object
                                User newUser = new User(userId, "", 0, "", firebaseUser.getEmail(), "Beginer", "https://firebasestorage.googleapis.com/v0/b/duolingo-cb87b.appspot.com/o/profile_images%2Ffree-user-icon.png?alt=media&token=50a34461-18ce-4c0e-abd3-e9ccc0fa58fe");

                                // Get an instance of the Firestore database
                                FirebaseFirestore db = FirebaseFirestore.getInstance();

                                // Add a new document with the user's UID as the document ID
                                db.collection("users").document(userId).set(newUser)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                // Document was successfully written!
                                                Log.d("SignUpActivity", "DocumentSnapshot successfully written!");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Handle the error
                                                Log.w("SignUpActivity", "Error writing document", e);
                                                if (firebaseUser != null) {
                                                    firebaseUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> deleteTask) {
                                                            if (deleteTask.isSuccessful()) {
                                                                Log.d("SignUpActivity", "User deleted from authentication");
                                                            } else {
                                                                Log.e("SignUpActivity", "Failed to delete user from authentication", deleteTask.getException());
                                                                // Optionally handle further if user deletion also fails
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        });

                                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                                finish();

                            } else {
                                loadingProgressBar.setVisibility(View.GONE);
                                signupButton.setVisibility(View.VISIBLE);
                                Toast.makeText(SignUpActivity.this, "SignUp Failed" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

            }
        });

        loginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                finish();
            }
        });

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the current email and password entered by the user
        outState.putString("savedEmail", signupEmail.getText().toString());
        outState.putString("savedPassword", signupPassword.getText().toString());

        // Save the visibility state of the ProgressBar and the login Button
        outState.putInt("progressBarVisibility", loadingProgressBar.getVisibility());
        outState.putInt("loginButtonVisibility", signupButton.getVisibility());
    }
}