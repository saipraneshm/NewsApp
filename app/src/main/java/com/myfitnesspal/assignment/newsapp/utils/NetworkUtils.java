package com.myfitnesspal.assignment.newsapp.utils;

import android.net.Uri;
import android.text.format.DateFormat;

import com.google.gson.Gson;
import com.myfitnesspal.assignment.newsapp.models.Doc;
import com.myfitnesspal.assignment.newsapp.models.Multimedia;
import com.myfitnesspal.assignment.newsapp.models.NewsStories;
import com.myfitnesspal.assignment.newsapp.models.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
* A Utility class that helps to build appropriate url and to parse items.
* */

public class NetworkUtils {


    private static final String TAG = NetworkUtils.class.getSimpleName();


    //API related configuration static variables
    private static final String ARTICLE_SEARCH_API = "https://api.nytimes.com/svc/search/v2/articlesearch.json";
    private static final String TOP_STORIES_HOME_API ="https://api.nytimes.com/svc/topstories/v2/home.json";
   // private static final String API_KEY = "d31fe793adf546658bd67e2b6a7fd11a";
    private static final String API_KEY = "ba0867969a8e4e9687b9be1a6f01877c";
    private static final String PAGE_QUERY = "page";
    private static final String API_KEY_QUERY = "api-key";
    private static final String SORT_BY_NEWEST = "newest";
    private static final String SORT_BY_OLDEST = "oldest";
    private static final String FL_PARAMETERS = "headline,web_url,multimedia,lead_paragraph,_id,pub_date,byline";
    private static final Uri ARTICLE_SEARCH_ENDPOINT = Uri.parse(ARTICLE_SEARCH_API)
            .buildUpon()
            .appendQueryParameter(API_KEY_QUERY, API_KEY)
            .appendQueryParameter("fl",FL_PARAMETERS)
            .appendQueryParameter("sort",SORT_BY_NEWEST)
            .build();

    private static final Uri TOP_STORIES_ENDPOINT = Uri.parse(TOP_STORIES_HOME_API)
            .buildUpon()
            .appendQueryParameter(API_KEY_QUERY,API_KEY)
            .build();




    public static String buildArticleStoriesUrl(String query, int page){
        Uri.Builder builder = ARTICLE_SEARCH_ENDPOINT.buildUpon();
        if(query!= null){
             return builder.appendQueryParameter("q", query)
                     .appendQueryParameter(PAGE_QUERY, String.valueOf(page))
                     .build().toString();

        }else{
            return builder
                    .appendQueryParameter(PAGE_QUERY, String.valueOf(page))
                    .build().toString();
        }
    }


    public static void parseItems(List<NewsStories> newsStories, JSONObject jsonResponse) {

        Gson gson = new Gson();
        Response response = null;
        try {
            response = gson.fromJson(jsonResponse.getString("response"), Response.class);
            List<Doc> listOfDocs = response.getDocs();
            for(Doc doc : listOfDocs){
                NewsStories newsStory = new NewsStories();
                if(doc.getMultimedia().size() > 0){
                    String thumbnailUrl = getThumbnailImageUrl(doc.getMultimedia());
                    if(thumbnailUrl != null){
                        newsStory.setThumbnailAvailable(true);
                        newsStory.setThumbnailUrl(thumbnailUrl);
                    }else{
                        newsStory.setThumbnailAvailable(false);
                    }
                }else{
                    newsStory.setThumbnailAvailable(false);
                }
                if(doc.getByline() != null)
                    newsStory.setByline(doc.getByline().getOriginal());
                if(doc.getHeadline() != null)
                    newsStory.setHeadline(doc.getHeadline().getMain());
                if(response.getMeta() != null){
                    newsStory.setHits(response.getMeta().getHits());
                }
                newsStory.setWebUrl(doc.getWebUrl());
                if(doc.getPubDate() != null){
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ", Locale.getDefault());
                    Date pubDate = null;
                    try{
                        pubDate = formatter.parse(doc.getPubDate());
                        String date = DateFormat.format("EEE, MMM dd yyyy",pubDate).toString();
                        newsStory.setPubDate(date);
                    } catch (ParseException e) {
                        e.printStackTrace();
                        SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'",
                                Locale.getDefault());
                        try {
                            pubDate = formatter2.parse(doc.getPubDate());
                        } catch (ParseException e1) {
                            e1.printStackTrace();
                        }
                        String date = DateFormat.format("EEE, MMM dd yyyy",pubDate).toString();
                        newsStory.setPubDate(date);
                    }
                }else{
                    newsStory.setPubDate("Not available");
                }
                newsStories.add(newsStory);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }



    }

    private static String getThumbnailImageUrl(List<Multimedia> multimedias){
        for(Multimedia multimedia : multimedias){
            if(multimedia != null)
                return "http://www.nytimes.com/" + multimedia.getUrl();
        }
        return null;
    }




}
