package fr.clakech.tvening;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.InputStream;
import java.util.Properties;

public class PropertyReader {

    private Context context;
    private Properties properties;

    public PropertyReader(Context context) {
        this.context = context;
        properties = new Properties();
    }

    public Properties getMyProperties(String file) {
        try {
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open(file);
            properties.load(inputStream);

        } catch (Exception e) {
            Log.d("TV", "getMyProperties error: " + e.getMessage(), e);
        }

        return properties;
    }
}