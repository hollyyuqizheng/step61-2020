package com.google.sps.data;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/** Models an implementation of the TimeRangeGroup model using ArrayList. */
public class TimeRangeGroupArrayList {

  public List<TimeRange> allTimeRanges;

  public TimeRangeGroupArrayList(Collection<TimeRange> timeRanges) {
    allTimeRanges = (List<TimeRange>) timeRanges;
    Collections.sort(allTimeRanges, TimeRange.sortByTimeRangeStartTimeAscending);
  }

  /**
   * Adds a new time range to the list. This new range must be disjoint from all existing ranges.
   */
  public void addTimeRange(TimeRange timeRange) {
    for (TimeRange currentRange : allTimeRanges) {
      if (currentRange.overlaps(timeRange)) {
        throw new IllegalArgumentException(
            "New time range to add cannot overlap with any existing time ranges");
      }
    }

    allTimeRanges.add(timeRange);
    Collections.sort(allTimeRanges, TimeRange.sortByTimeRangeStartTimeAscending);
  }

  /** Returns the array list of all time ranges. */
  public List<TimeRange> getAllTimeRanges() {
    return allTimeRanges;
  }

  /**
   * Checks if a time range exists in the collection. For example, if [3:00 - 4:00] is in the
   * collection, [3:00 - 3:30] is considered to exist as a time range in the collection.
   */
  public boolean hasTimeRange(TimeRange timeRangeToCheck) {
    for (TimeRange timeRange : allTimeRanges) {
      if (timeRange.contains(timeRangeToCheck)) {
        return true;
      }
    }

    return false;
  }

  /** @return the newly modified collection of time ranges. */
  public List<TimeRange> deleteTimeRange(TimeRange timeRangeToDelete) {
    for (TimeRange currentRange : allTimeRanges) {
      if (currentRange.contains(timeRangeToDelete)) {}
    }

    // If at this point the method hasn't returned yet, it means none of the existing
    // time ranges actually contains the range to delete.
    // Throw an invalid input exception here.
    throw new IllegalArgumentException(
        "The time range to delete must be contained by an existing time range");
  }
}
