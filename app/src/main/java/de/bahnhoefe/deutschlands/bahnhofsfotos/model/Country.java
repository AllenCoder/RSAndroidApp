package de.bahnhoefe.deutschlands.bahnhofsfotos.model;

import java.io.Serializable;

import static android.R.attr.id;

/**
 * Created by android_oma on 04.12.16.
 */

public class Country implements Serializable {
    private String countryName;
    private String countryShortCode;
    private String email;
    private String twitterTags;

    public Country(){

    }

    public Country(String countryName, String countryShortCode, String email, String twitterTags) {
        this.countryName = countryName;
        this.countryShortCode = countryShortCode;
        this.email = email;
        this.twitterTags = twitterTags;

    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getCountryShortCode() {
        return countryShortCode;
    }

    public void setCountryShortCode(String countryShortCode) {
        this.countryShortCode = countryShortCode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTwitterTags() {
        return twitterTags;
    }

    public void setTwitterTags(String twitterTags) {
        this.twitterTags = twitterTags;
    }

    @Override
    public String toString(){
        return "Land [Laendername=" + countryName + ", Laenderkuerzel=" + countryShortCode + ", E-Mail=" + email + ", TwitterTags=" + twitterTags  + "]";
    }
}
