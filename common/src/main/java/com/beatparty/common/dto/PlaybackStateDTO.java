package com.beatparty.common.dto;

import java.io.Serializable;

public class PlaybackStateDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String clubId;
    private TrackDTO currentTrack;
    private int elapsedSec;
    private boolean playing;
    private int skipVotes;
    private int skipThreshold;

    public PlaybackStateDTO() {}

    public PlaybackStateDTO(String clubId, TrackDTO currentTrack, int elapsedSec,
                            boolean playing, int skipVotes, int skipThreshold) {
        this.clubId = clubId;
        this.currentTrack = currentTrack;
        this.elapsedSec = elapsedSec;
        this.playing = playing;
        this.skipVotes = skipVotes;
        this.skipThreshold = skipThreshold;
    }

    public String getClubId() { return clubId; }
    public void setClubId(String clubId) { this.clubId = clubId; }

    public TrackDTO getCurrentTrack() { return currentTrack; }
    public void setCurrentTrack(TrackDTO currentTrack) { this.currentTrack = currentTrack; }

    public int getElapsedSec() { return elapsedSec; }
    public void setElapsedSec(int elapsedSec) { this.elapsedSec = elapsedSec; }

    public boolean isPlaying() { return playing; }
    public void setPlaying(boolean playing) { this.playing = playing; }

    public int getSkipVotes() { return skipVotes; }
    public void setSkipVotes(int skipVotes) { this.skipVotes = skipVotes; }

    public int getSkipThreshold() { return skipThreshold; }
    public void setSkipThreshold(int skipThreshold) { this.skipThreshold = skipThreshold; }
}