package com.myfitnesspal.assignment.newsapp;

import android.app.SearchManager;
import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout)
                findViewById(R.id.collapsing_toolbar_layout);

        AppBarLayout appBarLayout =(AppBarLayout) findViewById(R.id.app_bar_layout);

        /*final SearchView searchView = (SearchView) findViewById(R.id.search_view_test);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d("MainActivity", "Query submitted "+ query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });*/
/*        Toolbar toolbar = (Toolbar) findViewById(R.id.search_toolbar);
        toolbar.inflateMenu(R.menu.activity_main_menu);
        Menu test = toolbar.getMenu();

        MenuItem searchItem = test.findItem(R.id.app_bar_search);
        final SearchView searchView =(SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d("MainActivity", "Query submitted "+ query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });*/

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbarLayout.setTitle(" ");
                    isShow = true;
                   // searchView.setVisibility(View.GONE);
                } else if(isShow) {
                    collapsingToolbarLayout.setTitle(getString(R.string.app_name));
                    isShow = false;
                  //  searchView.setVisibility(View.VISIBLE);
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.app_bar_search);
        SearchView searchView =(SearchView) searchItem.getActionView();

        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getActionBar() !=null)
                    getActionBar().setTitle(" ");
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d("MainActivity", "Query submitted "+ query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }
}
