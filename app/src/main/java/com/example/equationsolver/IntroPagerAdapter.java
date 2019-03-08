package com.example.equationsolver;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class IntroPagerAdapter extends FragmentPagerAdapter {

    int fragCount;

    public IntroPagerAdapter(FragmentManager fm, int fragCount) {
        super(fm);
        this.fragCount = fragCount;
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fragment;
        switch (i) {
            case 0: fragment = ClickFragment.newInstance();
            break;
            case 1: fragment = RecogniseFragment.newInstance();
            break;
            case 2: fragment = SolutionFragment.newInstance();
            break;
            default: fragment = null;
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return fragCount;
    }
}
