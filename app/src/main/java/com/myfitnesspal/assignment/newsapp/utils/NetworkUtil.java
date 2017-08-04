package com.myfitnesspal.assignment.newsapp.utils;

import android.net.Uri;
import android.text.format.DateFormat;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.myfitnesspal.assignment.newsapp.models.Doc;
import com.myfitnesspal.assignment.newsapp.models.Multimedia;
import com.myfitnesspal.assignment.newsapp.models.NewsStories;
import com.myfitnesspal.assignment.newsapp.models.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/**
 * Created by saip92 on 8/2/2017.
 */

public class NetworkUtil {


    private static final String TAG = NetworkUtil.class.getSimpleName();


    //API related configuration static variables
    private static final String ARTICLE_SEARCH_API = "https://api.nytimes.com/svc/search/v2/articlesearch.json";
    private static final String TOP_STORIES_HOME_API ="https://api.nytimes.com/svc/topstories/v2/home.json";
    private static final String API_KEY = "d31fe793adf546658bd67e2b6a7fd11a";
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



    public static byte[] getUrlBytes(String urlSpec) throws IOException{
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try{

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream inputStream = connection.getInputStream();

            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK){
                throw new IOException(connection.getResponseMessage()
                        + ": with " + urlSpec);
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while((bytesRead = inputStream.read(buffer))> 0){
                out.write(buffer, 0 , bytesRead);
            }
            out.close();
            return out.toByteArray();
        }finally {
            connection.disconnect();
        }


    }


    public static String getUrlString(String urlSpec) throws IOException{
        return new String(getUrlBytes(urlSpec));
    }



    private static String buildArticleStoriesUrl(int page){
        Uri.Builder builder = ARTICLE_SEARCH_ENDPOINT.buildUpon();
        if(page >=0){
            return builder.appendQueryParameter(PAGE_QUERY, String.valueOf(page)).build().toString();
        }else{
            return null;
        }
    }

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

    private static String buildTopStoriesUrl(){
        return TOP_STORIES_ENDPOINT.buildUpon().build().toString();
    }



    private static List<NewsStories> fetchArticleStories(String url) {
        List<NewsStories> newsStories = new ArrayList<>();
        try{
            String jsonString = getUrlString(url);
            Log.d(TAG, "Received Json Object" +jsonString);
            JSONObject jsonObject = new JSONObject(jsonString);
            parseItems(newsStories, jsonObject);
        }catch (JSONException e) {
            Log.e(TAG, "Failed to parse json", e);
        }
        catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items: ", ioe);
        }
        return newsStories;
    }

    public static void parseItems(List<NewsStories> newsStories, JSONObject jsonResponse) {

        Gson gson = new Gson();
        Response response = null;
        try {
            response = gson.fromJson(jsonResponse.getString("response"), Response.class);
        } catch (JSONException e) {
            Log.d(TAG, "Volley request failure");
            e.printStackTrace();
        }
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

                    //newsStory.setPubDate("Not available");
                }
            }else{
                newsStory.setPubDate("Not available");
            }

            //newsStory.setPubDate(doc.getPubDate());
            newsStories.add(newsStory);
        }

    }

    private static String getThumbnailImageUrl(List<Multimedia> multimedias){
        for(Multimedia multimedia : multimedias){
            return "http://www.nytimes.com/" + multimedia.getUrl();
        }
        return null;
    }


    public static List<NewsStories> downloadArticleStories(int page){
        String url = buildArticleStoriesUrl(page);
        Log.d(TAG, "Article Stories URL: " + url);
        return fetchArticleStories(url);
    }

    public static List<NewsStories> downloadArticleStories(String query, int page){
        String url = buildArticleStoriesUrl(query,page);
        Log.d(TAG, "Article Stories URL: " + url);
        return fetchArticleStories(url);
    }

}
