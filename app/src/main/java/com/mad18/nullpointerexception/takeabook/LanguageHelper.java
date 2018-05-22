package com.mad18.nullpointerexception.takeabook;

import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

public class LanguageHelper {
    public static void changeLocale(Resources res, String locale){


        Configuration config;
        config = new Configuration(res.getConfiguration());

        switch (locale){

            case "it":
                config.locale = Locale.ITALIAN;
                break;

            case "eng":
                config.locale = Locale.ENGLISH;
                break;

             default:
                 config.locale = Locale.ENGLISH;
                 break;
        }

        res.updateConfiguration(config,res.getDisplayMetrics());
    }
}
