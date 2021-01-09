// 
// Decompiled by Procyon v0.5.36
// 

package com.jagrosh.jmusicbot.playlist;

import com.jagrosh.jmusicbot.BotConfig;
import com.jagrosh.jmusicbot.utils.OtherUtil;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PlaylistLoader
{
    private final BotConfig config;
    
    public PlaylistLoader(final BotConfig config) {
        this.config = config;
    }
    
    public List<String> getPlaylistNames() {
        if (this.folderExists()) {
            final File folder = new File(this.config.getPlaylistsFolder());
            return Arrays.asList(folder.listFiles(pathname -> pathname.getName().endsWith(".txt"))).stream().map(f -> f.getName().substring(0, f.getName().length() - 4)).collect(Collectors.toList());
        }
        this.createFolder();
        return (List<String>)Collections.EMPTY_LIST;
    }
    
    public void createFolder() {
        try {
            Files.createDirectory(OtherUtil.getPath(this.config.getPlaylistsFolder()), (FileAttribute<?>[])new FileAttribute[0]);
        }
        catch (IOException ex) {}
    }
    
    public boolean folderExists() {
        return Files.exists(OtherUtil.getPath(this.config.getPlaylistsFolder()), new LinkOption[0]);
    }
    
    public void createPlaylist(final String name) throws IOException {
        Files.createFile(OtherUtil.getPath(this.config.getPlaylistsFolder() + File.separator + name + ".txt"), (FileAttribute<?>[])new FileAttribute[0]);
    }
    
    public void deletePlaylist(final String name) throws IOException {
        Files.delete(OtherUtil.getPath(this.config.getPlaylistsFolder() + File.separator + name + ".txt"));
    }
    
    public void writePlaylist(final String name, final String text) throws IOException {
        Files.write(OtherUtil.getPath(this.config.getPlaylistsFolder() + File.separator + name + ".txt"), text.trim().getBytes(), new OpenOption[0]);
    }
    
    public Playlist getPlaylist(final String name) {
        if (!this.getPlaylistNames().contains(name)) {
            return null;
        }
        try {
            if (this.folderExists()) {
                final boolean[] shuffle = { false };
                final List<String> list = new ArrayList<String>();
                final Object o;
                final List<String> list2 = new ArrayList<>();
                Files.readAllLines(OtherUtil.getPath(this.config.getPlaylistsFolder() + File.separator + name + ".txt")).forEach(str -> {
                    String s = str.trim();
                    String s2;
                    if (s.isEmpty()) {
                        return;
                    }
                    else {
                        if (s.startsWith("#") || s.startsWith("//")) {
                            s2 = s.replaceAll("\\s+", "");
                            if (s2.equalsIgnoreCase("#shuffle") || s2.equalsIgnoreCase("//shuffle")) {
                                shuffle[0] = true;
                            }
                        }
                        else {
                            list2.add(s);
                        }
                        return;
                    }
                });
                if (shuffle[0]) {
                    shuffle(list);
                }
                return new Playlist(name, (List)list, shuffle[0]);
            }
            this.createFolder();
            return null;
        }
        catch (IOException e) {
            return null;
        }
    }
    
    private static <T> void shuffle(final List<T> list) {
        for (int first = 0; first < list.size(); ++first) {
            final int second = (int)(Math.random() * list.size());
            final T tmp = list.get(first);
            list.set(first, list.get(second));
            list.set(second, tmp);
        }
    }
    
    public class Playlist
    {
        private final String name;
        private final List<String> items;
        private final boolean shuffle;
        private final List<AudioTrack> tracks;
        private final List<PlaylistLoadError> errors;
        private boolean loaded;
        
        private Playlist(final String name, final List<String> items, final boolean shuffle) {
            this.tracks = new LinkedList<AudioTrack>();
            this.errors = new LinkedList<PlaylistLoadError>();
            this.loaded = false;
            this.name = name;
            this.items = items;
            this.shuffle = shuffle;
        }
        
        public void loadTracks(final AudioPlayerManager manager, final Consumer<AudioTrack> consumer, final Runnable callback) {
            if (this.loaded) {
                return;
            }
            this.loaded = true;
            for (int i = 0; i < this.items.size(); ++i) {
                final boolean last = i + 1 == this.items.size();
                final int index = i;
                manager.loadItemOrdered(this.name, this.items.get(i), new AudioLoadResultHandler() {
                    private void done() {
                        if (last) {
                            if (Playlist.this.shuffle) {
                                Playlist.this.shuffleTracks();
                            }
                            if (callback != null) {
                                callback.run();
                            }
                        }
                    }
                    
                    @Override
                    public void trackLoaded(final AudioTrack at) {
                        if (PlaylistLoader.this.config.isTooLong(at)) {
                            Playlist.this.errors.add(new PlaylistLoadError(index, (String)Playlist.this.items.get(index), "This track is longer than the allowed maximum"));
                        }
                        else {
                            at.setUserData(0L);
                            Playlist.this.tracks.add(at);
                            consumer.accept(at);
                        }
                        this.done();
                    }
                    
                    @Override
                    public void playlistLoaded(final AudioPlaylist ap) {
                        if (ap.isSearchResult()) {
                            this.trackLoaded(ap.getTracks().get(0));
                        }
                        else if (ap.getSelectedTrack() != null) {
                            this.trackLoaded(ap.getSelectedTrack());
                        }
                        else {
                            final List<AudioTrack> loaded = new ArrayList<AudioTrack>(ap.getTracks());
                            if (Playlist.this.shuffle) {
                                for (int first = 0; first < loaded.size(); ++first) {
                                    final int second = (int)(Math.random() * loaded.size());
                                    final AudioTrack tmp = loaded.get(first);
                                    loaded.set(first, loaded.get(second));
                                    loaded.set(second, tmp);
                                }
                            }
                            loaded.removeIf(track -> PlaylistLoader.this.config.isTooLong(track));
                            loaded.forEach(at -> at.setUserData(0L));
                            Playlist.this.tracks.addAll(loaded);
                            loaded.forEach(at -> consumer.accept(at));
                        }
                        this.done();
                    }
                    
                    @Override
                    public void noMatches() {
                        Playlist.this.errors.add(new PlaylistLoadError(index, (String)Playlist.this.items.get(index), "No matches found."));
                        this.done();
                    }
                    
                    @Override
                    public void loadFailed(final FriendlyException fe) {
                        Playlist.this.errors.add(new PlaylistLoadError(index, (String)Playlist.this.items.get(index), "Failed to load track: " + fe.getLocalizedMessage()));
                        this.done();
                    }
                });
            }
        }
        
        public void shuffleTracks() {
            shuffle(this.tracks);
        }
        
        public String getName() {
            return this.name;
        }
        
        public List<String> getItems() {
            return this.items;
        }
        
        public List<AudioTrack> getTracks() {
            return this.tracks;
        }
        
        public List<PlaylistLoadError> getErrors() {
            return this.errors;
        }
    }
    
    public class PlaylistLoadError
    {
        private final int number;
        private final String item;
        private final String reason;
        
        private PlaylistLoadError(final int number, final String item, final String reason) {
            this.number = number;
            this.item = item;
            this.reason = reason;
        }
        
        public int getIndex() {
            return this.number;
        }
        
        public String getItem() {
            return this.item;
        }
        
        public String getReason() {
            return this.reason;
        }
    }
}
