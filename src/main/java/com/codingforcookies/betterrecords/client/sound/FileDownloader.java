package com.codingforcookies.betterrecords.client.sound;

import com.codingforcookies.betterrecords.ModConfig;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class FileDownloader {

    public static boolean downloadFile(File saveLoc, final SoundManager manager, final int songIndex) {
        Sound sound = manager.songs.get(songIndex);
        return downloadFile(sound.name, sound.url, sound.local, saveLoc, new DownloadInfo() {
            private boolean started = false;
            public void onDownloading(String local, float percent) {
                if(songIndex == 0 && ModConfig.client.playWhileDownloading && !started) {
                    started = true;
                    SoundHandler.playSound(manager, -1);
                }
            }

            public void onFileTooLarge(String local) {
                SoundHandler.nowPlaying = "Error: " + local + " is over download limit!";
                SoundHandler.nowPlayingEnd = System.currentTimeMillis() + 5000;
            }

            public void onGeneralFailure(String local) {
                SoundHandler.nowPlaying = "Error: General download error for " + local;
                SoundHandler.nowPlayingEnd = System.currentTimeMillis() + 5000;
            }
        });
    }

    private static boolean downloadFile(String name, String urlLocation, String localName, File saveLoc, DownloadInfo dwnloadNfo) {
        urlLocation = urlLocation.replace(" ", "%20");
        System.err.println("Downloading " + name + " from " + urlLocation + "...");

        BufferedInputStream in = null;
        FileOutputStream out = null;
        URL url;

        try {
            url = new URL(urlLocation);
            URLConnection conn = url.openConnection();

            int size = conn.getContentLength();
            if(size / 1024 / 1024 > (ModConfig.client.downloadMax != 100 ? ModConfig.client.downloadMax : 102400)) {
                System.err.println("File larger than configured max size!");
                if(dwnloadNfo != null)
                    dwnloadNfo.onFileTooLarge(localName);
                return false;
            }

            in = new BufferedInputStream(url.openStream());
            out = new FileOutputStream(new File(saveLoc, name));
            byte data[] = new byte[1024];
            int count;
            float sumCount = 0F;

            while((count = in.read(data, 0, 1024)) != -1) {
                out.write(data, 0, count);
                sumCount += count;

            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if(dwnloadNfo != null)
                dwnloadNfo.onGeneralFailure(localName);
            System.err.println("Failed to download: " + localName);
        } finally {
            if(in != null)
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.err.println("Failed to close inputstream while downloading: " + localName + "\247" + urlLocation);
                }
            if(out != null)
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.err.println("Failed to close outputstream while downloading: " + localName + "\247" + urlLocation);
                }
        }

        return false;
    }

    private static abstract class DownloadInfo {
        public abstract void onDownloading(String local, float percent);
        public abstract void onFileTooLarge(String local);
        public abstract void onGeneralFailure(String local);
    }
}
