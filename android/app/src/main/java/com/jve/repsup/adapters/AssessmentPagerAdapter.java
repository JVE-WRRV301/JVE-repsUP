package com.jve.repsup.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.jve.repsup.fragments.AssessmentAgeFragment;
import com.jve.repsup.fragments.AssessmentBodyTypeFragment;
import com.jve.repsup.fragments.AssessmentGenderFragment;
import com.jve.repsup.fragments.AssessmentGoalsFragment;
import com.jve.repsup.fragments.AssessmentHeightFragment;
import com.jve.repsup.fragments.AssessmentScheduleFragment;
import com.jve.repsup.fragments.AssessmentWeightFragment;
import com.jve.repsup.fragments.AssessmentWelcomeFragment;
import com.jve.repsup.fragments.AssessmentWorkoutLevelFragment;

/**
 * FragmentStateAdapter for the Fitness Assessment ViewPager2.
 * Hosts 9 steps in order.
 */
public class AssessmentPagerAdapter extends FragmentStateAdapter {

    public static final int TOTAL_STEPS = 9;

    public AssessmentPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new AssessmentWelcomeFragment();
            case 1: return new AssessmentGenderFragment();
            case 2: return new AssessmentAgeFragment();
            case 3: return new AssessmentWeightFragment();
            case 4: return new AssessmentHeightFragment();
            case 5: return new AssessmentBodyTypeFragment();
            case 6: return new AssessmentWorkoutLevelFragment();
            case 7: return new AssessmentScheduleFragment();
            case 8: return new AssessmentGoalsFragment();
            default: return new AssessmentWelcomeFragment();
        }
    }

    @Override
    public int getItemCount() {
        return TOTAL_STEPS;
    }
}
