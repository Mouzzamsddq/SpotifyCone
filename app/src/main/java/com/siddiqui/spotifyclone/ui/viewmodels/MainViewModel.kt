package com.siddiqui.spotifyclone.ui.viewmodels

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.siddiqui.spotifyclone.data.entities.Song
import com.siddiqui.spotifyclone.exoplayer.MusicServiceConnection
import com.siddiqui.spotifyclone.exoplayer.isPlayEnabled
import com.siddiqui.spotifyclone.exoplayer.isPlaying
import com.siddiqui.spotifyclone.exoplayer.isPrepared
import com.siddiqui.spotifyclone.other.Constants.MEDIA_ROOT_ID
import com.siddiqui.spotifyclone.other.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel
    @Inject
    constructor(
        private val musicServiceConnection: MusicServiceConnection,
    ) : ViewModel() {
        private val _mediaItems = MutableLiveData<Resource<List<Song>>>()
        val mediaItems: LiveData<Resource<List<Song>>> = _mediaItems

        val isConnected = musicServiceConnection.isConnected
        val networkError = musicServiceConnection.networkError
        val currPlayingSong = musicServiceConnection.currPlayingSong
        val playbackState = musicServiceConnection.playbackState

        init {
            _mediaItems.postValue(Resource.loading(null))
            musicServiceConnection.subscribe(
                MEDIA_ROOT_ID,
                object : MediaBrowserCompat.SubscriptionCallback() {
                    override fun onChildrenLoaded(
                        parentId: String,
                        children: MutableList<MediaBrowserCompat.MediaItem>,
                    ) {
                        super.onChildrenLoaded(parentId, children)
                        val items =
                            children.map {
                                Song(
                                    mediaId = it.mediaId!!,
                                    title = it.description.title.toString(),
                                    subtitle = it.description.subtitle.toString(),
                                    imageUrl = it.description.iconUri.toString(),
                                    songUrl = it.description.mediaUri.toString(),
                                )
                            }
                        _mediaItems.postValue(Resource.success(items))
                    }
                },
            )
        }

        fun skipToNextSong() {
            musicServiceConnection.transportControls.skipToNext()
        }

        fun skipToPreviousSong() {
            musicServiceConnection.transportControls.skipToPrevious()
        }

        fun seekTo(pos: Long) {
            musicServiceConnection.transportControls.seekTo(pos)
        }

        fun playOrToggleSong(
            mediaItem: Song,
            toggle: Boolean = false,
        ) {
            val isPrepared = playbackState.value?.isPrepared ?: false
            if (isPrepared && mediaItem.mediaId == currPlayingSong.value?.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)) {
                playbackState.value?.let { playbackState ->
                    when {
                        playbackState.isPlaying -> if (toggle) musicServiceConnection.transportControls.pause()
                        playbackState.isPlayEnabled -> musicServiceConnection.transportControls.play()
                        else -> Unit
                    }
                }
            } else {
                musicServiceConnection.transportControls.playFromMediaId(mediaItem.mediaId, null)
            }
        }

        override fun onCleared() {
            super.onCleared()
            musicServiceConnection.unsubscribe(
                MEDIA_ROOT_ID,
                object : MediaBrowserCompat.SubscriptionCallback() {},
            )
        }
    }
