package at.xirado.bean.audio

data class TrackInfo(val requester: Long, val playlistInfo: PlaylistInfo?)

data class PlaylistInfo(val name: String, val url: String)
