package fun.flexpad.com.Model;

public class Chat {

    private String sender;
    private String receiver;
    private String messagelabel;
    private String message;
    private boolean isseen;

    private String name;
    private String type;

    private String userid;
    private String messagekey;




    public Chat(String sender, String receiver, String messagelabel, String message, boolean isseen, String name, String type, String userid, String messageid, String messagekey){
        this.sender = sender;
        this.receiver = receiver;
        this.messagelabel = messagelabel;
        this.message = message;
        this.isseen = isseen;
        this.name = name;
        this.type = type;
        this.userid = userid;
        this.messagekey = messagekey;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessagelabel() {
        return messagelabel;
    }

    public void setMessagelabel(String messagelabel) {
        this.messagelabel = messagelabel;
    }

    public String getMessagekey() {
        return messagekey;
    }

    public void setMessagekey(String messagekey) {
        this.messagekey = messagekey;
    }

    public Chat() {

    }

    public boolean isIsseen() {
        return isseen;
    }

    public void setIsseen(boolean isseen) {
        this.isseen = isseen;
    }
}
