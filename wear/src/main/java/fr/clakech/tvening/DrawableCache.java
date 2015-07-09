package fr.clakech.tvening;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.LruCache;

/**
 * Singleton
 * Classe permettant de sauvegarder en mémoire des drawable
 * Possède deux fonctions principales :
 *  - put(Integer key, Drawable drawable)
 *  - get(Integer key) : Drawable
 */
public class DrawableCache extends LruCache<String, Drawable> {

    private static DrawableCache INSTANCE;

    public static DrawableCache init(int size) {
        INSTANCE = new DrawableCache(size);
        return INSTANCE;
    }

    private DrawableCache(int size) {
        super(size);
    }

    public static DrawableCache getInstance() {
        return INSTANCE;
    }

    @Override
    protected Drawable create(final String entry) {
        return new ColorDrawable(Color.TRANSPARENT);
    }
}
