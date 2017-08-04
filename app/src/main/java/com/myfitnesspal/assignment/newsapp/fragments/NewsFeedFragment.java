package com.myfitnesspal.assignment.newsapp.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.myfitnesspal.assignment.newsapp.R;
import com.myfitnesspal.assignment.newsapp.adapters.NewsFeedRecyclerViewAdapter;
import com.myfitnesspal.assignment.newsapp.adapters.PaginationScrollListener;
import com.myfitnesspal.assignment.newsapp.fragments.abs.VisibleFragment;
import com.myfitnesspal.assignment.newsapp.models.NewsStories;
import com.myfitnesspal.assignment.newsapp.utils.AppUtils;
import com.myfitnesspal.assignment.newsapp.utils.ConnectivityBroadcastReceiver;
import com.myfitnesspal.assignment.newsapp.utils.NetworkUtil;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 */
public class NewsFeedFragment extends VisibleFragment implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String SAVE_NEWS_STORIES = "saveNewsStories";
    private static final String SAVE_FIRST_VISIBLE_ITEM_POSITION = "saveFirstVisibleItemPosition";
    private static final String SAVE_BOOLEAN_RESULTS_FOUND = "saveBooleanResultsFound";
    private static final String SAVE_QUERY_TAG = "save_query_tag";
    private static final String SAVE_CURRENT_PAGE = "saveQueryForRotation";

    @BindView(R.id.news_feed_fragment_recycler_view)
    RecyclerView mNewsFeedRecyclerView;

    @BindView(R.id.loading_feed_progress_bar)
    ProgressBar mLoadingProgressBar;

    @BindView(R.id.news_feed_fragment_frame_layout)
    FrameLayout mFrameLayout;

    @BindView(R.id.error_message)
    FrameLayout mErrorMessageFL;

    @BindView(R.id.error_message_text_view)
    TextView mErrorMessageTextView;

    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @BindView(R.id.news_feed_fragment_toolbar)
    Toolbar mToolbar;

    SearchView searchView;
    MenuItem searchItem;

    private static final String TAG = NewsFeedFragment.class.getSimpleName();

    private static final int PAGE_START = 0;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int mTotalPages = 5;
    private int mCurrentPage = PAGE_START;
    private boolean isFirstPageLoading = false;

    private SharedPreferences mSharedPreferences;


    private NewsFeedRecyclerViewAdapter mAdapter;
    private int mFirstVisibleItemPosition = 0;

    private RequestQueue mRequestQueue;
    private boolean mFoundResults = true;


    public NewsFeedFragment() {
        // Required empty public constructor
    }

    public static NewsFeedFragment newInstance(){
        return new NewsFeedFragment();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_news_feed, container, false);

        ButterKnife.bind(this,view);
        ButterKnife.setDebug(true);

        mToolbar.setTitleTextColor(getResources().getColor(R.color.white));
        ((AppCompatActivity)getActivity()).setSupportActionBar(mToolbar);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mNewsFeedRecyclerView.setLayoutManager(linearLayoutManager);
        mAdapter = new NewsFeedRecyclerViewAdapter(getActivity());
        mNewsFeedRecyclerView.setAdapter(mAdapter);
        mNewsFeedRecyclerView.addOnScrollListener(new PaginationScrollListener(linearLayoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                mCurrentPage += 1;

                loadNextPage();
            }

            @Override
            public int getTotalPageCount() {
                return mTotalPages;
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }

            @Override
            public void getFirstVisibleItemPosition(int position) {
                mFirstVisibleItemPosition = position;
            }
        });

        Cache cache = new DiskBasedCache(getActivity().getCacheDir(), 1024 * 1024);
        Network network = new BasicNetwork(new HurlStack());
        mRequestQueue =  new RequestQueue(cache, network);
        mRequestQueue.start();

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(AppUtils.isNetworkAvailableAndConnected(getActivity())){
                    isFirstPageLoading = true;
                    makeActionSearchApiQuery(0);
                }
            }
        });

        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        if(savedInstanceState != null){

            if(mFoundResults){
                mAdapter.setNewsStories(savedInstanceState
                        .<NewsStories>getParcelableArrayList(SAVE_NEWS_STORIES));
                mNewsFeedRecyclerView
                        .scrollToPosition(savedInstanceState.getInt(SAVE_FIRST_VISIBLE_ITEM_POSITION));
                mCurrentPage = savedInstanceState.getInt(SAVE_CURRENT_PAGE);
            }else{
                showNoResultsFoundMessage();
            }

        }else{
            if(AppUtils.isNetworkAvailableAndConnected(getActivity()))
                loadFirstPage();
            else{
                mNewsFeedRecyclerView.setVisibility(View.GONE);
                mErrorMessageFL.setVisibility(View.VISIBLE);
            }

        }


        return view;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.activity_main_menu, menu);

        searchItem = menu.findItem(R.id.app_bar_search);
        searchView = (SearchView) searchItem.getActionView();
        searchItem.setIcon(R.drawable.ic_search_white_24dp);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                AppUtils.hideKeyboard(getActivity(),searchView);
                saveQuery(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String query = getQuery();
                if(query != null && !TextUtils.isEmpty(query))
                    searchView.setQuery(query,false);
            }
        });

        searchView.setQueryHint(getString(R.string.search_for_news_articles));

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                searchView.clearFocus();
                searchView.setFocusable(false);
                searchItem.collapseActionView();
                return false;
            }
        });
    }

    private void loadFirstPage(){
        isFirstPageLoading = true;
        hideNoResultsFoundMessage();
        mNewsFeedRecyclerView.setVisibility(View.GONE);
        mLoadingProgressBar.setVisibility(View.VISIBLE);
        makeActionSearchApiQuery(0);
    }

    private void loadNextPage(){
        makeActionSearchApiQuery(mCurrentPage);
    }

    private void makeActionSearchApiQuery(int page){
        String url = NetworkUtil.buildArticleStoriesUrl(getQuery(), page);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        List<NewsStories> newsStories =  new ArrayList<>();
                        NetworkUtil.parseItems(newsStories,response);
                        mLoadingProgressBar.setVisibility(View.GONE);
                        mSwipeRefreshLayout.setRefreshing(false);
                        if(newsStories.size() > 0){
                            mFoundResults = true;
                            mNewsFeedRecyclerView.setVisibility(View.VISIBLE);
                            mErrorMessageFL.setVisibility(View.GONE);
                            if(!isFirstPageLoading){
                                mAdapter.removeLoadingFooter();
                                mAdapter.addMoreData(newsStories);
                                isLoading = false;
                                if( mCurrentPage != mTotalPages) mAdapter.addLoadingFooter();
                                else isLastPage = true;
                            }else{
                                isFirstPageLoading = false;
                                mNewsFeedRecyclerView.scrollToPosition(0);
                                mTotalPages = newsStories.get(0).getHits() / 10 - 1;
                                mAdapter.setNewsStories(newsStories);
                                if (mCurrentPage <= mTotalPages) mAdapter.addLoadingFooter();
                                else isLastPage = true;
                            }
                        }else{
                            showNoResultsFoundMessage();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mLoadingProgressBar.setVisibility(View.GONE);
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                });
        mRequestQueue.add(jsonObjectRequest);


    }

    private void showNoResultsFoundMessage(){
        mFoundResults = false;
        mNewsFeedRecyclerView.setVisibility(View.GONE);
        mErrorMessageFL.setVisibility(View.VISIBLE);
        mErrorMessageTextView.setText(R.string.no_results_found);
    }

    private void hideNoResultsFoundMessage(){
        if(mErrorMessageFL.getVisibility() == View.VISIBLE){
            mFoundResults = false;
            mErrorMessageFL.setVisibility(View.GONE);
        }
    }

    private void saveQuery(String query){
        mSharedPreferences
                .edit()
                .putString(SAVE_QUERY_TAG, query)
                .apply();
    }

    private String getQuery(){
        return mSharedPreferences.getString(SAVE_QUERY_TAG, null);
    }




    @Override
    public void onDestroy() {
        super.onDestroy();
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        loadFirstPage();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<NewsStories> stories = (ArrayList<NewsStories>) mAdapter.getNewsStories();
        outState.putInt(SAVE_CURRENT_PAGE,mCurrentPage);
        outState.putParcelableArrayList(SAVE_NEWS_STORIES, stories);
        outState.putInt(SAVE_FIRST_VISIBLE_ITEM_POSITION , mFirstVisibleItemPosition);
        outState.putBoolean(SAVE_BOOLEAN_RESULTS_FOUND, mFoundResults);
    }



    @Override
    protected BroadcastReceiver createConnectivityBroadcastReceiver() {
        return new ConnectivityBroadcastReceiver(mFrameLayout);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        switch(itemId){
            case R.id.action_refresh:
                if(AppUtils.isNetworkAvailableAndConnected(getActivity())){
                    loadFirstPage();
                }
                return true;
            case R.id.action_clear:
                if(AppUtils.isNetworkAvailableAndConnected(getActivity())) {
                    saveQuery(null);
                    loadFirstPage();
                    getActivity().invalidateOptionsMenu();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRequestQueue.stop();
    }
}
