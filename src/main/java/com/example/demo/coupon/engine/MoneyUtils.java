package com.example.demo.coupon.engine;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public final class MoneyUtils {
	private MoneyUtils() {}

	public static BigDecimal round2(BigDecimal value) {
		return value.setScale(2, RoundingMode.HALF_UP);
	}

	public static List<BigDecimal> allocateProportionally(List<BigDecimal> amounts, BigDecimal totalToAllocate) {
		BigDecimal sum = amounts.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
		if (sum.compareTo(BigDecimal.ZERO) <= 0) {
			List<BigDecimal> zeros = new ArrayList<>();
			for (int i = 0; i < amounts.size(); i++) zeros.add(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
			return zeros;
		}
		List<BigDecimal> allocations = new ArrayList<>(amounts.size());
		BigDecimal allocated = BigDecimal.ZERO;
		int maxIndex = 0;
		BigDecimal maxAmount = BigDecimal.ZERO;
		for (int i = 0; i < amounts.size(); i++) {
			BigDecimal proportion = amounts.get(i).divide(sum, 10, RoundingMode.HALF_UP);
			BigDecimal share = round2(totalToAllocate.multiply(proportion));
			allocations.add(share);
			allocated = allocated.add(share);
			if (amounts.get(i).compareTo(maxAmount) > 0) {
				maxAmount = amounts.get(i);
				maxIndex = i;
			}
		}
		BigDecimal delta = round2(totalToAllocate.subtract(allocated));
		if (delta.compareTo(BigDecimal.ZERO) != 0 && !allocations.isEmpty()) {
			allocations.set(maxIndex, round2(allocations.get(maxIndex).add(delta)));
		}
		return allocations;
	}
}