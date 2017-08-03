package com.myfitnesspal.assignment.newsapp.fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.input.InputManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
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
import android.widget.ProgressBar;

import com.myfitnesspal.assignment.newsapp.R;
import com.myfitnesspal.assignment.newsapp.adapters.NewsFeedRecyclerViewAdapter;
import com.myfitnesspal.assignment.newsapp.adapters.PaginationScrollListener;
import com.myfitnesspal.assignment.newsapp.models.NewsStories;
import com.myfitnesspal.assignment.newsapp.utils.NetworkUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 */
public class NewsFeedFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<NewsStories>>, SharedPreferences.OnSharedPreferenceChangeListener {


    private static final String SEARCH_QUERY_EXTRA = "searchQueryExtra";
    private static final String PAGE_QUERY_EXTRA = "pageQueryExtra";

    @BindView(R.id.news_feed_fragment_recycler_view)
    RecyclerView mNewsFeedRecyclerView;

    @BindView(R.id.loading_feed_progress_bar)
    ProgressBar mLoadingProgressBar;

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
    private static final String SAVE_QUERY_FOR_ROTATION = "saveQueryForRotation";


    private NewsFeedRecyclerViewAdapter mAdapter;

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

        Log.d(TAG,"in on Create of NewsFeedFragment");
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_news_feed, container, false);
        ButterKnife.bind(this,view);

        ButterKnife.setDebug(true);
       // mNewsFeedRecyclerView =(RecyclerView) view.findViewById(R.id.news_feed_fragment_recycler_view);
       // mLoadingProgressBar = (ProgressBar) view.findViewById(R.id.loading_feed_progress_bar);
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
        });

        loadFirstPage();

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
                InputMethodManager im = (InputMethodManager) getActivity()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromInputMethod(searchView.getWindowToken(),0);
                searchView.clearFocus();
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
        makeActionSearchApiQuery(getQuery(),0);
    }

    private void loadNextPage(){
        makeActionSearchApiQuery(getQuery(), mCurrentPage);
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
                isFirstPageLoading = false;
                mTotalPages = data.get(0).getHits() / 10;
                mAdapter.setNewsStories(data);
                if (mCurrentPage <= mTotalPages) mAdapter.addLoadingFooter();
                else isLastPage = true;
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



}
