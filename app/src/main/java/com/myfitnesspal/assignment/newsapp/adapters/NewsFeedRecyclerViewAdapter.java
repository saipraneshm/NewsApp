package com.myfitnesspal.assignment.newsapp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.myfitnesspal.assignment.newsapp.R;
import com.myfitnesspal.assignment.newsapp.activities.NewsFeedDetailActivity;
import com.myfitnesspal.assignment.newsapp.models.NewsStories;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;



public class NewsFeedRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{


    private boolean isLoadingAdded = false;
    private Context mContext;
    List<NewsStories> mNewsStories;

    //Constants for switching views
    private static final int ITEM = 0;
    private static final int LOADING = 1;

    public NewsFeedRecyclerViewAdapter(Context context){
        mContext = context;
    }

    public NewsFeedRecyclerViewAdapter(Context context, List<NewsStories> newsStories){
        mContext = context;
        mNewsStories = newsStories;
    }

    public List<NewsStories> getNewsStories() {
        return mNewsStories;
    }

    public void setNewsStories(List<NewsStories> newsStories) {
        mNewsStories = newsStories;
        notifyItemRangeChanged(0, mNewsStories.size());
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
        NewsStories newsStory = mNewsStories.get(position);
        switch (getItemViewType(position)) {
            case ITEM:
                NewsFeedViewHolder storiesViewHolder = (NewsFeedViewHolder) holder;
                storiesViewHolder.bind(newsStory);
                break;
            case LOADING:
//                Do nothing
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
    public void addMoreData(List<NewsStories> newsStories){
        int oldSize = mNewsStories.size() - 1;
        mNewsStories.addAll(newsStories);
        notifyItemRangeChanged(oldSize, mNewsStories.size());
    }

    //Another helper method to show progress bar
    public void add(NewsStories newsStory){
        mNewsStories.add(newsStory);
    }

    //Adds progress bar at the footer
    public void addLoadingFooter(){
        isLoadingAdded = true;
        add(new NewsStories());
    }

    public void removeLoadingFooter(){
        isLoadingAdded = false;

        int position = mNewsStories.size() - 1;
        NewsStories story = getItem(position);

        if(story != null){
            mNewsStories.remove(position);
            notifyItemRemoved(position);
        }
    }


    public NewsStories getItem(int position){
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

        public NewsFeedViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
            itemView.setOnClickListener(this);
        }

        public void bind(NewsStories newsStory){
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

        }
    }


    class LoadingViewHolder extends RecyclerView.ViewHolder{

        public LoadingViewHolder(View itemView) {
            super(itemView);
        }
    }



}


