package com.kostyabakay.kbmp.util;

import com.kostyabakay.kbmp.audio.AudioPlayer;

/**
 * Created by Kostya on 31.03.2016.
 * This class represents some important application data.
 */
public class AppData {
    public static AudioPlayer audioPlayer = new AudioPlayer();
    public static boolean isSongPlayed;
    public static String songUrl;
}