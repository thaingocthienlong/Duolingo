package com.app.duolingo.services;

import androidx.annotation.NonNull;

import com.app.duolingo.models.CourseWord;
import com.app.duolingo.models.Point;
import com.app.duolingo.models.Progress;
import com.app.duolingo.models.Word;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import java.util.ArrayList;
import java.util.List;

public class DatabaseService {

    private DatabaseReference databaseReference;

    public DatabaseService() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    public void updateLearnProgress(String progressRecordRefId, int progressPercentage, final Callback<Void> callback) {
        DatabaseReference progressUpdateRef = FirebaseDatabase.getInstance().getReference()
                .child("progress").child(progressRecordRefId);

        progressUpdateRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Integer currentLearnProgress = dataSnapshot.child("learnProgress").getValue(Integer.class);
                    if (currentLearnProgress == null || progressPercentage > currentLearnProgress) {
                        progressUpdateRef.child("learnProgress").setValue(progressPercentage)
                                .addOnSuccessListener(aVoid -> callback.onResult(null))
                                .addOnFailureListener(callback::onError);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.toException());
            }
        });
    }

    public void getProgressRecordRefId(String userId, String courseId, final Callback<String> callback) {
        DatabaseReference progressRef = FirebaseDatabase.getInstance().getReference().child("progress");
        progressRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean recordFound = false;
                String progressRecordRefId = null;

                for (DataSnapshot progressSnapshot : dataSnapshot.getChildren()) {
                    String userIdInRecord = progressSnapshot.child("userId").getValue(String.class);
                    String courseIdInRecord = progressSnapshot.child("courseId").getValue(String.class);

                    if (userId.equals(userIdInRecord) && courseId.equals(courseIdInRecord)) {
                        progressRecordRefId = progressSnapshot.getKey();
                        recordFound = true;
                        break;
                    }
                }

                if (!recordFound) {
                    DatabaseReference newProgressRef = FirebaseDatabase.getInstance().getReference().child("progress").push();
                    progressRecordRefId = newProgressRef.getKey();
                    Progress newProgress = new Progress();
                    newProgress.setUserId(userId);
                    newProgress.setCourseId(courseId);
                    newProgress.setLearnProgress(0);
                    newProgress.setReviewProgress(0);
                    newProgressRef.setValue(newProgress);
                    callback.onResult(newProgressRef.getKey());
                }

                if (recordFound) {
                    callback.onResult(progressRecordRefId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.toException());
            }
        });
    }

    public void fetchCourseWords(String courseId, final Callback<List<Word>> callback) {
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

    public void savePoint(Point point, SavePointCallback callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("points").push().setValue(point)
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e);
                });
    }

    public interface SavePointCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    // Define the Callback interface if it's not already globally defined
    public interface Callback<T> {
        void onResult(T result);
        void onError(Exception e);
    }
}
