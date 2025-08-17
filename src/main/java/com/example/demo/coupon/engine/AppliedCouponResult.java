package com.example.demo.coupon.engine;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AppliedCouponResult {
	private final String couponId;
	private final BigDecimal discountAmount;
	private final Map<String, BigDecimal> perProductDiscount;

	public AppliedCouponResult(String couponId, BigDecimal discountAmount, Map<String, BigDecimal> perProductDiscount) {
		this.couponId = couponId;
		this.discountAmount = MoneyUtils.round2(discountAmount);
		this.perProductDiscount = Collections.unmodifiableMap(new HashMap<>(perProductDiscount));
	}

	public String getCouponId() {
		return couponId;
	}

	public BigDecimal getDiscountAmount() {
		return discountAmount;
	}

	public Map<String, BigDecimal> getPerProductDiscount() {
		return perProductDiscount;
	}
}