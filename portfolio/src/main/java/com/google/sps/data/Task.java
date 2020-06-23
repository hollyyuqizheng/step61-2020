package com.google.sps.data;

/** Models a task that the user creates. */
public final class Task {

  private final String name;
  private final String description;
  private final Integer durationMinute;
  private final Integer priority;

  // TODO: does a task need an ID?

  public Task(String name, String description, Integer durationMinute, Integer priority) {
    if (durationMinute == null) {
      throw new IllegalArgumentException("Task needs a duration");
    }

    this.name = name;
    this.description = description;
    this.durationMinute = durationMinute;
    this.priority = priority;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public Integer getDurationMinute() {
    return durationMinute;
  }

  public Integer getPriority() {
    return priority;
  }
}
