package com.google.sps.data;

import java.time.Duration;
import java.util.Optional;

/**
 * Models a task. It can be used for both creation and import flow. The description variable is
 * optional while all other fields are required. If a null is passed in for the description, then
 * description will be stored as an empty Optional object.
 */
public final class Task {

  private final String name;
  private final Optional<String> description;
  private final Duration durationMinute;
  private final TaskPriority priority;

  // TODO(raulcruise): Add an ID field if necessary.

  // The constructor will make sure that all necessary parameters are passed in,
  // and populate each class variable appropriately.
  public Task(String name, String description, Duration durationMinute, Integer priority) {
    if (name == null) {
      throw new IllegalArgumentException("Name cannot be null");
    }

    if (durationMinute == null) {
      throw new IllegalArgumentException("Duration cannot be null");
    }

    if (priority == null) {
      throw new IllegalArgumentException("Priority cannot be null");
    }

    this.name = name;
    this.description = Optional.ofNullable(description);
    this.durationMinute = durationMinute;
    this.priority = new TaskPriority(priority);
  }

  public String getName() {
    return name;
  }

  public Optional<String> getDescription() {
    return description;
  }

  public Duration getDurationMinute() {
    return durationMinute;
  }

  public Integer getPriority() {
    return priority.getPriority();
  }
}
