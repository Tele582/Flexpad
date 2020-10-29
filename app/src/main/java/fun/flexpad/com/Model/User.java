package fun.flexpad.com.Model;

import org.jetbrains.annotations.NotNull;

public class User {

    private String id;
    private String username;
    private String imageURI;
    private String status;
    private String search;
    private String contact;

    public User (String id, String username, String imageURI, String status, String search, String contact) {
        this.id = id;
        this.username = username;
        this.imageURI = imageURI;
        this.status = status;
        this.search = search;
        this.contact = contact;

    }

    public User () {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImageURI() {
        return imageURI;
    }

    public void setImageURI(String imageURI) {
        this.imageURI = imageURI;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    @NotNull
    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", imageURI='" + imageURI + '\'' +
                ", status='" + status + '\'' +
                ", search='" + search + '\'' +
                ", contact='" + contact + '\'' +
                '}';
    }
}
