package com.himanshubakshi.sunshine;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private String mWeatherForecastJson = "";
    private final String LOG_TAG = MainActivityFragment.class.getName();
    private View mRootView;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        mRootView = inflater.inflate(R.layout.fragment_main, container, false);

        getWeatherForecast();

        return mRootView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {

        if (menuItem.getItemId() == R.id.action_debug_refresh) {
            new FetchWeatherTask().execute("110058");
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.forecast_fragment, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void getWeatherForecast() {

        String postCode = "110058";

        new FetchWeatherTask().execute(postCode);
    }

    private void setmWeatherForecastJson(String json) {
        mWeatherForecastJson = json;

        processJson(json);
    }

    private void processJson(String json) {
        ArrayList<String> forecastListItems = new ArrayList<String>();

        try {
            JSONObject myJson = new JSONObject(json);

            JSONArray listItems = myJson.getJSONArray("list");

            for (int i = 0; i < listItems.length(); ++i) {
                JSONObject arrayItem = listItems.getJSONObject(i);

                Double max = arrayItem.getJSONObject("temp").getDouble("max");
                Double min = arrayItem.getJSONObject("temp").getDouble("min");

                forecastListItems.add("<Day>, Max: " + max + ", Min: " + min);

                // ...
            }

        } catch (JSONException ex) {
            Log.v(LOG_TAG, ex.toString());
        }

        ((ListView)mRootView.findViewById(R.id.listview_forecast)).setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview, forecastListItems));

    }

    public class FetchWeatherTask extends AsyncTask<String, Void, String> {

        private final String LOG_TAG = FetchWeatherTask.class.getName();

        @Override
        protected String doInBackground(String... params) {

            String userPostCode = params[0];

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                final String FORECAST_BASE_URL =
                        "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY_PARAM = "q";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";
                final String APPID_PARAM = "APPID";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL)
                        .buildUpon()
                        .appendQueryParameter(QUERY_PARAM, userPostCode)
                        .appendQueryParameter(FORMAT_PARAM, "json")
                        .appendQueryParameter(UNITS_PARAM, "metric")
                        .appendQueryParameter(DAYS_PARAM, Integer.toString(7))
                        .appendQueryParameter(APPID_PARAM, "7e9474658b7de6bb08dadbe137d1c738")
                        .build();
                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, builtUri.toString());


                //URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=" + userPostCode +"&mode=json&units=metric&cnt=7&APPID=" + );


                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            //Log.v(LOG_TAG, forecastJsonStr);
            return forecastJsonStr;
        }

        @Override
        protected void onPostExecute(String result) {
            //Log.v(LOG_TAG, "Downloaded json: " + result);
            setmWeatherForecastJson(result);
        }
    }

}

