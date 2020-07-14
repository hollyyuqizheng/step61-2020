package com.google.sps.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TimeRangeGroup {

  public List<TimeRange> allTimeRanges;

  public TimeRangeGroup() {
    allTimeRanges = new ArrayList();
  }

  public void addTimeRange(TimeRange newTimeRange) {
    if (!allTimeRanges.contains(newTimeRange)) {
      allTimeRanges.add(newTimeRange);
    }
  }

  public List<TimeRange> getAllTimeRanges() {
    return allTimeRanges;
  }

  /**
   * Deletes a time range from the list of all time ranges.
   *
   * @return the modifed list of time ranges.
   */
  public List<TimeRange> deleteTimeRange(TimeRange timeRange) {
    if (allTimeRanges.contains(timeRange)) {
      allTimeRanges.remove(timeRange);
    } else {
      throw new IllegalArgumentException("This time range does not represent any free time range.");
    }
    return allTimeRanges;
  }

  /**
   * Modifies an original time range with a new one.
   *
   * @return the modified list of time ranges.
   */
  public List<TimeRange> modifyTimeRange(TimeRange originalTimeRange, TimeRange newTimeRange) {
    if (originalTimeRange.start().isAfter(newTimeRange.start())
        || originalTimeRange.end().isBefore(newTimeRange.end())) {
      throw new IllegalArgumentException(
          "New time range can only be shorter than original time range");
    }

    allTimeRanges.remove(originalTimeRange);
    allTimeRanges.add(newTimeRange);

    // Sorts all free time ranges before returning.
    Collections.sort(allTimeRanges, TimeRange.sortByTimeRangeStartTimeAscending);
    return allTimeRanges;
  }
}
