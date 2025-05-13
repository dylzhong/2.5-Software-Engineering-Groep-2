package org.example;

public class Item {

    private String id;
    private String ndc;
    private String details;
    private int amount;

    public Item (String id, String ndc, String details, int amount) {
        this.id = id;
        this.ndc = ndc;
        this.details = details;
        this.amount = amount;
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

    public int getAmount() {
        return amount;
    }

    // Setters
    public void setNdc(String ndc) {
        this.ndc = ndc;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
