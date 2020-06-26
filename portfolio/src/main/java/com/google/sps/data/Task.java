package com.google.sps.data;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Models a task. It can be used for both creation and import flow. The description variable is
 * optional while all other fields are required. If a null is passed in for the description, then
 * description will be stored as an empty Optional object.
 */
public final class Task {
  private final String name;
  private final Optional<String> description;
  private final Duration duration;
  private final TaskPriority priority;
  private Instant scheduledTime;

  // TODO(raulcruise): Add an ID field if necessary.

  /**
   * The constructor will make sure that all necessary parameters are passed in, and populate each
   * class variable appropriately. None of the fields can be null with exception to the description
   * as it is optional. If the description is passed in as null then it will be stored as an empty
   * Optional object. Tasks by default do not have a scheduled time, they are only assigned this
   * field as a result of getting scheduled by the scheduling algorithm.
   */
  public Task(String name, String description, long durationMinutes, TaskPriority priority) {
    if (name == null) {
      throw new IllegalArgumentException("Name cannot be null");
    }

    if (priority == null) {
      throw new IllegalArgumentException("Priority cannot be null");
    }

    this.name = name;
    this.description = Optional.ofNullable(description);
    this.duration = Duration.ofMinutes(durationMinutes);
    this.priority = priority;
  }

  /**
   * The constructor allows for Tasks to have their scheduled time be set. New tasks being made
   * for scheduling purposes use this constructor instead.
   */
  public Task(String name, String description, long durationMinutes, TaskPriority priority,
      String scheduledTime) {
    if (name == null) {
      throw new IllegalArgumentException("Name cannot be null");
    }

    if (priority == null) {
      throw new IllegalArgumentException("Priority cannot be null");
    }

    if (scheduledTime == null) {
      throw new IllegalArgumentException("Scheduled time cannot be null");
    }

    this.name = name;
    this.description = Optional.ofNullable(description);
    this.duration = Duration.ofMinutes(durationMinutes);
    this.priority = priority;
    this.scheduledTime = Instant.parse(scheduledTime);
  }

  public String getName() {
    return name;
  }

  public Optional<String> getDescription() {
    return description;
  }

  public Duration getDuration() {
    return duration;
  }

  public TaskPriority getPriority() {
    return priority;
  }

  public Integer getPriorityInt() {
    return priority.getPriority();
  }

  public long getDurationSeconds() {
    return duration.getSeconds();
  }

  // These methods provide a way to tell if two objects are both Task objects
  // and have all the same fields

  @Override
  public boolean equals(Object other) {
    return other instanceof Task && equals(this, (Task) other);
  }

  private static boolean equals(Task a, Task b) {
    return a.name.equals(b.name) && a.description.equals(b.description)
        && a.getDurationSeconds()==b.getDurationSeconds() && a.priority.equals(b.priority)
        && a.scheduledTime.getEpochSecond()==b.scheduledTime.getEpochSecond();
  }
}
