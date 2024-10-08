package com.heon9u.alarm_weather_app.alarm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class RingtoneListAdapter extends FragmentPagerAdapter {

    ArrayList<String> arrayList =  new ArrayList<>();
    List<Fragment> fragmentList = new ArrayList<>();

    public void addFragment(Fragment fragment, String title) {
        arrayList.add(title);
        fragmentList.add(fragment);
    }

    public RingtoneListAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return arrayList.get(position);
    }

}
