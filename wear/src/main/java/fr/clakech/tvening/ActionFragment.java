package fr.clakech.tvening;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.wearable.view.CircledImageView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ActionFragment extends Fragment implements View.OnClickListener {

    private static Listener mListener;
    private CircledImageView vIcon;
    private TextView vLabel;
    private boolean clicked = false;

    public static ActionFragment create(int iconResId, int labelResId, Listener listener) {
        mListener = listener;
        ActionFragment fragment = new ActionFragment();
        Bundle args = new Bundle();
        args.putInt("ICON", iconResId);
        args.putInt("LABEL", labelResId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_action, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        vIcon = (CircledImageView) view.findViewById(R.id.icon);
        vLabel = (TextView) view.findViewById(R.id.label);
        vIcon.setImageResource(getArguments().getInt("ICON"));
        vLabel.setText(getArguments().getInt("LABEL"));
        view.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (clicked)
            return;
        vLabel.setText("Loading");
        mListener.onActionPerformed();
        clicked = true;
    }

    public interface Listener {
        void onActionPerformed();
    }
}