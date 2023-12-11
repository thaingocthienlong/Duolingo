package com.app.duolingo.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
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
import android.widget.TextView;
import android.widget.Toast;

import com.app.duolingo.HomeFragment;
import com.app.duolingo.R;
import com.app.duolingo.models.Point;
import com.app.duolingo.models.QuizQuestion;
import com.app.duolingo.models.Word;
import com.app.duolingo.services.DatabaseService;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class QuizFragment extends Fragment {

    private String course;
    private DatabaseService databaseService;
    private List<QuizQuestion> quizQuestions;
    private int currentQuestionIndex = 0;
    private TextView tvQuestion, tvScore;
    private Button[] optionButtons;
    private Button btnNext;
    private FirebaseAuth auth;
    private int correctAnswers = 0;
    private int totalQuestions = 0;
    private int score = 0;

    public QuizFragment() {
        // Required empty public constructor
    }

    public static QuizFragment newInstance(String courseId) {
        QuizFragment fragment = new QuizFragment();
        Bundle args = new Bundle();
        args.putString("COURSE_KEY", courseId);
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
        View view = inflater.inflate(R.layout.fragment_quiz, container, false);

        tvQuestion = view.findViewById(R.id.tvQuestion);
        optionButtons = new Button[] {
                view.findViewById(R.id.btnOption1),
                view.findViewById(R.id.btnOption2),
                view.findViewById(R.id.btnOption3),
                view.findViewById(R.id.btnOption4)
        };
        btnNext = view.findViewById(R.id.btnNext);
        tvScore = view.findViewById(R.id.tvScore);
        updateScore();
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentQuestionIndex < quizQuestions.size() - 1) {
                    currentQuestionIndex++;
                    loadQuestion(quizQuestions.get(currentQuestionIndex));
                } else {
                    showCompletionDialog();
                }
            }
        });

        fetchWordsForCourse();
        return view;
    }

    private void fetchWordsForCourse() {
        databaseService.fetchCourseWords(course, new DatabaseService.Callback<List<Word>>() {
            @Override
            public void onResult(List<Word> words) {
                updateUIWithWords(words);
                loadQuestion(quizQuestions.get(currentQuestionIndex));
            }
            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(), "Error fetching words", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUIWithWords(List<Word> words) {
        Collections.shuffle(words);
        quizQuestions = new ArrayList<>();

        for (Word word : words) {
            List<String> options = generateOptions(words, word);
            String questionSentence = "What is the meaning of " + word.getEnglish();
            quizQuestions.add(new QuizQuestion(questionSentence, options, word.getMeaning()));
        }

        totalQuestions = quizQuestions.size();
        correctAnswers = 0;
        updateScore();
    }

    private void loadQuestion(QuizQuestion question) {
        tvQuestion.setText(question.getQuestion());
        for (int i = 0; i < optionButtons.length; i++) {
            Button optionButton = optionButtons[i];
            optionButtons[i].setText(question.getOptions().get(i));
            optionButtons[i].setEnabled(true);
            optionButtons[i].setBackgroundColor(Color.parseColor("#8692f7"));

            optionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onOptionSelected(optionButton, question.getCorrectOption());
                }
            });
        }
        btnNext.setEnabled(false); // Disable next button until an option is selected
    }

    private void onOptionSelected(Button selectedButton, String correctAnswer) {
        String selectedAnswer = selectedButton.getText().toString();

        boolean isCorrect = selectedAnswer.equals(correctAnswer);

        selectedButton.setBackgroundColor(isCorrect ? Color.GREEN : Color.RED);

        if (isCorrect) {
            correctAnswers++;
            playAudio(true);
            updateScore();
        }

        if (!isCorrect) {
            highlightCorrectButton(correctAnswer);
            playAudio(false);
        }

        disableOptionButtons();
        btnNext.setEnabled(true);
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

    private void showCompletionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Quiz Completed");
        builder.setMessage("Your score: " + score);

        builder.setPositiveButton("Go to Home", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                savePointRecord();
                navigateToHomeFragment();
            }
        });

        builder.setNegativeButton("Try again", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                QuizFragment quizFragment = QuizFragment.newInstance(course);
                Bundle args = new Bundle();
                args.putString("COURSE_KEY", course);
                quizFragment.setArguments(args);
                FragmentManager fragmentManager = getParentFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.frame_layout, quizFragment);
                transaction.commit();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
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

    private void updateScore() {
        if (totalQuestions > 0) {
            score = (correctAnswers * 100) / totalQuestions;
            tvScore.setText("Score: " + score);
        }
    }

    private void highlightCorrectButton(String correctOption) {
        for (Button optionButton : optionButtons) {
            if (optionButton.getText().toString().equals(correctOption)) {
                optionButton.setBackgroundColor(Color.GREEN);
            }
        }
    }

    private void disableOptionButtons() {
        for (Button optionButton : optionButtons) {
            optionButton.setEnabled(false);
        }
    }

    private void navigateToHomeFragment() {
        HomeFragment homeFragment = new HomeFragment(); // Replace with your home fragment's constructor
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.frame_layout, homeFragment); // Replace 'fragment_container' with your actual container ID
        fragmentTransaction.addToBackStack(null); // Optional, if you want to add the transaction to the back stack
        fragmentTransaction.commit();
    }

    private List<String> generateOptions(List<Word> words, Word correctWord) {
        List<String> options = new ArrayList<>();
        options.add(correctWord.getMeaning());

        Random random = new Random();
        while (options.size() < 4) {
            Word optionWord = words.get(random.nextInt(words.size()));
            if (!optionWord.getMeaning().equals(correctWord.getMeaning()) && !options.contains(optionWord.getMeaning())) {
                options.add(optionWord.getMeaning());
            }
        }

        Collections.shuffle(options);
        return options;
    }
}