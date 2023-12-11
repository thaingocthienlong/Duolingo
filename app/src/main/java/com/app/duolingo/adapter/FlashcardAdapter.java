package com.app.duolingo.adapter;

import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.app.duolingo.R;
import com.app.duolingo.models.Word;
import com.wajahatkarim3.easyflipview.EasyFlipView;

import java.io.IOException;
import java.util.List;

public class FlashcardAdapter extends RecyclerView.Adapter<FlashcardAdapter.FlashcardViewHolder> {
    public List<Word> words;

    public FlashcardAdapter(List<Word> words) {
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
        holder.tvWordNumberFront.setText(String.valueOf(position + 1) + "/" + words.size());

        holder.tvWordBack.setText(word.getEnglish());
        holder.tvMeaningBack.setText(word.getMeaning());
        holder.tvPronounceBack.setText(word.getPronounce());
        holder.tvWordNumberBack.setText(String.valueOf(position + 1) + "/" + words.size());

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
        TextView tvWordFront, tvMeaningFront, tvPronounceFront, tvWordBack, tvMeaningBack, tvPronounceBack, tvWordNumberFront, tvWordNumberBack;
        Button btnPlayAudioBack, btnPlayAudioFront;
        public FlashcardViewHolder(View itemView) {
            super(itemView);

            tvWordFront = itemView.findViewById(R.id.tvWordFront);
            tvMeaningFront = itemView.findViewById(R.id.tvMeaningFront);
            tvPronounceFront = itemView.findViewById(R.id.tvPronounceFront);
            btnPlayAudioFront = itemView.findViewById(R.id.btnPlayAudioFront);
            tvWordNumberFront = itemView.findViewById(R.id.tvWordNumberFront);

            tvWordBack = itemView.findViewById(R.id.tvWordBack);
            tvMeaningBack = itemView.findViewById(R.id.tvMeaningBack);
            tvPronounceBack = itemView.findViewById(R.id.tvPronounceBack);
            btnPlayAudioBack = itemView.findViewById(R.id.btnPlayAudioBack);
            tvWordNumberBack = itemView.findViewById(R.id.tvWordNumberBack);
        }
    }

    private void playAudio(String audioUrl) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioUrl);
            mediaPlayer.prepareAsync();

            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            }
        });
    }
}
