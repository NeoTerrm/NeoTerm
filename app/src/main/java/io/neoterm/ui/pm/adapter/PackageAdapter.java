package io.neoterm.ui.pm.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.wrdlbrnft.sortedlistadapter.SortedListAdapter;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.Comparator;

import io.neoterm.R;
import io.neoterm.ui.pm.adapter.viewholder.PackageViewHolder;
import io.neoterm.ui.pm.model.PackageModel;

public class PackageAdapter extends SortedListAdapter<PackageModel> implements FastScrollRecyclerView.SectionedAdapter {

    @NonNull
    @Override
    public String getSectionName(int position) {
        return sectionedAdapter != null ? sectionedAdapter.getSectionName(position) : "#";
    }

    public interface Listener {
        void onModelClicked(PackageModel model);
    }

    private final Listener listener;
    private final FastScrollRecyclerView.SectionedAdapter sectionedAdapter;

    public PackageAdapter(Context context, Comparator<PackageModel> comparator, Listener listener, FastScrollRecyclerView.SectionedAdapter sectionedAdapter) {
        super(context, PackageModel.class, comparator);
        this.listener = listener;
        this.sectionedAdapter = sectionedAdapter;
    }

    @NonNull
    @Override
    protected ViewHolder<? extends PackageModel> onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent, int viewType) {
        final View rootView = inflater.inflate(R.layout.package_item, parent, false);
        return new PackageViewHolder(rootView, listener);
    }
}
