package com.app.duolingo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.duolingo.R;
import com.app.duolingo.models.Course;
import com.app.duolingo.models.Progress;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoursesAdapter extends RecyclerView.Adapter<CoursesAdapter.CourseViewHolder> {

    private List<Course> courses = new ArrayList<>();
    private OnCourseItemClickListener listener;
    private TextView courseNameTextView;
    private TextView courseDescriptionTextView;
    private ProgressBar progressBar;
    private TextView progressTextView;
    private Map<String, Progress> progressMap = new HashMap<>();
    private FragmentManager fragmentManager;

    public CoursesAdapter(FragmentManager fragmentManager,OnCourseItemClickListener listener) {
        this.listener = listener;
        this.fragmentManager = fragmentManager;
    }

    @Override
    public CourseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CourseViewHolder holder, int position) {
        Course course = courses.get(position);
        Progress progress = progressMap != null ? progressMap.get(course.getId()) : null;

        courseNameTextView = holder.itemView.findViewById(R.id.textViewCourseTitle);
        courseDescriptionTextView = holder.itemView.findViewById(R.id.textViewCourseDescription);
        progressBar = holder.itemView.findViewById(R.id.progressBarCourse);
        progressTextView = holder.itemView.findViewById(R.id.textViewProgressPercentage);

        courseNameTextView.setText(course.getName());
        courseDescriptionTextView.setText(course.getDescription());
        if (progress != null) {
            progressBar.setProgress((int) progress.getLearnProgress());
            progressTextView.setText((int) progress.getLearnProgress() + "%");
        }else{
            progressBar.setProgress(0);
            progressTextView.setText("0%");
        }

        holder.bind(course, listener);
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
        notifyDataSetChanged();
    }

    public interface OnCourseItemClickListener {
        void onCourseItemClick(Course course);
    }

    static class CourseViewHolder extends RecyclerView.ViewHolder {

        public CourseViewHolder(View itemView) {
            super(itemView);
        }

        public void bind(final Course course, final OnCourseItemClickListener listener) {
            // Bind course data to UI elements
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onCourseItemClick(course);
                }
            });
        }
    }

    public void setProgressMap(Map<String, Progress> progressMap) {
        this.progressMap = progressMap;
        notifyDataSetChanged();
    }
}
