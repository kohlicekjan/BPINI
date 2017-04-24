package cz.kohlicek.bpini.ui.tag;


import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.kohlicek.bpini.R;
import cz.kohlicek.bpini.adapter.BaseRecyclerViewAdapter;
import cz.kohlicek.bpini.adapter.EndlessRecyclerViewScrollListener;
import cz.kohlicek.bpini.adapter.TagAdapter;
import cz.kohlicek.bpini.model.Tag;
import cz.kohlicek.bpini.service.BPINIClient;
import cz.kohlicek.bpini.service.BPINIService;
import cz.kohlicek.bpini.ui.view.EmptyRecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class TagListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener, BaseRecyclerViewAdapter.OnClickListener<Tag> {

    @BindView(R.id.recycler_view)
    EmptyRecyclerView recyclerView;

    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSnipSwipeRefreshLayout;

    @BindView(R.id.empty_view)
    View mEmptyView;

    @BindView(R.id.stub_no_connection)
    ViewStub stub;
    Snackbar snackbar;

    FloatingActionButton fab;

    private TagAdapter adapter;
    private BPINIService bpiniService;
    private LinearLayoutManager linearLayoutManager;
    private EndlessRecyclerViewScrollListener scrollListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tag_list, container, false);
        ButterKnife.bind(this, view);

        this.getActivity().setTitle(R.string.tag_list_title);

        fab = (FloatingActionButton) this.getActivity().findViewById(R.id.fab_add);
        fab.setVisibility(View.GONE);
        fab.setOnClickListener(null);

        bpiniService = BPINIClient.getInstance(this.getContext());
        adapter = new TagAdapter(this.getContext());
        adapter.setOnClickListener(this);

        linearLayoutManager = new LinearLayoutManager(this.getActivity());
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public int getFooterViewType(int defaultNoFooterViewType) {
                return 1;
            }

            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                load(totalItemsCount, true);
            }
        };

        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addOnScrollListener(scrollListener);
        recyclerView.setEmptyView(mEmptyView);


        mSnipSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent);
        mSnipSwipeRefreshLayout.setOnRefreshListener(this);

        load(0, true);

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (snackbar != null)
            snackbar.dismiss();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case android.support.design.R.id.snackbar_action:
                adapter.clear();
                load(0, true);
                break;
        }
    }

    @Override
    public void onClick(View v, int position, Tag data) {
        Intent intent = new Intent(this.getContext(), TagFormActivity.class);
        intent.putExtra(TagFormActivity.TAG_ID, data.getId());
        startActivity(intent);
    }


    @Override
    public void onRefresh() {
        adapter.clear();
        load(0, false);
        mSnipSwipeRefreshLayout.setRefreshing(false);
    }


    private void load(int skip, boolean loading) {
        adapter.setLoading(loading);
        if (loading) {
            recyclerView.setVisibility(View.VISIBLE);
            visibleNoConnection(false);
        }

        Call<List<Tag>> call = bpiniService.getTags("-created", skip);
        call.enqueue(new Callback<List<Tag>>() {
            @Override
            public void onResponse(Call<List<Tag>> call, Response<List<Tag>> response) {
                if (response.isSuccessful()) {
                    adapter.addAll(response.body());

                    recyclerView.setVisibility(View.VISIBLE);
                    visibleNoConnection(false);
                } else {

                }
            }

            @Override
            public void onFailure(Call<List<Tag>> call, Throwable t) {
                recyclerView.setVisibility(View.GONE);
                visibleNoConnection(true);
            }
        });
    }

    private void visibleNoConnection(boolean visible) {
        if (visible) {
            snackbar = Snackbar.make(getActivity().findViewById(R.id.coordinatorLayout), R.string.no_connection_message, Snackbar.LENGTH_LONG);
            snackbar.setAction(R.string.no_connection_repeat, this);
            snackbar.show();
            stub.setVisibility(View.VISIBLE);
        } else {
            if (snackbar != null)
                snackbar.dismiss();
            stub.setVisibility(View.GONE);
        }
    }


}