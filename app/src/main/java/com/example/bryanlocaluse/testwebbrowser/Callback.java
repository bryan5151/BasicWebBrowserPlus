package com.example.bryanlocaluse.testwebbrowser;

import android.webkit.WebView;
import android.webkit.WebViewClient;

import static android.provider.ContactsContract.CommonDataKinds.Website.URL;

/**
 * Created by bryan on 3/4/2018.
 */



public class Callback extends WebViewClient {

    private WebView mWebView;

    //public WebView getWebView(){return mWebView;}
    //MainActivity myActivity;

    //Callback(MainActivity b){
    //nWebView = myActivity.getWebView();}
    Callback(WebView w){
        mWebView = w;

    }

    @Override

    public boolean shouldOverrideUrlLoading(WebView w, String s)
    {
        mWebView.loadData(URL, "text/html", "UTF-8");  // load the webview

        return true;
    }
}
