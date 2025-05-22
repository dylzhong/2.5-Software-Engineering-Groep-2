package org.example;

import java.util.List;
import java.util.UUID;

/**
 * Represents a medication request submitted by a user.
 * <p>
 * A request contains metadata (e.g., submitter's name, location, date)
 * and a list of requested {@link ItemDetails}, along with a unique ID and status.
 */
public class Request {

    /** Unique identifier for the request, generated as a UUID string. */
    private String id;

    /** Full name of the user who submitted the request. */
    private String name;

    /** Location (e.g., department, unit) associated with the requester. */
    private String location;

    /** The submission date of the request, formatted as a string. */
    private String date;

    /** List of items (medications) included in this request. */
    private List<ItemDetails> items;

    /**
     * The current status of the request.
     * Possible values: {@code "Pending"}, {@code "Approved"}, {@code "Rejected"}.
     */
    private String status;

    /**
     * Constructs a new {@code Request} with the provided details.
     * A unique ID is automatically generated using {@link UUID}.
     *
     * @param name     The name of the requester.
     * @param location The location of the requester.
     * @param date     The date the request was made.
     * @param status   The initial status of the request.
     * @param itemDetails The list of items being requested.
     */
    public Request(String name, String location, String date, String status, List<ItemDetails> itemDetails) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.location = location;
        this.date = date;
        this.status = status;
        this.items = itemDetails;
    }

    // --- Getters ---

    /**
     * Returns the unique ID of the request.
     *
     * @return The request ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the name of the user who submitted the request.
     *
     * @return The user's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the location associated with the request.
     *
     * @return The user's location.
     */
    public String getLocation() {
        return location;
    }

    /**
     * Returns the submission date of the request.
     *
     * @return The request date.
     */
    public String getDate() {
        return date;
    }

    /**
     * Returns the list of items in the request.
     *
     * @return A list of requested items.
     */
    public List<ItemDetails> getItems() {
        return items;
    }

    /**
     * Returns the current status of the request.
     *
     * @return The request status.
     */
    public String getStatus() {
        return status;
    }

    // --- Setters ---

    /**
     * Sets the name of the requester.
     *
     * @param name The user's full name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the location of the requester.
     *
     * @param location The location name.
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Sets the date of the request.
     *
     * @param date The date as a string.
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * Sets the list of items included in the request.
     *
     * @param itemDetails The new list of items.
     */
    public void setItems(List<ItemDetails> itemDetails) {
        this.items = itemDetails;
    }

    /**
     * Sets the current status of the request.
     *
     * @param status New status value (e.g., {@code "Approved"}).
     */
    public void setStatus(String status) {
        this.status = status;
    }
}
