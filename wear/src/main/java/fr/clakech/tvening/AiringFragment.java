package fr.clakech.tvening;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.wearable.view.CardFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AiringFragment extends CardFragment {

    private View fragmentView;
    private View.OnClickListener listener;

    @Override
    public View onCreateContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        fragmentView = inflater.inflate(R.layout.watch_card_content, container, false);

        fragmentView.setOnClickListener(view -> {
            if (listener != null) {
                listener.onClick(view);
            }
        });

        Bundle args = this.getArguments();
        if (args != null) {
            TextView title = (TextView) fragmentView.findViewById(R.id.title);
            if (args.containsKey("CardFragment_title") && title != null) {
                title.setText(args.getString("CardFragment_title"));
            }

            if (args.containsKey("CardFragment_text")) {
                TextView text = (TextView) fragmentView.findViewById(R.id.text);
                if (text != null) {
                    text.setText(args.getString("CardFragment_text"));
                }
            }

            if (args.containsKey("CardFragment_imgUrl") && title != null) {
                String url = args.getString("CardFragment_imgUrl");
                Drawable image = DrawableCache.getInstance().get(url);
                title.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, image, null);
            }
        }

        return fragmentView;
    }

    public void setOnClickListener(final View.OnClickListener listener) {
        this.listener = listener;
    }

    public static AiringFragment create(String title, String text, String imgUrl) {
        AiringFragment fragment = new AiringFragment();
        Bundle args = new Bundle();
        if (title != null) {
            args.putString("CardFragment_title", title);
        }

        if (text != null) {
            args.putString("CardFragment_text", text);
        }

        if (imgUrl != null) {
            args.putString("CardFragment_imgUrl", imgUrl);
        }

        fragment.setArguments(args);
        return fragment;
    }

}
