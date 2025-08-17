package com.example.demo.coupon.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Coupon {
	private final String id;
	private final CouponType type;
	private final CouponScope scope;
	private final BigDecimal discountRate; // for PERCENTAGE: multiplier in (0,1], e.g., 0.9 means 10% off
	private final BigDecimal fixedAmountOff; // for FIXED_AMOUNT
	private final BigDecimal thresholdAmount; // minimum eligible subtotal required, nullable
	private final boolean exclusive; // cannot combine with any other coupon
	private final Set<String> incompatibleCouponIds; // cannot be combined with these coupon ids
	private final int applyOrder; // lower applies first

	private Coupon(Builder builder) {
		this.id = Objects.requireNonNull(builder.id, "id");
		this.type = Objects.requireNonNull(builder.type, "type");
		this.scope = Objects.requireNonNull(builder.scope, "scope");
		this.discountRate = builder.discountRate == null ? null : builder.discountRate.setScale(2, RoundingMode.HALF_UP);
		this.fixedAmountOff = builder.fixedAmountOff == null ? null : builder.fixedAmountOff.setScale(2, RoundingMode.HALF_UP);
		this.thresholdAmount = builder.thresholdAmount == null ? null : builder.thresholdAmount.setScale(2, RoundingMode.HALF_UP);
		this.exclusive = builder.exclusive;
		this.incompatibleCouponIds = Collections.unmodifiableSet(new HashSet<>(builder.incompatibleCouponIds));
		this.applyOrder = builder.applyOrder;
		validate();
	}

	private void validate() {
		switch (type) {
			case PERCENTAGE -> {
				if (discountRate == null) {
					throw new IllegalArgumentException("discountRate required for PERCENTAGE coupon");
				}
				if (discountRate.compareTo(BigDecimal.ZERO) <= 0 || discountRate.compareTo(BigDecimal.ONE) > 0) {
					throw new IllegalArgumentException("discountRate must be in (0,1]");
				}
			}
			case FIXED_AMOUNT -> {
				if (fixedAmountOff == null || fixedAmountOff.compareTo(BigDecimal.ZERO) <= 0) {
					throw new IllegalArgumentException("fixedAmountOff must be positive for FIXED_AMOUNT coupon");
				}
			}
		}
	}

	public String getId() {
		return id;
	}

	public CouponType getType() {
		return type;
	}

	public CouponScope getScope() {
		return scope;
	}

	public BigDecimal getDiscountRate() {
		return discountRate;
	}

	public BigDecimal getFixedAmountOff() {
		return fixedAmountOff;
	}

	public BigDecimal getThresholdAmount() {
		return thresholdAmount;
	}

	public boolean isExclusive() {
		return exclusive;
	}

	public Set<String> getIncompatibleCouponIds() {
		return incompatibleCouponIds;
	}

	public int getApplyOrder() {
		return applyOrder;
	}

	public static Builder builder(String id, CouponType type, CouponScope scope) {
		return new Builder(id, type, scope);
	}

	public static class Builder {
		private final String id;
		private final CouponType type;
		private final CouponScope scope;
		private BigDecimal discountRate;
		private BigDecimal fixedAmountOff;
		private BigDecimal thresholdAmount;
		private boolean exclusive;
		private Set<String> incompatibleCouponIds = new HashSet<>();
		private int applyOrder = 10; // default percentage first

		public Builder(String id, CouponType type, CouponScope scope) {
			this.id = id;
			this.type = type;
			this.scope = scope;
		}

		public Builder discountRate(BigDecimal discountRate) {
			this.discountRate = discountRate;
			return this;
		}

		public Builder fixedAmountOff(BigDecimal fixedAmountOff) {
			this.fixedAmountOff = fixedAmountOff;
			return this;
		}

		public Builder thresholdAmount(BigDecimal thresholdAmount) {
			this.thresholdAmount = thresholdAmount;
			return this;
		}

		public Builder exclusive(boolean exclusive) {
			this.exclusive = exclusive;
			return this;
		}

		public Builder incompatibleWith(Set<String> couponIds) {
			if (couponIds != null) this.incompatibleCouponIds.addAll(couponIds);
			return this;
		}

		public Builder applyOrder(int applyOrder) {
			this.applyOrder = applyOrder;
			return this;
		}

		public Coupon build() {
			return new Coupon(this);
		}
	}
}