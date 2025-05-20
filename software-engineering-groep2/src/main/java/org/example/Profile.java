package org.example;

public class Profile {

    private final String name;
    private final String location;

    public Profile(String name, String location) {
        this.name = name;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }
}
