package grizilly;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.FieldKey;

public class MusicLibraryManager {

    private MusicLibrary musicLibrary;

    public MusicLibraryManager() {
        this.musicLibrary = new MusicLibrary();
    }

    public static void main(String[] args) {
        String musicFolderPath = "Path_to_music_folder";
        String libraryFolderPath = "Path_to_library_folder";

        MusicLibraryManager manager = new MusicLibraryManager();

        // If you want to copy files to the library folder
        try {
            manager.copyMusicFiles(musicFolderPath, libraryFolderPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Now update the library with metadata
        manager.updateLibraryWithMetadata(musicFolderPath);
    }

    public void updateLibraryWithMetadata(String directoryPath) {
        File musicFolder = new File(directoryPath);
        File[] musicFiles = musicFolder.listFiles((dir, name) -> name.toLowerCase().matches(".*\\.(mp3|wav|flac)"));

        if (musicFiles != null) {
            for (File musicFile : musicFiles) {
                try {
                    Song song = readMetadata(musicFile);
                    musicLibrary.addSong(song);
                    System.out.println("Added to library: " + song);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Song readMetadata(File musicFile) throws Exception {
        AudioFile audioFile = AudioFileIO.read(musicFile);
        Tag tag = audioFile.getTag();
        String title = tag.getFirst(FieldKey.TITLE);
        String artist = tag.getFirst(FieldKey.ARTIST);
        String album = tag.getFirst(FieldKey.ALBUM);
        String year = tag.getFirst(FieldKey.YEAR);
        String genre = tag.getFirst(FieldKey.GENRE);
        int lengthInSeconds = audioFile.getAudioHeader().getTrackLength();

        return new Song(musicFile.getAbsolutePath(), title, artist, album, year, genre, lengthInSeconds);
    }

    private void copyMusicFiles(String sourceDir, String targetDir) throws IOException {
        File musicFolder = new File(sourceDir);
        File libraryFolder = new File(targetDir);

        if (!musicFolder.exists() || !libraryFolder.exists()) {
            System.out.println("One or both folders do not exist. Please check your paths.");
            return;
        }

        File[] musicFiles = musicFolder.listFiles();
        if (musicFiles != null) {
            for (File musicFile : musicFiles) {
                if (musicFile.isFile() && isMusicFile(musicFile)) {
                    Path sourcePath = musicFile.toPath();
                    Path targetPath = new File(libraryFolder, musicFile.getName()).toPath();
                    Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Copied: " + musicFile.getName());
                }
            }
        }
    }

    private static boolean isMusicFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".flac");
    }
}
