package com.google.sps.data;

import java.time.Instant;

/** Models a calendar event. It can be used for both creation and import flow. */
public class CalendarEvent {
  private String name;
  private final Instant startTimeString;
  private final Instant endTimeString;

  // TODO(hollyyuqizheng): Add an ID field if necessary.

  /**
   * Constructs a calendar event.
   *
   * @param name: Name for the event, can be null startTimeString: a string representation of the
   *     event's start time. The string is in format Day Month Date Year HH:MM:SS GMT-Time-zone
   *     endTimeString: a string representation of the event's end time. The string's format is the
   *     same as startTimeString's. All of these fields are required for a calendar event.
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
    this.startTimeString = Instant.parse(startTimeString);
    this.endTimeString = Instant.parse(endTimeString);
  }

  public String getName() {
    return name;
  }

  public Instant getStartTimeInstant() {
    return startTimeString;
  }

  public Instant getEndTimeInstant() {
    return endTimeString;
  }
}
