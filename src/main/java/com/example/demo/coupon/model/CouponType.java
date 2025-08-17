package com.example.demo.coupon.model;

public enum CouponType {
	PERCENTAGE, // e.g. 10% off (represented by discountRate)
	FIXED_AMOUNT // e.g. $20 off (represented by fixedAmountOff and optional threshold)
}