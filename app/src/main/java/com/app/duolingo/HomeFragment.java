package com.app.duolingo;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.duolingo.adapter.CoursesAdapter;
import com.app.duolingo.fragment.FlashcardFragment;
import com.app.duolingo.fragment.ModeFragment;
import com.app.duolingo.models.Course;
import com.app.duolingo.models.CoursesViewModel;
import com.app.duolingo.models.Progress;

import java.util.List;
import java.util.Map;


public class HomeFragment extends Fragment {

    RecyclerView recyclerView;
    CoursesAdapter adapter;
    CoursesViewModel viewModel;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        initializeRecyclerView();

        return view;
    }

    private void initializeRecyclerView() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 1);
        recyclerView.setLayoutManager(gridLayoutManager);

        viewModel = new ViewModelProvider(this).get(CoursesViewModel.class);

        adapter = new CoursesAdapter(getParentFragmentManager(), new CoursesAdapter.OnCourseItemClickListener() {
            @Override
            public void onCourseItemClick(Course course) {
                ModeFragment modeFragment = ModeFragment.newInstance(course.getId());
                FragmentManager fragmentManager = getParentFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.frame_layout, modeFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        recyclerView.setAdapter(adapter);

        viewModel.getCourses().observe(getViewLifecycleOwner(), new Observer<List<Course>>() {
            @Override
            public void onChanged(List<Course> courses) {
                adapter.setCourses(courses);
            }
        });
        viewModel.getProgress().observe(getViewLifecycleOwner(), new Observer<Map<String, Progress>>() {
            @Override
            public void onChanged(Map<String, Progress> progressMap) {
                adapter.setProgressMap(progressMap); // Assuming your adapter has this method
            }
        });
    }

}