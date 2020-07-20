package com.google.sps.data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** Models an implementation of the TimeRangeGroup model using ArrayList. */
public class TimeRangeGroupArrayList {

  public List<TimeRange> allTimeRanges;
  private Comparator<TimeRange> comparator;
  private boolean ascending;

  /**
   * Adds all the input time ranges to the list of all time ranges. Also sorts the list of all
   * ranges in the constructor.
   */
  public TimeRangeGroupArrayList(
      Collection<TimeRange> timeRanges, Comparator<TimeRange> comparator, boolean ascending) {
    allTimeRanges = new ArrayList<TimeRange>();
    timeRanges.forEach(
        (range) -> {
          addTimeRange(range);
        });
    this.comparator = comparator;
    this.ascending = ascending;
    sortTimeRanges(comparator, ascending);
  }

  /**
   * Adds a new time range to the list. This new range must be disjoint from all existing ranges.
   * TODO(hollyyuqizheng): Depending on how the algorithms are implemented, we might need to add the
   * merging logic in this method. For example, if a time range is deleted but then later added back
   * to the list for some reason, this list will potentially need to be merged.
   */
  public void addTimeRange(TimeRange timeRange) {
    for (TimeRange currentRange : allTimeRanges) {
      if (currentRange.overlaps(timeRange)) {
        throw new IllegalArgumentException(
            "New time range to add cannot overlap with any existing time ranges");
      }
    }
    allTimeRanges.add(timeRange);
  }

  /** Returns the array list of all time ranges. */
  public List<TimeRange> getAllTimeRanges() {
    sortTimeRanges(comparator, ascending);
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

  /**
   * Delete a time range from the list. Because the list of all time ranges are always kept to be
   * pairwise disjoint, the potentially two new time ranges resulted from a deletion will not
   * overlap with any other existing time ranges.
   *
   * <p>For example, if the list contains [3:00 - 4:00] and [5:00 - 6:00], deleting [3:15 - 3:30]
   * will result in [3 - 3:15] and [3:30 - 4] as new time ranges. Trying to delete [4 - 4:15] will
   * result in an invalid input exception.
   *
   * @return the newly modified collection of time ranges.
   */
  public List<TimeRange> deleteTimeRange(TimeRange timeRangeToDelete) {
    for (TimeRange currentRange : allTimeRanges) {
      if (currentRange.contains(timeRangeToDelete)) {
        Instant currentRangeStart = currentRange.start();
        Instant currentRangeEnd = currentRange.end();
        Instant toDeleteRangeStart = timeRangeToDelete.start();
        Instant toDeleteRangeEnd = timeRangeToDelete.end();

        allTimeRanges.remove(currentRange);

        // Construct one or two new time ranges after the deletion.
        if (currentRangeStart.isBefore(toDeleteRangeStart)) {
          allTimeRanges.add(TimeRange.fromStartEnd(currentRangeStart, toDeleteRangeStart));
        }

        if (currentRangeEnd.isAfter(toDeleteRangeEnd)) {
          allTimeRanges.add(TimeRange.fromStartEnd(toDeleteRangeEnd, currentRangeEnd));
        }
        // Collections.sort(allTimeRanges, TimeRange.sortByTimeRangeStartTimeAscending);
        sortTimeRanges(comparator, ascending);
        return allTimeRanges;
      }
    }

    // If at this point the method hasn't returned yet, it means none of the existing
    // time ranges actually contains the range to delete.
    // Throw an invalid input exception here.
    throw new IllegalArgumentException(
        "The time range to delete must be contained by an existing time range");
  }

  /**
   * Sorts the list of time ranges based on given comparator and whether to sort ascending or
   * descending.
   */
  public void sortTimeRanges(Comparator<TimeRange> comparator, boolean ascending) {
    if (ascending) {
      Collections.sort(allTimeRanges, comparator);
    } else {
      Collections.sort(allTimeRanges, Collections.reverseOrder(comparator));
    }
  }
}
