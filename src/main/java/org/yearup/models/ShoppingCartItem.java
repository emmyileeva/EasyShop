package org.yearup.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;

public class ShoppingCartItem {
    private Product product = null;
    private int quantity = 1;

    public ShoppingCartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @JsonIgnore
    public int getProductId() {
        return this.product.getProductId();
    }

    public BigDecimal getLineTotal() {
        BigDecimal basePrice = product.getPrice();
        BigDecimal quantity = new BigDecimal(this.quantity);

        BigDecimal subTotal = basePrice.multiply(quantity);
        return subTotal;
    }
}
