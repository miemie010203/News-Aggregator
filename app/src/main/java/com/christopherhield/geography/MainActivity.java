package com.christopherhield.geography;

import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private final static ArrayList<SpannableString> drawerArticles = new ArrayList<>();
    private static Menu opt_menu;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private List<Fragment> fragments;
    public static ArrayList<Article> articles;
    private MyPageAdapter pageAdapter;
    private ViewPager pager;
    public String t_selected = "all";
    public String l_selected = "all";
    public String c_selected = "all";
    private String itemTitle;
    public static HashMap<String, String> map_NameID = new HashMap<>();
    public static int page = 0;
    private static HashMap<String, Integer> map_color = new HashMap<>();

    public static int width, height;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerList = findViewById(R.id.drawer_list);

        // Set up the drawer item click callback method
        mDrawerList.setOnItemClickListener(
                (parent, view, position, id) -> {
                    select(position);
                    mDrawerLayout.closeDrawer(mDrawerList);
                }
        );

        // Create the drawer toggle
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        );

        fragments = new ArrayList<>();

        pageAdapter = new MyPageAdapter(getSupportFragmentManager());
        pager = findViewById(R.id.viewpager);
        pager.setAdapter(pageAdapter);

        // Load the data
        new Thread(new MenuUpdateRunnable(this, 2, "all", "all", "all")).start();
    }


    public void setupMenu(HashSet<Source> set) {
        opt_menu.clear();
        Menu topicMenu = opt_menu.addSubMenu("Topics");
        topicMenu.add("all");
        Menu languageMenu = opt_menu.addSubMenu("Languages");
        languageMenu.add("all");
        Menu countryMenu = opt_menu.addSubMenu("Countries");
        countryMenu.add("all");

        HashSet<String> tset = new HashSet<>();
        HashSet<String> lset = new HashSet<>();
        HashSet<String> cset = new HashSet<>();

        for(Source s : set) {
            tset.add(s.getCategory());
            lset.add(s.getLanguage());
            cset.add(s.getCountry());
        }

        ArrayList<String> topicList = new ArrayList<>(tset);
        Collections.sort(topicList);

        ArrayList<String> languageList = new ArrayList<>(lset);
        for(int i=0; i<languageList.size(); i++)
            languageList.set(i, getName(languageList.get(i), R.raw.language_codes, "languages"));
        Collections.sort(languageList);

        ArrayList<String> countryList = new ArrayList<>(cset);
        for(int i=0; i<countryList.size(); i++)
            countryList.set(i, getName(countryList.get(i), R.raw.country_codes, "countries"));
        Collections.sort(countryList);

        for (String s : topicList) {
            SpannableString spannableString = new SpannableString(s);
            int color = setupColor();
            map_color.put(s, color);
            spannableString.setSpan(new ForegroundColorSpan(color), 0, spannableString.length(), 0);
            topicMenu.add(spannableString);
        }
        for (String s : languageList) languageMenu.add(s);
        for (String s : countryList) countryMenu.add(s);
    }

    private int setupColor() {
        Random random = new Random();
        int r = random.nextInt(255);
        int g = random.nextInt(255);
        int b = random.nextInt(255);
        return Color.rgb(r,g,b);
    }

    public void setupDrawer(HashSet<Source> set) {
        if(set.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("No Sources Exist!");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {}
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        ArrayList<Source> list = new ArrayList<>(set);
        ArrayList<SpannableString> names = new ArrayList<>();

        for(Source s : list) {
            if(map_color.containsKey(s.getCategory())) {
                SpannableString spannableString = new SpannableString(s.getName());
                spannableString.setSpan(new ForegroundColorSpan(map_color.get(s.getCategory())), 0, spannableString.length(), 0);
                names.add(spannableString);
            }
        }

        drawerArticles.clear();
        drawerArticles.addAll(names);
        Collections.sort(drawerArticles, new Comparator<SpannableString>() {
            @Override
            public int compare(SpannableString spannableString, SpannableString t1) {
                return spannableString.toString().compareTo(t1.toString());
            }
        });

        setTitle("News Gateway (" + drawerArticles.size() + ")");
        mDrawerList.setAdapter(new ArrayAdapter<SpannableString>(this, R.layout.drawer_item, drawerArticles){});

        ((ArrayAdapter) mDrawerList.getAdapter()).notifyDataSetChanged();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }

    private String getName(String code, int num, String key) {
        JSONArray ja = readJson(num, key);
        String language="";
        try {
            for (int i = 0; i < ja.length(); i++) {
                JSONObject obj = ja.getJSONObject(i);
                if (obj.getString("code").equals(code.toUpperCase()))
                    language = obj.getString("name");
            }
        } catch(Exception e) {e.printStackTrace();}
        return language;
    }

    private String getCode(String name, int num, String key) {
        JSONArray ja = readJson(num, key);
        String language = "";
        try {
            for (int i = 0; i < ja.length(); i++) {
                JSONObject obj = ja.getJSONObject(i);
                if (obj.getString("name").equals(name))
                    language = obj.getString("code");
            }
        } catch(Exception e) {e.printStackTrace();}
        return language.toLowerCase();
    }

    private JSONArray readJson(int num, String key) {
        InputStream is = getResources().openRawResource(num);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        JSONArray ja = null;
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
            is.close();
            String jsonString = writer.toString();
            JSONObject obj = new JSONObject(jsonString);
            ja = obj.getJSONArray(key);
        } catch(Exception e) {e.printStackTrace();}

        return ja;
    }

    private void select(int position) {
        pager.setBackground(null);
        String title_selected = drawerArticles.get(position).toString();
        setTitle(title_selected);
        title_selected = map_NameID.get(title_selected);
        new Thread(new ArticleDetailRunnable(this, title_selected)).start();
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    public void setArticle() {

        for (int i = 0; i < pageAdapter.getCount(); i++)
            pageAdapter.notifyChangeInPosition(i);
        fragments.clear();

        if(articles!=null) for (int i = 0; i < articles.size(); i++) {
            fragments.add(
                    ArticleFragment.newInstance(this, articles.get(i), i+1, articles.size()));
        }

        pageAdapter.notifyDataSetChanged();
        pager.setCurrentItem(0);

    }

    // You need the 2 below to make the drawer-toggle work properly:

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }



    // You need the below to open the drawer when the toggle is clicked
    // Same method is called when an options menu item is selected.

    public boolean onOptionsItemSelected(MenuItem item) {

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        if(item.hasSubMenu())
            itemTitle = item.getTitle().toString();
        else {
            String s = item.getTitle().toString();
            switch (itemTitle) {
                case "Topics":
                    t_selected = s.equals("all")?"":s;
                    break;
                case "Languages":
                    l_selected = s.equals("all")?"": getCode(s, R.raw.language_codes, "languages");
                    break;
                case "Countries":
                    c_selected = s.equals("all")?"": getCode(s, R.raw.country_codes, "countries");
                    break;
            }
            new Thread(new MenuUpdateRunnable(this, 1, t_selected, l_selected, c_selected)).start();
        }

//        updateMenu();
        return super.onOptionsItemSelected(item);
    }

    // You need this to set up the options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        opt_menu = menu;
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        page = pager.getCurrentItem();
        outState.putString("currentCategory", t_selected);
        outState.putString("currentLanguage", l_selected);
        outState.putString("currentCountry", c_selected);


        // Call super last
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        // Call super first
        super.onRestoreInstanceState(savedInstanceState);
        t_selected = "all";
        l_selected = "all";
        c_selected = "all";
        new Thread(new MenuUpdateRunnable(this, 0, "all", "all", "all")).start();
        t_selected = savedInstanceState.getString("currentCategory");
        l_selected = savedInstanceState.getString("currentLanguage");
        c_selected = savedInstanceState.getString("currentCountry");
        new Thread(new MenuUpdateRunnable(this, 1, t_selected, l_selected, c_selected)).start();
        setArticle();
        pageAdapter.notifyDataSetChanged();

        pager.setCurrentItem(page);

        for (int i = 0; i< pageAdapter.getCount(); i++) pageAdapter.notifyChangeInPosition(i);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private class MyPageAdapter extends FragmentPagerAdapter {
        private long baseId = 0;


        MyPageAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public long getItemId(int position) {
            return baseId + position;
        }



        /**
         * Notify that the position of a fragment has been changed.
         * Create a new ID for each position to force recreation of the fragment
         * @param n number of items which have been changed
         */
        void notifyChangeInPosition(int n) {
            // shift the ID returned by getItemId outside the range of all previous fragments
            baseId += getCount() + n;
        }

    }
}
