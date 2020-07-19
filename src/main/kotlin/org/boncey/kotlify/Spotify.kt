package org.boncey.kotlify

import com.wrapper.spotify.SpotifyApi
import com.wrapper.spotify.exceptions.detailed.NotFoundException
import com.wrapper.spotify.model_objects.specification.Paging
import com.wrapper.spotify.model_objects.specification.Playlist
import com.wrapper.spotify.model_objects.specification.PlaylistTrack
import com.wrapper.spotify.model_objects.specification.Track

class Spotify {

    private val spotifyClientId: String = System.getenv("SPOTIFY_CLIENT_ID")
    private val spotifyClientSecret: String = System.getenv("SPOTIFY_CLIENT_SECRET")

    val spotifyApi: SpotifyApi = SpotifyApi.Builder()
        .setClientId(spotifyClientId)
        .setClientSecret(spotifyClientSecret)
        .build()

    init {
        auth()
    }

    fun getPlaylist(playlistLink: String): Playlist? {
        val playlistId = parse(playlistLink)
        println("Looking up playlist id: $playlistId")
        val playlister = Playlister()
        return try {
            playlister.getPlaylist(spotifyApi, playlistId)
        } catch (e: NotFoundException) {
            println(e.message)
            null
        }
    }

    fun getTracks(playlist: Playlist): List<Track> {
        val playlister = Playlister()
        var paging: Paging<PlaylistTrack> = playlister.getPlaylistItems(spotifyApi, playlist.id)
        var tracks: MutableList<Track> = mutableListOf()

        tracks.addAll(paging.items.map { it.track as Track }.toList())

        while (paging.next != null) {
            paging = playlister.getPlaylistItems(spotifyApi, playlist.id, paging.offset + paging.items.size)
            tracks.addAll(paging.items.map { it.track as Track }.toList())
        }

        return tracks
    }

    private fun parse(playlistLink: String): String {
        val regex = Regex("https://open.spotify.com/playlist/(\\w+)\\?si=\\w+")
        val match = regex.find(playlistLink)
        return match?.destructured?.component1() ?: playlistLink
    }

    private fun auth() {
        val clientCredentialsRequest = this.spotifyApi.clientCredentials().build()
        val clientCredentials = clientCredentialsRequest.execute()

        spotifyApi.accessToken = clientCredentials.accessToken
        println("""Authorised against Spotify for ${clientCredentials.expiresIn} seconds with token ${clientCredentials.accessToken}""")
    }

}

fun main(args: Array<String>) {
    val spotify = Spotify()
    for (playlistId in args) {
        val playlist = spotify.getPlaylist(playlistId)
        println("Got playlist: '${playlist?.name}'")
        if (playlist != null) {
            val tracks = spotify.getTracks(playlist)
            println("Found ${tracks.size} tracks")
            for (track in tracks) {
                println("'${track.name}' by ${track.artists.first()?.name}")
            }
        }
        println()
    }
}
