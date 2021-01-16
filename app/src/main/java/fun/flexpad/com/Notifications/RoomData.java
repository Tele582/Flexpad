package fun.flexpad.com.Notifications;

public class RoomData {
    private String roomuser;
//    private String room;
    private int roomicon;
    private String roombody;
    private String roomtitle;
    private String roomsented;

    public RoomData(String roomuser, int roomicon, String roombody, String roomtitle, String roomsented) {//, String room
        this.roomuser = roomuser;
//        this.room = room;
        this.roomicon = roomicon;
        this.roombody = roombody;
        this.roomtitle = roomtitle;
        this.roomsented = roomsented;
    }

    public RoomData() {
    }

    public String getRoomuser() {
        return roomuser;
    }

    public void setRoomuser(String roomuser) {
        this.roomuser = roomuser;
    }

//    public String getRoom() {
//        return room;
//    }
//
//    public void setRoom(String room) {
//        this.room = room;
//    }

    public int getRoomicon() {
        return roomicon;
    }

    public void setRoomicon(int roomicon) {
        this.roomicon = roomicon;
    }

    public String getRoombody() {
        return roombody;
    }

    public void setRoombody(String roombody) {
        this.roombody = roombody;
    }

    public String getRoomtitle() {
        return roomtitle;
    }

    public void setRoomtitle(String roomtitle) {
        this.roomtitle = roomtitle;
    }

    public String getRoomsented() {
        return roomsented;
    }

    public void setRoomsented(String roomsented) {
        this.roomsented = roomsented;
    }
}





