package com.app.duolingo.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.app.duolingo.HomeFragment;
import com.app.duolingo.R;
import com.app.duolingo.models.Point;
import com.app.duolingo.models.QuizQuestion;
import com.app.duolingo.models.Word;
import com.app.duolingo.services.DatabaseService;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Collections;
import java.util.List;

public class WordSolveFragment extends Fragment {

    private String course;
    private DatabaseService databaseService;
    private List<Word> listWords;
    private TextView tvTranslation, tvWSScore;
    private EditText etEnglishWord;
    private Button btnCheck, btnNext;
    private FirebaseAuth auth;
    private int currentQuestionIndex = 0;
    private int correctAnswers = 0;
    private int totalQuestions = 0;
    private int score = 0;

    public WordSolveFragment() {
        // Required empty public constructor
    }

    public static WordSolveFragment newInstance(String courseId) {
        WordSolveFragment fragment = new WordSolveFragment();
        Bundle args = new Bundle();
        args.putString("COURSE_ID", courseId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        course = getArguments().getString("COURSE_KEY");
        databaseService = new DatabaseService();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_word_solve, container, false);

        course = getArguments().getString("COURSE_ID");

        tvTranslation = view.findViewById(R.id.tvTranslation);
        etEnglishWord = view.findViewById(R.id.etEnglishWord);
        btnCheck = view.findViewById(R.id.btnCheck);
        btnNext = view.findViewById(R.id.btnNext);
        tvWSScore = view.findViewById(R.id.tvWSScore);

        updateScore();

        btnNext.setOnClickListener(v -> {
            if (currentQuestionIndex < listWords.size() - 1) {
                currentQuestionIndex++;
                loadQuestion(listWords.get(currentQuestionIndex));
            } else {
                showCompletionDialog();
            }
        });

        btnCheck.setOnClickListener(v -> {
            checkAnswer();
        });

        fetchWordsForSolve();

        return view;
    }

    private void checkAnswer() {
        String answer = etEnglishWord.getText().toString().toLowerCase();
        if (answer.equals(listWords.get(currentQuestionIndex).getEnglish().toLowerCase())) {
            etEnglishWord.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_check_24, 0);
            correctAnswers++;
            playAudio(true);
            btnCheck.setEnabled(false);
        }else {
            etEnglishWord.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_clear_24, 0);
            playAudio(false);
        }
        updateScore();
        btnNext.setEnabled(true);
    }

    private void fetchWordsForSolve() {
        databaseService.fetchCourseWords(course, new DatabaseService.Callback<List<Word>>() {
            @Override
            public void onResult(List<Word> words) {
                listWords = words;
                Collections.shuffle(listWords);
                loadQuestion(listWords.get(currentQuestionIndex));
                totalQuestions = listWords.size();
            }

            @Override
            public void onError(Exception e) {
                Log.e("WordSolve", "Failed to fetch words for solve", e);
            }
        });
    }

    private void loadQuestion(Word word) {
        etEnglishWord.setText("");
        etEnglishWord.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        tvTranslation.setText(word.getMeaning());
        btnCheck.setEnabled(true);
        btnNext.setEnabled(false);
    }

    private void showCompletionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Word Solve Completed");
        builder.setMessage("Your score: " + score);

        builder.setPositiveButton("Go to Home", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                savePointRecord();
            }
        });

        builder.setNegativeButton("Try again", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                WordSolveFragment wordSolveFragment = WordSolveFragment.newInstance(course);
                FragmentManager fragmentManager = getParentFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.frame_layout, wordSolveFragment);
                transaction.commit();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void playAudio(boolean isCorrect) {
        if (isCorrect) {
            MediaPlayer mediaPlayer = MediaPlayer.create(getView().getContext(), R.raw.rightanswer);
            mediaPlayer.start();
        } else {
            MediaPlayer mediaPlayer = MediaPlayer.create(getView().getContext(), R.raw.wronganswer);
            mediaPlayer.start();
        }
    }

    private void savePointRecord() {
        auth = FirebaseAuth.getInstance();
        Point point = new Point();
        point.setPoint(score);
        point.setUserId(auth.getCurrentUser().getUid());
        point.setCourseId(course);

        databaseService.savePoint(point, new DatabaseService.SavePointCallback() {
            @Override
            public void onSuccess() {
                navigateToHomeFragment();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("Quiz", "Failed to save point record", e);
            }
        });
    }

    private void navigateToHomeFragment() {
        HomeFragment homeFragment = new HomeFragment(); // Replace with your home fragment's constructor
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.frame_layout, homeFragment); // Replace 'fragment_container' with your actual container ID
        fragmentTransaction.addToBackStack(null); // Optional, if you want to add the transaction to the back stack
        fragmentTransaction.commit();
    }

    private void updateScore() {
        if (totalQuestions > 0) {
            score = (correctAnswers * 100) / totalQuestions;
            tvWSScore.setText("Score: " + score);
        }
    }
}