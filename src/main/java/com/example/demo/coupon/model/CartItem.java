package com.example.demo.coupon.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class CartItem {
	private final String productId;
	private final String categoryId;
	private final BigDecimal unitPrice;
	private final int quantity;

	public CartItem(String productId, String categoryId, BigDecimal unitPrice, int quantity) {
		if (productId == null || productId.isBlank()) {
			throw new IllegalArgumentException("productId must not be blank");
		}
		if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("unitPrice must be non-negative");
		}
		if (quantity <= 0) {
			throw new IllegalArgumentException("quantity must be positive");
		}
		this.productId = productId;
		this.categoryId = categoryId;
		this.unitPrice = unitPrice.setScale(2, RoundingMode.HALF_UP);
		this.quantity = quantity;
	}

	public String getProductId() {
		return productId;
	}

	public String getCategoryId() {
		return categoryId;
	}

	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	public int getQuantity() {
		return quantity;
	}

	public BigDecimal getLineTotal() {
		return unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CartItem cartItem = (CartItem) o;
		return quantity == cartItem.quantity && Objects.equals(productId, cartItem.productId) && Objects.equals(categoryId, cartItem.categoryId) && Objects.equals(unitPrice, cartItem.unitPrice);
	}

	@Override
	public int hashCode() {
		return Objects.hash(productId, categoryId, unitPrice, quantity);
	}

	@Override
	public String toString() {
		return "CartItem{" +
			"productId='" + productId + '\'' +
			", categoryId='" + categoryId + '\'' +
			", unitPrice=" + unitPrice +
			", quantity=" + quantity +
			'}';
	}
}