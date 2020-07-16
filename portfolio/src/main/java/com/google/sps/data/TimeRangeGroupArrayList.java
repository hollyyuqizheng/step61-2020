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

  public void addTimeRange(TimeRange timeRange) {}

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
  // public List<TimeRange> deleteTimeRange(TimeRange timeRangeToDelete) {

  // }

}
