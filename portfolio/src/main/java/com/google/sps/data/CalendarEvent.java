package com.google.sps.data;

import java.time.Instant;

/** Models a calendar event. It can be used for both creation and import flow. */
public class CalendarEvent {
  private final String name;
  private final Instant startTime;
  private final Instant endTime;

  // TODO(hollyyuqizheng): Add an ID field if necessary.

  /**
   * Constructs a calendar event.
   *
   * @param name: Name for the event,
   * @param startTime: event's start time, of type Instant.
   * @param endTime: event's end time, of type Instant. All of these fields are required for a
   *     calendar event.
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
    if (startTime.isAfter(endTime)) {
      throw new IllegalArgumentException("Start time cannot be after end time");
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
  
  @Override
  public boolean equals(Object other) {
    return other instanceof CalendarEvent && equals(this, (CalendarEvent) other);
  }

  public static boolean equals(CalendarEvent a, CalendarEvent b) {
    return a.name.equals(b.name)
        && a.startTime.equals(b.startTime)
        && a.endTime.equals(b.endTime);
  }
}
