package com.axzydev.checkapp.entities.schedule.model

data class Schedule(
    val id: String,
    val name: String,
    val startTime: String,
    val endTime: String,
    val active: Boolean,
)

data class ScheduleDraft(
    val name: String,
    val startTime: String,
    val endTime: String,
)
