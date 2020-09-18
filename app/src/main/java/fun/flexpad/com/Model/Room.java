package fun.flexpad.com.Model;

public class Room {

    private String id;
    private String roomname;

    public Room (String id, String roomname) {
        this.id = id;
        this.roomname = roomname;
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
}


