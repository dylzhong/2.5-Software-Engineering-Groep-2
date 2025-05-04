package org.example;

public class Item {

    private String id;
    private String ndc;
    private String details;

    public Item (String id, String ndc, String details) {
        this.id = id;
        this.ndc = ndc;
        this.details = details;
    }

    //Getters
    public String getId() {
        return id;
    }

    public String getNdc() {
        return ndc;
    }

    public String getDetails() {
        return details;
    }

    @Override
    public String toString() {
        return "Medicijn{" +
                "id='" + id + '\'' +
                ", ndc='" + ndc + '\'' +
                ", details='" + details + '\'' +
                '}';
    }
}
