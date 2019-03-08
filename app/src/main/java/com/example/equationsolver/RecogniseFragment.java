package com.example.equationsolver;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class RecogniseFragment extends Fragment {

    public RecogniseFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recognise, container, false);
    }

    public static RecogniseFragment newInstance() {
        return new RecogniseFragment();
    }

}
