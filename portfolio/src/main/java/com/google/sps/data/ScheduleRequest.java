package com.google.sps.data;

import java.time.Instant;
import java.util.Collection;
/**
 * Models request that are sent to the algorithm. It can be used for both
 * creation and import flow.
 */
public class ScheduleRequest {
  private Collection<CalendarEvent> events;
  private Collection<Task> tasks;
  private final Instant startTime;
  private final Instant endTime;

  /**
   * Constructs a request to be scheduled.
   *
   * @param events: events happening the day of the scheduling,
   * @param tasks: the tasks a user would like to schedule on that day
   * @param startTimeString: a string representation of the event's start time.
   *     The string is in format Day Month Date Year HH:MM:SS GMT-Time-zone
   * @param endTimeString: a string representation of the event's end time. The
   *    string's format is the same as startTimeString's.
   * All of these fields are required for a request (can be empty not null).
   */
  public ScheduleRequest(Collection<CalendarEvent> events, Collection<Task> tasks,
      String startTimeString, String endTimeString) {
    if (events == null) {
      throw new IllegalArgumentException("Events cannot be null");
    }
    if (tasks == null) {
      throw new IllegalArgumentException("Tasks cannot be null");
    }
    if (startTimeString == null) {
      throw new IllegalArgumentException("Request needs a start time");
    }
    if (endTimeString == null) {
      throw new IllegalArgumentException("Request needs an end time");
    }
    this.events = events;
    this.tasks = tasks;
    // Converts time from string representation into an instance of Instant class.
    this.startTime = Instant.parse(startTimeString);
    this.endTime = Instant.parse(endTimeString);
  }

  /* Because all three fields are required, the following getters won't return null. */
  public Collection<CalendarEvent> getEvents() {
    return events;
  }

  public Collection<Task> getTasks() {
    return tasks;
  }
  public Instant getStartTimeInstant() {
    return startTime;
  }
  public Instant getEndTimeInstant() {
    return endtime;
  }

  public long getStartTimeLong() {
    return startTime.getEpochSecond();
  }

  public long getEndTimeLong() {
    return endTime.getEpochSecond();
  }
}
