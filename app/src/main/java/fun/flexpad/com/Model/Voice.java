package fun.flexpad.com.Model;

public class Voice {

    private String sender;
    private String roomname;
    private String roomID;
    private String roomCreatorID;
    private String time;
    private String messagelabel;
    private String type;
    private String message;
    private String name;
    private String messagekey;
    private String duration;

    public Voice(String sender, String roomname, String roomID, String roomCreatorID, String time, String messagelabel, String type, String message, String name, String messagekey, String duration) {
        this.sender = sender;
        this.roomname = roomname;
        this.roomID = roomID;
        this.roomCreatorID = roomCreatorID;
        this.time = time;
        this.messagelabel = messagelabel;
        this.type = type;
        this.message = message;
        this.name = name;
        this.messagekey = messagekey;
        this.duration = duration;
    }

    public Voice() {
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRoomname() {
        return roomname;
    }

    public void setRoomname(String roomname) {
        this.roomname = roomname;
    }

    public String getRoomID() {
        return roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }

    public String getRoomCreatorID() {
        return roomCreatorID;
    }

    public void setRoomCreatorID(String roomCreatorID) {
        this.roomCreatorID = roomCreatorID;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getMessagelabel() {
        return messagelabel;
    }

    public void setMessagelabel(String messagelabel) {
        this.messagelabel = messagelabel;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessagekey() {
        return messagekey;
    }

    public void setMessagekey(String messagekey) {
        this.messagekey = messagekey;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
