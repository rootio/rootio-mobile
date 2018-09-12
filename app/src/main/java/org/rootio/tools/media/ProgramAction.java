package org.rootio.tools.media;

import android.content.Context;

import java.util.ArrayList;

public class ProgramAction {
    private ArrayList<String> playlists, streams;
    private ProgramActionType programActionType;
    private Context parent;
    private PlayList playlist;
    private int duration;

    public ProgramAction(Context parent, ArrayList<String> playlists, ArrayList<String> streams, ProgramActionType programType, int duration) {
        this.parent = parent;
        this.playlists = playlists;
        this.streams = streams;
        this.programActionType = programType;
        this.duration = duration;
    }

    void run() {
        switch (this.programActionType) {
            case Media:
            case Audio:
                this.playlist = PlayList.getInstance();
                this.playlist.init(this.parent, this.playlists, this.streams, this.programActionType);
                this.playlist.load();
                this.playlist.play();
                break;
            case Jingle:
                break;
            case Outcall:
                break;
            default:
                break;
        }
    }

    void resume() {
        switch (this.programActionType) {
            case Media:
            case Audio:
                try {
                    this.playlist.resume();
                }
                catch(Exception ex)
                {
                    //todo: log this
                }
                break;
            case Jingle:
                 break;
            case Outcall:
                break;
            default:
                break;
        }
    }

    void play() {
        switch (this.programActionType) {
            case Media:
            case Audio:
                this.playlist.play();
                break;
            case Jingle:
                break;
            case Outcall:
                break;
            default:
                break;
        }
    }

    void pause() {
        switch (this.programActionType) {
            case Media:
            case Audio:
                this.playlist.pause(false);
                break;
            case Jingle:
                break;
            case Outcall:
                this.playlist.pause(false);
                break;
            default:
                break;
        }
    }

    void stop() {
        switch (this.programActionType) {
            case Media:
            case Audio:
                this.playlist.stop();
                break;
            case Jingle:
                break;
            case Outcall:
                break;
            default:
                break;
        }
    }

    public int getDuration()
    {
        return this.duration;
    }


}
