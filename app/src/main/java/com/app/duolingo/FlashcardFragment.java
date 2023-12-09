package com.app.duolingo;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.app.duolingo.models.CourseWord;
import com.app.duolingo.models.Word;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wajahatkarim3.easyflipview.EasyFlipView;

import android.media.MediaPlayer;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

public class FlashcardFragment extends Fragment {

    private ViewPager2 viewPagerWords;
    private FlashcardAdapter flashcardAdapter;
    private Button btnNext;
    private List<Word> wordList;

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

        String courseId = getArguments().getString("COURSE_ID");

        flashcardAdapter = new FlashcardAdapter(new ArrayList<>());
        viewPagerWords.setAdapter(flashcardAdapter);

        fetchCourseWords(courseId, new Callback<List<Word>>() {
            @Override
            public void onResult(List<Word> result) {
                if (getActivity() == null || flashcardAdapter == null) {
                    Log.e("FlashcardFragment", "Activity or adapter is not initialized");
                    return;
                }
                Log.e("FlashcardFragment", "Setting words");
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


        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentItem = viewPagerWords.getCurrentItem();
                if (currentItem < flashcardAdapter.getItemCount() - 1) {
                    viewPagerWords.setCurrentItem(currentItem + 1);
                } else {
                    btnNext.setEnabled(false);
                }
            }
        });


        return view;
    }

    // Placeholder for the method to fetch words for a given courseId from the data source
    private void fetchCourseWords(String courseId, final Callback<List<Word>> callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        List<Task<DataSnapshot>> tasks = new ArrayList<>();

        databaseReference.child("course_words").orderByChild("courseId").equalTo(courseId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                CourseWord courseWord = snapshot.getValue(CourseWord.class);
                                if (courseWord != null) {
                                    Task<DataSnapshot> wordFetchTask = databaseReference.child("words").child(courseWord.getWordId()).get();
                                    tasks.add(wordFetchTask);
                                }
                            }

                            Tasks.whenAllComplete(tasks).addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
                                @Override
                                public void onComplete(@NonNull Task<List<Task<?>>> task) {
                                    if (task.isSuccessful()) {
                                        List<Word> words = new ArrayList<>();
                                        for (Task<DataSnapshot> wordTask : tasks) {
                                            DataSnapshot wordSnapshot = wordTask.getResult();
                                            Word word = wordSnapshot.getValue(Word.class);
                                            if (word != null) {
                                                words.add(word);
                                            }
                                        }
                                        callback.onResult(words);
                                    } else {
                                        callback.onError(task.getException());
                                    }
                                }
                            });
                        } else {
                            callback.onResult(new ArrayList<>()); // No course words found
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        callback.onError(databaseError.toException());
                    }
                });
    }


    public interface Callback<T> {
        void onResult(T result);
        void onError(Exception e);
    }

    public class FlashcardAdapter extends RecyclerView.Adapter<FlashcardAdapter.FlashcardViewHolder> {
        private List<Word> words;

        FlashcardAdapter(List<Word> words) {
            this.words = words;
        }

        @Override
        public FlashcardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.flashcard_item, parent, false);
            return new FlashcardViewHolder(view);
        }

        @Override
        public void onBindViewHolder(FlashcardViewHolder holder, int position) {
            Word word = words.get(position);

            holder.tvWordFront.setText(word.getEnglish());
            holder.tvMeaningFront.setText(word.getMeaning());
            holder.tvPronounceFront.setText(word.getPronounce());

            holder.tvWordBack.setText(word.getEnglish());
            holder.tvMeaningBack.setText(word.getMeaning());
            holder.tvPronounceBack.setText(word.getPronounce());

            holder.btnPlayAudioFront.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playAudio(word.getSound());
                }
            });
            holder.btnPlayAudioBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playAudio(word.getSound());
                }
            });

            EasyFlipView flipView = holder.itemView.findViewById(R.id.flipView);
            flipView.setOnClickListener(v -> flipView.flipTheView());
        }

        @Override
        public int getItemCount() {
            return words == null ? 0 : words.size();
        }

        public void setWords(List<Word> result) {
            this.words = result;
            notifyDataSetChanged();
        }

        public class FlashcardViewHolder extends RecyclerView.ViewHolder {
            TextView tvWordFront, tvMeaningFront, tvPronounceFront, tvWordBack, tvMeaningBack, tvPronounceBack;
            Button btnPlayAudioBack, btnPlayAudioFront;
            public FlashcardViewHolder(View itemView) {
                super(itemView);

                tvWordFront = itemView.findViewById(R.id.tvWordFront);
                tvMeaningFront = itemView.findViewById(R.id.tvMeaningFront);
                tvPronounceFront = itemView.findViewById(R.id.tvPronounceFront);
                btnPlayAudioFront = itemView.findViewById(R.id.btnPlayAudioFront);

                tvWordBack = itemView.findViewById(R.id.tvWordBack);
                tvMeaningBack = itemView.findViewById(R.id.tvMeaningBack);
                tvPronounceBack = itemView.findViewById(R.id.tvPronounceBack);
                btnPlayAudioBack = itemView.findViewById(R.id.btnPlayAudioBack);
            }
        }

        private void playAudio(String audioUrl) {
            MediaPlayer mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(audioUrl); // Set the audio URL
                mediaPlayer.prepareAsync(); // Prepare the MediaPlayer asynchronously

                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.start(); // Start playback when media player is ready
                    }
                });
            } catch (IOException e) {
                e.printStackTrace(); // Handle exceptions
            }

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.release(); // Release the MediaPlayer when playback is complete
                }
            });
        }
    }

}
