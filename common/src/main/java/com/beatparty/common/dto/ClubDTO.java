package com.beatparty.common.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClubDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String genre;
    private String description;
    private String accentColor;
    private int listenerCount;
    private List<GuestDTO> onlineGuests = new ArrayList<>();
    private TrackDTO currentTrack;
    private int elapsedSec;
    private int skipVotes;
    private int skipThreshold;
    private List<TrackDTO> queue = new ArrayList<>();
    private Map<String, Integer> hypeCounts = new ConcurrentHashMap<>();

    public ClubDTO() {}

    public ClubDTO(String id, String name, String genre, String description, String accentColor) {
        this.id = id;
        this.name = name;
        this.genre = genre;
        this.description = description;
        this.accentColor = accentColor;
        this.skipThreshold = 3;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAccentColor() { return accentColor; }
    public void setAccentColor(String accentColor) { this.accentColor = accentColor; }

    public int getListenerCount() { return listenerCount; }
    public void setListenerCount(int listenerCount) { this.listenerCount = listenerCount; }

    public List<GuestDTO> getOnlineGuests() { return onlineGuests; }
    public void setOnlineGuests(List<GuestDTO> onlineGuests) { this.onlineGuests = onlineGuests; }

    public TrackDTO getCurrentTrack() { return currentTrack; }
    public void setCurrentTrack(TrackDTO currentTrack) { this.currentTrack = currentTrack; }

    public int getElapsedSec() { return elapsedSec; }
    public void setElapsedSec(int elapsedSec) { this.elapsedSec = elapsedSec; }

    public int getSkipVotes() { return skipVotes; }
    public void setSkipVotes(int skipVotes) { this.skipVotes = skipVotes; }

    public int getSkipThreshold() { return skipThreshold; }
    public void setSkipThreshold(int skipThreshold) { this.skipThreshold = skipThreshold; }

    public List<TrackDTO> getQueue() { return queue; }
    public void setQueue(List<TrackDTO> queue) { this.queue = queue; }

    public Map<String, Integer> getHypeCounts() { return hypeCounts; }
    public void setHypeCounts(Map<String, Integer> hypeCounts) { this.hypeCounts = hypeCounts; }
}