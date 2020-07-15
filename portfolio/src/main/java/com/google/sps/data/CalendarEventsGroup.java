package com.google.sps.data;

import com.google.common.collect.Lists;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** This class models a group of calendar events. */
public class CalendarEventsGroup {

  // This variable that represents a group of calendar events will
  // always be sorted in ascending order based on their start time.
  public final List<CalendarEvent> events;

  // These two variables represent the start and end of
  // possible schedule time. When these times are passed in
  // to instantiate a this class, these times are
  // already adjusted based on user's working hour times
  // and the current timestamp of when the user starts
  // the scheduling process.
  public final Instant overallStartTime;
  public final Instant overallEndTime;

  // Comparator for sorting events by start time
  private static final Comparator<CalendarEvent> sortByEventStartTimeAscending =
      Comparator.comparing(CalendarEvent::getStartTime);

  /**
   * @param events: a list of CalendarEvents' that represents the events already scheduled for the
   *     user.
   * @param startTime: start of possible scheduling blocks, of type Instant. This is the earlier of
   *     working hour start time and the current timestamp.
   * @param endTime: end of possible scheduling blocks, of type Instant.
   */
  public CalendarEventsGroup(
      List<CalendarEvent> events, Instant overallStartTime, Instant overallEndTime) {
    if (events == null) {
      throw new IllegalArgumentException("Events cannot be null");
    }
    if (overallStartTime == null) {
      throw new IllegalArgumentException("Overall start of scheduling hours cannot be null");
    }
    if (overallEndTime == null) {
      throw new IllegalArgumentException("Overall end of scheduling hours cannot be null");
    }
    if (overallStartTime.isAfter(overallEndTime)) {
      throw new IllegalArgumentException("Start time cannot be after end time");
    }
    this.events = Lists.newArrayList(events);
    Collections.sort(this.events, sortByEventStartTimeAscending);

    this.overallStartTime = overallStartTime;
    this.overallEndTime = overallEndTime;
  }

  /**
   * Calculates a TimeRange list which represents the periods of time that are empty of events and
   * lie completely inside the possible scheduling hours. Originally written by tomasalvarez,
   * modified by hollyyuqizheng.
   */
  public List<TimeRange> getFreeTimeRanges() {
    List<TimeRange> possibleTimes = new ArrayList<TimeRange>();

    // This represents the earliest time that we can schedule a window for the
    // meeting. As events are processed, this changes to their end times.
    Instant earliestNonScheduledInstant = overallStartTime;

    for (CalendarEvent event : events) {
      // Make sure that there is some time between the events and it is not
      // later than the person's scheduling hours' ending time.
      if (event.getStartTime().isAfter(earliestNonScheduledInstant)) {
        if (event.getStartTime().isAfter(overallEndTime)) {
          possibleTimes.add(TimeRange.fromStartEnd(earliestNonScheduledInstant, overallEndTime));
        } else {
          possibleTimes.add(
              TimeRange.fromStartEnd(earliestNonScheduledInstant, event.getStartTime()));
        }
      }
      // Check if the earliest non scheduled time needs to be shifted to later.
      if (earliestNonScheduledInstant.isBefore(event.getEndTime())) {
        earliestNonScheduledInstant = event.getEndTime();
      }
    }
    // The end of the work hours is potentially never included so we check.
    if (overallEndTime.isAfter(earliestNonScheduledInstant)) {
      possibleTimes.add(TimeRange.fromStartEnd(earliestNonScheduledInstant, overallEndTime));
    }

    return possibleTimes;
  }

  /**
   * Adds a new event into the event group. Returns the new collection of events, sorted based on
   * start time.
   */
  public void addNewEvent(CalendarEvent event) {
    events.add(event);
    Collections.sort(events, sortByEventStartTimeAscending);
  }
}
