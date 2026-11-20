package com.beatparty.common.rmi;

import com.beatparty.common.dto.ClubDTO;
import com.beatparty.common.dto.GuestDTO;
import com.beatparty.common.dto.PlaybackStateDTO;
import com.beatparty.common.dto.TrackDTO;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface ClubService extends Remote {

    List<ClubDTO> listClubs() throws RemoteException;

    ClubDTO getClub(String clubId) throws RemoteException;

    ClubDTO createClub(String name, String genre, String description, String accentColor) throws RemoteException;

    ClubDTO joinClub(String clubId, GuestDTO guest) throws RemoteException;

    ClubDTO leaveClub(String clubId, String guestHandle) throws RemoteException;

    ClubDTO suggestTrack(String clubId, TrackDTO track) throws RemoteException;

    ClubDTO voteTrack(String clubId, String trackId, String guestHandle, int delta) throws RemoteException;

    ClubDTO voteSkip(String clubId, String guestHandle) throws RemoteException;

    PlaybackStateDTO advanceTrack(String clubId) throws RemoteException;

    Map<String, Integer> triggerHype(String clubId, String tag, String guestHandle) throws RemoteException;
}