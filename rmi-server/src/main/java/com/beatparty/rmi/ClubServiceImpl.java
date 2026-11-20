package com.beatparty.rmi;

import com.beatparty.common.dto.ClubDTO;
import com.beatparty.common.dto.GuestDTO;
import com.beatparty.common.dto.PlaybackStateDTO;
import com.beatparty.common.dto.TrackDTO;
import com.beatparty.common.rmi.ClubService;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ClubServiceImpl extends UnicastRemoteObject implements ClubService {

    private static final long serialVersionUID = 1L;

    private final Map<String, ClubState> clubs = new ConcurrentHashMap<>();
    private final JmsEventPublisher publisher;

    private static class ClubState {
        final ClubDTO dto;
        final Set<String> skipVoters = new HashSet<>();
        // trackId -> (guestHandle -> +1 or -1)
        final Map<String, Map<String, Integer>> trackVotes = new HashMap<>();

        ClubState(ClubDTO dto) {
            this.dto = dto;
            this.dto.getHypeCounts().put("fire", 0);
            this.dto.getHypeCounts().put("hype", 0);
            this.dto.getHypeCounts().put("chill", 0);
            this.dto.getHypeCounts().put("heart", 0);
        }
    }

    public ClubServiceImpl(JmsEventPublisher publisher) throws RemoteException {
        super();
        this.publisher = publisher;
        seedDefaultClubs();
    }

    private void seedDefaultClubs() {
        createSeedClub("club-synth", "Midnight Synthwave", "Synthwave",
                "Retro analog drives and late-night neon grooves.", "#6D5DF6",
                new TrackDTO("seed-1", "Midnight City", "M83",
                        "https://is1-ssl.mzstatic.com/image/thumb/Music124/v4/5c/7f/2e/5c7f2e70-6a8f-0e61-0b55-6802d6e5e07e/886443188860.jpg/400x400bb.jpg",
                        "", 30, "PixelWave", 14));

        createSeedClub("club-lofi", "Tokyo Lo-Fi Study", "Lo-Fi",
                "Dusty vinyl beats for coding and late-night focus.", "#10B981",
                new TrackDTO("seed-2", "Snowman", "WYS",
                        "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/7b/44/73/7b4473a1-e4e5-6a40-4e3f-9b09fa372a36/artwork.jpg/400x400bb.jpg",
                        "", 30, "ChillBot", 9));

        createSeedClub("club-hiphop", "90s Boom Bap", "Hip-Hop",
                "Golden-era drums, jazz samples, and classic bars.", "#F59E0B",
                new TrackDTO("seed-3", "N.Y. State of Mind", "Nas",
                        "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/54/8d/62/548d6245-f8d3-f0e7-12e6-8b5b5f1940b6/074645768427.jpg/400x400bb.jpg",
                        "", 30, "BassDrop", 11));

        createSeedClub("club-house", "Deep House Session", "Electronic",
                "Warm basslines and hypnotic four-on-the-floor rhythms.", "#06B6D4",
                new TrackDTO("seed-4", "Latch", "Disclosure",
                        "https://is1-ssl.mzstatic.com/image/thumb/Music125/v4/9a/43/7c/9a437c6d-1231-5a5c-692e-783e5e32b51f/13UAAIM68740.rgb.jpg/400x400bb.jpg",
                        "", 30, "VinylFox", 7));
    }

    private void createSeedClub(String id, String name, String genre, String desc, String color, TrackDTO starterTrack) {
        ClubDTO dto = new ClubDTO(id, name, genre, desc, color);
        dto.setCurrentTrack(starterTrack);
        clubs.put(id, new ClubState(dto));
    }

    @Override
    public List<ClubDTO> listClubs() throws RemoteException {
        List<ClubDTO> result = new ArrayList<>();
        for (ClubState state : clubs.values()) {
            synchronized (state) {
                result.add(snapshot(state.dto));
            }
        }
        return result;
    }

    @Override
    public ClubDTO getClub(String clubId) throws RemoteException {
        ClubState state = requireClub(clubId);
        synchronized (state) {
            return snapshot(state.dto);
        }
    }

    @Override
    public ClubDTO createClub(String name, String genre, String description, String accentColor) throws RemoteException {
        String id = "club-" + UUID.randomUUID().toString().substring(0, 8);
        ClubDTO dto = new ClubDTO(id, name, genre, description, accentColor);
        ClubState state = new ClubState(dto);
        clubs.put(id, state);

        ClubDTO snap = snapshot(dto);
        publisher.publishQueueEvent("CLUB_CREATED", id, snap);
        return snap;
    }

    @Override
    public ClubDTO joinClub(String clubId, GuestDTO guest) throws RemoteException {
        ClubState state = requireClub(clubId);
        ClubDTO snap;
        synchronized (state) {
            if (!state.dto.getOnlineGuests().contains(guest)) {
                state.dto.getOnlineGuests().add(guest);
            }
            updateListenerMetrics(state);
            snap = snapshot(state.dto);
        }
        publisher.publishQueueEvent("GUEST_LIST_UPDATED", clubId, snap);
        return snap;
    }

    @Override
    public ClubDTO leaveClub(String clubId, String guestHandle) throws RemoteException {
        ClubState state = requireClub(clubId);
        ClubDTO snap;
        synchronized (state) {
            state.dto.getOnlineGuests().removeIf(g -> g.getHandle().equals(guestHandle));
            state.skipVoters.remove(guestHandle);
            updateListenerMetrics(state);
            snap = snapshot(state.dto);
        }
        publisher.publishQueueEvent("GUEST_LIST_UPDATED", clubId, snap);
        return snap;
    }

    @Override
    public ClubDTO suggestTrack(String clubId, TrackDTO track) throws RemoteException {
        ClubState state = requireClub(clubId);
        ClubDTO snap;
        synchronized (state) {
            if (track.getId() == null || track.getId().isBlank()) {
                track.setId(UUID.randomUUID().toString().substring(0, 8));
            }
            if (track.getDurationSec() <= 0) {
                track.setDurationSec(30);
            }
            track.setScore(1);

            if (state.dto.getCurrentTrack() == null) {
                state.dto.setCurrentTrack(track);
                state.dto.setElapsedSec(0);
                state.skipVoters.clear();
                state.dto.setSkipVotes(0);
            } else {
                state.dto.getQueue().add(track);
                Map<String, Integer> voters = new HashMap<>();
                if (track.getSuggestedBy() != null) {
                    voters.put(track.getSuggestedBy(), 1);
                }
                state.trackVotes.put(track.getId(), voters);
                sortQueue(state.dto.getQueue());
            }
            snap = snapshot(state.dto);
        }
        publisher.publishQueueEvent("QUEUE_UPDATED", clubId, snap);
        return snap;
    }

    @Override
    public ClubDTO voteTrack(String clubId, String trackId, String guestHandle, int delta) throws RemoteException {
        if (delta != 1 && delta != -1) {
            throw new RemoteException("Vote delta must be +1 or -1");
        }
        ClubState state = requireClub(clubId);
        ClubDTO snap;
        synchronized (state) {
            TrackDTO target = state.dto.getQueue().stream()
                    .filter(t -> t.getId().equals(trackId))
                    .findFirst()
                    .orElseThrow(() -> new RemoteException("Track not found in queue: " + trackId));

            Map<String, Integer> voters = state.trackVotes.computeIfAbsent(trackId, k -> new HashMap<>());
            int oldVote = voters.getOrDefault(guestHandle, 0);
            int newVote = (oldVote == delta) ? 0 : delta; // Toggle off if clicking same button again

            if (newVote == 0) {
                voters.remove(guestHandle);
            } else {
                voters.put(guestHandle, newVote);
            }

            target.setScore(target.getScore() + (newVote - oldVote));
            sortQueue(state.dto.getQueue());
            snap = snapshot(state.dto);
        }
        publisher.publishQueueEvent("QUEUE_UPDATED", clubId, snap);
        return snap;
    }

    @Override
    public ClubDTO voteSkip(String clubId, String guestHandle) throws RemoteException {
        ClubState state = requireClub(clubId);
        ClubDTO snap;
        boolean skipped = false;
        synchronized (state) {
            if (state.dto.getCurrentTrack() != null) {
                state.skipVoters.add(guestHandle);
                state.dto.setSkipVotes(state.skipVoters.size());

                if (state.dto.getSkipVotes() >= state.dto.getSkipThreshold()) {
                    advanceInternal(state);
                    skipped = true;
                }
            }
            snap = snapshot(state.dto);
        }
        publisher.publishQueueEvent(skipped ? "TRACK_CHANGED" : "SKIP_VOTE_UPDATED", clubId, snap);
        return snap;
    }

    @Override
    public PlaybackStateDTO advanceTrack(String clubId) throws RemoteException {
        ClubState state = requireClub(clubId);
        ClubDTO snap;
        PlaybackStateDTO playback;
        synchronized (state) {
            advanceInternal(state);
            snap = snapshot(state.dto);
            playback = new PlaybackStateDTO(
                    clubId,
                    snap.getCurrentTrack(),
                    snap.getElapsedSec(),
                    snap.getCurrentTrack() != null,
                    snap.getSkipVotes(),
                    snap.getSkipThreshold()
            );
        }
        publisher.publishQueueEvent("TRACK_CHANGED", clubId, snap);
        return playback;
    }

    @Override
    public Map<String, Integer> triggerHype(String clubId, String tag, String guestHandle) throws RemoteException {
        ClubState state = requireClub(clubId);
        Map<String, Integer> countsCopy;
        synchronized (state) {
            Map<String, Integer> counts = state.dto.getHypeCounts();
            counts.merge(tag, 1, Integer::sum);
            countsCopy = new HashMap<>(counts);
        }
        Map<String, Object> hypePayload = new HashMap<>();
        hypePayload.put("tag", tag);
        hypePayload.put("guest", guestHandle);
        hypePayload.put("counts", countsCopy);
        publisher.publishHypeEvent("HYPE_TRIGGERED", clubId, hypePayload);
        return countsCopy;
    }

    private void advanceInternal(ClubState state) {
        state.skipVoters.clear();
        state.dto.setSkipVotes(0);
        state.dto.setElapsedSec(0);

        List<TrackDTO> queue = state.dto.getQueue();
        if (!queue.isEmpty()) {
            TrackDTO next = queue.remove(0);
            state.trackVotes.remove(next.getId());
            state.dto.setCurrentTrack(next);
        } else {
            state.dto.setCurrentTrack(null);
        }
    }

    private void updateListenerMetrics(ClubState state) {
        int count = state.dto.getOnlineGuests().size();
        state.dto.setListenerCount(count);
        state.dto.setSkipThreshold(Math.max(1, Math.min(5, (count + 1) / 2)));
    }

    private void sortQueue(List<TrackDTO> queue) {
        queue.sort(Comparator.comparingInt(TrackDTO::getScore).reversed());
    }

    private ClubState requireClub(String clubId) throws RemoteException {
        ClubState state = clubs.get(clubId);
        if (state == null) {
            throw new RemoteException("Club not found: " + clubId);
        }
        return state;
    }

    // Defensive deep-ish snapshot so RMI serialization never hits ConcurrentModificationException
    private ClubDTO snapshot(ClubDTO src) {
        ClubDTO copy = new ClubDTO(src.getId(), src.getName(), src.getGenre(), src.getDescription(), src.getAccentColor());
        copy.setListenerCount(src.getListenerCount());
        copy.setOnlineGuests(new ArrayList<>(src.getOnlineGuests()));
        copy.setCurrentTrack(src.getCurrentTrack());
        copy.setElapsedSec(src.getElapsedSec());
        copy.setSkipVotes(src.getSkipVotes());
        copy.setSkipThreshold(src.getSkipThreshold());
        copy.setQueue(new ArrayList<>(src.getQueue()));
        copy.setHypeCounts(new ConcurrentHashMap<>(src.getHypeCounts()));
        return copy;
    }
}