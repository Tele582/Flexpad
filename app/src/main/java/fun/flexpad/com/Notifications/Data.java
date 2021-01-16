package fun.flexpad.com.Notifications;

public class Data {
    private String user;
    private String room;
    private String roomTitle;
    private int icon;
    private String body;
    private String title;
    private String sented;
    private String notificationType;

    public Data(String user, String room, String roomTitle, int icon, String body, String title, String sented, String notificationType) {
        this.user = user;
        this.room = room;
        this.roomTitle = roomTitle;
        this.icon = icon;
        this.body = body;
        this.title = title;
        this.sented = sented;
        this.notificationType = notificationType;
    }

    public Data() {
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

    public String getRoomTitle() {
        return roomTitle;
    }

    public void setRoomTitle(String roomTitle) {
        this.roomTitle = roomTitle;
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

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }
}





