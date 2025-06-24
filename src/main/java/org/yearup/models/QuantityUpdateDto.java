package org.yearup.models;

/**
 * QuantityUpdateDto is a simple Data Transfer Object (DTO) used specifically
 * for handling PUT requests to update the quantity of a product in the shopping cart.
 * This class only contains the 'quantity' field, which is all that the client
 * needs to send in the body of a PUT request.
 */

public class QuantityUpdateDto {

    // This is the only value expected from the request body when updating quantity
    private int quantity;

    // Default constructor (required for deserialization)
    public QuantityUpdateDto() {
    }

    // Getter for quantity
    public int getQuantity() {
        return quantity;
    }

    // Setter for quantity
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
