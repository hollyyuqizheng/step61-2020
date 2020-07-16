package com.google.sps.data;

import java.util.Collection;

public interface TimeRangeGroup {

  /**
   * Merges the collection of time ranges that is passed into the constructor so that all time
   * ranges are disjoint.
   */
  public Collection<TimeRange> mergeInputTimeRanges(Collection<TimeRange> timeRanges);

  /** Adds a new time range into the collection of time ranges. */
  public void addTimeRange(TimeRange timeRange);

  /** Returns the collection of all time ranges. */
  public Collection<TimeRange> getAllTimeRanges();

  /** Checks if a time range exists in the collection. */
  public boolean hasTimeRange(TimeRange timeRange);

  /**
   * Deletes a given time range.
   *
   * @return the newly modified collection of time ranges.
   */
  public Collection<TimeRange> deleteTimeRange(TimeRange timeRangeToDelete);
}
