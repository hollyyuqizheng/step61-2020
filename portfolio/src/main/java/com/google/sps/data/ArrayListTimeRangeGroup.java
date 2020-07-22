package com.google.sps.data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/** Models an implementation of the TimeRangeGroup model using ArrayList. */
public class ArrayListTimeRangeGroup implements TimeRangeGroup {

  // This list of all the time ranges will not be sorted within the methods
  // of this class. The caller of this class, however, can sort the list of all
  // time ranges after they obtain it from the getter method.
  public List<TimeRange> allTimeRanges;

  /**
   * Adds all the input time ranges to the list of all time ranges. Also sorts the list of all
   * ranges in the constructor.
   */
  public ArrayListTimeRangeGroup(Collection<TimeRange> timeRanges) {
    allTimeRanges = new ArrayList<TimeRange>();
    timeRanges.forEach(
        (range) -> {
          addTimeRange(range);
        });
  }

  /**
   * Adds a new time range to the list. If the time range to add overlaps with any existing time
   * range, the overlapping time ranges will be merged.
   */
  public void addTimeRange(TimeRange timeRange) {
    // If the original allTimeRanges list is empty,
    // this is the first time we add anything to the list,
    // so simply add the new time range to the list and return.
    if (allTimeRanges.isEmpty()) {
      allTimeRanges.add(timeRange);
      return;
    }

    List<TimeRange> newTimeRanges = new ArrayList<TimeRange>();

    // This variable represents the latest time range previously exmained.
    // Initially, this variable points to the time range we want to add.
    // As the for loop iterates through all the current time ranges already existing
    // in allTimeRanges, this lastExaminedTimeRange variable is the time range with which
    // any existing time range merges with, if necessary.
    TimeRange lastExaminedTimeRange = timeRange;

    for (int i = 0; i < allTimeRanges.size(); i++) {
      TimeRange currentRange = allTimeRanges.get(i);

      // If the current range is complete contained by the lastExaminedTimeRange,
      // no merging or adding needs to happen.
      if (lastExaminedTimeRange.contains(currentRange)) {
        continue;
      }

      // The case for when two time ranges need to be merged.
      // Change the lastExaminedTimeRange pointer to the new time range created
      // after the merging.
      if (lastExaminedTimeRange.overlaps(currentRange)) {
        lastExaminedTimeRange = mergeTwoTimeRanges(currentRange, lastExaminedTimeRange);
      } else {
        // This is the case that lastExaminedTimeRange and the current time range do not overlap.
        // If the current range from the original list ends after the last examined time range,
        // add the time range pointed to by lastExaminedTimeRange to the new list of all ranges, and
        // change the lastExaminedTimeRange to point to the current range.
        if (currentRange.end().isAfter(lastExaminedTimeRange.end())) {
          newTimeRanges.add(lastExaminedTimeRange);
          lastExaminedTimeRange = currentRange;
        } else {
          // If lastExamiendTimeRange is later than the current range,
          // add the current range to the new list of all ranges.
          // The lastExaminedTimeRange pointer shouldn't move.
          newTimeRanges.add(currentRange);
        }
      }

      // If current time range is the last element in the allTimeRanges list,
      // add the last examined time range to the new list.
      if (i == allTimeRanges.size() - 1) {
        newTimeRanges.add(lastExaminedTimeRange);
      }
    }

    // Finally, set the global variable allTimeRanges to this newly built list of time ranges.
    allTimeRanges = newTimeRanges;
  }

  /** Helper method for merging two time ranges. This method is public so that it can be tested. */
  public static TimeRange mergeTwoTimeRanges(TimeRange a, TimeRange b) {
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
  public Iterator<TimeRange> getAllTimeRanges() {
    // return ImmutableList.of(allTimeRanges);
    return allTimeRanges.iterator();
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

  /**
   * Delete a time range from the list. Because the list of all time ranges are always kept to be
   * pairwise disjoint, the potentially two new time ranges resulted from a deletion will not
   * overlap with any other existing time ranges.
   *
   * <p>For example, if the list contains [3:00 - 4:00] and [5:00 - 6:00], deleting [3:15 - 3:30]
   * will result in [3 - 3:15] and [3:30 - 4] as new time ranges.
   *
   * <p>Another example for deleting overlapping time ranges: if [3 - 4] and [5 - 6] are in the
   * original list, deleting [3:30 - 5:30] will result in two new ranges: [3 - 3:30] and [5:30 - 6].
   */
  public void deleteTimeRange(TimeRange timeRangeToDelete) {
    List<TimeRange> newTimeRanges = new ArrayList<TimeRange>();

    for (TimeRange currentRange : allTimeRanges) {
      if (currentRange.overlaps(timeRangeToDelete)) {
        Instant currentRangeStart = currentRange.start();
        Instant currentRangeEnd = currentRange.end();
        Instant toDeleteRangeStart = timeRangeToDelete.start();
        Instant toDeleteRangeEnd = timeRangeToDelete.end();

        // Construct one or two new time ranges after the deletion.
        if (currentRangeStart.isBefore(toDeleteRangeStart)) {
          newTimeRanges.add(TimeRange.fromStartEnd(currentRangeStart, toDeleteRangeStart));
        }

        if (currentRangeEnd.isAfter(toDeleteRangeEnd)) {
          newTimeRanges.add(TimeRange.fromStartEnd(toDeleteRangeEnd, currentRangeEnd));
        }
      } else {
        newTimeRanges.add(currentRange);
      }
    }

    allTimeRanges = newTimeRanges;
  }
}
