package com.google.sps.data;

import java.time.Instant;

/** Models a calendar event. It can be used for both creation and import flow. */
public class CalendarEvent {
  private String name;
  private final String startTimeString;
  private final String endTimeString;

  // TODO(hollyyuqizheng): Add an ID field if necessary.

  public CalendarEvent(String name, String startTimeString, String endTimeString) {
    if (startTimeString == null) {
      throw new IllegalArgumentException("Event needs a start time");
    }
    if (endTimeString == null) {
      throw new IllegalArgumentException("Event needs an end time");
    }
    this.name = name;
    this.startTimeString = startTimeString;
    this.endTimeString = endTimeString;
  }

  public String getName() {
    if (name == null) {
      name = "New Event";
    }
    return name;
  }

  public Instant getStartTimeInstant() {
    return Instant.parse(startTimeString);
  }

  public Instant getEndTimeInstant() {
    return Instant.parse(endTimeString);
  }
}
