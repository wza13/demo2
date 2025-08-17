package com.example.demo.coupon.engine;

import com.example.demo.coupon.model.CartItem;
import com.example.demo.coupon.model.Coupon;
import com.example.demo.coupon.model.CouponScopeType;
import com.example.demo.coupon.model.CouponType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CouponPricingEngine {

	public PricingResult applyCoupons(List<CartItem> cartItems, List<Coupon> coupons) {
		Objects.requireNonNull(cartItems, "cartItems");
		Objects.requireNonNull(coupons, "coupons");

		List<MutableItem> working = cartItems.stream()
			.map(MutableItem::from)
			.collect(Collectors.toCollection(ArrayList::new));

		BigDecimal originalTotal = working.stream()
			.map(MutableItem::getCurrentTotal)
			.reduce(BigDecimal.ZERO, BigDecimal::add);

		List<Coupon> sortedCoupons = new ArrayList<>(coupons);
		sortedCoupons.sort(Comparator.comparingInt(Coupon::getApplyOrder).thenComparing(Coupon::getId));

		List<AppliedCouponResult> applied = new ArrayList<>();

		for (Coupon coupon : sortedCoupons) {
			AppliedCouponResult result = applySingleCoupon(working, coupon);
			if (result != null && result.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
				applied.add(result);
			}
		}

		BigDecimal finalTotal = working.stream()
			.map(MutableItem::getCurrentTotal)
			.reduce(BigDecimal.ZERO, BigDecimal::add);

		return new PricingResult(originalTotal, finalTotal, applied);
	}

	private AppliedCouponResult applySingleCoupon(List<MutableItem> items, Coupon coupon) {
		List<MutableItem> eligible = items.stream()
			.filter(i -> isEligible(i, coupon))
			.collect(Collectors.toList());
		if (eligible.isEmpty()) {
			return null;
		}

		BigDecimal eligibleSubtotal = eligible.stream()
			.map(MutableItem::getCurrentTotal)
			.reduce(BigDecimal.ZERO, BigDecimal::add);

		if (coupon.getThresholdAmount() != null && eligibleSubtotal.compareTo(coupon.getThresholdAmount()) < 0) {
			return null;
		}

		Map<String, BigDecimal> perProductDiscount = new HashMap<>();
		BigDecimal discountTotal = switch (coupon.getType()) {
			case PERCENTAGE -> applyPercentageDiscount(eligible, coupon, perProductDiscount);
			case FIXED_AMOUNT -> applyFixedAmountDiscount(eligible, coupon, perProductDiscount);
		};

		if (discountTotal.compareTo(BigDecimal.ZERO) <= 0) {
			return null;
		}
		return new AppliedCouponResult(coupon.getId(), discountTotal, perProductDiscount);
	}

	private BigDecimal applyPercentageDiscount(List<MutableItem> eligible, Coupon coupon, Map<String, BigDecimal> perProductDiscount) {
		BigDecimal sumEligible = eligible.stream().map(MutableItem::getCurrentTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
		if (sumEligible.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;

		BigDecimal multiplier = coupon.getDiscountRate();
		BigDecimal discountTotal = MoneyUtils.round2(sumEligible.multiply(BigDecimal.ONE.subtract(multiplier)));

		List<BigDecimal> bases = eligible.stream().map(MutableItem::getCurrentTotal).collect(Collectors.toList());
		List<BigDecimal> allocations = MoneyUtils.allocateProportionally(bases, discountTotal);
		for (int i = 0; i < eligible.size(); i++) {
			MutableItem item = eligible.get(i);
			BigDecimal d = allocations.get(i);
			item.reduce(d);
			perProductDiscount.merge(item.productId, d, BigDecimal::add);
		}
		return discountTotal;
	}

	private BigDecimal applyFixedAmountDiscount(List<MutableItem> eligible, Coupon coupon, Map<String, BigDecimal> perProductDiscount) {
		BigDecimal sumEligible = eligible.stream().map(MutableItem::getCurrentTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
		if (sumEligible.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;

		BigDecimal discountTotal = coupon.getFixedAmountOff().min(sumEligible).setScale(2, RoundingMode.HALF_UP);
		List<BigDecimal> bases = eligible.stream().map(MutableItem::getCurrentTotal).collect(Collectors.toList());
		List<BigDecimal> allocations = MoneyUtils.allocateProportionally(bases, discountTotal);
		for (int i = 0; i < eligible.size(); i++) {
			MutableItem item = eligible.get(i);
			BigDecimal d = allocations.get(i);
			item.reduce(d);
			perProductDiscount.merge(item.productId, d, BigDecimal::add);
		}
		return discountTotal;
	}

	private boolean isEligible(MutableItem item, Coupon coupon) {
		return switch (coupon.getScope().getScopeType()) {
			case ALL -> true;
			case CATEGORY -> coupon.getScope().getCategoryIds().contains(item.categoryId);
			case PRODUCT -> coupon.getScope().getProductIds().contains(item.productId);
		};
	}

	private static class MutableItem {
		private final String productId;
		private final String categoryId;
		private final int quantity;
		private final BigDecimal originalTotal;
		private BigDecimal currentTotal;

		private MutableItem(String productId, String categoryId, int quantity, BigDecimal originalTotal) {
			this.productId = productId;
			this.categoryId = categoryId;
			this.quantity = quantity;
			this.originalTotal = MoneyUtils.round2(originalTotal);
			this.currentTotal = this.originalTotal;
		}

		public static MutableItem from(CartItem cartItem) {
			BigDecimal line = cartItem.getUnitPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
			return new MutableItem(cartItem.getProductId(), cartItem.getCategoryId(), cartItem.getQuantity(), line);
		}

		public BigDecimal getCurrentTotal() {
			return currentTotal;
		}

		public void reduce(BigDecimal amount) {
			currentTotal = MoneyUtils.round2(currentTotal.subtract(amount));
			if (currentTotal.compareTo(BigDecimal.ZERO) < 0) {
				currentTotal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
			}
		}
	}
}