package fun.flexpad.com.Notifications;

public class RoomData {
    private String user;
    private String room;
    private int icon;
    private String body;
    private String title;
    private String sented;

    public RoomData(String user, String room, int icon, String body, String title, String sented) {
        this.user = user;
        this.room = room;
        this.icon = icon;
        this.body = body;
        this.title = title;
        this.sented = sented;
    }

    public RoomData() {
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSented() {
        return sented;
    }

    public void setSented(String sented) {
        this.sented = sented;
    }
}





