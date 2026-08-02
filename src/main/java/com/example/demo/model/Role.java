package com.example.demo.model;

/**
 * الأدوار (الصلاحيات) المتاحة في النظام.
 * تُستخدم لتسيير الصلاحيات على مستوى الـ API بناءً على محتوى JWT.
 */
public enum Role {
	ADMIN,
	USER
}
