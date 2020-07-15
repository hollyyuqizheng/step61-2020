package com.google.sps.data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TimeRangeGroup {

  public List<TimeRange> allTimeRanges;

  public TimeRangeGroup() {
    allTimeRanges = new ArrayList();
  }

  /** Adds a new time range to the list of all time ranges. */
  public void addTimeRange(TimeRange newTimeRange) {
    if (!allTimeRanges.contains(newTimeRange)) {
      allTimeRanges.add(newTimeRange);
    }
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof TimeRangeGroup && this.equals((TimeRangeGroup) other);
  }

  /** Returns the list of all time ranges. */
  public List<TimeRange> getAllTimeRanges() {
    return allTimeRanges;
  }

  /**
   * Deletes an entire time range from the list of all time ranges.
   *
   * @return the modifed list of time ranges.
   */
  public List<TimeRange> deleteEntireTimeRange(TimeRange timeRange) {
    if (timeRange == null) {
      throw new IllegalArgumentException("Time range to delete cannot be null");
    }

    if (allTimeRanges.contains(timeRange)) {
      allTimeRanges.remove(timeRange);
    } else {
      throw new IllegalArgumentException(
          "The time range to delete does not exist in the collection of all time ranges.");
    }
    return allTimeRanges;
  }

  /**
   * Deletes only part of a time range from the middle.
   *
   * @param originalTimeRange: |----------------------|, of type TimeRange
   * @param timeRangeToDelete: |-----| , of type TimeRange new time ranges: |------| |---------|
   * @return the sorted all time ranges with the new ranges included
   */
  public List<TimeRange> deletePartOfTimeRange(
      TimeRange originalTimeRange, TimeRange timeRangeToDelete) {
    if (originalTimeRange == null || timeRangeToDelete == null) {
      throw new IllegalArgumentException(
          "Original time range or time range to delete cannot be null");
    }
    if (!allTimeRanges.contains(originalTimeRange)) {
      throw new IllegalArgumentException(
          "The original time range does not exist in the collection of all time ranges.");
    }

    if (originalTimeRange.start().isAfter(timeRangeToDelete.start())
        || originalTimeRange.end().isBefore(timeRangeToDelete.end())) {
      throw new IllegalArgumentException(
          "The busy time range does not lie entirely inside the original time range.");
    }

    Instant originalStart = originalTimeRange.start();
    Instant originalEnd = originalTimeRange.end();
    Instant toDeleteTimeRangeStart = timeRangeToDelete.start();
    Instant toDeleteTimeRangeEnd = timeRangeToDelete.end();

    if (originalStart.isBefore(toDeleteTimeRangeStart)) {
      TimeRange newTimeRangeOne = TimeRange.fromStartEnd(originalStart, toDeleteTimeRangeStart);
      allTimeRanges.add(newTimeRangeOne);
    }

    if (originalEnd.isAfter(toDeleteTimeRangeEnd)) {
      TimeRange newTimeRangeTwo = TimeRange.fromStartEnd(toDeleteTimeRangeEnd, originalEnd);
      allTimeRanges.add(newTimeRangeTwo);
    }

    allTimeRanges.remove(originalTimeRange);

    // Sorts all free time ranges before returning.
    Collections.sort(allTimeRanges, TimeRange.sortByTimeRangeStartTimeAscending);

    return allTimeRanges;
  }
}
