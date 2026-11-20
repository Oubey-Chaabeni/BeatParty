package com.beatparty.common.dto;

import java.io.Serializable;
import java.util.Objects;

public class GuestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String handle;
    private String color;

    public GuestDTO() {}

    public GuestDTO(String handle, String color) {
        this.handle = handle;
        this.color = color;
    }

    public String getHandle() { return handle; }
    public void setHandle(String handle) { this.handle = handle; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GuestDTO)) return false;
        GuestDTO guestDTO = (GuestDTO) o;
        return Objects.equals(handle, guestDTO.handle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(handle);
    }
}