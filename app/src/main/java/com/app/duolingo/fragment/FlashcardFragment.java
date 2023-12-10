package com.app.duolingo.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.app.duolingo.HomeFragment;
import com.app.duolingo.R;
import com.app.duolingo.adapter.FlashcardAdapter;
import com.app.duolingo.models.Word;
import com.app.duolingo.services.DatabaseService;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FlashcardFragment extends Fragment {

    private ViewPager2 viewPagerWords;
    private FlashcardAdapter flashcardAdapter;
    private Button btnNext, btnPrevious;
    private String progressRecordRefId, userId;
    private FirebaseAuth auth;
    private DatabaseService databaseService;

    public FlashcardFragment() {
        // Required empty public constructor
    }

    public static FlashcardFragment newInstance(String courseId) {
        FlashcardFragment fragment = new FlashcardFragment();
        Bundle args = new Bundle();
        args.putString("COURSE_ID", courseId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_flashcard, container, false);
        viewPagerWords = view.findViewById(R.id.viewPagerWords);
        btnNext = view.findViewById(R.id.btnNext);
        btnPrevious = view.findViewById(R.id.btnPrevious);

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            userId = auth.getCurrentUser().getUid();
        }

        String courseId = getArguments().getString("COURSE_ID");
        databaseService = new DatabaseService();
        flashcardAdapter = new FlashcardAdapter(new ArrayList<>());
        viewPagerWords.setAdapter(flashcardAdapter);

        databaseService.getProgressRecordRefId(userId, courseId, new DatabaseService.Callback<String>() {
            @Override
            public void onResult(String progressRecordRefId) {
                FlashcardFragment.this.progressRecordRefId = progressRecordRefId;
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getActivity(), "Error fetching progress record", Toast.LENGTH_SHORT).show();
            }
        });

        databaseService.fetchCourseWords(courseId, new DatabaseService.Callback<List<Word>>() {
            @Override
            public void onResult(List<Word> result) {
                if (getActivity() == null || flashcardAdapter == null) {
                    Log.e("FlashcardFragment", "Activity or adapter is not initialized");
                    return;
                }
                Collections.shuffle(result);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        flashcardAdapter.setWords(result);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getActivity(), "Error fetching course words", Toast.LENGTH_SHORT).show();
            }
        });

        btnNext.setOnClickListener(v -> {
            int currentItem = viewPagerWords.getCurrentItem();
            int totalItems = flashcardAdapter.getItemCount();
            if (currentItem < totalItems - 1) {
                viewPagerWords.setCurrentItem(currentItem + 1);
                int progressPercentage = (int) (((currentItem + 1) / (float) totalItems) * 100);
                if (progressRecordRefId != null) {
                    databaseService.updateLearnProgress(progressRecordRefId, progressPercentage, new DatabaseService.Callback<Void>() {
                        @Override
                        public void onResult(Void result) {}

                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(getActivity(), "Error updating progress", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                databaseService.updateLearnProgress(progressRecordRefId, 100, new DatabaseService.Callback<Void>() {
                    @Override
                    public void onResult(Void result) {}

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(getActivity(), "Error updating progress", Toast.LENGTH_SHORT).show();
                    }
                });
                HomeFragment homeFragment = new HomeFragment();
                FragmentManager fragmentManager = getParentFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.frame_layout, homeFragment);
                transaction.addToBackStack(this.getClass().getName());
                transaction.commit();
            }
        });
        btnPrevious.setOnClickListener(v -> {
            int currentItem = viewPagerWords.getCurrentItem();
            if (currentItem > 0) {
                viewPagerWords.setCurrentItem(currentItem - 1);
            }
        });

        return view;
    }

}
