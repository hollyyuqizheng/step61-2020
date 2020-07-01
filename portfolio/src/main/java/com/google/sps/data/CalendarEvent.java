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
   * @param startTime: An Instant representing the time that the event starts.
   * @param endTime: An Instant representing the time that the event end.
   */
  public CalendarEvent(String name, Instant startTime, Instant endTime) {
    if (name == null) {
      throw new IllegalArgumentException("Event needs a name");
    }
    if (startTime == null) {
      throw new IllegalArgumentException("Event needs a start time");
    }
    if (endTime == null) {
      throw new IllegalArgumentException("Event needs an end time");
    }
    this.name = name;
    this.startTime = startTime;
    this.endTime = endTime;
  }

  /* Because all three fields are required, the following getters won't return null. */
  public String getName() {
    return name;
  }

  public Instant getStartTime() {
    return startTime;
  }

  public Instant getEndTime() {
    return endTime;
  }
}
