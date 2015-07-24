package fr.clakech.tvening;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.wearable.view.FragmentGridPagerAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class ScheduleGridPagerAdapter extends FragmentGridPagerAdapter {

    private final Context mContext;
    private List<Row> mRows;

    public ScheduleGridPagerAdapter(List<GridChannel> gridChannels, ActionFragment.Listener nextAction, Context ctx, FragmentManager fm) {
        super(fm);
        mContext = ctx;
        loadRows(gridChannels, nextAction);
    }

    public void loadRows(List<GridChannel> gridChannels, ActionFragment.Listener nextAction) {
        mRows = new ArrayList<>();
        for (GridChannel gridChannel : gridChannels) {
            if (gridChannel != null && gridChannel.Channel != null) {
                List<Fragment> fragments = new ArrayList<>();
                if (gridChannel.Airings != null) {
                    for (Airing airing : gridChannel.Airings) {
                        String imgUrl = null;
                        if (gridChannel.ChannelImages != null && gridChannel.ChannelImages.length > 0) {
                            imgUrl = gridChannel.ChannelImages[0].ImageUrl;
                        }
                        Fragment fragment = cardFragment(airing, imgUrl);
                        fragments.add(fragment);
                    }
                }
                Fragment[] f = fragments.toArray(new Fragment[0]);
                mRows.add(new Row(gridChannel, f));
            }
        }

        mRows.add(new Row(null, ActionFragment.create(R.drawable.ic_forward_white_48dp, R.string.next, nextAction)));
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
        if (airing.Duration > 0) {
            descriptions
                    .append("\n\n")
                    .append(airing.Duration)
                    .append(" min.");
        }
        AiringFragment fragment =
                AiringFragment.create(dislayHeure, descriptions.toString(), imgUrl);
        fragment.setOnClickListener(v -> {
            Intent program = new Intent(mContext, ProgramActivity.class);
            program.putExtra("airing", airing);
            program.putExtra("imgUrl", imgUrl);
            mContext.startActivity(program);
        });
        // Add some extra bottom margin to leave room for the page indicator
        fragment.setCardMarginTop(
                res.getDimensionPixelSize(R.dimen.card_margin_bottom));
        return fragment;
    }

    /**
     * A convenient container for a row of fragments.
     */
    private class Row {
        final List<Fragment> columns = new ArrayList<>();
        final GridChannel gridChannel;

        public Row(GridChannel gridChannel, Fragment... fragments) {
            this.gridChannel = gridChannel;
            for (Fragment f : fragments) {
                add(f);
            }
        }

        public void add(Fragment f) {
            columns.add(f);
        }

        Fragment getColumn(int i) {
            return columns.get(i);
        }

        public int getColumnCount() {
            return columns.size();
        }
    }

    @Override
    public Fragment getFragment(int row, int col) {
        Row adapterRow = mRows.get(row);
        return adapterRow.getColumn(col);
    }

    @Override
    public Drawable getBackgroundForRow(final int row) {
        return getBackgroundForPage(row, 0);
    }

    @Override
    public Drawable getBackgroundForPage(final int row, final int column) {
        String url = null;
        if (mRows.get(row).gridChannel != null
                && mRows.get(row).gridChannel.Airings != null
                && mRows.get(row).gridChannel.Airings.length > 0) {
            url = mRows.get(row).gridChannel.Airings[column].ImageUrl;
        }
        if (url != null)
            return DrawableCache.getInstance().get(url);
        else
            return new ColorDrawable(Color.TRANSPARENT);
    }

    @Override
    public int getRowCount() {
        return mRows.size();
    }

    @Override
    public int getColumnCount(int rowNum) {
        return mRows.get(rowNum).getColumnCount();
    }

}
