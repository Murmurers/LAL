package com.test.li182.my_game;

/**
 * Created by li182 on 2018/1/20.
 */

public class Player {
    String name;
    boolean ready;
    long score;

    public Player(String name) {
        this.name = name;
    }
    public Player(String name, long score) {
        this.name = name;
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getScore() {
        return score;
    }

    public void setScore(long score) {
        this.score = score;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }
}
