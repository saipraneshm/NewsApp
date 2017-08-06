package com.myfitnesspal.assignment.newsapp.adapters;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

/*A custom scroll listener that calls load more items when it reaches the last page of the current search results */
public abstract class PaginationScrollListener extends RecyclerView.OnScrollListener {

    private static final String TAG = PaginationScrollListener.class.getSimpleName();
    private LinearLayoutManager layoutManager;

    public PaginationScrollListener(LinearLayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        int visibleItemCount = layoutManager.getChildCount();
        int totalItemCount = layoutManager.getItemCount();
        int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
        getFirstVisibleItemPosition(firstVisibleItemPosition);

        Log.d(TAG, "totalcount: " + totalItemCount + ", visibleCount " + visibleItemCount +
                ", firstvisible: " + firstVisibleItemPosition + " " +" is lastPage: " + isLastPage()
         + " isLOading: " + isLoading());
        if (!isLoading() && !isLastPage()) {
            if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                    && firstVisibleItemPosition >= 0) {
                Log.d(TAG,"calling load more");
                loadMoreItems();
            }
        }
    }

    protected abstract void loadMoreItems();

    public abstract int getTotalPageCount();

    public abstract boolean isLastPage();

    public abstract boolean isLoading();

    public abstract void getFirstVisibleItemPosition(int position);



}
