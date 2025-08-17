package com.example.demo.coupon.engine;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PricingResult {
	private final BigDecimal originalTotal;
	private final BigDecimal finalPayable;
	private final BigDecimal totalDiscount;
	private final List<AppliedCouponResult> appliedCoupons;

	public PricingResult(BigDecimal originalTotal, BigDecimal finalPayable, List<AppliedCouponResult> appliedCoupons) {
		this.originalTotal = MoneyUtils.round2(originalTotal);
		this.finalPayable = MoneyUtils.round2(finalPayable);
		this.totalDiscount = MoneyUtils.round2(this.originalTotal.subtract(this.finalPayable));
		this.appliedCoupons = Collections.unmodifiableList(new ArrayList<>(appliedCoupons));
	}

	public BigDecimal getOriginalTotal() {
		return originalTotal;
	}

	public BigDecimal getFinalPayable() {
		return finalPayable;
	}

	public BigDecimal getTotalDiscount() {
		return totalDiscount;
	}

	public List<AppliedCouponResult> getAppliedCoupons() {
		return appliedCoupons;
	}
}