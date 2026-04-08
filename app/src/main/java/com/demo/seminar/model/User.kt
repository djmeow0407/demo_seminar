package com.demo.seminar.model

data class User(
    val email: String,
    val balance: Long,
    val createdAt: Long
) {
    constructor() : this("", 0, 0)
}
