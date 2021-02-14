package com.christopherhield.geography;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;


public class ArticleFragment extends Fragment implements View.OnClickListener {

    public static MainActivity mainActivity;

    public TextView title;
    public TextView date;
    public TextView author;
    public ImageView image;
    public TextView description;
    public TextView count;

    public Article article;



    public static final ArticleFragment newInstance(MainActivity ma, Article article, int index, int max) {
        mainActivity = ma;
        ArticleFragment fra = new ArticleFragment();
        Bundle bdl = new Bundle(1);
        bdl.putSerializable("article", article);
        bdl.putSerializable("INDEX", index);
        bdl.putSerializable("TOTAL_COUNT", max);
        fra.setArguments(bdl);
        return fra;
    }

    @Override
    public void onClick(View view) {

    }




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_article,container,false);


        if (savedInstanceState == null) {
            article = (Article) getArguments().getSerializable("article");

        }
        else{
            article = (Article) savedInstanceState.getSerializable("article");
        }

        int index = getArguments().getInt("INDEX");
        int max = getArguments().getInt("TOTAL_COUNT");

        title = view.findViewById(R.id.title);
        date = view.findViewById(R.id.date);
        author = view.findViewById(R.id.author);
        image = view.findViewById(R.id.image);
        description = view.findViewById(R.id.description);
        count = view.findViewById(R.id.count);

        title.setOnClickListener(this);
        image.setOnClickListener(this);
        description.setOnClickListener(this);


        title.setText(article.getTitle());
        author.setText(article.getAuthor());
        date.setText(article.getPublishedAt());
        description.setText(article.getDescription());
        count.setText((index) + " of " + max);

        description.setMovementMethod(new ScrollingMovementMethod());
        image.setImageResource(R.drawable.loading);

        if(checkNetwork()){
            if(article.getUrlToImage() != null){
                final String photoUrl = (article.getUrlToImage());
                Picasso picasso = new Picasso.Builder(mainActivity).listener(new Picasso.Listener() {
                    @Override
                    public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                        // Here we try https if the http image attempt failed
                        final String changedUrl = photoUrl.replace("http:", "https:");
                        picasso.load(changedUrl)
                                .error(R.drawable.brokenimage)
                                .placeholder(R.drawable.loading)
                                .into(image);

                    }
                }).build();

                picasso.load(photoUrl)
                        .error(R.drawable.brokenimage)
                        .placeholder(R.drawable.loading)
                        .into(image);

            }
        }

        if(article.getUrl() != null){
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = article.getUrl();
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                }
            });
            title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = article.getUrl();
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                }
            });

            description.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = article.getUrl();
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                }
            });
        }
        return view;
    }


    private boolean checkNetwork(){
        ConnectivityManager cm = (ConnectivityManager) mainActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putSerializable("article", article);
        super.onSaveInstanceState(outState);
    }

}
