package com.app.duolingo.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.duolingo.MainActivity;
import com.app.duolingo.R;

public class ModeFragment extends Fragment {

    CardView flashcardCard, quizCard, wordSolveCard;

    public ModeFragment() {
        // Required empty public constructor
    }

    public static ModeFragment newInstance(String courseId) {
        ModeFragment fragment = new ModeFragment();
        Bundle args = new Bundle();
        args.putString("COURSE_ID", courseId);
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
        View view = inflater.inflate(R.layout.fragment_mode, container, false);

        String courseId = getArguments().getString("COURSE_ID");

        flashcardCard = view.findViewById(R.id.flashcardCard);
        quizCard = view.findViewById(R.id.quizCard);
        wordSolveCard = view.findViewById(R.id.wordSolveCard);
        flashcardCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FlashcardFragment flashcardFragment = FlashcardFragment.newInstance(courseId);
                FragmentManager fragmentManager = getParentFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.frame_layout, flashcardFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        quizCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                QuizFragment quizFragment = QuizFragment.newInstance(courseId);
                Bundle args = new Bundle();
                args.putString("COURSE_KEY", courseId);
                quizFragment.setArguments(args);
                FragmentManager fragmentManager = getParentFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.frame_layout, quizFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        wordSolveCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WordSolveFragment wordSolveFragment = WordSolveFragment.newInstance(courseId);
                FragmentManager fragmentManager = getParentFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.frame_layout, wordSolveFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        return view;
    }
}