package com.app.duolingo;

import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.app.duolingo.models.User;

import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class AccountFragment extends Fragment {

    private EditText editTextName, editTextAge, editTextPhoneNumber, editTextEmail, editTextLevel;
    private Button buttonSave, buttonLogout;
    private String userId, imageUrl;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private SwitchCompat switchEdit;
    private ImageView imageViewProfilePicture;
    private Uri fileUri;

    public AccountFragment() {
        // Required empty public constructor
    }

    public static AccountFragment newInstance(String param1, String param2) {
        AccountFragment fragment = new AccountFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_account, container, false);
        imageViewProfilePicture = view.findViewById(R.id.imageViewUserProfile);
        editTextName = view.findViewById(R.id.editTextUserProfileName);
        editTextAge = view.findViewById(R.id.editTextUserProfileAge);
        editTextPhoneNumber = view.findViewById(R.id.editTextUserProfilePhone);
        editTextEmail = view.findViewById(R.id.editTextEmailUserProfile);
        editTextLevel = view.findViewById(R.id.editTextLevelUserProfile);
        switchEdit = view.findViewById(R.id.switchEditUserProfile);
        buttonSave = view.findViewById(R.id.buttonSaveUserProfile);
        buttonLogout = view.findViewById(R.id.buttonLogout);

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            userId = auth.getCurrentUser().getUid();
        }
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        initializeViews();
        setImageForUser();
        revertChanges();
        return view;
    }

    private void initializeViews() {
        imageViewProfilePicture.setOnClickListener(view -> {
            mGetContent.launch("image/*");
        });

        switchEdit.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                enableEditing();
            } else {
                revertChanges();
            }
        });
        buttonSave.setOnClickListener(v -> uploadUserImage());
        buttonLogout.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(getContext(), LoginActivity.class));
            getActivity().finish();
        });
    }

    private void setImageForUser() {
        storageRef = storage.getReference().child("profile_images/" + userId + ".jpg");
        storageRef.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    Glide.with(AccountFragment.this)
                            .asBitmap()
                            .load(uri)
                            .into(new CustomTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                    // Save bitmap to file and get Uri
                                    fileUri = saveImageToFile(resource, "profile_picture");
                                    imageViewProfilePicture.setImageBitmap(resource);
                                }

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private Uri saveImageToFile(Bitmap bitmap, String filename) {
        // ContextWrapper to get the app's file directory
        ContextWrapper wrapper = new ContextWrapper(getContext());
        // Creating a file in the app's internal storage
        File file = new File(wrapper.getFilesDir(), filename + ".jpg");
        try (OutputStream stream = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Uri.fromFile(file);
    }

    private void uploadUserImage() {
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference().child("profile_images/" + userId + ".jpg");

        storageRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> {
                    storageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                imageUrl = uri.toString();
                                updateUserProfile();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUserProfile() {
        String name = editTextName.getText().toString();
        int age = Integer.parseInt(editTextAge.getText().toString());
        String phoneNumber = editTextPhoneNumber.getText().toString();
        String email = editTextEmail.getText().toString();
        String level = editTextLevel.getText().toString();

        User user = new User(userId, name, age, phoneNumber, email, level, imageUrl);
        db.collection("users").document(userId).set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "User updated successfully", Toast.LENGTH_SHORT).show();
                    revertChanges();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    if (uri != null) {
                        fileUri = uri;
                        imageViewProfilePicture.setImageURI(uri);
                    }
                }
            });

    private void enableEditing() {
        imageViewProfilePicture.setEnabled(true);
        editTextName.setEnabled(true);
        editTextAge.setEnabled(true);
        editTextPhoneNumber.setEnabled(true);
        buttonSave.setEnabled(true);
    }

    private void revertChanges() {
        fetchUserDetails();
        imageViewProfilePicture.setEnabled(false);
        editTextName.setEnabled(false);
        editTextAge.setEnabled(false);
        editTextPhoneNumber.setEnabled(false);
        buttonSave.setEnabled(false);
    }

    private void fetchUserDetails() {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null) {
                        editTextName.setText(user.getName());
                        editTextAge.setText(String.valueOf(user.getAge()));
                        editTextPhoneNumber.setText(user.getPhoneNumber());
                        editTextEmail.setText(user.getEmail());
                        editTextLevel.setText(user.getLevel());
                    }
                });
    }
}