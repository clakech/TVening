package fr.clakech.tvening;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.GridViewPager;

public class ProgramActivity extends Activity {

    private GridViewPager pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_program);
        pager = (GridViewPager) findViewById(R.id.program);

        Bundle b = this.getIntent().getExtras();
        fr.clakech.tvening.Airing airing = (Airing) b.getSerializable("airing");
        String imgUrl = b.getString("imgUrl");

        pager.setAdapter(new ProgramGridPagerAdapter(airing, imgUrl, ProgramActivity.this, getFragmentManager()));
    }
}
