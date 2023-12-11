package com.app.duolingo.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
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
    private Handler autoPlayHandler = new Handler();
    private Runnable autoPlayRunnable;
    private boolean isAutoPlayActive = false;
    private FlashcardAdapter flashcardAdapter;
    private Button btnNext, btnPrevious;
    private ImageButton btnAutoPlay;
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
        btnAutoPlay = view.findViewById(R.id.btnAutoPlay);
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
                viewPagerWords.setCurrentItem(0);
                databaseService.updateLearnProgress(progressRecordRefId, 100, new DatabaseService.Callback<Void>() {
                    @Override
                    public void onResult(Void result) {}

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(getActivity(), "Error updating progress", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        btnPrevious.setOnClickListener(v -> {
            int currentItem = viewPagerWords.getCurrentItem();
            if (currentItem > 0) {
                viewPagerWords.setCurrentItem(currentItem - 1);
            }
        });

        btnAutoPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAutoPlayActive) {
                    stopAutoPlay();
                    btnAutoPlay.setImageResource(R.drawable.baseline_play_arrow_24);
                } else {
                    startAutoPlay();
                    btnAutoPlay.setImageResource(R.drawable.baseline_pause_24);
                }
                isAutoPlayActive = !isAutoPlayActive;
            }
        });

        autoPlayRunnable = new Runnable() {
            @Override
            public void run() {
                if (viewPagerWords != null) {
                    int currentItem = viewPagerWords.getCurrentItem();
                    int nextItem = currentItem + 1;
                    if (nextItem >= flashcardAdapter.getItemCount()) {
                        nextItem = 0;
                    }
                    viewPagerWords.setCurrentItem(nextItem, true);

                    if (isAutoPlayActive) {
                        autoPlayHandler.postDelayed(this, 3000);
                    }
                }
            }
        };

        return view;
    }

    private void startAutoPlay() {
        Toast.makeText(getActivity(), "Auto play started", Toast.LENGTH_SHORT).show();
        autoPlayHandler.postDelayed(autoPlayRunnable, 5000);
    }

    private void stopAutoPlay() {
        Toast.makeText(getActivity(), "Auto play stopped", Toast.LENGTH_SHORT).show();
        autoPlayHandler.removeCallbacks(autoPlayRunnable);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopAutoPlay(); // Important to avoid memory leaks
    }


}
