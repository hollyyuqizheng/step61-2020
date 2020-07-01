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
   * @param startTimeInstant: event's start time, of type Instant.
   * @param endTimeInstant: event's end time, of type Instant. All of these fields are required for
   *     a calendar event.
   */
  public CalendarEvent(String name, Instant startTimeInstant, Instant endTimeInstant) {
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
    this.startTimeInstant = startTimeInstant;
    this.endTimeInstant = endTimeInstant;
  }

  /* Because all three fields are required, the following getters won't return null. */
  public String getName() {
    return name;
  }

  public Instant getStartTimeInstant() {
    return startTimeInstant;
  }

  public Instant getEndTimeInstant() {
    return endTimeInstant;
  }
}
