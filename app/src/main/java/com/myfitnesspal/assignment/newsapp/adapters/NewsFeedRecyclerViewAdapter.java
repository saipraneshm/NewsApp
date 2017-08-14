package com.myfitnesspal.assignment.newsapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.myfitnesspal.assignment.newsapp.R;
import com.myfitnesspal.assignment.newsapp.activities.NewsFeedDetailActivity;
import com.myfitnesspal.assignment.newsapp.models.NewsStory;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.ContentValues.TAG;


public class NewsFeedRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{


    private boolean isLoadingAdded = false;
    private Context mContext;
    private List<NewsStory> mNewsStories;

    //Constants for switching views
    private static final int ITEM = 0;
    private static final int LOADING = 1;

    public NewsFeedRecyclerViewAdapter(Context context){
        mContext = context;
    }

    public List<NewsStory> getNewsStories() {
        return mNewsStories;
    }

    public void setNewsStories(List<NewsStory> newsStories) {
        if(mNewsStories != null && mNewsStories.size() >= 0)
            mNewsStories.clear();
        mNewsStories = newsStories;
        notifyDataSetChanged();
    }





    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch(viewType){
            case ITEM:
                View itemView = inflater.inflate(R.layout.news_list_item_layout,parent, false);
                viewHolder = new NewsFeedViewHolder(itemView);
                break;
            case LOADING:
                View progressView = inflater.inflate(R.layout.load_more_items_layout, parent,false);
                viewHolder = new LoadingViewHolder(progressView);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        NewsStory newsStory = mNewsStories.get(position);
        Log.d("CHECK", getItemViewType(position) + " " + position + " " +newsStory.toString() + " " +isLoadingAdded);

        //binds view based on position
        switch (getItemViewType(position)) {
            case ITEM:
                NewsFeedViewHolder storiesViewHolder = (NewsFeedViewHolder) holder;
                storiesViewHolder.bind(newsStory);
                break;
            case LOADING:
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mNewsStories == null ? 0 : mNewsStories.size();
    }

    @Override
    public int getItemViewType(int position) {
        return (position == mNewsStories.size() - 1 && isLoadingAdded) ? LOADING : ITEM;
    }


    /*All the helper Methods */

    //Helper method to append data items and to notify the recycler view at the same time.
    public void addMoreData(List<NewsStory> newsStories){
        int oldSize = mNewsStories.size() - 1;
        mNewsStories.addAll(newsStories);
        notifyItemRangeChanged(oldSize, mNewsStories.size());
    }

    //Another helper method to show progress bar
    public void add(NewsStory newsStory){
        mNewsStories.add(newsStory);
    }

    //Adds progress bar at the footer
    public void addLoadingFooter(){
        isLoadingAdded = true;
        add(new NewsStory());
    }

    //Removes the loading footer
    public void removeLoadingFooter(){
        if(mNewsStories.size() <= 0) return;
        isLoadingAdded = false;

        int position = mNewsStories.size() - 1;
        NewsStory story = getItem(position);

        if(story != null){
            mNewsStories.remove(position);
            notifyItemRemoved(position);
        }
    }



    public NewsStory getItem(int position){
        return mNewsStories.get(position);
    }

    /*End of all the helper methods*/


    //View holder that binds the news story to the view
    class NewsFeedViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.thumbnail_image_view)
        ImageView mThumbnailImageView;

        @BindView(R.id.section_text_view)
        TextView mSection;

        @BindView(R.id.headline_text_view)
        TextView mHeadline;

        @BindView(R.id.pub_time_text_view)
        TextView mPubDate;

        @BindView(R.id.cardView)
        CardView mCardView;

        NewsStory mNewsStory;

        public NewsFeedViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
            mCardView.setOnClickListener(this);
        }

        public void bind(NewsStory newsStory){
            mNewsStory = newsStory;
                Picasso.with(mContext)
                        .load(newsStory.getThumbnailUrl())
                        .placeholder(R.drawable.ic_news)
                        .error(R.drawable.ic_news)
                        .resize(80,80)
                        .centerCrop()
                        .into(mThumbnailImageView);
            mHeadline.setText(newsStory.getHeadline());
            mPubDate.setText(newsStory.getPubDate());

        }

        @Override
        public void onClick(View view) {
            if(mNewsStory != null && mNewsStory.getWebUrl() != null){
                Uri uri = Uri.parse(mNewsStory.getWebUrl());
                Intent i = NewsFeedDetailActivity.newIntent(mContext,uri);
                mContext.startActivity(i);
            }

        }
    }


    class LoadingViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.show_no_more_result_tv)
        TextView mNoResultsTextView;

        @BindView(R.id.loading_footer_progress_bar)
        ProgressBar mProgressBar;

        public LoadingViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }



}


