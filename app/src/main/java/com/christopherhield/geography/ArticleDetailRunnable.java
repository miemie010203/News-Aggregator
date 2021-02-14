package com.christopherhield.geography;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static java.net.HttpURLConnection.HTTP_OK;

public class ArticleDetailRunnable implements Runnable {

    private final MainActivity activity;
    private final String selectedSubRegion;

    private static String APIKey = "&apiKey=";
    private static final String baseURL = "https://newsapi.org/v2/top-headlines?sources=";

    ArticleDetailRunnable(MainActivity ma, String selectedArticle) {
        activity = ma;
        this.selectedSubRegion = selectedArticle;
    }

    public void run() {

        Uri dataUri = Uri.parse(baseURL + selectedSubRegion + APIKey + "2dbb18b42bc14f5faabed3140edbfb95");
        String urlToUse = dataUri.toString();

        try {
            URL url = new URL(urlToUse);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.addRequestProperty("User-Agent","");
            conn.connect();

            StringBuilder sb = new StringBuilder();
            String line;

            if (conn.getResponseCode() == HTTP_OK) {
                BufferedReader reader =
                        new BufferedReader((new InputStreamReader(conn.getInputStream())));

                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }
                conn.disconnect();
                MainActivity.articles = parseJSON(sb.toString());
                activity.runOnUiThread(() -> activity.setArticle());

            } else {
                BufferedReader reader =
                        new BufferedReader((new InputStreamReader(conn.getErrorStream())));

                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }
                conn.disconnect();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ArrayList<Article> parseJSON(String s) {

        ArrayList<Article> countryList = new ArrayList<>();
        try {
            JSONArray jObjMain = new JSONObject(s).getJSONArray("articles");

            // Here we only want to regions and subregions
            for (int i = 0; i < (jObjMain.length()>10?10:jObjMain.length()); i++) {
                JSONObject jCountry = (JSONObject) jObjMain.get(i);
                JSONObject sourceObj = jCountry.getJSONObject("source");
                Source source = new Source(sourceObj.getString("id"), sourceObj.getString("name"));
                String author = jCountry.getString("author");
                String title = jCountry.getString("title");
                String description = jCountry.getString("description");
                String url = jCountry.getString("url");
                String urlToImage = jCountry.getString("urlToImage");
                String publishedAt = jCountry.getString("publishedAt");
                String content = jCountry.getString("content");

                Article article = new Article(source, author, title, description, url, urlToImage, publishedAt, content);

                countryList.add(article);
            }
            return countryList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
