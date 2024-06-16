package com.siddiqui.spotifyclone.data.entities

/**
 *  Default values is required for firestore
 */
data class Song(
    val mediaId: String = "",
    val title: String = "",
    val subtitle: String = "",
    val imageUrl: String = "",
    val songUrl: String = "",
)
