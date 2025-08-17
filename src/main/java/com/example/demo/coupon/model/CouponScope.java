package com.example.demo.coupon.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class CouponScope {
	private final CouponScopeType scopeType;
	private final Set<String> categoryIds;
	private final Set<String> productIds;

	private CouponScope(CouponScopeType scopeType, Set<String> categoryIds, Set<String> productIds) {
		this.scopeType = Objects.requireNonNull(scopeType, "scopeType");
		this.categoryIds = categoryIds == null ? Collections.emptySet() : Collections.unmodifiableSet(new HashSet<>(categoryIds));
		this.productIds = productIds == null ? Collections.emptySet() : Collections.unmodifiableSet(new HashSet<>(productIds));
	}

	public static CouponScope all() {
		return new CouponScope(CouponScopeType.ALL, Collections.emptySet(), Collections.emptySet());
	}

	public static CouponScope categories(Set<String> categoryIds) {
		return new CouponScope(CouponScopeType.CATEGORY, categoryIds, Collections.emptySet());
	}

	public static CouponScope products(Set<String> productIds) {
		return new CouponScope(CouponScopeType.PRODUCT, Collections.emptySet(), productIds);
	}

	public CouponScopeType getScopeType() {
		return scopeType;
	}

	public Set<String> getCategoryIds() {
		return categoryIds;
	}

	public Set<String> getProductIds() {
		return productIds;
	}
}