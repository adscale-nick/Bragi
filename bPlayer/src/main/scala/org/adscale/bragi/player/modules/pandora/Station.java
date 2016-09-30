package org.adscale.bragi.player.modules.pandora;
import java.io.Serializable;
import java.util.HashMap;
public class Station implements Comparable<Station>, Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String idToken;
    private boolean isCreator;
    private boolean isQuickMix;
    private String name;

    transient private boolean useQuickMix;

    public Station(HashMap<String, Object> data) {
        id = (String) data.get("stationId");
        idToken = (String) data.get("stationIdToken");
        isCreator = (Boolean) data.get("isCreator");
        isQuickMix = (Boolean) data.get("isQuickMix");
        name = (String) data.get("stationName");

        useQuickMix = false;
    }

    public Station(String id, String idToken, boolean creator, boolean quickMix, String name) {
        this.id = id;
        this.idToken = idToken;
        isCreator = creator;
        isQuickMix = quickMix;
        this.name = name;
    }

    public long getId() {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException ex) {
            return id.hashCode();
        }
    }

    public String getName() {
        return name;
    }

    public int compareTo(Station another) {
        return getName().compareTo(another.getName());
    }

    public boolean equals(Station another) {
        return getName().equals(another.getName());
    }

    public String getStationId() {
        return id;
    }

    public String getStationIdToken() {
        return idToken;
    }

    public boolean isCreator() {
        return isCreator;
    }

    public boolean isQuickMix() {
        return isQuickMix;
    }

    @Override
    public String toString() {
        return name;
    }
}
