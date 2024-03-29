package com.myfitnesspal.assignment.newsapp.activities.abs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.myfitnesspal.assignment.newsapp.R;

/*
* An abstract class that helps to load a single fragment
 * */

public abstract class SingleFragmentActivity extends AppCompatActivity {


    protected abstract Fragment createFragment();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_fragment);

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);
        if(fragment == null){
            fragment = createFragment();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container,fragment).commit();
        }
    }
}
