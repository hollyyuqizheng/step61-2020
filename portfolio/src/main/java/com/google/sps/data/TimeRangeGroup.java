package com.google.sps.data;

import java.util.Collection;

public interface TimeRangeGroup {

  /**
   * Adds a new time range into the collection of time ranges. This newly added time range should be
   * disjoint with all other time ranges already in the group.
   */
  public void addTimeRange(TimeRange timeRange);

  /** Returns the collection of all time ranges. */
  public Collection<TimeRange> getAllTimeRanges();

  /**
   * Checks if a time range exists in the collection. For example, if [3:00 - 4:00] is in the
   * collection, [3:00 - 3:30] is considered to exist as a time range in the collection.
   */
  public boolean hasTimeRange(TimeRange timeRange);

  /**
   * Deletes a given time range. For example, if [3:00 - 4:00] is one of the time ranges in the
   * group, deleting [3:15 - 3:30] will result in two new time ranges: [3 - 3:15], [3:30 - 4], which
   * will replace the original [3:00 - 4:00].
   *
   * @return the newly modified collection of time ranges.
   */
  public Collection<TimeRange> deleteTimeRange(TimeRange timeRangeToDelete);
}
