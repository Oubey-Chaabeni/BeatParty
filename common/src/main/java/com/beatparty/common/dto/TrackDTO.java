package com.beatparty.common.dto;

import java.io.Serializable;

public class TrackDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String title;
    private String artist;
    private String albumArtUrl;
    private String previewUrl;
    private int durationSec;
    private String suggestedBy;
    private int score;

    public TrackDTO() {}

    public TrackDTO(String id, String title, String artist, String albumArtUrl,
                    String previewUrl, int durationSec, String suggestedBy, int score) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.albumArtUrl = albumArtUrl;
        this.previewUrl = previewUrl;
        this.durationSec = durationSec;
        this.suggestedBy = suggestedBy;
        this.score = score;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public String getAlbumArtUrl() { return albumArtUrl; }
    public void setAlbumArtUrl(String albumArtUrl) { this.albumArtUrl = albumArtUrl; }

    public String getPreviewUrl() { return previewUrl; }
    public void setPreviewUrl(String previewUrl) { this.previewUrl = previewUrl; }

    public int getDurationSec() { return durationSec; }
    public void setDurationSec(int durationSec) { this.durationSec = durationSec; }

    public String getSuggestedBy() { return suggestedBy; }
    public void setSuggestedBy(String suggestedBy) { this.suggestedBy = suggestedBy; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
}