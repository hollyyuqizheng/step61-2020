package com.google.sps.data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** This class models a group of calendar events. */
public class CalendarGroup {
  public List<CalendarEvent> events;
  public List<TimeRange> allFreeTimeRanges;

  // These two variables represent the start and end of
  // possible schedule time. When these times are passed in
  // to instantiate a CalendarGroup class, these times are
  // already adjusted based on user's working hour times
  // and the current timestamp of when the user starts
  // the scheduling process.
  public final Instant startTime;
  public final Instant endTime;

  // Comparator for sorting events by start time
  private static final Comparator<CalendarEvent> sortByEventStartTimeAscending =
      Comparator.comparing(CalendarEvent::getStartTime);

  // Comparator for sorting free time ranges by start time
  private static final Comparator<TimeRange> sortByTimeRangeStartTimeAscending =
      Comparator.comparing(TimeRange::start);

  /**
   * @param events: a list of CalendarEvents' that represents the events already scheduled for the
   *     user.
   * @param startTime: start of possible scheduling blocks, of type Instant. This is the earlier of
   *     working hour start time and the current timestamp.
   * @param endTime: end of possible scheduling blocks, of type Instant.
   */
  public CalendarGroup(List<CalendarEvent> events, Instant startTime, Instant endTime) {
    if (events == null) {
      throw new IllegalArgumentException("Calendar Group needs a collection of events");
    }
    if (startTime == null) {
      throw new IllegalArgumentException("Calendar Group needs the start of scheduling hours");
    }
    if (endTime == null) {
      throw new IllegalArgumentException("Calendar Group needs the end of scheduling hours");
    }
    if (startTime.isAfter(endTime)) {
      throw new IllegalArgumentException("Start time cannot be after end time");
    }
    this.events = events;
    this.startTime = startTime;
    this.endTime = endTime;
    this.allFreeTimeRanges = calculateFreeTimeRanges();
  }

  /**
   * Calculates a TimeRange list which represents the periods of time that are empty of events and
   * lie completely inside the possible scheduling hours. Originally written by tomasalvarez,
   * modified by hollyyuqizheng.
   */
  public List<TimeRange> calculateFreeTimeRanges() {
    Collections.sort(events, sortByEventStartTimeAscending);

    List<TimeRange> possibleTimes = new ArrayList<TimeRange>();

    // This represents the earliest time that we can schedule a window for the
    // meeting. As events are processed, this changes to their end times.
    Instant earliestNonScheduledInstant = startTime;
    for (CalendarEvent event : events) {
      // Make sure that there is some time between the events and it is not
      // later than the person's scheduling hours' ending time.
      if (event.getStartTime().isAfter(earliestNonScheduledInstant)
          && !event.getStartTime().isAfter(endTime)) {
        possibleTimes.add(
            TimeRange.fromStartEnd(earliestNonScheduledInstant, event.getStartTime()));
      }
      if (earliestNonScheduledInstant.isBefore(event.getEndTime())) {
        earliestNonScheduledInstant = event.getEndTime();
      }
    }
    // The end of the work hours is potentially never included so we check.
    if (endTime.isAfter(earliestNonScheduledInstant)) {
      possibleTimes.add(TimeRange.fromStartEnd(earliestNonScheduledInstant, endTime));
    }
    return possibleTimes;
  }

  /** Getter for the list of all free time ranges. */
  public List<TimeRange> getFreeTimeRanges() {
    return allFreeTimeRanges;
  }

  /**
   * Deletes a time range from the list of all free times.
   *
   * @return the modifed list of free time ranges.
   */
  public List<TimeRange> deleteFreeTimeRange(TimeRange timeRange) {
    boolean exist = false;
    for (TimeRange freeTimeRange : allFreeTimeRanges) {
      if (freeTimeRange.equals(timeRange)) {
        exist = true;
      }
    }
    if (!exist) {
      throw new IllegalArgumentException("This time range does not represent any free time range.");
    }
    allFreeTimeRanges.remove(timeRange);
    return allFreeTimeRanges;
  }

  /**
   * Modifies an original time range with a new one.
   *
   * @return the modified list of free times.
   */
  public List<TimeRange> modifyFreeTimeRange(TimeRange originalTimeRange, TimeRange newTimeRange) {
    if (originalTimeRange.start().isAfter(newTimeRange.start())
        || originalTimeRange.end().isBefore(newTimeRange.end())) {
      throw new IllegalArgumentException(
          "New time range can only be shorter than original time range");
    }

    allFreeTimeRanges.remove(originalTimeRange);
    allFreeTimeRanges.add(newTimeRange);

    // Sorts all free time ranges before returning.
    Collections.sort(allFreeTimeRanges, sortByTimeRangeStartTimeAscending);
    return allFreeTimeRanges;
  }
}
