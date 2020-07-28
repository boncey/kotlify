package org.boncey.kotlify

import com.wrapper.spotify.SpotifyApi
import com.wrapper.spotify.exceptions.detailed.NotFoundException
import com.wrapper.spotify.model_objects.specification.Paging
import com.wrapper.spotify.model_objects.specification.Playlist
import com.wrapper.spotify.model_objects.specification.PlaylistTrack
import com.wrapper.spotify.model_objects.specification.Track
import java.nio.file.Files
import java.nio.file.Path

class Kotlify {

    private val spotifyClientId: String = System.getenv("SPOTIFY_CLIENT_ID")
    private val spotifyClientSecret: String = System.getenv("SPOTIFY_CLIENT_SECRET")

    val spotifyApi: SpotifyApi = SpotifyApi.Builder()
        .setClientId(spotifyClientId)
        .setClientSecret(spotifyClientSecret)
        .build()

    init {
        auth()
    }

    fun getPlaylist(playlistId: String): Playlist? {
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
        val tracks: MutableList<Track> = mutableListOf()

        tracks.addAll(paging.items.map { it.track as Track }.toList())

        while (paging.next != null) {
            paging = playlister.getPlaylistItems(spotifyApi, playlist.id, paging.offset + paging.items.size)
            tracks.addAll(paging.items.map { it.track as Track }.toList())
        }

        return tracks
    }

    fun analyse(allTracks: List<Track>) {
        val durationMs = allTracks.map { it.durationMs }.reduce { total, track -> total + track }

        val max = allTracks.maxBy { it.durationMs }
        val min = allTracks.minBy { it.durationMs }

        val artists = allTracks.groupingBy { it.artists.first().name }.eachCount().toList()
            .sortedByDescending { (_, value) -> value }
        val songs = allTracks.groupingBy { "${it.name} - ${it.artists.first().name}" }.eachCount().toList()
            .sortedByDescending { (_, value) -> value }

        println("${allTracks.size} Tracks")
        println("Total Time ${formatMilliseconds(durationMs)}")
        println("Longest track (${max?.name}) ${formatMilliseconds(max?.durationMs ?: 0)}")
        println("Shortest track (${min?.name}) ${formatMilliseconds(min?.durationMs ?: 0)}")
        println("Most Popular Artists")
        artists.take(5).forEach { println("- ${it}") }
        println("Most Popular Tracks")
        songs.take(5).forEach { println("- ${it}") }
    }

    private fun formatMilliseconds(durationMs: Int): String {
        val duration = durationMs / 1000
        val daysNum = duration / (3600 * 24)
        val hoursNum = (duration / 3600) % 24
        val minsNum = (duration / 60) % 60
        val secsNum = (duration % 60)

        val days = if (daysNum > 0) String.format(
            "%d days",
            daysNum
        ) else null
        val hours = if (hoursNum > 0) String.format(
            "%d hours",
            hoursNum
        ) else null
        val mins = if (minsNum > 0) String.format(
            "%02d minutes",
            minsNum
        ) else null
        val secs = if (secsNum > 0) String.format(
            "%02d seconds",
            secsNum
        ) else null

        return listOfNotNull(days, hours, mins, secs).joinToString(", ")
    }

    private fun auth() {
        val clientCredentialsRequest = this.spotifyApi.clientCredentials().build()
        val clientCredentials = clientCredentialsRequest.execute()

        spotifyApi.accessToken = clientCredentials.accessToken
        println("""Authorised against Spotify for ${clientCredentials.expiresIn} seconds with token ${clientCredentials.accessToken}""")
    }

}

fun main(args: Array<String>) {
    val playlistIds: List<String?> = readPlaylistIds(args.first())

    val spotify = Kotlify()
    val allTracks: MutableList<Track> = mutableListOf()
    for (playlistId in playlistIds) {
        val playlist = playlistId?.let { spotify.getPlaylist(it) }

        if (playlist != null) {
            val tracks = spotify.getTracks(playlist)
            println("Got playlist: '${playlist.name}' (${tracks.size} tracks)")
            allTracks.addAll(tracks)
        }
    }

    println()
    println("${playlistIds.size} Playlists")
    spotify.analyse(allTracks)
}

fun readPlaylistIds(first: String): List<String?> {
    val regex = Regex("https://open.spotify.com/playlist/(\\w+)\\?si=\\w+")
    val lines = Files.readAllLines(Path.of(first))
    return lines.filter {
        regex.matches(it)
    }.map {
        val match = regex.find(it)
        match?.destructured?.component1()
    }.toList()
}
