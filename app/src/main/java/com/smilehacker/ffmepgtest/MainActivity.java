package com.smilehacker.ffmepgtest;

import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    String fileLogo = Environment.getExternalStorageDirectory() + "/ushow/logo.mp4";
    String fileContent = Environment.getExternalStorageDirectory() + "/ushow/test3.mp4";
    String fileLogoChange  = Environment.getExternalStorageDirectory() + "/ushow/1.ts";
    String fileContentChange = Environment.getExternalStorageDirectory() + "/ushow/2.ts";
    String fileDest = Environment.getExternalStorageDirectory() + "/ushow/c.mp4";
    String fileTxt = Environment.getExternalStorageDirectory() + "/ushow/file.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.run).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                useMuxer();
//                parse();
//                concat();
//                concat1();
            }

        });
    }

    private void useMuxer() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                Muxer muxer = new Muxer();
                muxer.concat(fileDest, fileLogo, fileContent);
                return null;
            }
        }.execute();
    }

    private void concat() {
        new AsyncTask<Void, Void, Void>() {

            private long time;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                time = System.currentTimeMillis();
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Log.i("","total cost=" + (System.currentTimeMillis() - time));
            }

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    VideoHelper.concat(fileLogoChange, fileContent);
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                try {
//                    VideoHelper.videoToVideo(file1, file2);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                return null;
            }
        }.execute();
    }

    private void parse() {
       FFmpeg ffmpeg = FFmpeg.getInstance(this);
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {

                @Override
                public void onStart() {}

                @Override
                public void onFailure() {}

                @Override
                public void onSuccess() {
                    concat1();
                }

                @Override
                public void onFinish() {}
            });
        } catch (FFmpegNotSupportedException e) {
            // Handle if FFmpeg is not supported by device
        }
    }

    private void doing() {
        final long time = System.currentTimeMillis();
        int rate = getFrameRate(fileContent);
        Log.i("mm", "======== fps ======= " + rate);

        String[] cmd2 = new String[]{"-i", fileContent, "-i", "/sdcard/ushow/logo.png", "-filter_complex", "overlay=10:main_h-overlay_h-10", fileDest};

        FFmpeg ffmpeg = FFmpeg.getInstance(this);
        try {
            // to execute "ffmpeg -version" command you just need to pass "-version"
            ffmpeg.execute(cmd2, new ExecuteBinaryResponseHandler() {

                @Override
                public void onStart() {}

                @Override
                public void onProgress(String message) {
                    Log.i("mm", message);
                }

                @Override
                public void onFailure(String message) {
                    Log.i("mm", "fail = " + message);
                }

                @Override
                public void onSuccess(String message) {
                }

                @Override
                public void onFinish() {
                    long time1 = System.currentTimeMillis();
                    Log.i("mm", "cost=" + (time1 - time));
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // Handle if FFmpeg is already running
        }
    }

    private void doing1() {
        final long time = System.currentTimeMillis();
        String[] cmd = new String[]{"-i", fileLogo, "-qscale", "0", fileLogoChange};

        FFmpeg ffmpeg = FFmpeg.getInstance(this);
        try {
            // to execute "ffmpeg -version" command you just need to pass "-version"
            ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {

                @Override
                public void onStart() {}

                @Override
                public void onProgress(String message) {
                    Log.i("mm", message);
                }

                @Override
                public void onFailure(String message) {
                    Log.i("mm", "fail = " + message);
                }

                @Override
                public void onSuccess(String message) {
                    doing2();
                }

                @Override
                public void onFinish() {
                    long time1 = System.currentTimeMillis();
                    Log.i("mm", "cost=" + (time1 - time));
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // Handle if FFmpeg is already running
        }
    }

    private void doing2() {
        final long time = System.currentTimeMillis();
        String[] cmd = new String[]{"-i", fileContent, "-qscale", "0", fileContentChange};

        FFmpeg ffmpeg = FFmpeg.getInstance(this);
        try {
            // to execute "ffmpeg -version" command you just need to pass "-version"
            ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {

                @Override
                public void onStart() {}

                @Override
                public void onProgress(String message) {
                    Log.i("mm", message);
                }

                @Override
                public void onFailure(String message) {
                    Log.i("mm", "fail = " + message);
                }

                @Override
                public void onSuccess(String message) {
                    concatByFF();
                }

                @Override
                public void onFinish() {
                    long time1 = System.currentTimeMillis();
                    Log.i("mm", "cost=" + (time1 - time));
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // Handle if FFmpeg is already running
        }
    }


    private int getFrameRate(String file) {
        MediaExtractor extractor = new MediaExtractor();
        int frameRate = -1; //may be default
        try {
            //Adjust data source as per the requirement if file, URI, etc.
            extractor.setDataSource(file);
            int numTracks = extractor.getTrackCount();
            for (int i = 0; i < numTracks; ++i) {
                MediaFormat format = extractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);
                if (mime.startsWith("video/")) {
                    if (format.containsKey(MediaFormat.KEY_FRAME_RATE)) {
                        frameRate = format.getInteger(MediaFormat.KEY_FRAME_RATE);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            //Release stuff
            extractor.release();
        }

        return frameRate;
    }

    private void concatByFF() {
        String[] cmd = new String[] {"-f", "concat", "-i", "/sdcard/ushow/file.txt", "-c", "copy", fileDest};
        FFmpeg ffmpeg = FFmpeg.getInstance(this);
        try {
            // to execute "ffmpeg -version" command you just need to pass "-version"
            ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {

                @Override
                public void onStart() {}

                @Override
                public void onProgress(String message) {
                    Log.i("mm", message);
                }

                @Override
                public void onFailure(String message) {
                    Log.i("mm", "fail = " + message);
                }

                @Override
                public void onSuccess(String message) {
                }

                @Override
                public void onFinish() {
                    long time1 = System.currentTimeMillis();
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // Handle if FFmpeg is already running
        }
    }

    private void runCmd(String cmd, final Runnable runnable) {
        FFmpeg ffmpeg = FFmpeg.getInstance(this);
        final long time = System.currentTimeMillis();
        try {
            ffmpeg.execute(cmd.split(" "), new ExecuteBinaryResponseHandler() {

                @Override
                public void onStart() {}

                @Override
                public void onProgress(String message) {
                    Log.i("mm", message);
                }

                @Override
                public void onFailure(String message) {
                    Log.i("mm", "fail = " + message);
                }

                @Override
                public void onSuccess(String message) {
                    runnable.run();
                }

                @Override
                public void onFinish() {
                    Log.i("ffmpeg", "cost = " + (System.currentTimeMillis() - time));
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }
    }

    private void concat1() {
//        String cmd1 = String.format("-i %s -c copy -bsf:v h264_mp4toannexb -f mpegts %s", fileLogo, fileLogoChange);
//        final String cmd2 = String.format("-i %s -c copy -bsf:v h264_mp4toannexb -f mpegts %s", fileContent, fileContentChange);
//        final String cmd3 = String.format("-f concat -i %s -c copy -bsf:a aac_adtstoasc %s", fileTxt, fileDest);
        final long time = System.currentTimeMillis();
        final String cmd1 = String.format("-i %s -vcodec copy -acodec copy -f mpegts %s", fileLogo, fileLogoChange);
        final String cmd2 = String.format("-i %s -vcodec copy -acodec copy -f mpegts %s", fileContent, fileContentChange);
        final String cmd3 = String.format("-f concat -i %s -c copy %s", fileTxt, fileDest);
        runCmd(cmd1, new Runnable() {
            @Override
            public void run() {
                runCmd(cmd2, new Runnable() {
                    @Override
                    public void run() {
                        runCmd(cmd3, new Runnable() {
                            @Override
                            public void run() {
                                Log.i("success", "time cost= " + (System.currentTimeMillis() - time));
                            }
                        });
                    }
                });
            }
        });
    }
}
