package org.example;

import java.util.List;
import java.util.UUID;

public class Request {
    private String id;
    private String name;
    private String location;
    private String date;
    private List<Item> items;
    private String status; // bijv. "Pending", "Approved", "Rejected"// als String of LocalDateTime// toegevoegd

    public Request(String name, String location, String date, List<Item> items, String status) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.location = location;
        this.date = date;
        this.items = items;
        this.status = status;
    }

    //Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    public String getLocation() {
        return location;
    }

    public String getDate() {
        return date;
    }

    public List<Item> getItems() {
        return items;
    }

    public String getStatus() {
        return status;
    }

    //Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
