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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
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
        LoaderManager.LoaderCallbacks<List<NewsStories>>,
        SharedPreferences.OnSharedPreferenceChangeListener {


    private static final String SEARCH_QUERY_EXTRA = "searchQueryExtra";
    private static final String PAGE_QUERY_EXTRA = "pageQueryExtra";
    private static final String SAVE_NEWS_STORIES = "saveNewsStories";
    private static final String SAVE_FIRST_VISIBLE_ITEM_POSITION = "saveFirstVisibleItemPosition";

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

    private static final int NEWS_STORY_SEARCH_LOADER = 25;
    private static final String TAG = NewsFeedFragment.class.getSimpleName();

    private static final int PAGE_START = 0;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int mTotalPages = 5;
    private int mCurrentPage = PAGE_START;
    private boolean isFirstPageLoading = false;

    private SharedPreferences mSharedPreferences;
    private static final String SAVE_QUERY_TAG = "save_query_tag";
    private static final String SAVE_CURRENT_PAGE = "saveQueryForRotation";


    private NewsFeedRecyclerViewAdapter mAdapter;
    private int mFirstVisibleItemPosition = 0;

    private RequestQueue mRequestQueue;


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

        Log.d(TAG,"in on Create of NewsFeedFragment");
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_news_feed, container, false);
        ButterKnife.bind(this,view);

        Log.d(TAG,"oncreateview called");
        ButterKnife.setDebug(true);
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

        Cache cache = new DiskBasedCache(getActivity().getCacheDir(), 1024 * 1024); // 1MB cap

        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());

        mRequestQueue =  new RequestQueue(cache, network);
        mRequestQueue.start();

        if(savedInstanceState != null){
            mAdapter.setNewsStories(savedInstanceState
                            .<NewsStories>getParcelableArrayList(SAVE_NEWS_STORIES));
            mNewsFeedRecyclerView
                    .scrollToPosition(savedInstanceState.getInt(SAVE_FIRST_VISIBLE_ITEM_POSITION));
            mCurrentPage = savedInstanceState.getInt(SAVE_CURRENT_PAGE);
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

        MenuItem searchItem = menu.findItem(R.id.app_bar_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, " Submitted query: " + query);
                AppUtils.hideKeyboard(getActivity(),searchView);
                //searchView.clearFocus();
                saveQuery(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void loadFirstPage(){
        isFirstPageLoading = true;
        mNewsFeedRecyclerView.setVisibility(View.GONE);
        mLoadingProgressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "current page: " + mCurrentPage + " " + isFirstPageLoading);
        //makeActionSearchApiQuery(getQuery(),0);
        //makeActionSearchApiQuery(0);


    }

    private void loadNextPage(){
       // makeActionSearchApiQuery(getQuery(), mCurrentPage);
        makeActionSearchApiQuery(mCurrentPage);
    }

    private void makeActionSearchApiQuery(String query, int page){
        Bundle queryBundle = new Bundle();
        queryBundle.putString(SEARCH_QUERY_EXTRA, query);
        queryBundle.putInt(PAGE_QUERY_EXTRA, page);

        LoaderManager loaderManager = getActivity().getSupportLoaderManager();
        Loader<List<NewsStories>> newsFeedLoader = loaderManager.getLoader(NEWS_STORY_SEARCH_LOADER);

        if(newsFeedLoader == null){
            loaderManager.initLoader(NEWS_STORY_SEARCH_LOADER, queryBundle, this);
        }else{
            loaderManager.restartLoader(NEWS_STORY_SEARCH_LOADER, queryBundle,this);
        }
    }

    private void makeActionSearchApiQuery(int page){
        String url = NetworkUtil.buildArticleStoriesUrl(getQuery(), page);
        Log.d(TAG, "making request with volley: " + url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "Inside volley response with: " +response.toString());
                        List<NewsStories> newsStories =  new ArrayList<>();
                        NetworkUtil.parseItems(newsStories,response);
                        mLoadingProgressBar.setVisibility(View.GONE);
                        Log.d(TAG, "News stories fetched size " + newsStories.size());
                        if(newsStories.size() > 0){
                            mNewsFeedRecyclerView.setVisibility(View.VISIBLE);
                            mErrorMessageFL.setVisibility(View.GONE);
                            Log.d(TAG, "loading First Page: " + isFirstPageLoading);
                            if(!isFirstPageLoading){
                                mAdapter.removeLoadingFooter();
                                mAdapter.addMoreData(newsStories);
                                isLoading = false;
                                if( mCurrentPage != mTotalPages) mAdapter.addLoadingFooter();
                                else isLastPage = true;
                            }else{

                                isFirstPageLoading = false;
                                mTotalPages = newsStories.get(0).getHits() / 10 - 1;
                                mAdapter.setNewsStories(newsStories);
                                if (mCurrentPage <= mTotalPages) mAdapter.addLoadingFooter();
                                else isLastPage = true;
                            }
                        }else{
                            //mLoadingProgressBar.setVisibility(View.GONE);
                            mNewsFeedRecyclerView.setVisibility(View.GONE);
                            mErrorMessageFL.setVisibility(View.VISIBLE);
                            mErrorMessageTextView.setText(R.string.no_results_found);
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mAdapter.removeLoadingFooter();
                        isLoading = false;
                    }
                });
        mRequestQueue.add(jsonObjectRequest);


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
    public Loader<List<NewsStories>> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<List<NewsStories>>(getActivity()) {
            List<NewsStories> NewsStories;
            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                if(args == null){
                    return;
                }

                if(NewsStories != null){
                    deliverResult(NewsStories);
                }else{
                    mLoadingProgressBar.setVisibility(View.VISIBLE);
                    forceLoad();
                }

            }

            @Override
            public void deliverResult(List<NewsStories> data) {
                NewsStories = data;
                super.deliverResult(NewsStories);
            }

            @Override
            public List<NewsStories> loadInBackground() {
                String searchQueryString = args.getString(SEARCH_QUERY_EXTRA);
                int pageQueryString = args.getInt(PAGE_QUERY_EXTRA);

                if(searchQueryString == null || TextUtils.isEmpty(searchQueryString)){
                    if(pageQueryString >= 0)
                        return NetworkUtil.downloadArticleStories(pageQueryString);

                }else{
                    if(pageQueryString>=0){
                        return NetworkUtil.downloadArticleStories(searchQueryString, pageQueryString);
                    }
                }

                return null;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<List<NewsStories>> loader, List<NewsStories> data) {
        mLoadingProgressBar.setVisibility(View.GONE);
        if(data != null){
            if(!isFirstPageLoading){
                mAdapter.removeLoadingFooter();
                mAdapter.addMoreData(data);
                isLoading = false;
                if( mCurrentPage != mTotalPages) mAdapter.addLoadingFooter();
                else isLastPage = true;
            }else{

                if(data.size() > 0){
                    mNewsFeedRecyclerView.setVisibility(View.VISIBLE);
                    mErrorMessageFL.setVisibility(View.GONE);
                    mErrorMessageTextView.setText(R.string.no_internet_message);
                    isFirstPageLoading = false;
                    mTotalPages = data.get(0).getHits() / 10 - 1;
                    mAdapter.setNewsStories(data);
                    if (mCurrentPage <= mTotalPages) mAdapter.addLoadingFooter();
                    else isLastPage = true;
                }else{
                    mNewsFeedRecyclerView.setVisibility(View.GONE);
                    mErrorMessageFL.setVisibility(View.VISIBLE);
                    mErrorMessageTextView.setText(R.string.no_results_found);
                }

            }

        }
    }

    @Override
    public void onLoaderReset(Loader<List<NewsStories>> loader) {

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
                    mCurrentPage = 0;
                    isLoading = false;
                }
                return true;
            case R.id.action_clear:
                if(AppUtils.isNetworkAvailableAndConnected(getActivity())) {
                    saveQuery(null);
                    loadFirstPage();
                    mCurrentPage = 0;
                    isLoading = false;
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
