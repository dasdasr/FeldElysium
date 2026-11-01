package com.feldelysium.app.domain.model

data class SceneInfo(
    val id: String,
    val title: String,
    val lines: Int,
    val lengths: List<Int> = emptyList(),
    val modified: Long,
    val preview: String = "",
    val barcode: List<Long> = emptyList(),
)
