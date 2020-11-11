package fun.flexpad.com.Model;

public class Room {

    private String id;
    private String roomname;
    public String creatorId;

    public Room (String id, String roomname, String creatorId) {
        this.id = id;
        this.roomname = roomname;
        this.creatorId = creatorId;
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
}


