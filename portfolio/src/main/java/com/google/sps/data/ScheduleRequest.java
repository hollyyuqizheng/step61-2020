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
  private final Instant workHoursStartTime;
  private final Instant workHoursEndTime;

  /**
   * Constructs a request to be scheduled.
   *
   * @param events: events happening the day of the scheduling,
   * @param tasks: the tasks a user would like to schedule on that day
   * @param workHoursStartTimeString: a string representation of the work hours start
   *     time. The string is in format Day Month Date Year HH:MM:SS GMT-Time-zone
   * @param workHoursEndTimeString: a string representation of the work hours end time.
   *     The string's format is the same as startTimeString's.
   * All of these fields are required for a request (can be empty not null).
   */
  public ScheduleRequest(Collection<CalendarEvent> events, Collection<Task> tasks,
      String workHoursStartTimeString, String workHoursEndTimeString) {
    if (events == null) {
      throw new IllegalArgumentException("Events cannot be null");
    }
    if (tasks == null) {
      throw new IllegalArgumentException("Tasks cannot be null");
    }
    if (workHoursStartTimeString == null) {
      throw new IllegalArgumentException("Request needs a start time");
    }
    if (workHoursEndTimeString == null) {
      throw new IllegalArgumentException("Request needs an end time");
    }
    this.events = events;
    this.tasks = tasks;
    // Converts time from string representation into an instance of Instant class.
    this.workHoursStartTime = Instant.parse(workHoursStartTimeString);
    this.workHoursEndTime = Instant.parse(workHoursEndTimeString);
  }

  /* Because all four fields are required, the following getters won't return null. */
  public Collection<CalendarEvent> getEvents() {
    return events;
  }

  public Collection<Task> getTasks() {
    return tasks;
  }
  public Instant getWorkHoursStartTimeInstant() {
    return workHoursStartTime;
  }
  public Instant getWorkHoursEndTimeInstant() {
    return workHoursEndTime;
  }

  // These methods return the instant as a number of seconds from 00:00:00 UTC
  // on 1 January 1970

  public long getWorkHoursStartTimeLong() {
    return workHoursStartTime.getEpochSecond();
  }

  public long getWorkHoursEndTimeLong() {
    return workHoursEndTime.getEpochSecond();
  }
}
