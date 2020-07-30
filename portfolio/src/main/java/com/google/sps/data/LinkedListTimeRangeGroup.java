package com.google.sps.data;

import java.time.Instant;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Models an implementation of the TimeRangeGroup model using LinkedList. This is a slightly
 * modified version of what hollyyuqizheng wrote for the ArrayList implementation.
 */
public class LinkedListTimeRangeGroup implements TimeRangeGroup {

  // This list of all the time ranges will be sorted by start time ascending
  // in add and delete methods. The time ranges stored in this list are
  // pair-wise disjoint at any moment.
  public List<TimeRange> allTimeRanges;

  /**
   * Adds all the input time ranges to the list of all time ranges. Also sorts the list of all
   * ranges in the constructor.
   */
  public LinkedListTimeRangeGroup(Collection<TimeRange> timeRanges) {
    allTimeRanges = new LinkedList<TimeRange>();
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
    ListIterator<TimeRange> iterator = allTimeRanges.listIterator();

    // This variable represents the latest time range previously exmamined.
    // Initially, this variable points to the time range we want to add.
    // As the for loop iterates through all the current time ranges already existing
    // in allTimeRanges, this lastExaminedTimeRange variable is the time range with which
    // any existing time range merges with, if necessary.
    TimeRange lastExaminedTimeRange = timeRange;

    while (iterator.hasNext()) {
      TimeRange currentRange = iterator.next();

      // If the current range is completely contained by the lastExaminedTimeRange,
      // we need to remove the current range from the list as lastExaminedTimeRange
      // will eventually replace it.
      if (lastExaminedTimeRange.contains(currentRange)) {
        iterator.remove();
        continue;
      }

      // The case for when two time ranges need to be merged.
      // Similar to the case above, it will eventually be replaced so we can
      // remove currentRange
      if (lastExaminedTimeRange.overlaps(currentRange)) {
        lastExaminedTimeRange = mergeTwoTimeRanges(currentRange, lastExaminedTimeRange);
        iterator.remove();
      } else if (currentRange.end().isAfter(lastExaminedTimeRange.end())) {
        // This is the case that lastExaminedTimeRange and the current time range do not overlap.
        // If the current range from the original list ends after the last examined time range,
        // add the time range pointed to by lastExaminedTimeRange to the new list before the current
        // range,and change the lastExaminedTimeRange to point to the current range.
        iterator.previous();
        iterator.add(lastExaminedTimeRange);
        iterator.next();
        lastExaminedTimeRange = currentRange;
      }

      // If current time range is the last element in the allTimeRanges list,
      // then there are two cases: if we just updated our pointer and are at
      // the same element then there is no need to add it. However, if
      // lastExaminedTimeRange does not match, this means lastExaminedTimeRange
      // actually belongs at the end of the new list.
      if (!iterator.hasNext()) {
        if (!currentRange.equals(lastExaminedTimeRange)) {
          iterator.add(lastExaminedTimeRange);
        }
        // Once we, potentially, add the last element we should end the
        // loop because it is the final range in the process.
        break;
      }
    }
  }

  /**
   * Helper method for merging two time ranges. This method is package-private so that it can be
   * tested.
   */
  static TimeRange mergeTwoTimeRanges(TimeRange a, TimeRange b) {
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
  public Iterator<TimeRange> getAllTimeRangesIterator() {
    return allTimeRanges.iterator();
  }

  /**
   * Checks if a time range exists in the collection. For example, if [3:00 - 4:00] is in the
   * collection, [3:00 - 3:30] is considered to exist as a time range in the collection. This method
   * uses linear search to find the time ranges whose start time is before the target range's start
   * and whose end time is after the target range's end. Then the method calls contains to see if
   * the target range is contained within this current range.
   */
  public boolean hasTimeRange(TimeRange timeRangeToCheck) {

    for (TimeRange currentRange : allTimeRanges) {
      if (currentRange.contains(timeRangeToCheck)) {
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

    ListIterator<TimeRange> iterator = allTimeRanges.listIterator();

    while (iterator.hasNext()) {
      TimeRange currentRange = iterator.next();
      if (currentRange.overlaps(timeRangeToDelete)) {
        Instant currentRangeStart = currentRange.start();
        Instant currentRangeEnd = currentRange.end();
        Instant toDeleteRangeStart = timeRangeToDelete.start();
        Instant toDeleteRangeEnd = timeRangeToDelete.end();

        // If currentRange overlaps then it is about to be modified so we
        // remove it and later add the fixed versions.
        iterator.remove();

        // Construct one or two new time ranges after the deletion.
        if (currentRangeStart.isBefore(toDeleteRangeStart)) {
          iterator.add(TimeRange.fromStartEnd(currentRangeStart, toDeleteRangeStart));
        }

        if (currentRangeEnd.isAfter(toDeleteRangeEnd)) {
          iterator.add(TimeRange.fromStartEnd(toDeleteRangeEnd, currentRangeEnd));
        }
      }
    }
  }
}
