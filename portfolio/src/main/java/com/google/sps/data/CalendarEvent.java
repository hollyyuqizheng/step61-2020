package com.google.sps.data;

/** Models a calendar event that the user creates. */
public class CalendarEvent {
  private final String name;
  private final String startTime;
  private final String endTime;

  // TODO: does an event need an ID?

  public CalendarEvent(String name, String startTime, String endTime) {
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

  public String getName() {
    return name;
  }

  public String getStartTime() {
    return startTime;
  }

  public String getEndTime() {
    return endTime;
  }
}
