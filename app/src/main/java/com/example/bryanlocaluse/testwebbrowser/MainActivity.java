package com.example.bryanlocaluse.testwebbrowser;

        import android.Manifest;
        import android.app.Activity;
        import android.app.AlertDialog;
        import android.app.DownloadManager;
        import android.content.ClipData;
        import android.content.ClipboardManager;
        import android.content.Context;
        import android.content.DialogInterface;
        import android.content.Intent;
        import android.content.SharedPreferences;
        import android.content.pm.PackageManager;
        import android.content.pm.ResolveInfo;
        import android.graphics.Bitmap;
        import android.net.Uri;
        import android.os.Environment;
        import android.os.Message;
        import android.preference.PreferenceManager;
        import android.support.annotation.NonNull;
        import android.support.v4.app.ActivityCompat;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.util.Log;
        import android.util.Patterns;
        import android.view.ContextMenu;
        import android.view.KeyEvent;
        import android.view.LayoutInflater;
        import android.view.Menu;
        import android.view.MenuInflater;
        import android.view.MenuItem;
        import android.view.View;
        import android.webkit.CookieManager;
        import android.webkit.DownloadListener;
        import android.webkit.GeolocationPermissions;
        import android.webkit.MimeTypeMap;
        import android.webkit.URLUtil;
        import android.webkit.WebChromeClient;
        import android.webkit.WebView;
        import android.webkit.WebViewClient;
        import android.widget.EditText;
        import android.widget.ProgressBar;
        import android.widget.Toast;
        import java.net.URISyntaxException;
        import java.util.ArrayList;
        import java.util.regex.Matcher;
        import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity {
    private Context mContext;
    public static final String CLASSTAG=MainActivity.class.getName();
    public static ArrayList<Activity> activities= new ArrayList<>();
    private Activity thisInstance;
    private WebView mWebView;
    private String backupMUrl;
    private String mUrl = "google.com";
    private static final String TAG = "MainActivity";
    private String usrAgent;
    private String usrAgentChromeMobile = "Mozilla/5.0 (Linux; Android 5.1.1; Nexus 5 Build/LMY48B; wv) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3361.0 Mobile Safari/537.36";
    final Context context = this;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activities.add(this);
        thisInstance = this;
        setContentView(R.layout.activity_main);
        backupMUrl = mUrl;
        initializeVarsFromSharedPreferences();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mUrl = extras.getString("urlToLoad", backupMUrl);
        }
        // Get the application context
        mContext = getApplicationContext();
        // Get the activity
        mWebView = findViewById(R.id.web_view);
        mWebView.setWebViewClient(new Callback(mWebView));
        progressBar = findViewById(R.id.progressBar1);
        // Request to render the web page
        renderWebPage(mUrl);
        //
        //public void registerForContextMenu (View view)
        //Registers a context menu to be shown for the given view (multiple views can show the
        //context menu). This method will set the View.OnCreateContextMenuListener on the view
        //to this activity, so onCreateContextMenu(ContextMenu, View, ContextMenuInfo) will be
        //called when it is time to show the context menu.
        //
        //Parameters
        //view : The view that should show a context menu.
        /** First step to show a custom context menu on web view**/
        registerForContextMenu(mWebView);
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    // Custom method to render a web page
    protected void renderWebPage(String urlToRender) {


        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                // Do something on page loading started
                //Page title is not available in this stage, set title to blank
                //As page starts loading shows the progressBar
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                // Do something when page loading finished
                // Hide progressBar
                progressBar.setVisibility(View.INVISIBLE);
                // Update mUrl with new URL
                mUrl = url;
                // Page title is available in this stage
                setTitle(view.getTitle());
                SharedPreferences a = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = a.edit();
                editor.putString("LAST_PAGE_VISITED", mUrl);
                editor.commit();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if(url.equals("hrupin://second_activity")){
                    Intent i = new Intent(context, MainActivity.class);
                    context.startActivity(i);
                    return true;
                }
                if (url.startsWith("intent://")) {
                    try {
                        Context context = view.getContext();
                        Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                        if (intent != null) {
                            view.stopLoading();
                            PackageManager packageManager = context.getPackageManager();
                            ResolveInfo info = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
                            if (info != null) {
                                context.startActivity(intent);
                            } else {
                                String fallbackUrl = intent.getStringExtra("browser_fallback_url");
                                view.loadUrl(fallbackUrl);
                            }
                            return true;
                        }
                    } catch (URISyntaxException e) { Log.e(TAG, "Can't resolve intent://", e);}
                }
                return false;
            }
        });
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, Message resultMsg){
                WebView.HitTestResult urlToLoad = view.getHitTestResult();
                String data = urlToLoad.getExtra();
                Intent newActivity = new Intent(getApplicationContext(), MainActivity.class);
                newActivity.putExtra("urlToLoad", data);
                context.startActivity(newActivity);
                return false;

            }
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                Log.v(TAG, "called");
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {} //need to rephrase this
                    else {
                    ActivityCompat.requestPermissions(thisInstance, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                        mWebView.onPause();
                    callback.invoke(origin, true, false);
                    }
            }
        });
        mWebView.getSettings().setGeolocationEnabled(true);
        mWebView.getSettings().setGeolocationDatabasePath( context.getFilesDir().getPath() );
        mWebView.getSettings().setUserAgentString(usrAgent);
        // Enable the javascript
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.getSettings().setSupportMultipleWindows(true);
        mWebView.getSettings().setSaveFormData(false);
        mWebView.getSettings().setSavePassword(false);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.getSettings().setLightTouchEnabled(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setSupportZoom(true);
        mWebView.getSettings().setSupportMultipleWindows(true);

        //
        //public abstract void setBuiltInZoomControls (boolean enabled)
        //Sets whether the WebView should use its built-in zoom mechanisms. The built-in zoom
        //mechanisms comprise on-screen zoom controls, which are displayed over the WebView's
        //content, and the use of a pinch gesture to control zooming. Whether or not these
        //on-screen controls are displayed can be set with setDisplayZoomControls(boolean).
        //The default is false.
        //
        //The built-in mechanisms are the only currently supported zoom mechanisms, so it is
        //recommended that this setting is always enabled.
        //
        //Parameters
        //enabled : whether the WebView should use its built-in zoom mechanisms
        //
        mWebView.getSettings().setBuiltInZoomControls(true);
        //
        //public abstract void setDisplayZoomControls (boolean enabled)
        //Sets whether the WebView should display on-screen zoom controls when using the
        //built-in zoom mechanisms. The default is true.
        //
        //Parameters
        //enabled : whether the WebView should display on-screen zoom controls
        //
        mWebView.getSettings().setDisplayZoomControls(false);
        mWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setMimeType(mimeType);
                //------------------------COOKIE!!------------------------
                String cookies = CookieManager.getInstance().getCookie(url);
                request.addRequestHeader("cookie", cookies);
                //------------------------COOKIE!!------------------------
                request.addRequestHeader("User-Agent", userAgent);
                request.setDescription("Downloading file...");
                request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType));
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimeType));
                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                dm.enqueue(request);
                Toast.makeText(getApplicationContext(), "Downloading File", Toast.LENGTH_LONG).show();
            }
        });
        if (urlToRender.startsWith("intent://")) {
            Intent externall = new Intent(Intent.ACTION_VIEW);
            externall.setData(Uri.parse(urlToRender));
            startActivity(externall);
        } else if (urlToRender.contains("http")) {
            Log.v(TAG, urlToRender);
            if (isValidUrl(urlToRender)) mWebView.loadUrl(urlToRender);
            else mWebView.loadUrl("https://www.google.com/search?q=" + urlToRender);
        } else {
            Log.v(TAG, urlToRender);
            if (isValidUrl(urlToRender)) {
                urlToRender = "http://" + urlToRender;
                mWebView.loadUrl(urlToRender);
            } else mWebView.loadUrl("https://www.google.com/search?q=" + urlToRender);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this,"Permission granted.", Toast.LENGTH_SHORT).show();
                // perform your action here

            } else {
                Toast.makeText(this,"Permission not granted.", Toast.LENGTH_SHORT).show();
            }
        }

    }

    /**
     * This is used to check the given URL is valid or not.
     *
     * @param url
     * @return true if url is valid, false otherwise.
     */
    private boolean isValidUrl(String url) {
        Pattern p = Patterns.WEB_URL;
        Matcher m = p.matcher(url.toLowerCase());
        return m.matches();
    }

    //public void onCreateContextMenu (ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    //Called when a context menu for the view is about to be shown.
    //Unlike onCreateOptionsMenu(Menu), this will be called every time the context menu is
    //about to be shown and should be populated for the view (or item inside the view for
    //AdapterView subclasses, this can be found in the menuInfo)).
    //
    //Use onContextItemSelected(android.view.MenuItem) to know when an item has been selected.
    //
    //It is not safe to hold onto the context menu after this method returns.
    //
    //Parameters
    //menu : The context menu that is being built
    //v : The view for which the context menu is being built
    //menuInfo : Extra information about the item for which the context menu should be shown.
    //This information will vary depending on the class of v.

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        WebView.HitTestResult resultA = mWebView.getHitTestResult();
        if (resultA.getType() == WebView.HitTestResult.SRC_ANCHOR_TYPE){
            super.onCreateContextMenu(menu, v, menuInfo);
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.url_handling, menu);
        }
        if (resultA.getType() == WebView.HitTestResult.IMAGE_TYPE || resultA.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
            super.onCreateContextMenu(menu, v, menuInfo);
            //
            //MenuInflater
            //This class is used to instantiate menu XML files into Menu objects.
            //
            //For performance reasons, menu inflation relies heavily on pre-processing of XML
            //files that is done at build time.
            //
            //
            //public MenuInflater getMenuInflater ()
            //Returns a MenuInflater with this context.
            //
            MenuInflater inflater = getMenuInflater();
            //
            //public void inflate (int menuRes, Menu menu)
            //Inflate a menu hierarchy from the specified XML resource. Throws InflateException if there is an error.
            //
            //Parameters
            //menuRes : Resource ID for an XML layout resource to load (e.g., R.menu.main_activity)
            //menu : The Menu to inflate into. The items and submenus will be added to this Menu.
            //
            //
            inflater.inflate(R.menu.download_image_prompt, menu);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        //respond to menu item selection
        switch (item.getItemId()) {
            case R.id.manage:
                Toast.makeText(mContext,"Not implemented yet.",Toast.LENGTH_SHORT).show();
                return true;
            case R.id.refresh:
                renderWebPage(mUrl);
                return true;
            case R.id.mainPage:
                mUrl = "https://google.com/";
                renderWebPage(mUrl);
                return true;
            case R.id.gitHub:
                mUrl = "https://github.com/bryan5151";
                renderWebPage(mUrl);
                return true;
            case R.id.facebook:
                mUrl = "https://m.facebook.com/";
                renderWebPage(mUrl);
                return true;
            case R.id.customURL:
                LayoutInflater li = LayoutInflater.from(context);
                View promptsView = li.inflate(R.layout.prompts, null);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(promptsView);
                final EditText userInput;
                userInput = promptsView.findViewById(R.id.editTextDialogUserInput);
                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // get user input and set it to result
                                        mUrl = userInput.getText().toString();
                                        Log.v(TAG, mUrl);
                                        renderWebPage(mUrl);
                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                })
                        .setOnKeyListener(new DialogInterface.OnKeyListener(){
                            @Override
                            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                                if (keyCode == KeyEvent.KEYCODE_ENTER){
                                mUrl = userInput.getText().toString();
                                Log.v(TAG, mUrl);
                                dialog.dismiss();
                                renderWebPage(mUrl);
                                    return true;
                                }
                                return false;
                            }
                        });
                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();
                // show it
                alertDialog.show();
                return true;
            case R.id.app_settings:
                Intent intentSetPref = new Intent(getApplicationContext(), AppSettingsActivity.class);
                startActivityForResult(intentSetPref, 0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
        case 0:
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String prefList = sharedPreferences.getString("PREF_LIST", usrAgentChromeMobile);
        usrAgent = prefList;
        if(mWebView.getSettings().getUserAgentString() != usrAgent)
            renderWebPage(mUrl);
        break;
        }
    }

    //public boolean onContextItemSelected (MenuItem item)
    //This hook is called whenever an item in a context menu is selected. The default
    //implementation simply returns false to have the normal processing happen (calling the
    //item's Runnable or sending a message to its Handler as appropriate). You can use this
    //method for any items for which you would like to do processing without
    //those other facilities.
    //
    //Use getMenuInfo() to get extra information set by the View that added this menu item.
    //
    //Derived classes should call through to the base class for it to perform the
    //default menu handling.
    //
    //Parameters
    //item : The context menu item that was selected.
    //
    //Returns
    //boolean : Return false to allow normal context menu processing to proceed,
    //true to consume it here.

    @Override
    public boolean onContextItemSelected(MenuItem item){
        // Handle the menu item selection
        String resultUrl = mWebView.getHitTestResult().getExtra();
        Log.v(TAG,resultUrl);
        switch(item.getItemId()){
            case R.id.copyURL:
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(resultUrl, resultUrl);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(mContext, "Link copied to clipboard.", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.downloadImage:
                if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Log.v(TAG,"Permission is granted");
                    String fileExtension = MimeTypeMap.getFileExtensionFromUrl(resultUrl);
                    DownloadManager.Request r = new DownloadManager.Request(Uri.parse(resultUrl));
                    r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(resultUrl, null, fileExtension));
                    r.allowScanningByMediaScanner();
                    r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                    dm.enqueue(r);
                }
                else {
                    Log.v(TAG,"Permission is revoked");
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                }
                return true;
            default:
                super.onContextItemSelected(item);
        }
        return false;
    }
    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        activities.remove(this);
    }

    public void initializeVarsFromSharedPreferences(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mUrl = sharedPreferences.getString("LAST_PAGE_VISITED", mUrl);
        String prefList = sharedPreferences.getString("PREF_LIST", usrAgentChromeMobile);
        usrAgent = prefList;
    }
}