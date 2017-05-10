package com.smilehacker.ffmepgtest;


import android.util.Log;

import org.mp4parser.Container;
import org.mp4parser.muxer.FileDataSourceImpl;
import org.mp4parser.muxer.Movie;
import org.mp4parser.muxer.Track;
import org.mp4parser.muxer.builder.DefaultMp4Builder;
import org.mp4parser.muxer.container.mp4.MovieCreator;
import org.mp4parser.muxer.tracks.AppendTrack;
import org.mp4parser.muxer.tracks.h264.H264TrackImpl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by kleist on 2017/5/5.
 */
public class VideoHelper {

    public static void concat(String... videos) throws IOException {
        Movie[] srcs = new Movie[videos.length];

        for (int i = 0; i < videos.length; i++) {
                srcs[i] = MovieCreator.build(videos[i]);
        }

        List<Track> videoTracks = new LinkedList<>();
        List<Track> audioTracks = new LinkedList<>();

        for (Movie src : srcs) {
            for (Track track: src.getTracks()) {
                Log.i("tag", track.getHandler());
                if (track.getHandler().equals("vide")) {
                    videoTracks.add(track);
                } else if (track.getHandler().equals("soun")) {
                    audioTracks.add(track);
                }
            }
        }

        Movie dist = new Movie();
        if (videoTracks.size() > 0) {
            dist.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));
        }
        if (audioTracks.size() > 0) {
            dist.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));
        }
        Container mp4file = new DefaultMp4Builder().build(dist);

        File storagePath = new File("/sdcard/ushow/");
        storagePath.mkdirs();
        FileOutputStream fos = new FileOutputStream(new File(storagePath, "c.mp4"));
        FileChannel fco = fos.getChannel();
        mp4file.writeContainer(fco);
        fco.close();
        fos.close();
    }

    public static void videoToVideo(String srcVideoPath, String dstVideoPath) throws IOException {
        H264TrackImpl h264Track = new H264TrackImpl(new FileDataSourceImpl(srcVideoPath));
        Movie m = new Movie();
        m.addTrack(h264Track);

        Container out = new DefaultMp4Builder().build(m);
        FileOutputStream fos = new FileOutputStream(new File(dstVideoPath));
        FileChannel fc = fos.getChannel();
        out.writeContainer(fc);
        fos.close();

    }
}
