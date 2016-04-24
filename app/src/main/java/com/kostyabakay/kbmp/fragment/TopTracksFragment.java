package com.kostyabakay.kbmp.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.kostyabakay.kbmp.R;
import com.kostyabakay.kbmp.activity.MainActivity;
import com.kostyabakay.kbmp.adapter.PlaylistAdapter;
import com.kostyabakay.kbmp.model.chart.top.tracks.Track;
import com.kostyabakay.kbmp.network.asynctask.GetTopTracksAsyncTask;
import com.kostyabakay.kbmp.network.asynctask.PlayTrackAsyncTask;
import com.kostyabakay.kbmp.util.AppData;
import com.kostyabakay.kbmp.util.Constants;
import com.vk.sdk.VKSdk;

import java.util.ArrayList;

/**
 * Created by Kostya on 10.03.2016.
 * This class represents the list of different music tracks.
 */
public class TopTracksFragment extends Fragment {
    private PlaylistAdapter mPlaylistAdapter;
    private ListView mListView;
    private ArrayList<Track> mTracks = new ArrayList<>();
    private Track mCurrentTrack;
    private int mTrackPosition;

    public static TopTracksFragment newInstance() {
        TopTracksFragment fragment = new TopTracksFragment();
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TopTracksFragment.class.getSimpleName(), "onCreateView");
        View view = inflater.inflate(R.layout.fragment_top_tracks, container, false);
        MainActivity activity = (MainActivity) getActivity();
        if (activity.isNetworkConnected()) {
            if (!VKSdk.isLoggedIn()) {
                Toast.makeText(getActivity(),
                        R.string.please_log_in_to_vk, Toast.LENGTH_SHORT).show();
                return inflater.inflate(R.layout.fragment_starting, container, false);
            } else {
                return view;
            }
        } else {
            Toast.makeText(getActivity(),
                    R.string.please_check_internet_connection, Toast.LENGTH_LONG).show();
            return inflater.inflate(R.layout.fragment_starting, container, false);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TopTracksFragment.class.getSimpleName(), "onStart");
        if (VKSdk.isLoggedIn()) {
            setupUI();
            setTopTracksToListView();
            getTopTracks();
            listenUI();
        } else {
            Toast.makeText(getActivity(), R.string.please_log_in_to_vk, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mTracks.clear();
    }

    /**
     * Initialization of view elements.
     */
    private void setupUI() {
        mListView = (ListView) getActivity().findViewById(R.id.playlist_list_view);
    }

    /**
     * Gets top tracks from last.fm with chart.getTopTracks API method.
     */
    private void getTopTracks() {
        GetTopTracksAsyncTask getTopTracksAsyncTask = new GetTopTracksAsyncTask(getActivity(),
                mTracks, mPlaylistAdapter);
        getTopTracksAsyncTask.execute();
    }

    /**
     * Sets ArrayList of top tracks to ListView using PlaylistAdapter.
     */
    private void setTopTracksToListView() {
        mPlaylistAdapter = new PlaylistAdapter(getActivity(), mTracks);
        ((MainActivity) getActivity()).getViewPagerAdapter().setTracks(mTracks);
        mListView.setAdapter(mPlaylistAdapter);
    }

    /**
     * This is listener for the user interface.
     */
    private void listenUI() {
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mTrackPosition = position; // Makes global information about item position
                updateAudioPlayer();
                setCurrentTrack();
                playTrack();
                updateViewPager();
            }
        });
    }

    /**
     * Updates AudioPlayer data.
     */
    private void updateAudioPlayer() {
        if (AppData.isSongPlayed) AppData.sAudioPlayer.stop();
    }

    /**
     * Sets current track using information about track position.
     */
    private void setCurrentTrack() {
        mCurrentTrack = mPlaylistAdapter.getItem(mTrackPosition);
    }

    /**
     * Plays track from vk.com using data from last.fm.
     */
    private void playTrack() {
        AppData.sPlayingTrackMode = Constants.NETWORK_PLAYING_TRACK_MODE;
        PlayTrackAsyncTask playTrackAsyncTask = new PlayTrackAsyncTask(getActivity());
        playTrackAsyncTask.execute(createCurrentSongFullName());
        AppData.isSongPlayed = true;
    }

    /**
     * Creates full name of the song using artist name, splitter and song name.
     *
     * @return String with full song name.
     */
    private String createCurrentSongFullName() {
        return mCurrentTrack.getArtist().getName() + " - " + mCurrentTrack.getName();
    }

    /**
     * Updates ViewPager corresponding of the user action.
     */
    private void updateViewPager() {
        ((MainActivity) getActivity()).getViewPagerAdapter().setCurrentTrack(mCurrentTrack);
        ((MainActivity) getActivity()).getViewPagerAdapter()
                .setCurrentTrackItemIndex(mTrackPosition);
        ((MainActivity) getActivity()).getViewPager().setCurrentItem(1);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TopTracksFragment.class.getSimpleName(), "onDestroy");
        AppData.sAudioPlayer.stop();
    }
}