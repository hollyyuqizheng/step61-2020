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
  private final Duration duration;
  private final TaskPriority priority;

  // TODO(raulcruise): Add an ID field if necessary.

  /**
   * The constructor will make sure that all necessary parameters are passed in, and populate each
   * class variable appropriately.
   *
   * @param name: Name for the Task
   * @param description: Optional description for the Task, this can be passed in as null but will
   *     be the constructor will set the object description to an empty Optional object
   * @param duration: the amount of time the user estimates this Task will require to be completed
   * @param priority: the priority the user wants the algorithm to consider when creating a
   *     schedule. The priority can range from 1 through 5 with 5 being the highest priority.
   *     Priority is handled by the custom class TaskPriority which checks input values.
   */
  public Task(String name, String description, Duration duration, TaskPriority priority) {
    if (name == null) {
      throw new IllegalArgumentException("Name cannot be null");
    }
    if (duration == null) {
      throw new IllegalArgumentException("Duration cannot be null");
    }
    if (priority == null) {
      throw new IllegalArgumentException("Priority cannot be null");
    }

    this.name = name;
    this.description = Optional.ofNullable(description);
    this.duration = duration;
    this.priority = priority;
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

  // These methods provide a way to tell if two objects are both Task objects
  // and have all the same fields

  @Override
  public boolean equals(Object other) {
    return other instanceof Task && equals(this, (Task) other);
  }

  private static boolean equals(Task a, Task b) {
    return a.name.equals(b.name)
        && a.description.equals(b.description)
        && a.getDuration().equals(b.getDuration())
        && a.priority.equals(b.priority);
  }
}
