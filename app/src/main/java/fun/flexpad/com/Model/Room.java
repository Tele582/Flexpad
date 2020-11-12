package fun.flexpad.com.Model;

public class Room {

    private String id;
    private String roomname;
    public String creatorId;
    public String creatorUsername;
    private String roomKey;

    public Room (String id, String roomname, String creatorId, String creatorUsername, String roomKey) {
        this.id = id;
        this.roomname = roomname;
        this.creatorId = creatorId;
        this.creatorUsername = creatorUsername;
        this.roomKey = roomKey;
    }

    public Room() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRoomname() {
        return roomname;
    }

    public void setRoomname(String roomname) {
        this.roomname = roomname;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public String getCreatorUsername() {
        return creatorUsername;
    }

    public void setCreatorUsername(String creatorUsername) {
        this.creatorUsername = creatorUsername;
    }

    public String getRoomKey() {
        return roomKey;
    }

    public void setRoomKey(String roomKey) {
        this.roomKey = roomKey;
    }
}


