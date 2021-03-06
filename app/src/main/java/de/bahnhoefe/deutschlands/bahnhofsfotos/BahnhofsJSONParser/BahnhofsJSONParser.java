package de.bahnhoefe.deutschlands.bahnhofsfotos.BahnhofsJSONParser;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.bahnhoefe.deutschlands.bahnhofsfotos.model.Bahnhof;
import de.bahnhoefe.deutschlands.bahnhofsfotos.model.Country;
import de.bahnhoefe.deutschlands.bahnhofsfotos.util.Constants;

import static android.R.attr.country;
import static java.lang.Integer.parseInt;

/**
 * Created by android_oma on 29.05.16.
 */

public class BahnhofsJSONParser {
    public static List<Bahnhof> parse(String json) throws JSONException {

        List<Bahnhof> bahnhoefe = new ArrayList<Bahnhof>();
        JSONArray bahnhofList = new JSONArray((bahnhoefe));

        for (int i = 0; i < bahnhofList.length(); i++){
            JSONObject jsonObj = (JSONObject) bahnhofList.get(i);
            String title = jsonObj.getString(Constants.DB_JSON_CONSTANTS.KEY_TITLE);
            String id = jsonObj.getString(Constants.DB_JSON_CONSTANTS.KEY_ID);
            String lat = jsonObj.getString(Constants.DB_JSON_CONSTANTS.KEY_LAT);
            String lon = jsonObj.getString(Constants.DB_JSON_CONSTANTS.KEY_LON);

            Bahnhof bahnhof = new Bahnhof();
            bahnhof.setTitle(title);
            bahnhof.setId(parseInt(id));
            bahnhof.setLat(Float.parseFloat(lat));
            bahnhof.setLon(Float.parseFloat(lon));

            Log.d("DatenbankInsertOk", bahnhof.toString());
            bahnhoefe.add(bahnhof);
        }

        return bahnhoefe;
    }

    public static List<Country> parseLaender(String jsonLaender) throws JSONException {

        List<Country> countries = new ArrayList<Country>();
        JSONArray countryList = new JSONArray((countries));

        for (int i = 0; i < countryList.length(); i++){
            JSONObject jsonObj = (JSONObject) countryList.get(i);
            String countryShortCode = jsonObj.getString(Constants.DB_JSON_CONSTANTS.KEY_COUNTRYSHORTCODE);
            String countryName = jsonObj.getString(Constants.DB_JSON_CONSTANTS.KEY_COUNTRYNAME);
            String email = jsonObj.getString(Constants.DB_JSON_CONSTANTS.KEY_EMAIL);
            String twitterTags = jsonObj.getString(Constants.DB_JSON_CONSTANTS.KEY_TWITTERTAGS);

            Country country = new Country();
            country.setCountryShortCode(countryShortCode);
            country.setCountryName(countryName);
            country.setEmail(email);
            country.setTwitterTags(twitterTags);

            Log.d("DatenbankLdInsertOk", country.toString());
            countries.add(country);
        }

        return countries;
    }


}
