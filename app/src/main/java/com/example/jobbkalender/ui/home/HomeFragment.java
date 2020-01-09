package com.example.jobbkalender.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.content.Intent;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.jobbkalender.CreateEvent;
import com.example.jobbkalender.R;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button buttonAddEvent = view.findViewById(R.id.buttonAddEvent);
        buttonAddEvent.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), CreateEvent.class);
                startActivity(intent);
            }
        });
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        final TextView textView = root.findViewById(R.id.text_home);
        homeViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);


            }
        });
        return root;
    }


}