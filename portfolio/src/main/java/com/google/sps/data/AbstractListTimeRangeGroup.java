package com.google.sps.data;

import java.time.Instant;
import java.util.Iterator;
import java.util.List;

abstract class AbstractListTimeRangeGroup implements TimeRangeGroup {
  protected List<TimeRange> allTimeRanges;

  /**
   * Helper method for merging two time ranges. This method is package-private so that it can be
   * tested.
   */
  public static TimeRange mergeTwoTimeRanges(TimeRange a, TimeRange b) {
    if (!a.overlaps(b)) {
      throw new IllegalArgumentException("Merging two time ranges that do not overlap is invalid");
    }

    Instant newTimeRangeStart;
    Instant newTimeRangeEnd;

    // The new time range after merging should have the earlier start time
    // and the later end time among the two overlapping time ranges.
    if (a.start().isBefore(b.start())) {
      newTimeRangeStart = a.start();
    } else {
      newTimeRangeStart = b.start();
    }

    if (a.end().isBefore(b.end())) {
      newTimeRangeEnd = b.end();
    } else {
      newTimeRangeEnd = a.end();
    }

    return TimeRange.fromStartEnd(newTimeRangeStart, newTimeRangeEnd);
  }

  /** Returns an iterator for the list of all time ranges. */
  public Iterator<TimeRange> iterator() {
    return allTimeRanges.iterator();
  }
}
