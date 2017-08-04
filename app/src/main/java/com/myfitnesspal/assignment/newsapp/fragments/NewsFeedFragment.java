package com.myfitnesspal.assignment.newsapp.fragments;


import android.content.BroadcastReceiver;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
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
import android.widget.FrameLayout;
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
import com.myfitnesspal.assignment.newsapp.R;
import com.myfitnesspal.assignment.newsapp.adapters.NewsFeedRecyclerViewAdapter;
import com.myfitnesspal.assignment.newsapp.adapters.PaginationScrollListener;
import com.myfitnesspal.assignment.newsapp.fragments.abs.VisibleFragment;
import com.myfitnesspal.assignment.newsapp.models.NewsStories;
import com.myfitnesspal.assignment.newsapp.utils.AppUtils;
import com.myfitnesspal.assignment.newsapp.utils.ConnectivityBroadcastReceiver;
import com.myfitnesspal.assignment.newsapp.utils.NetworkUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Fragment responsible to fetch data from NYtimes API and displays them in a list view
 */
public class NewsFeedFragment extends VisibleFragment implements
        SharedPreferences.OnSharedPreferenceChangeListener {


    //Constants for saving data and maintaining state across configuration changes
    private static final String SAVE_NEWS_STORIES = "saveNewsStories";
    private static final String SAVE_FIRST_VISIBLE_ITEM_POSITION = "saveFirstVisibleItemPosition";
    private static final String SAVE_BOOLEAN_RESULTS_FOUND = "saveBooleanResultsFound";
    private static final String SAVE_QUERY_TAG = "save_query_tag";
    private static final String SAVE_CURRENT_PAGE = "saveQueryForRotation";
    private static final String SAVE_TYPED_QUERY = "saveTypedQuery";


    //Binding views using ButterKnife
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

    //Used for debugging purpose
    private static final String TAG = NewsFeedFragment.class.getSimpleName();

    //Member variables needed for the scroll listener
    private static final int PAGE_START = 0;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int mTotalPages = 5;
    private int mCurrentPage = PAGE_START;
    private boolean isFirstPageLoading = false;
    private NewsFeedRecyclerViewAdapter mAdapter;
    private int mFirstVisibleItemPosition = 0;

    //To persist user query
    private SharedPreferences mSharedPreferences;

    //Volley request queue
    private RequestQueue mRequestQueue;


    private boolean mFoundResults = true;
    private String mSaveTypedText;



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
        //listening for changes in SharedPreferences and handling changes accordingly
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_news_feed, container, false);

        ButterKnife.bind(this,view);
        ButterKnife.setDebug(true);

        //Setting support toolbar as the action toolbar
        mToolbar.setTitleTextColor(getResources().getColor(R.color.white));
        ((AppCompatActivity)getActivity()).setSupportActionBar(mToolbar);

        //Instantiating News feed fragment recycler view
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
        //Caching volley requests for better performance
        mRequestQueue =  new RequestQueue(cache, network);
        mRequestQueue.start();


        //Swipe to refresh results
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(AppUtils.isNetworkAvailableAndConnected(getActivity())){
                    mCurrentPage = 0;
                    isFirstPageLoading = true;
                    makeActionSearchApiQuery(0);
                }
            }
        });

        //Setting colors to the loading indicator
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);


        //Here's where we handle configuration changes
        if(savedInstanceState != null){

            mSaveTypedText = savedInstanceState.getString(SAVE_TYPED_QUERY);
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


        //Fetching SearchView and handling user inputs
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
                mSaveTypedText = newText;
                return true;
            }
        });

        //Makes sure that user's data is saved across device rotation
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String query = getQuery();
                if(query != null && !TextUtils.isEmpty(query)) {
                    searchView.setQuery(query, false);
                }else if(mSaveTypedText != null && !TextUtils.isEmpty(mSaveTypedText)){
                    searchView.setQuery(mSaveTypedText,false);
                }

            }
        });

        searchView.setQueryHint(getString(R.string.search_for_news_articles));

        //Helper method to close and clear out searchview focus
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

    //Loads the first page
    private void loadFirstPage(){
        mCurrentPage = 0;
        isFirstPageLoading = true;
        hideNoResultsFoundMessage();
        mNewsFeedRecyclerView.setVisibility(View.GONE);
        mLoadingProgressBar.setVisibility(View.VISIBLE);
        makeActionSearchApiQuery(0);
    }

    //Loads consequent pages
    private void loadNextPage(){
        makeActionSearchApiQuery(mCurrentPage);
    }

    //Performs HTTP request and fetches the data asynchronously
    private void makeActionSearchApiQuery(int page){
        String url = NetworkUtils.buildArticleStoriesUrl(getQuery(), page);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        List<NewsStories> newsStories =  new ArrayList<>();
                        NetworkUtils.parseItems(newsStories,response);
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
                                Log.d(TAG,"Number of hits: " + newsStories.get(0).getHits() + ", mCurrentPAge " + mCurrentPage);
                                mTotalPages = newsStories.get(0).getHits() / 10 ;
                                mAdapter.setNewsStories(newsStories);

                                if (mCurrentPage <= mTotalPages) mAdapter.addLoadingFooter();
                                else{ isLastPage = true; isLoading = false; }
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
                        mAdapter.removeLoadingFooter();
                        final Snackbar snackbar = Snackbar.make(mFrameLayout,R.string.no_more_search_results,
                                Snackbar.LENGTH_SHORT);
                        snackbar.setActionTextColor(getActivity().getResources().getColor(R.color.colorPrimary))
                                .setAction(R.string.dismiss, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        snackbar.dismiss();
                                    }
                                }).show();
                    }
                });
        mRequestQueue.add(jsonObjectRequest);


    }

    //Helper method to display error message
    private void showNoResultsFoundMessage(){
        mFoundResults = false;
        mNewsFeedRecyclerView.setVisibility(View.GONE);
        mErrorMessageFL.setVisibility(View.VISIBLE);
        mErrorMessageTextView.setText(R.string.no_results_found);
    }


    //Helper method to hide error message
    private void hideNoResultsFoundMessage(){
        if(mErrorMessageFL.getVisibility() == View.VISIBLE){
            mFoundResults = false;
            mErrorMessageFL.setVisibility(View.GONE);
        }
    }

    //Persists user query
    private void saveQuery(String query){
        mSharedPreferences
                .edit()
                .putString(SAVE_QUERY_TAG, query)
                .apply();
    }

    //Fetches user query
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
        outState.putString(SAVE_TYPED_QUERY, mSaveTypedText);
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
