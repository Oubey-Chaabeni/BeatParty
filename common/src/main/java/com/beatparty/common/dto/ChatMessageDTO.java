package com.beatparty.common.dto;

import java.io.Serializable;

public class ChatMessageDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String clubId;
    private String sender;
    private String senderColor;
    private String content;
    private String type; // CHAT, SYSTEM, VIBE
    private String vibeTag;
    private long timestamp;

    public ChatMessageDTO() {}

    public ChatMessageDTO(String id, String clubId, String sender, String senderColor,
                          String content, String type, String vibeTag, long timestamp) {
        this.id = id;
        this.clubId = clubId;
        this.sender = sender;
        this.senderColor = senderColor;
        this.content = content;
        this.type = type;
        this.vibeTag = vibeTag;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getClubId() { return clubId; }
    public void setClubId(String clubId) { this.clubId = clubId; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getSenderColor() { return senderColor; }
    public void setSenderColor(String senderColor) { this.senderColor = senderColor; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getVibeTag() { return vibeTag; }
    public void setVibeTag(String vibeTag) { this.vibeTag = vibeTag; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}