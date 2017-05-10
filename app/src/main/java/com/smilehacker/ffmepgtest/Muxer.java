package com.smilehacker.ffmepgtest;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by kleist on 2017/5/10.
 */

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class Muxer {

    public void concat(String output, String... sources) {
        MediaWrap[] mediaWrap = new MediaWrap[sources.length];
        for (int i = 0; i < sources.length; i++) {
            try {
                mediaWrap[i] = exactorMedia(sources[i]);
            } catch (Exception e) {
                mediaWrap[i] = null;
                e.printStackTrace();
            }
        }
        try {
            combineMedia(output, mediaWrap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private MediaWrap exactorMedia(String file) throws IOException {
        MediaExtractor mediaExtractor = new MediaExtractor();
        mediaExtractor.setDataSource(file);

        int trackCount = mediaExtractor.getTrackCount();
        int audioTrackIndex = -1;
        int videoTrackIndex = -1;
        for (int i = 0; i < trackCount; i++) {
            MediaFormat trackFormat = mediaExtractor.getTrackFormat(i);
            String mineType = trackFormat.getString(MediaFormat.KEY_MIME);
            if (mineType.startsWith("video/")) {
                videoTrackIndex = i;
            } else if (mineType.startsWith("audio/")) {
                audioTrackIndex = i;
            }
        }

        Log.i("Mux", "file[" + file + "]" + " audioIndex=" + audioTrackIndex + " videoIndex=" + videoTrackIndex);

        MediaWrap mediaWrap = new MediaWrap();
        mediaWrap.audioTrackIndex = audioTrackIndex;
        mediaWrap.videoTrackIndex = videoTrackIndex;
        mediaWrap.mediaExtractor = mediaExtractor;
        return mediaWrap;
    }

    private void combineMedia(String out, MediaWrap[] mediaWraps) throws IOException {
        if (mediaWraps.length == 0) {
            return;
        }
        MediaMuxer muxer = new MediaMuxer(out, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        MediaCodec.BufferInfo videoBufferInfo = new MediaCodec.BufferInfo();
        MediaCodec.BufferInfo audioBufferInfo = new MediaCodec.BufferInfo();

        for (int i = 0; i < mediaWraps.length; i++) {

        }
        {
            MediaWrap wrap = mediaWraps[1];

//            MediaFormat videoFormat = wrap.mediaExtractor.getTrackFormat(wrap.videoTrackIndex);
//            videoFormat.setLong(MediaFormat.KEY_DURATION, videoFormat.getLong(MediaFormat.KEY_DURATION) + 3000000);
//            videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
            MediaFormat videoFormat = MediaFormat.createVideoFormat("video/avc", 480, 480);
            videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, 512 * 1024);
            videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);


            wrap.writeVideoTrackIndex = muxer.addTrack(videoFormat);
            wrap.writeAudioTrackIndex = muxer.addTrack(wrap.mediaExtractor.getTrackFormat(wrap.audioTrackIndex));
            Log.i("Mux", "writeVideoIndex=" + wrap.writeVideoTrackIndex + " writeAudioIndex=" + wrap.writeAudioTrackIndex);
            muxer.start();
        }


        long lastVideoSampleTime  = 0;
        long lastAudioSampleTime  = 0;

        for (int i = 0; i < mediaWraps.length; i++) {
            MediaWrap wrap = mediaWraps[i];
            if (wrap == null) {
                continue;
            }


            ByteBuffer byteBuffer = ByteBuffer.allocate(500 * 1024);

//            long sampleTime = 0;
//            {
//                wrap.mediaExtractor.selectTrack(wrap.videoTrackIndex);
//                wrap.mediaExtractor.readSampleData(byteBuffer, 0);
//                if (wrap.mediaExtractor.getSampleFlags() == MediaExtractor.SAMPLE_FLAG_SYNC) {
//                    wrap.mediaExtractor.advance();
//                }
//                wrap.mediaExtractor.readSampleData(byteBuffer, 0);
//                long secondTime = wrap.mediaExtractor.getSampleTime();
//                wrap.mediaExtractor.advance();
//                long thirdTime = wrap.mediaExtractor.getSampleTime();
//                sampleTime = Math.abs(thirdTime - secondTime);
//                wrap.mediaExtractor.unselectTrack(wrap.videoTrackIndex);
//            }


            wrap.mediaExtractor.unselectTrack(wrap.audioTrackIndex);
            wrap.mediaExtractor.selectTrack(wrap.videoTrackIndex);
            while (true) {
                int readVideoSampleSize = wrap.mediaExtractor.readSampleData(byteBuffer, 0);
                if (readVideoSampleSize < 0) {
                    break;
                }
                videoBufferInfo.size = readVideoSampleSize;
                videoBufferInfo.presentationTimeUs = wrap.mediaExtractor.getSampleTime() + lastVideoSampleTime;
                videoBufferInfo.offset = 0;
                videoBufferInfo.flags = wrap.mediaExtractor.getSampleFlags();

                muxer.writeSampleData(wrap.writeVideoTrackIndex, byteBuffer, videoBufferInfo);
                wrap.mediaExtractor.advance();
            }

            wrap.mediaExtractor.unselectTrack(wrap.videoTrackIndex);
            wrap.mediaExtractor.selectTrack(wrap.audioTrackIndex);
            while (true) {
                int readMediaSampleSize = wrap.mediaExtractor.readSampleData(byteBuffer, 0);
                if (readMediaSampleSize < 0) {
                    break;
                }
                audioBufferInfo.size = readMediaSampleSize;
                audioBufferInfo.presentationTimeUs = wrap.mediaExtractor.getSampleTime() + lastAudioSampleTime;
                audioBufferInfo.offset = 0;
                audioBufferInfo.flags = wrap.mediaExtractor.getSampleFlags();

                muxer.writeSampleData(wrap.writeAudioTrackIndex, byteBuffer, audioBufferInfo);
                wrap.mediaExtractor.advance();
            }

            wrap.mediaExtractor.release();

            lastAudioSampleTime = audioBufferInfo.presentationTimeUs;
            lastVideoSampleTime = videoBufferInfo.presentationTimeUs;
        }
        muxer.stop();
        muxer.release();
    }

    public static class MediaWrap {
        public MediaExtractor mediaExtractor;
        public int audioTrackIndex = -1;
        public int videoTrackIndex = -1;
        public int writeVideoTrackIndex = 0;
        public int writeAudioTrackIndex = 1;
    }
}
