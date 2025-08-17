package com.example.demo.coupon;

import com.example.demo.coupon.engine.CouponPricingEngine;
import com.example.demo.coupon.engine.OptimalCouponSelector;
import com.example.demo.coupon.engine.PricingResult;
import com.example.demo.coupon.model.CartItem;
import com.example.demo.coupon.model.Coupon;
import com.example.demo.coupon.model.CouponScope;
import com.example.demo.coupon.model.CouponType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

class CouponEngineTests {
	@Test
	void percentage_all_items() {
		List<CartItem> cart = new ArrayList<>();
		cart.add(new CartItem("A", "c1", new BigDecimal("100.00"), 1));
		cart.add(new CartItem("B", "c2", new BigDecimal("50.00"), 2)); // 100

		Coupon c = Coupon.builder("C1", CouponType.PERCENTAGE, CouponScope.all())
			.discountRate(new BigDecimal("0.90")) // 10% off
			.applyOrder(1)
			.build();

		CouponPricingEngine engine = new CouponPricingEngine();
		PricingResult result = engine.applyCoupons(cart, List.of(c));

		Assertions.assertEquals(new BigDecimal("200.00"), result.getOriginalTotal());
		Assertions.assertEquals(new BigDecimal("180.00"), result.getFinalPayable());
		Assertions.assertEquals(new BigDecimal("20.00"), result.getTotalDiscount());
		Assertions.assertEquals(1, result.getAppliedCoupons().size());
	}

	@Test
	void fixed_amount_category_only() {
		List<CartItem> cart = new ArrayList<>();
		cart.add(new CartItem("A", "c1", new BigDecimal("80.00"), 1));
		cart.add(new CartItem("B", "c2", new BigDecimal("40.00"), 1));

		Coupon c = Coupon.builder("C2", CouponType.FIXED_AMOUNT, CouponScope.categories(Set.of("c1")))
			.fixedAmountOff(new BigDecimal("30.00"))
			.applyOrder(2)
			.build();

		CouponPricingEngine engine = new CouponPricingEngine();
		PricingResult result = engine.applyCoupons(cart, List.of(c));

		Assertions.assertEquals(new BigDecimal("120.00"), result.getOriginalTotal());
		Assertions.assertEquals(new BigDecimal("90.00"), result.getFinalPayable());
		Assertions.assertEquals(new BigDecimal("30.00"), result.getTotalDiscount());
	}

	@Test
	void threshold_not_met_no_apply() {
		List<CartItem> cart = new ArrayList<>();
		cart.add(new CartItem("A", "c1", new BigDecimal("30.00"), 1));

		Coupon c = Coupon.builder("C3", CouponType.FIXED_AMOUNT, CouponScope.all())
			.fixedAmountOff(new BigDecimal("10.00"))
			.thresholdAmount(new BigDecimal("50.00"))
			.applyOrder(1)
			.build();

		CouponPricingEngine engine = new CouponPricingEngine();
		PricingResult result = engine.applyCoupons(cart, List.of(c));

		Assertions.assertEquals(new BigDecimal("30.00"), result.getOriginalTotal());
		Assertions.assertEquals(new BigDecimal("30.00"), result.getFinalPayable());
		Assertions.assertEquals(new BigDecimal("0.00"), result.getTotalDiscount());
		Assertions.assertEquals(0, result.getAppliedCoupons().size());
	}

	@Test
	void optimal_selection_between_exclusive_and_combo() {
		List<CartItem> cart = new ArrayList<>();
		cart.add(new CartItem("A", "c1", new BigDecimal("100.00"), 1));
		cart.add(new CartItem("B", "c2", new BigDecimal("100.00"), 1));

		Coupon exclusive = Coupon.builder("EX", CouponType.FIXED_AMOUNT, CouponScope.all())
			.fixedAmountOff(new BigDecimal("60.00"))
			.exclusive(true)
			.applyOrder(1)
			.build();

		Coupon percent = Coupon.builder("P20", CouponType.PERCENTAGE, CouponScope.all())
			.discountRate(new BigDecimal("0.80")) // 20% off, discount=40 on 200
			.applyOrder(1)
			.build();

		Coupon fixed = Coupon.builder("F50", CouponType.FIXED_AMOUNT, CouponScope.all())
			.fixedAmountOff(new BigDecimal("50.00"))
			.applyOrder(2)
			.build();

		OptimalCouponSelector selector = new OptimalCouponSelector();
		PricingResult best = selector.findBest(cart, List.of(exclusive, percent, fixed));

		// Best should be percent then fixed: 200 * 20% = 40, then -50 => total 110 payable
		Assertions.assertEquals(new BigDecimal("110.00"), best.getFinalPayable());
		Assertions.assertEquals(new BigDecimal("90.00"), best.getTotalDiscount());
	}
}