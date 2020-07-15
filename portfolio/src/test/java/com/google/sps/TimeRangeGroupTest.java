package com.google.sps;

import com.google.sps.data.TimeRange;
import com.google.sps.data.TimeRangeGroup;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class TimeRangeGroupTest {

  /** Tests for deleting an entire time range. */
  @Test
  public void deleteEntireTimeRange() {
    // Time Ranges:     |---|       |---|       |---|
    // To delete:       |---|
    // Result:                      |---|       |---|
    Instant timeRangeOneStart = Instant.now();
    Instant timeRangeOneEnd = timeRangeOneStart.plusSeconds(100);
    Instant timeRangeTwoStart = timeRangeOneEnd.plusSeconds(200);
    Instant timeRangeTwoEnd = timeRangeTwoStart.plusSeconds(100);
    Instant timeRangeThreeStart = timeRangeTwoEnd.plusSeconds(200);
    Instant timeRangeThreeEnd = timeRangeThreeStart.plusSeconds(100);

    TimeRange timeRangeOne = TimeRange.fromStartEnd(timeRangeOneStart, timeRangeOneEnd);
    TimeRange timeRangeTwo = TimeRange.fromStartEnd(timeRangeTwoStart, timeRangeTwoEnd);
    TimeRange timeRangeThree = TimeRange.fromStartEnd(timeRangeThreeStart, timeRangeThreeEnd);

    TimeRangeGroup timeRangeGroup = new TimeRangeGroup();
    timeRangeGroup.addTimeRange(timeRangeOne);
    timeRangeGroup.addTimeRange(timeRangeTwo);
    timeRangeGroup.addTimeRange(timeRangeThree);

    List<TimeRange> expectedTimeRangesAfterDelete = Arrays.asList(timeRangeOne, timeRangeThree);

    List<TimeRange> actualTimeRangesAfterDelete =
        timeRangeGroup.deleteEntireTimeRange(timeRangeTwo);
    Assert.assertTrue(expectedTimeRangesAfterDelete.equals(actualTimeRangesAfterDelete));
  }

  @Test(expected = IllegalArgumentException.class)
  public void deleteEntireTimeRangeDoesNotExist() {
    // Time Ranges:  |--------|       |------|       |------|
    // To Delete:               |----|
    Instant timeRangeOneStart = Instant.now();
    Instant timeRangeOneEnd = timeRangeOneStart.plusSeconds(100);
    Instant timeRangeTwoStart = timeRangeOneEnd.plusSeconds(200);
    Instant timeRangeTwoEnd = timeRangeTwoStart.plusSeconds(100);
    Instant timeRangeThreeStart = timeRangeTwoEnd.plusSeconds(200);
    Instant timeRangeThreeEnd = timeRangeThreeStart.plusSeconds(100);

    TimeRange timeRangeOne = TimeRange.fromStartEnd(timeRangeOneStart, timeRangeOneEnd);
    TimeRange timeRangeTwo = TimeRange.fromStartEnd(timeRangeTwoStart, timeRangeTwoEnd);
    TimeRange timeRangeThree = TimeRange.fromStartEnd(timeRangeThreeStart, timeRangeThreeEnd);

    TimeRangeGroup timeRangeGroupOriginal = new TimeRangeGroup();
    timeRangeGroupOriginal.addTimeRange(timeRangeOne);
    timeRangeGroupOriginal.addTimeRange(timeRangeTwo);
    timeRangeGroupOriginal.addTimeRange(timeRangeThree);

    // This time range does not exist in the original time range group.
    Instant toDeleteStart = timeRangeOneEnd;
    Instant toDeleteEnd = timeRangeTwoStart.minusSeconds(20);
    TimeRange timeRangeToDelete = TimeRange.fromStartEnd(toDeleteStart, toDeleteEnd);
    timeRangeGroupOriginal.deleteEntireTimeRange(timeRangeToDelete);
  }

  /** Tests for deleting the middle part of a time range. */
  @Test
  public void deletePartofTimeRange() {
    // Time Ranges:  |--------|       |------|       |------|
    // To Delete:      |---|
    // Result:       |-|   |--|       |------|       |------|
    Instant timeRangeOneStart = Instant.now();
    Instant timeRangeOneEnd = timeRangeOneStart.plusSeconds(100);
    Instant timeRangeTwoStart = timeRangeOneEnd.plusSeconds(200);
    Instant timeRangeTwoEnd = timeRangeTwoStart.plusSeconds(100);
    Instant timeRangeThreeStart = timeRangeTwoEnd.plusSeconds(200);
    Instant timeRangeThreeEnd = timeRangeThreeStart.plusSeconds(100);

    TimeRange timeRangeOne = TimeRange.fromStartEnd(timeRangeOneStart, timeRangeOneEnd);
    TimeRange timeRangeTwo = TimeRange.fromStartEnd(timeRangeTwoStart, timeRangeTwoEnd);
    TimeRange timeRangeThree = TimeRange.fromStartEnd(timeRangeThreeStart, timeRangeThreeEnd);

    TimeRangeGroup timeRangeGroupOriginal = new TimeRangeGroup();
    timeRangeGroupOriginal.addTimeRange(timeRangeOne);
    timeRangeGroupOriginal.addTimeRange(timeRangeTwo);
    timeRangeGroupOriginal.addTimeRange(timeRangeThree);

    // Deletes part of the time range one.
    Instant toDeleteStart = timeRangeOneStart.plusSeconds(20);
    Instant toDeleteEnd = timeRangeOneStart.plusSeconds(60);
    TimeRange timeRangeToDelete = TimeRange.fromStartEnd(toDeleteStart, toDeleteEnd);

    // These are the expected newly created time ranges after the deletion.
    TimeRange timeRangeNewOne =
        TimeRange.fromStartEnd(timeRangeOneStart, timeRangeToDelete.start());
    TimeRange timeRangeNewTwo = TimeRange.fromStartEnd(timeRangeToDelete.end(), timeRangeOne.end());

    List<TimeRange> expectedTimeRangesAfterDelete =
        Arrays.asList(timeRangeNewOne, timeRangeNewTwo, timeRangeTwo, timeRangeThree);

    List<TimeRange> actualTimeRangesAfterDelete =
        timeRangeGroupOriginal.deletePartOfTimeRange(timeRangeOne, timeRangeToDelete);

    Assert.assertEquals(expectedTimeRangesAfterDelete, actualTimeRangesAfterDelete);
  }

  /**
   * Tests for deleting the second half of a time range. This should not throw the invalid input
   * exception.
   */
  @Test
  public void deletePartOfTimeRangeOverlappingBoundary() {
    // Time Ranges:  |--------|       |------|       |------|
    // To Delete:      |------|
    // Result:       |-|              |------|       |------|
    Instant timeRangeOneStart = Instant.now();
    Instant timeRangeOneEnd = timeRangeOneStart.plusSeconds(100);
    Instant timeRangeTwoStart = timeRangeOneEnd.plusSeconds(200);
    Instant timeRangeTwoEnd = timeRangeTwoStart.plusSeconds(100);
    Instant timeRangeThreeStart = timeRangeTwoEnd.plusSeconds(200);
    Instant timeRangeThreeEnd = timeRangeThreeStart.plusSeconds(100);

    TimeRange timeRangeOne = TimeRange.fromStartEnd(timeRangeOneStart, timeRangeOneEnd);
    TimeRange timeRangeTwo = TimeRange.fromStartEnd(timeRangeTwoStart, timeRangeTwoEnd);
    TimeRange timeRangeThree = TimeRange.fromStartEnd(timeRangeThreeStart, timeRangeThreeEnd);

    TimeRangeGroup timeRangeGroupOriginal = new TimeRangeGroup();
    timeRangeGroupOriginal.addTimeRange(timeRangeOne);
    timeRangeGroupOriginal.addTimeRange(timeRangeTwo);
    timeRangeGroupOriginal.addTimeRange(timeRangeThree);

    // Deletes part of the time range one.
    Instant toDeleteStart = timeRangeOneStart.plusSeconds(20);
    Instant toDeleteEnd = timeRangeOneEnd;
    TimeRange timeRangeToDelete = TimeRange.fromStartEnd(toDeleteStart, toDeleteEnd);

    // This the expected newly created time range after the deletion.
    TimeRange timeRangeNewOne =
        TimeRange.fromStartEnd(timeRangeOneStart, timeRangeToDelete.start());

    List<TimeRange> expectedTimeRangesAfterDelete =
        Arrays.asList(timeRangeNewOne, timeRangeTwo, timeRangeThree);

    List<TimeRange> actualTimeRangesAfterDelete =
        timeRangeGroupOriginal.deletePartOfTimeRange(timeRangeOne, timeRangeToDelete);

    Assert.assertEquals(expectedTimeRangesAfterDelete, actualTimeRangesAfterDelete);
  }

  /**
   * This scenario calls the method to delete only part of a time range, but the range to delete is
   * actually the entire original time range. This should not throw exceptions, and the result
   * should be same if deleteEntireTimeRange was called.
   */
  @Test
  public void deletePartOfTimeRangeButIsEntireRange() {
    // Time Ranges:  |--------|       |------|       |------|
    // To Delete:    |--------|
    // Result:                        |------|       |------|
    Instant timeRangeOneStart = Instant.now();
    Instant timeRangeOneEnd = timeRangeOneStart.plusSeconds(100);
    Instant timeRangeTwoStart = timeRangeOneEnd.plusSeconds(200);
    Instant timeRangeTwoEnd = timeRangeTwoStart.plusSeconds(100);
    Instant timeRangeThreeStart = timeRangeTwoEnd.plusSeconds(200);
    Instant timeRangeThreeEnd = timeRangeThreeStart.plusSeconds(100);

    TimeRange timeRangeOne = TimeRange.fromStartEnd(timeRangeOneStart, timeRangeOneEnd);
    TimeRange timeRangeTwo = TimeRange.fromStartEnd(timeRangeTwoStart, timeRangeTwoEnd);
    TimeRange timeRangeThree = TimeRange.fromStartEnd(timeRangeThreeStart, timeRangeThreeEnd);

    TimeRangeGroup timeRangeGroupOriginal = new TimeRangeGroup();
    timeRangeGroupOriginal.addTimeRange(timeRangeOne);
    timeRangeGroupOriginal.addTimeRange(timeRangeTwo);
    timeRangeGroupOriginal.addTimeRange(timeRangeThree);

    // Deletes the entire time range one, but using deletePartOfTimeRange method.
    Instant toDeleteStart = timeRangeOneStart;
    Instant toDeleteEnd = timeRangeOneEnd;
    TimeRange timeRangeToDelete = TimeRange.fromStartEnd(toDeleteStart, toDeleteEnd);

    List<TimeRange> expectedTimeRangesAfterDelete = Arrays.asList(timeRangeTwo, timeRangeThree);
    List<TimeRange> actualTimeRangesAfterDelete =
        timeRangeGroupOriginal.deletePartOfTimeRange(timeRangeOne, timeRangeToDelete);

    Assert.assertEquals(expectedTimeRangesAfterDelete, actualTimeRangesAfterDelete);
  }

  /**
   * Tests deleting from a time range but with the wrong boundary. The target time range to delete
   * does not lie completely within the original range.
   */
  @Test(expected = IllegalArgumentException.class)
  public void deletePartOfTimeRangeWrongBoundary() {
    // Time Ranges:  |--------|       |------|       |------|
    // To Delete:       |-------|
    Instant timeRangeOneStart = Instant.now();
    Instant timeRangeOneEnd = timeRangeOneStart.plusSeconds(100);
    Instant timeRangeTwoStart = timeRangeOneEnd.plusSeconds(200);
    Instant timeRangeTwoEnd = timeRangeTwoStart.plusSeconds(100);
    Instant timeRangeThreeStart = timeRangeTwoEnd.plusSeconds(200);
    Instant timeRangeThreeEnd = timeRangeThreeStart.plusSeconds(100);

    TimeRange timeRangeOne = TimeRange.fromStartEnd(timeRangeOneStart, timeRangeOneEnd);
    TimeRange timeRangeTwo = TimeRange.fromStartEnd(timeRangeTwoStart, timeRangeTwoEnd);
    TimeRange timeRangeThree = TimeRange.fromStartEnd(timeRangeThreeStart, timeRangeThreeEnd);

    TimeRangeGroup timeRangeGroupOriginal = new TimeRangeGroup();
    timeRangeGroupOriginal.addTimeRange(timeRangeOne);
    timeRangeGroupOriginal.addTimeRange(timeRangeTwo);
    timeRangeGroupOriginal.addTimeRange(timeRangeThree);

    // Deletes part of the time range one but with wrong boundary.
    Instant toDeleteStart = timeRangeOneStart.plusSeconds(20);
    Instant toDeleteEnd = timeRangeOneStart.plusSeconds(120);
    TimeRange timeRangeToDelete = TimeRange.fromStartEnd(toDeleteStart, toDeleteEnd);
    timeRangeGroupOriginal.deletePartOfTimeRange(timeRangeOne, timeRangeToDelete);
  }
}
