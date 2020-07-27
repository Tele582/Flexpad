package fun.flexpad.com.Model;

public class Contacts {

    public String phone_number;

    public Contacts(String phone_number) {
        this.phone_number = phone_number;
    }

    public Contacts() {
    }

    public String getId() {
        return phone_number;
    }

    public void setId(String id) {
        this.phone_number = phone_number;
    }
}



