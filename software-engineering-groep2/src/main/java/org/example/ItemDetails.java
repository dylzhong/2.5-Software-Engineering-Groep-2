package org.example;

/**
 * Represents the details of an inventory item, including its identifier,
 * NDC code, descriptive information, and available quantity.
 */
public class ItemDetails {

    /** The unique identifier of the item. */
    private String id;

    /** The National Drug Code (NDC) of the item. */
    private String ndc;

    /** Additional descriptive information about the item. */
    private String details;

    /** The current quantity available or requested. */
    private int amount;

    /**
     * Constructs a new {@code ItemDetails} object with the specified values.
     *
     * @param id      The unique identifier of the item.
     * @param ndc     The NDC code of the item.
     * @param details Additional descriptive information.
     * @param amount  The quantity available or requested.
     */
    public ItemDetails(String id, String ndc, String details, int amount) {
        this.id = id;
        this.ndc = ndc;
        this.details = details;
        this.amount = amount;
    }

    /**
     * Returns the unique identifier of the item.
     *
     * @return The item ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the NDC code of the item.
     *
     * @return The NDC code.
     */
    public String getNdc() {
        return ndc;
    }

    /**
     * Sets the NDC code of the item.
     *
     * @param ndc The new NDC code.
     */
    public void setNdc(String ndc) {
        this.ndc = ndc;
    }

    /**
     * Returns the details of the item.
     *
     * @return The item description.
     */
    public String getDetails() {
        return details;
    }

    /**
     * Sets the details or description of the item.
     *
     * @param details The new description.
     */
    public void setDetails(String details) {
        this.details = details;
    }

    /**
     * Returns the quantity of the item.
     *
     * @return The item amount.
     */
    public int getAmount() {
        return amount;
    }

    /**
     * Sets the quantity of the item.
     *
     * @param amount The new amount.
     */
    public void setAmount(int amount) {
        this.amount = amount;
    }
}
