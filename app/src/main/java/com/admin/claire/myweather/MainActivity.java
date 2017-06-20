package com.admin.claire.myweather;

import android.Manifest;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.admin.claire.myweather.Common.Common;
import com.admin.claire.myweather.Helper.Helper;
import com.admin.claire.myweather.Model.OpenWeatherMap;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;

import static com.admin.claire.myweather.ThemeToggle.PREFS_NAME;
import static com.admin.claire.myweather.ThemeToggle.PREF_DARK_THEME;
import static com.admin.claire.myweather.ThemeToggle.PREF_PINK_THEME;
import static com.admin.claire.myweather.ThemeToggle.PREF_PURPLE_THEME;

public class MainActivity extends AppCompatActivity{

    private LinearLayout settingLayout;
    DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private TextView txtCity, txtLastUpdate, txtDescription, txtHumidity, txtTime, txtCelsius;
    private ImageView imageView;

    private LocationManager locationManager;

    OpenWeatherMap openWeatherMap = new OpenWeatherMap();

    int MY_PERMISSION = 0;
    boolean getService = false;     //是否已開啟定位服務

    //不指定一個特定的提供者（例如GPS），但讓系統找到與我們設置的搜索條件最匹配的
    private static Criteria searchProviderCriteria = new Criteria();
    // Location Criteria
    static {
        searchProviderCriteria.setPowerRequirement(Criteria.POWER_LOW); //要求低耗電
        searchProviderCriteria.setAccuracy(Criteria.ACCURACY_COARSE); //近似精度要求
        searchProviderCriteria.setCostAllowed(false); //不允許產生資費
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Use the chosen theme
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
        boolean useDarkTheme = preferences.getBoolean(PREF_DARK_THEME, false);
        boolean usePinkTheme = preferences.getBoolean(PREF_PINK_THEME, false);
        boolean usePurpleTheme = preferences.getBoolean(PREF_PURPLE_THEME, false);
        if (useDarkTheme){
            setTheme(R.style.CustomerTheme_Black);
        }else if (usePinkTheme){
            setTheme(R.style.CustomerTheme_Pink);
        }else if (usePurpleTheme) {
            setTheme(R.style.CustomerTheme_Purple);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get Coordinates 取得座標
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        initView();
        initDrawerLayout();

        //由Criteria物件判斷提供最準確的資訊
        String provider = locationManager.getBestProvider(searchProviderCriteria, true); //選擇精準度最高的提供者

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            //這項功能尚未取得使用者的同意
            //開始執行徵詢使用者的流程
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    MainActivity.this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                        Manifest.permission.INTERNET,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_NETWORK_STATE,
//                        Manifest.permission.SYSTEM_ALERT_WINDOW,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE

                }, MY_PERMISSION);
            }
            return;
        }

        Location loc = locationManager.getLastKnownLocation(provider);
        if (loc == null){
            locationManager.requestSingleUpdate(provider, locListener, null);
        } else {
            locListener.onLocationChanged(loc);
        }

    }

    private LocationListener locListener = new LocationListener() {

        @Override
        public void onStatusChanged(String provider, int status,
                                    Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(MainActivity.this, "Location service is not yet available",
                    Toast.LENGTH_LONG).show();
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.d("SwA", "Location changed!");
            String sLat = "" + location.getLatitude();
            String sLon = "" + location.getLongitude();
           // Log.d("SwA", "Lat ["+sLat+"] - sLong ["+sLon+"]");

            LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locManager.removeUpdates(locListener);
            new GetWeather().execute(Common.apiRequest(sLat, sLon));

           // Log.e("TAG", "onLocationChanged: " + sLat + ":" + sLon);
            Toast.makeText(MainActivity.this, "onLocationChanged: "
                    + sLat + ":" + sLon, Toast.LENGTH_SHORT).show();
        }
    };


    private void initView() {
        //Control
        txtCity = (TextView) findViewById(R.id.txtCity);
        txtLastUpdate = (TextView) findViewById(R.id.txtLastUpdate);
        txtDescription = (TextView) findViewById(R.id.txtDescription);
        txtHumidity = (TextView) findViewById(R.id.txtHumidity);
        txtTime = (TextView) findViewById(R.id.txtTime);
        txtCelsius = (TextView) findViewById(R.id.txtCelsius);
        imageView = (ImageView) findViewById(R.id.imageView);
        settingLayout = (LinearLayout)findViewById(R.id.setting_layout);

    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(locListener);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_NETWORK_STATE,
//                        Manifest.permission.SYSTEM_ALERT_WINDOW,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE

            }, MY_PERMISSION);

            return;
        }
        //由Criteria物件判斷提供最準確的資訊
        String provider = locationManager.getBestProvider(searchProviderCriteria, true); //選擇精準度最高的提供者
        locationManager.requestLocationUpdates(provider, 1000, 1, locListener);
        //服務提供者、更新頻率60000毫秒=1分鐘、最短距離、地點改變時呼叫物件


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(locListener);
    }

    private class GetWeather extends AsyncTask<String, Void, String> {
        ProgressDialog pd = new ProgressDialog(MainActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd.setTitle("Please wait....");
            pd.show();

        }

        @Override
        protected String doInBackground(String... params) {
            String stream = null;
            String urlString = params[0];

            Helper http = new Helper();
            stream = http.getHttpData(urlString);
            return stream;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s.contains("Error: Not found city")) {
                pd.dismiss();
                return;
            }
            Gson gson = new Gson();
            Type myType = new TypeToken<OpenWeatherMap>() {
            }.getType();
            openWeatherMap = gson.fromJson(s, myType);
            pd.dismiss();

            txtCity.setText(String.format("%s,%s", openWeatherMap.getName(),
                    openWeatherMap.getSys().getCountry()));
            txtLastUpdate.setText(String.format("Last Updated: %s", Common.getDateNow()));
            txtDescription.setText(String.format("%s", openWeatherMap.getWeather().get(0).getDescription()));
            txtHumidity.setText(String.format("Humidity: "+"%d%%", openWeatherMap.getMain().getHumidity()));

            txtTime.setText(String.format(" "+"Sunrise: "+"%s " + " " + "Sunset: "+"%s",
                    Common.unixTimeStampToDateTime(openWeatherMap.getSys().getSunrise()),
                    Common.unixTimeStampToDateTime(openWeatherMap.getSys().getSunset())));
            txtCelsius.setText(String.format("%.2f °C", openWeatherMap.getMain().getTemp()));
            Picasso.with(MainActivity.this)
                    .load(Common.getImg(openWeatherMap.getWeather().get(0).getIcon()))
                    .into(imageView);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);

        final MenuItem searchItem = menu.findItem(R.id.search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        SearchManager searchManager = (SearchManager)getSystemService(Context.SEARCH_SERVICE);
        // final SearchView searchView = (SearchView)menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        // 這邊讓icon可以還原到搜尋的icon
        searchView.setIconifiedByDefault(true);
        //searchView.setOnQueryTextListener(this);
        //searchView.setIconifiedByDefault(false); //是否要點選搜尋圖示後再打開輸入框
        searchView.setFocusable(false);
        searchView.requestFocusFromTouch();      //要點選後才會開啟鍵盤輸入
        searchView.setSubmitButtonEnabled(true);//輸入框後是否要加上送出的按鈕
        searchView.setQueryHint("Search City..."); //輸入框沒有值時要顯示的提示文字


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                new GetWeather().execute(Common.apiRequestCity(query));
               // Log.e("TAG", "onQueryTextChange: " + Common.apiRequestCity(query) );
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                return true;
            }
        });

        // 取得選單項目物件
        //search_item = menu.findItem(R.id.search);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mActionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initDrawerLayout() {
        //側開式選單
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawerLayout);
        mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.app_name,R.string.app_name){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        mActionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.addDrawerListener(mActionBarDrawerToggle);

        //SettingTheme
        settingLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,ThemeToggle.class));
                mDrawerLayout.closeDrawers();
            }
        });
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mActionBarDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mActionBarDrawerToggle.onConfigurationChanged(newConfig);
    }
}
