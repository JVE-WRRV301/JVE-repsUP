package com.jve.repsup.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;
import com.jve.repsup.R;

/**
 * CalendarFragment displaying the monthly calendar with "My Plan" and "Goals" tabs.
 * The Goals tab hosts GoalsListFragment for viewing and completing goals.
 */
public class CalendarFragment extends Fragment {

    private TabLayout tabLayout;

    public CalendarFragment() {
        // Required empty public constructor
    }

    public static CalendarFragment newInstance() {
        return new CalendarFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tabLayout = view.findViewById(R.id.tabLayoutCalendar);

        setupTabs();

        // Default to Goals tab
        showGoalsTab();
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_my_plan));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_goals));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        showMyPlanTab();
                        break;
                    case 1:
                        showGoalsTab();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Select Goals tab by default
        TabLayout.Tab goalsTab = tabLayout.getTabAt(1);
        if (goalsTab != null) {
            goalsTab.select();
        }
    }

    private void showMyPlanTab() {
        // My Plan tab — placeholder for future implementation (Task 9.3)
        // For now, keep the container empty or show a placeholder
    }

    private void showGoalsTab() {
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.flTabContent, GoalsListFragment.newInstance())
                .commit();
    }
}
