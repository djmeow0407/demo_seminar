package com.demo.seminar.model

data class Transaction(
    val userId: String,
    val type: String,
    val amount: Long,
    val category: String,
    val createdAt: Long
) {
    constructor() : this("", "", 0, "", 0)
}
