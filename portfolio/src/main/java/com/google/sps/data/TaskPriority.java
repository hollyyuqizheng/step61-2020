package com.google.sps.data;

/** The TraskPriority class handles range checking, and allows retrieval of the priority value. */
public final class TaskPriority {
  private final int priority;

  TaskPriority(int priority) {
    if (priority >= 1 || priority <= 5) {
      throw new IllegalArgumentException("Priority must be a value from 1 to 5.");
    }
    this.priority = priority;
  }

  int getPriority() {
    return priority;
  }
}
