package com.christopherhield.geography;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;

import static java.net.HttpURLConnection.HTTP_OK;

public class MenuUpdateRunnable implements Runnable {

    private static final String TAG = "RegionLoader";
    private final MainActivity mainActivity;
    private int form;
    private static final String dataURL = "https://newsapi.org/v2/sources?";
    private static String APIKey = "apiKey=";

    public String t_selected;
    public String l_selected;
    public String c_selected;

    MenuUpdateRunnable(MainActivity ma, int code, String category, String language, String country) {
        mainActivity = ma;
        this.form = code;
        this.t_selected = category;
        this.l_selected = language;
        this.c_selected = country;
    }

    @Override
    public void run() {
        String para =
                (t_selected.equals("all")?"":("&category=" + t_selected)) +
                (l_selected.equals("all")?"":("&language=" + l_selected)) +
                (c_selected.equals("all")?"":("&country=" + c_selected));
        //bb3b87f254c74448ac595f998b45d5ce
        Uri dataUri = Uri.parse(dataURL + para + (para.equals("")?"":"&") + APIKey + "2dbb18b42bc14f5faabed3140edbfb95");
        String urlToUse = dataUri.toString();
        try {
            URL url = new URL(urlToUse);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.addRequestProperty("User-Agent","");
            conn.connect();

            StringBuilder sb = new StringBuilder();
            String line;

            int respondCode = conn.getResponseCode();
            if (respondCode == HTTP_OK) {
                BufferedReader reader =
                        new BufferedReader((new InputStreamReader(conn.getInputStream())));

                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }
                conn.disconnect();
                HashSet<Source> regionSet = parseJSON(sb.toString());
                if (regionSet != null)
                    mainActivity.runOnUiThread(() -> {
                        if(form == 0)
                            mainActivity.setupMenu(regionSet);
                        else if(form == 1)
                            mainActivity.setupDrawer(regionSet);
                        else {
                            mainActivity.setupMenu(regionSet);
                            mainActivity.setupDrawer(regionSet);//initialize
                        }
                    });
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


    private HashSet<Source> parseJSON(String s) {

        HashSet<Source> regionSet = new HashSet<>();
        try {
            JSONArray jObjMain = new JSONObject(s).getJSONArray("sources");

            // Here we only want to regions and subregions
            for (int i = 0; i < jObjMain.length(); i++) {
                JSONObject jCountry = (JSONObject) jObjMain.get(i);
                String id = jCountry.getString("id");
                String name = jCountry.getString("name");
                String description = jCountry.getString("description");
                String url = jCountry.getString("url");
                String category = jCountry.getString("category");
                String language = jCountry.getString("language");
                String country = jCountry.getString("country");

                MainActivity.map_NameID.put(name, id);

                Source source = new Source(id, name, description, url, category, language, country);

                regionSet.add(source);
            }
            return regionSet;
        } catch (
                Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
