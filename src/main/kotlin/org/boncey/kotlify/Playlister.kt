package org.boncey.kotlify

import com.wrapper.spotify.SpotifyApi
import com.wrapper.spotify.model_objects.specification.Paging
import com.wrapper.spotify.model_objects.specification.Playlist
import com.wrapper.spotify.model_objects.specification.PlaylistTrack
import com.wrapper.spotify.requests.data.playlists.GetPlaylistRequest
import com.wrapper.spotify.requests.data.playlists.GetPlaylistsItemsRequest

class Playlister {

    fun getPlaylist(spotifyApi: SpotifyApi, playlistId: String): Playlist? {
        val request: GetPlaylistRequest = spotifyApi.getPlaylist(playlistId)
            .additionalTypes("track")
            .build()

        return request.execute()
    }

    fun getPlaylistItems(spotifyApi: SpotifyApi, playlistId: String, offset: Int = 0): Paging<PlaylistTrack> {
        val request: GetPlaylistsItemsRequest = spotifyApi.getPlaylistsItems(playlistId)
            .offset(offset)
            .build()

        return request.execute()
    }
}