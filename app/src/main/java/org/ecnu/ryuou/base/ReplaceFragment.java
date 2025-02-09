package org.ecnu.ryuou.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import org.ecnu.ryuou.base.BasePager;

public class ReplaceFragment extends Fragment {

    private BasePager currPager;

    public ReplaceFragment(BasePager pager) {
        this.currPager = pager;
    }

    public ReplaceFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return currPager.rootView;
    }
}