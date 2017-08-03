package com.myfitnesspal.assignment.newsapp.utils;

import android.net.Uri;
import android.util.Log;

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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
            .build();

    private static final Uri TOP_STORIES_ENDPOINT = Uri.parse(TOP_STORIES_HOME_API)
            .buildUpon()
            .appendQueryParameter(API_KEY_QUERY,API_KEY)
            .build();



    public static byte[] getUrlBytes(String urlSpec) throws IOException{
        URL url = new URL(urlSpec);

        HttpURLConnection connection = null;

        try{
            connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setRequestMethod("GET");
            connection.connect();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ByteArrayInputStream inputStream = (ByteArrayInputStream) connection.getInputStream();

            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK){
                throw new IOException(connection.getResponseMessage()
                        + ": with " + urlSpec);
            }
            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while((bytesRead = inputStream.read(buffer))> 0){
                outputStream.write(buffer, 0 , bytesRead);
            }
            outputStream.close();
            return outputStream.toByteArray();

        }finally {
            if(connection != null)
                connection.disconnect();
        }

    }

    public static String getUrlString(String urlSpec) throws IOException{
        return new String(getUrlBytes(urlSpec));
    }

    private String buildArticleStoriesUrl(int page){
        Uri.Builder builder = ARTICLE_SEARCH_ENDPOINT.buildUpon();
        if(page >=0){
            return builder.appendQueryParameter(PAGE_QUERY, String.valueOf(page)).build().toString();
        }else{
            return null;
        }
    }

    private String buildTopStoriesUrl(){
        return TOP_STORIES_ENDPOINT.buildUpon().build().toString();
    }



    private List<NewsStories> fetchArticleStories(String url){
        List<NewsStories> newsStories = new ArrayList<>();
        try{
            String jsonString = getUrlString(url);
            Log.d(TAG, "Received Json Object" +jsonString);
            JSONObject jsonObject = new JSONObject(jsonString);
            parseItems(newsStories, jsonObject.getString("response"));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return newsStories;
    }

    private void parseItems(List<NewsStories> newsStories, String jsonString) {
        Gson gson = new Gson();
        Response response = gson.fromJson(jsonString, Response.class);
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
            newsStory.setByline(doc.getByline().getOriginal());
            newsStory.setHeadline(doc.getHeadline().getMain());
            newsStory.setPubDate(doc.getPubDate());
            newsStories.add(newsStory);
        }
    }

    private String getThumbnailImageUrl(List<Multimedia> multimedias){
        for(Multimedia multimedia : multimedias){
            if(multimedia.getSubtype().equals("thumbnail")){
                return "http://www.nytimes.com/" + multimedia.getUrl();
            }
        }
        return null;
    }


    public List<NewsStories> downloadArticleStories(int page){
        String url = buildArticleStoriesUrl(page);
        Log.d(TAG, "Article Stories URL: " + url);
        return fetchArticleStories(url);
    }

}
