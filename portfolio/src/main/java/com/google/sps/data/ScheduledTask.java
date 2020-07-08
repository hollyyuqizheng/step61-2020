package com.google.sps.data;

import java.time.Instant;

public class ScheduledTask {
  private final Task task;
  private final Instant startTime;

  public ScheduledTask(Task task, Instant startTime) {
    if (task == null) {
      throw new IllegalArgumentException("Task cannot be null");
    }
    if (startTime == null) {
      throw new IllegalArgumentException("Start time cannot be null");
    }
    this.task = task;
    this.startTime = startTime;
  }

  public Task getTask() {
    return task;
  }

  public Instant getStartTime() {
    return startTime;
  }
}
