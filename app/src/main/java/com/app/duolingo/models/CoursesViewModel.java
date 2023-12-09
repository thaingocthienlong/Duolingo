package com.app.duolingo.models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.app.duolingo.models.Course;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoursesViewModel extends ViewModel {

    private final MutableLiveData<List<Course>> coursesLiveData = new MutableLiveData<>();
    private final MutableLiveData<Map<String, Progress>> progressLiveData = new MutableLiveData<>();

    public CoursesViewModel() {
        loadCoursesFromFirebase();
        loadProgressFromFirebase();
    }

    private void loadCoursesFromFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("courses");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Course> courseList = new ArrayList<>();
                for (DataSnapshot courseSnapshot : dataSnapshot.getChildren()) {
                    Course course = courseSnapshot.getValue(Course.class);
                    courseList.add(course);
                }
                coursesLiveData.setValue(courseList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors.
            }
        });
    }

    private void loadProgressFromFirebase() {
        DatabaseReference progressRef = FirebaseDatabase.getInstance().getReference("progress");

        progressRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Progress> progressMap = new HashMap<>();
                for (DataSnapshot progressSnapshot : dataSnapshot.getChildren()) {
                    Progress progress = progressSnapshot.getValue(Progress.class);
                    progressMap.put(progress.getCourseId(), progress);
                }
                progressLiveData.setValue(progressMap);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors.
            }
        });
    }


    public LiveData<List<Course>> getCourses() {
        return coursesLiveData;
    }

    public LiveData<Map<String, Progress>> getProgress() {
        return progressLiveData;
    }

}

