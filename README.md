# Kotlify

Simple tool to analyse Spotify playlists.  
Uses [thelinmichael/spotify-web-api-java](https://github.com/thelinmichael/spotify-web-api-java) for the Spotify API access.

Environment variables required (see [Authorization Guide | Spotify for Developers](https://developer.spotify.com/documentation/general/guides/authorization-guide/) && [spotify-web-api-java#Authorization](https://github.com/thelinmichael/spotify-web-api-java#Authorization) for details).

- `SPOTIFY_CLIENT_SECRET`
- `SPOTIFY_CLIENT_ID`

Add a list of Spotify playlist URLs to a text file.
Eg.

```
https://open.spotify.com/playlist/XXXXXXX?si=YYYYYY
https://open.spotify.com/playlist/XXXXXXX?si=YYYYYY
https://open.spotify.com/playlist/XXXXXXX?si=YYYYYY
```

Run a build 

```shell script
    mvn clean install
```

Execute the program

```shell script
    java -jar target/kotlify-1.0-SNAPSHOT.jar playlists.txt
```

Example output.
```
25 Playlists
1620 Tracks
Total Time 4 days, 13 hours, 07 minutes, 15 seconds
Longest track (Timeless) 21 minutes, 02 seconds
Shortest track (Seinfeld - Theme from "Seinfeld") 54 seconds
Most Popular Artists
- (David Bowie, 12)
- (Metallica, 11)
- (Ozzy Osbourne, 9)
- (Guns N' Roses, 8)
- (Queens of the Stone Age, 7)
Most Popular Tracks
- (Use Somebody - Kings of Leon, 3)
- (Only You - Yazoo, 3)
- (Quiet Life - Japan, 3)
- (Rebel Rebel - 2016 Remaster - David Bowie, 3)
- (Love Train - The O'Jays, 3)
```