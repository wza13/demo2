package com.example.demo.coupon.engine;

import com.example.demo.coupon.model.CartItem;
import com.example.demo.coupon.model.Coupon;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class OptimalCouponSelector {
	private final CouponPricingEngine pricingEngine = new CouponPricingEngine();

	public PricingResult findBest(List<CartItem> items, List<Coupon> coupons) {
		Objects.requireNonNull(items, "items");
		Objects.requireNonNull(coupons, "coupons");
		if (coupons.isEmpty()) {
			return pricingEngine.applyCoupons(items, Collections.emptyList());
		}

		PricingResult bestResult = null;
		List<Coupon> bestSet = null;

		int n = coupons.size();
		if (n <= 18) {
			int total = 1 << n;
			for (int mask = 0; mask < total; mask++) {
				List<Coupon> subset = new ArrayList<>();
				for (int i = 0; i < n; i++) {
					if (((mask >> i) & 1) == 1) subset.add(coupons.get(i));
				}
				if (!isValidSubset(subset)) continue;
				PricingResult result = pricingEngine.applyCoupons(items, subset);
				if (isBetter(result, bestResult)) {
					bestResult = result;
					bestSet = subset;
				}
			}
		} else {
			// Greedy fallback: add coupons one by one if they improve total and remain compatible
			List<Coupon> chosen = new ArrayList<>();
			PricingResult current = pricingEngine.applyCoupons(items, chosen);
			boolean improved = true;
			while (improved) {
				improved = false;
				PricingResult bestLocal = current;
				Coupon bestCoupon = null;
				for (Coupon c : coupons) {
					if (chosen.contains(c)) continue;
					List<Coupon> trial = new ArrayList<>(chosen);
					trial.add(c);
					if (!isValidSubset(trial)) continue;
					PricingResult r = pricingEngine.applyCoupons(items, trial);
					if (isBetter(r, bestLocal)) {
						bestLocal = r;
						bestCoupon = c;
					}
				}
				if (bestCoupon != null) {
					chosen.add(bestCoupon);
					current = bestLocal;
					improved = true;
				}
			}
			bestResult = current;
		}

		return bestResult == null ? pricingEngine.applyCoupons(items, Collections.emptyList()) : bestResult;
	}

	private boolean isBetter(PricingResult a, PricingResult b) {
		if (b == null) return true;
		int cmp = a.getFinalPayable().compareTo(b.getFinalPayable());
		if (cmp != 0) return cmp < 0;
		// tie-breaker: larger total discount
		int cmp2 = a.getTotalDiscount().compareTo(b.getTotalDiscount());
		if (cmp2 != 0) return cmp2 > 0;
		// tie-breaker: fewer coupons used
		return a.getAppliedCoupons().size() < b.getAppliedCoupons().size();
	}

	private boolean isValidSubset(List<Coupon> subset) {
		if (subset.isEmpty()) return true;
		long exclusiveCount = subset.stream().filter(Coupon::isExclusive).count();
		if (exclusiveCount > 1) return false;
		if (exclusiveCount == 1 && subset.size() > 1) return false;

		Set<String> ids = new HashSet<>();
		for (Coupon c : subset) {
			if (!ids.add(c.getId())) return false; // duplicate
		}
		for (int i = 0; i < subset.size(); i++) {
			Coupon a = subset.get(i);
			for (int j = i + 1; j < subset.size(); j++) {
				Coupon b = subset.get(j);
				if (a.getIncompatibleCouponIds().contains(b.getId()) || b.getIncompatibleCouponIds().contains(a.getId())) {
					return false;
				}
			}
		}
		return true;
	}
}