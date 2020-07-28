package com.google.sps.data;

/** Handles range checking, and allows retrieval of the priority value. */
public final class TaskPriority implements Comparable<TaskPriority> {
  private final int priority;

  // Constants for minimum and maximum priority levels.
  public static final int MIN_PRIORITY = 1;
  public static final int MAX_PRIORITY = 5;

  public TaskPriority(int priority) {
    if (priority < MIN_PRIORITY || priority > MAX_PRIORITY) {
      throw new IllegalArgumentException("Priority must be a value from 1 to 5.");
    }

    this.priority = priority;
  }

  public int getPriority() {
    return priority;
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof TaskPriority && equals(this, (TaskPriority) other);
  }

  private static boolean equals(TaskPriority a, TaskPriority b) {
    return a.priority == b.priority;
  }

  /**
   * This method, when used in a comparator based on task priority, will sort the tasks by priority
   * descending, so the highest priority will come first after the sorting.
   */
  @Override
  public int compareTo(TaskPriority otherTaskPriority) {
    return Integer.compare(otherTaskPriority.getPriority(), priority);
  }
}
