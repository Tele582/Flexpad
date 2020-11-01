package fun.flexpad.com.Model;

public class Chatlist {

    public String id;
    public String idSender;

    public Chatlist(String id, String idSender) {
        this.id = id;
//        this.idSender = idSender;

    }

    public Chatlist() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

//    public String getIdSender() {
//        return idSender;
//    }
//
//    public void setIdSender(String idSender) {
//        this.idSender = idSender;
//    }
}



