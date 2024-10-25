package com.google.android.exoplayer2.util;

import java.util.List;

public class StallingTimeInfo {
  private List<List<Long>> stalling;
  private int streamId;

  // Constructor
  public StallingTimeInfo(List<List<Long>> stalling, int streamId) {
    this.stalling = stalling;
    this.streamId = streamId;
  }

  // Getters and Setters
  public List<List<Long>> getStalling() {
    return stalling;
  }

  public void setStalling(List<List<Long>> stalling) {
    this.stalling = stalling;
  }

  public int getStreamId() {
    return streamId;
  }

  public void setStreamId(int streamId) {
    this.streamId = streamId;
  }
}
