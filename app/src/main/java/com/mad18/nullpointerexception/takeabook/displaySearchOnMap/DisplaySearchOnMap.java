package com.mad18.nullpointerexception.takeabook.displaySearchOnMap;

import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.mad18.nullpointerexception.takeabook.R;

public class DisplaySearchOnMap extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_search_on_map);
        if (savedInstanceState == null) {
            FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
            trans.add(R.id.display_search_on_map_fragment, DisplaySearchOnMap_map.newInstance(null));
            trans.commit();
        }
    }
}
