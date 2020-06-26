package com.google.sps.data;

import java.time.Instant;

/** Models a calendar event. It can be used for both creation and import flow. */
public class CalendarEvent {
  private String name;
  private final Instant startTime;
  private final Instant endTime;

  // TODO(hollyyuqizheng): Add an ID field if necessary.

  /**
   * Constructs a calendar event.
   *
   * @param name: Name for the event,
   * @param startTimeString: a string representation of the event's start time. The string is in
   *     format Day Month Date Year HH:MM:SS GMT-Time-zone
   * @param endTimeString: a string representation of the event's end time. The string's format is
   *     the same as startTimeString's. All of these fields are required for a calendar event.
   */
  public CalendarEvent(String name, String startTimeString, String endTimeString) {
    if (name == null) {
      throw new IllegalArgumentException("Event needs a name");
    }
    if (startTimeString == null) {
      throw new IllegalArgumentException("Event needs a start time");
    }
    if (endTimeString == null) {
      throw new IllegalArgumentException("Event needs an end time");
    }
    this.name = name;
    // Converts time from string representation into an instance of Instant class.
    this.startTime = Instant.parse(startTimeString);
    this.endTime = Instant.parse(endTimeString);
  }

  /* Because all three fields are required, the following getters won't return null. */
  public String getName() {
    return name;
  }

  public Instant getStartTimeInstant() {
    return startTime;
  }

  public Instant getEndTimeInstant() {
    return endTime;
  }

  public long getStartTimeLong() {
    return startTime.getEpochSecond();
  }

  public long getEndTimeLong() {
    return endTime.getEpochSecond();
  }
}
