package fr.clakech.tvening;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.wearable.view.FragmentGridPagerAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class ProgramGridPagerAdapter extends FragmentGridPagerAdapter {

    private final Context mContext;

    private Fragment fragment;

    private fr.clakech.tvening.Airing airing;
    private String imgUrl;

    public ProgramGridPagerAdapter(final Airing airing, final String imgUrl, Context ctx, FragmentManager fm) {
        super(fm);
        mContext = ctx;
        this.airing = airing;
        this.imgUrl = imgUrl;

        this.fragment = cardFragment(airing, imgUrl);
    }

    private Fragment cardFragment(final Airing airing, final String imgUrl) {
        Resources res = mContext.getResources();

        SimpleDateFormat simpleDateFormatRead = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormatRead.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat simpleDateFormatWrite = new SimpleDateFormat("kk:mm");
        Date heure = null;
        try {
            heure = simpleDateFormatRead.parse(airing.AiringTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String dislayHeure = heure != null ? simpleDateFormatWrite.format(heure) : "";
        StringBuffer descriptions = new StringBuffer(airing.Title != null ? airing.Title : "");
        if (airing.EpisodeTitle != null) {
            descriptions
                    .append("\n")
                    .append(airing.EpisodeTitle);
        }
        if (airing.CopyText != null) {
            descriptions
                    .append("\n\n")
                    .append(airing.CopyText);
        }
        if (airing.Duration > 0) {
            descriptions
                    .append("\n\n")
                    .append(airing.Duration)
                    .append(" min.");
        }
        AiringFragment fragment =
                AiringFragment.create(dislayHeure, descriptions.toString(), imgUrl);

        // Add some extra bottom margin to leave room for the page indicator
        fragment.setCardMarginTop(
                res.getDimensionPixelSize(R.dimen.card_margin_bottom));
        return fragment;
    }

    @Override
    public Fragment getFragment(int row, int col) {
        return fragment;
    }

    @Override
    public Drawable getBackgroundForRow(final int row) {
        return getBackgroundForPage(row, 0);
    }

    @Override
    public Drawable getBackgroundForPage(final int row, final int column) {

        if (airing.ImageUrl != null)
            return DrawableCache.getInstance().get(airing.ImageUrl);
        else
            return new ColorDrawable(Color.TRANSPARENT);
    }

    @Override
    public int getRowCount() {
        return 1;
    }

    @Override
    public int getColumnCount(int rowNum) {
        return 1;
    }

}
