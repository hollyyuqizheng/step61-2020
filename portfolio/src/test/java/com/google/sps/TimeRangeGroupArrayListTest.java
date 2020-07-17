package com.google.sps;

import com.google.sps.data.TimeRange;
import com.google.sps.data.TimeRangeGroupArrayList;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class TimeRangeGroupArrayListTest {

  /** Tests for the method that checks if a time range exists in the group. */
  @Test
  public void testHasTimeRange() {
    // Time Ranges: |--A----|   |---B---|
    // To check:      |-C-|   |---D---|   |--E--|
    // C exists; D and E both don't exist in the group
    Instant timeRangeOneStart = Instant.now();
    Instant timeRangeOneEnd = timeRangeOneStart.plusSeconds(1000);
    Instant timeRangeTwoStart = timeRangeOneEnd.plusSeconds(500);
    Instant timeRangeTwoEnd = timeRangeTwoStart.plusSeconds(1000);

    TimeRange timeRangeOne = TimeRange.fromStartEnd(timeRangeOneStart, timeRangeOneEnd);
    TimeRange timeRangeTwo = TimeRange.fromStartEnd(timeRangeTwoStart, timeRangeTwoEnd);

    List<TimeRange> timeRanges = Arrays.asList(timeRangeOne, timeRangeTwo);
    TimeRangeGroupArrayList timeRangeGroup = new TimeRangeGroupArrayList(timeRanges);

    Instant timeRangeThreeStart = timeRangeOneStart.plusSeconds(200);
    Instant timeRangeThreeEnd = timeRangeThreeStart.plusSeconds(200);
    TimeRange timeRangeThree = TimeRange.fromStartEnd(timeRangeThreeStart, timeRangeThreeEnd);

    Instant timeRangeFourStart = timeRangeOneStart.plusSeconds(1200);
    Instant timeRangeFourEnd = timeRangeFourStart.plusSeconds(500);
    TimeRange timeRangeFour = TimeRange.fromStartEnd(timeRangeFourStart, timeRangeFourEnd);

    Instant timeRangeFiveStart = timeRangeTwoEnd.plusSeconds(200);
    Instant timeRangeFiveEnd = timeRangeFiveStart.plusSeconds(200);
    TimeRange timeRangeFive = TimeRange.fromStartEnd(timeRangeFiveStart, timeRangeFiveEnd);

    Assert.assertTrue(timeRangeGroup.hasTimeRange(timeRangeThree));
    Assert.assertFalse(timeRangeGroup.hasTimeRange(timeRangeFour));
    Assert.assertFalse(timeRangeGroup.hasTimeRange(timeRangeFive));
  }

  /** Adding a non-disjoint new time range should throw an exception. */
  @Test(expected = IllegalArgumentException.class)
  public void testAddInvalidTimeRange() {
    // Time Ranges: |--A----|   |---B---|
    // To add:          |--C--|
    Instant timeRangeOneStart = Instant.now();
    Instant timeRangeOneEnd = timeRangeOneStart.plusSeconds(1000);
    Instant timeRangeTwoStart = timeRangeOneEnd.plusSeconds(500);
    Instant timeRangeTwoEnd = timeRangeTwoStart.plusSeconds(1000);

    TimeRange timeRangeOne = TimeRange.fromStartEnd(timeRangeOneStart, timeRangeOneEnd);
    TimeRange timeRangeTwo = TimeRange.fromStartEnd(timeRangeTwoStart, timeRangeTwoEnd);

    List<TimeRange> timeRanges = Arrays.asList(timeRangeOne, timeRangeTwo);
    TimeRangeGroupArrayList timeRangeGroup = new TimeRangeGroupArrayList(timeRanges);

    TimeRange timeRangeThree =
        TimeRange.fromStartEnd(
            timeRangeOneStart.plusSeconds(500), timeRangeOneStart.plusSeconds(600));

    timeRangeGroup.addTimeRange(timeRangeThree);
  }

  /** Tests for deleting part of a time range. */
  @Test
  public void testDelete() {
    // Time Ranges: |-----A-----|   |---B---|
    // To delete:       |--C--|
    // Results:     |---|     |-|   |-------|
    Instant timeRangeOneStart = Instant.now();
    Instant timeRangeOneEnd = timeRangeOneStart.plusSeconds(1000);
    Instant timeRangeTwoStart = timeRangeOneEnd.plusSeconds(500);
    Instant timeRangeTwoEnd = timeRangeTwoStart.plusSeconds(1000);

    TimeRange timeRangeOne = TimeRange.fromStartEnd(timeRangeOneStart, timeRangeOneEnd);
    TimeRange timeRangeTwo = TimeRange.fromStartEnd(timeRangeTwoStart, timeRangeTwoEnd);

    List<TimeRange> timeRanges = Arrays.asList(timeRangeOne, timeRangeTwo);
    TimeRangeGroupArrayList timeRangeGroup = new TimeRangeGroupArrayList(timeRanges);

    TimeRange timeRangeToDelete =
        TimeRange.fromStartEnd(
            timeRangeOneStart.plusSeconds(300), timeRangeOneStart.plusSeconds(600));

    TimeRange timeRangeNewOne =
        TimeRange.fromStartEnd(timeRangeOneStart, timeRangeOneStart.plusSeconds(300));
    TimeRange timeRangeNewTwo =
        TimeRange.fromStartEnd(timeRangeOneStart.plusSeconds(600), timeRangeOneEnd);
    List<TimeRange> expectedTimeRangesAfterDelete =
        Arrays.asList(timeRangeNewOne, timeRangeNewTwo, timeRangeTwo);
    List<TimeRange> actualTimeRangesAfterDelete = timeRangeGroup.deleteTimeRange(timeRangeToDelete);

    Assert.assertEquals(expectedTimeRangesAfterDelete, actualTimeRangesAfterDelete);
  }

  /** Tests for deleting part of a time range. */
  @Test
  public void testDeleteEntireTimeRange() {
    // Time Ranges: |---A---|   |---B---|
    // To delete:   |---C---|
    // Results:                 |-------|
    Instant timeRangeOneStart = Instant.now();
    Instant timeRangeOneEnd = timeRangeOneStart.plusSeconds(1000);
    Instant timeRangeTwoStart = timeRangeOneEnd.plusSeconds(500);
    Instant timeRangeTwoEnd = timeRangeTwoStart.plusSeconds(1000);

    TimeRange timeRangeOne = TimeRange.fromStartEnd(timeRangeOneStart, timeRangeOneEnd);
    TimeRange timeRangeTwo = TimeRange.fromStartEnd(timeRangeTwoStart, timeRangeTwoEnd);

    List<TimeRange> timeRanges = Arrays.asList(timeRangeOne, timeRangeTwo);
    TimeRangeGroupArrayList timeRangeGroup = new TimeRangeGroupArrayList(timeRanges);

    TimeRange timeRangeToDelete =
        TimeRange.fromStartEnd(timeRangeOneStart, timeRangeOneStart.plusSeconds(1000));

    List<TimeRange> expectedTimeRangesAfterDelete = Arrays.asList(timeRangeTwo);
    List<TimeRange> actualTimeRangesAfterDelete = timeRangeGroup.deleteTimeRange(timeRangeToDelete);

    Assert.assertEquals(expectedTimeRangesAfterDelete, actualTimeRangesAfterDelete);
  }

  /**
   * Deleting a time range that is not entirely contained by one of the existing ranges should throw
   * an exception.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testDeleteInvalidTimeRange() {
    // Time Ranges: |-----A-----|   |---B---|
    // To delete:          |--C--|
    Instant timeRangeOneStart = Instant.now();
    Instant timeRangeOneEnd = timeRangeOneStart.plusSeconds(1000);
    Instant timeRangeTwoStart = timeRangeOneEnd.plusSeconds(500);
    Instant timeRangeTwoEnd = timeRangeTwoStart.plusSeconds(1000);

    TimeRange timeRangeOne = TimeRange.fromStartEnd(timeRangeOneStart, timeRangeOneEnd);
    TimeRange timeRangeTwo = TimeRange.fromStartEnd(timeRangeTwoStart, timeRangeTwoEnd);

    List<TimeRange> timeRanges = Arrays.asList(timeRangeOne, timeRangeTwo);
    TimeRangeGroupArrayList timeRangeGroup = new TimeRangeGroupArrayList(timeRanges);

    TimeRange timeRangeToDelete =
        TimeRange.fromStartEnd(
            timeRangeOneStart.plusSeconds(500), timeRangeOneStart.plusSeconds(1100));

    timeRangeGroup.deleteTimeRange(timeRangeToDelete);
  }
}
