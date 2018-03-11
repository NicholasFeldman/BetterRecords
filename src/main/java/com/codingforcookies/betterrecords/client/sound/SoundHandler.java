package com.codingforcookies.betterrecords.client.sound;

import com.codingforcookies.betterrecords.ModConfig;
import com.codingforcookies.betterrecords.api.connection.RecordConnection;
import com.codingforcookies.betterrecords.api.record.IRecordAmplitude;
import com.codingforcookies.betterrecords.api.wire.IRecordWireHome;
import com.codingforcookies.betterrecords.client.handler.ClientRenderHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.Loader;

import javax.sound.sampled.*;
import javax.sound.sampled.DataLine.Info;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import static javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED;
import static javax.sound.sampled.AudioSystem.getAudioInputStream;

public class SoundHandler{

    public static File soundLocation;
    public static HashMap<String, File> soundList;
    public static HashMap<String, SoundManager> soundPlaying;
    public static String nowPlaying = "";
    public static long nowPlayingEnd = 0;
    public static int nowPlayingInt = 0;

    public static void initalize(){
        soundLocation = new File(Minecraft.getMinecraft().mcDataDir, "betterrecords/cache");
        soundList = new HashMap<>();
        soundPlaying = new HashMap<>();
        if(!soundLocation.mkdirs()) for(File sound : soundLocation.listFiles()){
            System.out.println("Loaded " + sound.getName());
            soundList.put(sound.getName(), sound);
        }
        System.out.println("Loaded sound cache of " + soundList.size() + " sounds.");
    }

    public static void playSound(final int x, final int y, final int z, final int dimension, final float playRadius, final List<Sound> sounds, boolean repeat, boolean shuffle){
        playSound(x, y, z, dimension, playRadius, sounds, repeat, shuffle, 0);
    }

    private static void obtainSound(final SoundManager manager, final int songIndex){
        soundList.put(manager.songs.get(songIndex).name, new File(soundLocation, manager.songs.get(songIndex).name));
        new Thread(() -> {
            if(!FileDownloader.downloadFile(soundLocation, manager, songIndex)) soundList.remove(manager.songs.get(songIndex).name);
            else playSound(manager, songIndex);
        }).start();
    }

    protected static void playSound(SoundManager manager, int songIndex){
        if(songIndex == -1) playSound(manager.songs.get(0).x, manager.songs.get(0).y, manager.songs.get(0).z, manager.songs.get(0).dimension, manager.songs.get(0).playRadius, manager.songs, manager.repeat, manager.shuffle, -1);
        else playSound(manager.songs.get(songIndex).x, manager.songs.get(songIndex).y, manager.songs.get(songIndex).z, manager.songs.get(songIndex).dimension, manager.songs.get(songIndex).playRadius, manager.songs, manager.repeat, manager.shuffle, songIndex);
    }

    private static void playSound(final int x, final int y, final int z, final int dimension, final float playRadius, final List<Sound> sounds, boolean repeat, boolean shuffle, int songIndex){
        if(songIndex >= 0) {
            SoundManager sndMgr;
            if(soundPlaying.get(x + "," + y + "," + z + "," + dimension) == null) {
                sndMgr = new SoundManager(repeat, shuffle);
                sndMgr.songs = sounds;
                soundPlaying.put(x + "," + y + "," + z + "," + dimension, sndMgr);
            }else sndMgr = soundPlaying.get(x + "," + y + "," + z + "," + dimension);
            for(int i = songIndex; i < sounds.size(); i++){
                if(!soundList.containsKey(sounds.get(i).name)) {
                    if(ModConfig.client.downloadSongs) {
                        if(ClientRenderHandler.INSTANCE.getShowDownloading()) {
                            System.err.println("Song downloading... Please wait...");
                            nowPlaying = I18n.format("betterrecords.overlay.error.pleaseWait");
                            nowPlayingEnd = System.currentTimeMillis() + 5000;
                            return;
                        }
                        obtainSound(sndMgr, i);
                    }
                    return;
                }else{
                    if(!soundList.get(sounds.get(i).name).exists()) {
                        System.err.println("Sound exists in hashmap, but not in file. Reloading cache...");
                        soundList.clear();
                        if(!soundLocation.mkdirs()) {
                            for(File soundFile : soundLocation.listFiles()){
                                System.out.println("Loaded " + soundFile.getName());
                                soundList.put(soundFile.getName(), soundFile);
                            }
                        }
                        i--;
                    }else if(i == 0) {
                        tryToStart(x, y, z, dimension);
                        if(sounds.size() == 1) return;
                    }
                }
            }
        }
        tryToStart(x, y, z, dimension);
    }

    public static boolean isPlaying(BlockPos pos, int dimension) {
        return soundPlaying.containsKey(pos.getX() + "," + pos.getY() + "," + pos.getZ() + "," + dimension);
    }

    public static void stopPlaying(BlockPos pos, int dimension) {
        if (isPlaying(pos, dimension)) {
            soundPlaying.remove(pos.getX() + "," + pos.getY() + "," + pos.getZ() + "," + dimension);
        }
    }

    private static void tryToStart(final int x, final int y, final int z, final int dimension){
        if(soundPlaying.get(x + "," + y + "," + z + "," + dimension) != null && soundPlaying.get(x + "," + y + "," + z + "," + dimension).current == -1) new Thread(() -> {
            Sound snd = null;
            if(soundPlaying.get(x + "," + y + "," + z + "," + dimension).current != -1) return;
            while(soundPlaying.get(x + "," + y + "," + z + "," + dimension) != null && (snd = soundPlaying.get(x + "," + y + "," + z + "," + dimension).nextSong()) != null){
                nowPlaying = snd.local;
                nowPlayingEnd = System.currentTimeMillis() + 5000;
                playSourceDataLine(snd, x, y, z, dimension, BetterSoundType.SONG, new File(soundLocation, snd.name));
            }
            soundPlaying.remove(x + "," + y + "," + z + "," + dimension);
        }).start();
    }

    public static void playSoundFromStream(final BlockPos pos, final int dimension, final float playRadius, final String localName, final String url){
        if(!ModConfig.client.streamRadio) return;

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        soundPlaying.put(x + "," + y + "," + z + "," + dimension, new SoundManager(new Sound(x, y, z, dimension, playRadius).setInfo("", url, localName), false, false));
        new Thread(() -> {
            try{
                System.out.println("Connection to stream " + localName + "...");
                Sound snd = soundPlaying.get(x + "," + y + "," + z + "," + dimension).nextSong();
                IcyURLConnection urlConn = new IcyURLConnection(new URL(url.startsWith("http") ? url : "http://" + url));
                urlConn.setInstanceFollowRedirects(true);
                urlConn.connect();
                playSourceDataLine(snd, x, y, z, dimension, BetterSoundType.RADIO, new BufferedInputStream(urlConn.getInputStream()));
            }catch(Exception e){
                e.printStackTrace();
                if (Minecraft.getMinecraft().player != null) {
                    System.err.println("Failed to stream: " + url);
                    nowPlaying = I18n.format("betterrecords.overlay.error.cantReadStream");
                }
                nowPlayingEnd = System.currentTimeMillis() + 5000;
            }
            soundPlaying.remove(x + "," + y + "," + z + "," + dimension);
        }).start();
    }

    private static void playSourceDataLine(Sound snd, int x, int y, int z, int dimension, BetterSoundType type, Object object){
        try{
            final AudioInputStream in;
            if(object instanceof File) in = convertToPCM(getAudioInputStream((File) object));
            else if(object instanceof BufferedInputStream) in = convertToPCM(getAudioInputStream((BufferedInputStream) object));
            else in = convertToPCM((AudioInputStream) object);
            final AudioFormat outFormat = getOutFormat(in.getFormat());
            final Info info = new Info(SourceDataLine.class, outFormat);
            System.out.println("Playing " + snd.name + ": " + new File(soundLocation, snd.name).getAbsolutePath());
            try (SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info)) {
                if (line != null) {
                    line.open(outFormat);
                    if (snd.volume == null) {
                        snd.volume = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
                        snd.volume.setValue(-20F);
                    }
                    line.start();
                    stream(getAudioInputStream(outFormat, in), line, x, y, z, dimension, type);
                    line.drain();
                    line.stop();
                }
            }
        }catch(Exception e){
            e.printStackTrace();
            if (Minecraft.getMinecraft().player != null) {
                switch (type) {
                    case SONG:
                        nowPlaying = I18n.format("betterrecords.overlay.error.cantReadFile");
                        System.err.println("Could not read file: Local: " + snd.local + " File: " + snd.name);
                        break;
                    case RADIO:
                        nowPlaying = I18n.format("betterrecords.overlay.error.cantReadStream");
                        System.err.println("Failed to stream: URL: " + snd.url);
                        break;
                    default:
                        break;
                }
            }
            nowPlayingEnd = System.currentTimeMillis() + 5000;
        }
    }

    private static AudioFormat getOutFormat(AudioFormat inFormat){
        final int ch = inFormat.getChannels();
        final float rate = inFormat.getSampleRate();
        return new AudioFormat(PCM_SIGNED, rate, 16, ch, ch * 2, rate, false);
    }

    private static void stream(AudioInputStream in, SourceDataLine line, int x, int y, int z, int dimension, BetterSoundType soundType) throws IOException{
        final byte[] buffer = new byte[ModConfig.client.streamBuffer];
        for(int n = 0; n != -1; n = in.read(buffer, 0, buffer.length)){
            while(Minecraft.getMinecraft().isSingleplayer() && Minecraft.getMinecraft().currentScreen != null && Minecraft.getMinecraft().currentScreen.doesGuiPauseGame()){
                try{
                    Thread.sleep(5L);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
            if(soundPlaying.get(x + "," + y + "," + z + "," + dimension) == null || (soundType == BetterSoundType.RADIO && !ModConfig.client.streamRadio)) return;
            updateAmplitude(buffer, x, y, z, dimension);
            line.write(buffer, 0, n);
        }
        updateAmplitude(null, x, y, z, dimension);
    }

    private static void updateAmplitude(byte[] buffer, int x, int y, int z, int dimension){
        if (Minecraft.getMinecraft().world.provider.getDimension() != dimension) return;
        float unscaledTreble = -1F;
        float unscaledBass = -1F;
        TileEntity tileEntity = Minecraft.getMinecraft().world.getTileEntity(new BlockPos(x, y, z));
        if(tileEntity != null && tileEntity instanceof IRecordWireHome) {
            ((IRecordWireHome) tileEntity).addTreble(getUnscaledWaveform(buffer, true, false));
            ((IRecordWireHome) tileEntity).addBass(getUnscaledWaveform(buffer, false, false));
            for(RecordConnection con : ((IRecordWireHome) tileEntity).getConnections()){
                if(buffer == null) return;
                TileEntity tileEntityCon = Minecraft.getMinecraft().world.getTileEntity(new net.minecraft.util.math.BlockPos(con.getX2(), con.getY2(), con.getZ2()));
                if(tileEntityCon != null && tileEntityCon instanceof IRecordAmplitude) {
                    if(unscaledTreble == -1F || unscaledBass == 11F) {
                        unscaledTreble = getUnscaledWaveform(buffer, true, true);
                        unscaledBass = getUnscaledWaveform(buffer, false, true);
                    }
                    ((IRecordAmplitude) tileEntityCon).setTreble(unscaledTreble);
                    ((IRecordAmplitude) tileEntityCon).setBass(unscaledBass);
                }
            }
        }
    }

    public static float getUnscaledWaveform(byte[] eightBitByteArray, boolean high, boolean control){
        if(eightBitByteArray != null) {
            int[] toReturn = new int[eightBitByteArray.length / 2];
            float avg = 0;
            int index = 0;
            for(int audioByte = high ? 1 : 0; audioByte < eightBitByteArray.length; audioByte += 2){
                toReturn[index] = (int) eightBitByteArray[audioByte];
                avg += toReturn[index];
                index++;
            }
            avg = avg / toReturn.length;
            if(control) {
                if(avg < 0F) avg = Math.abs(avg);
                if(avg > 20F) return(ModConfig.client.flashMode < 3 ? 10F : 20F);
                else return (int) (avg * (ModConfig.client.flashMode < 3 ? 1F : 2F));
            }
            return avg;
        }
        return 0;
    }

    private static int getSixteenBitSample(int high, int low){
        return (high << 8) + (low & 0x00ff);
    }

    private static AudioInputStream convertToPCM(AudioInputStream audioInputStream){
        AudioFormat format = audioInputStream.getFormat();
        if((format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) && (format.getEncoding() != AudioFormat.Encoding.PCM_UNSIGNED)) {
            AudioFormat targetFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, format.getSampleRate(), 16, format.getChannels(), format.getChannels() * 2, format.getSampleRate(), format.isBigEndian());
            audioInputStream = AudioSystem.getAudioInputStream(targetFormat, audioInputStream);
        }
        return audioInputStream;
    }

    private enum BetterSoundType{
        SONG, RADIO
    }
}