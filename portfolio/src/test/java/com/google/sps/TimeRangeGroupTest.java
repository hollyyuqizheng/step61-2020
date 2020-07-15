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

  /** Tests deleting and modifying a free time range. */
  @Test
  public void deleteAndModifyTimeRange() {
    // Possible: |---------------------------|
    // Free:     |---|       |---|       |---|
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
    List<TimeRange> actualTimeRangesAfterDelete = timeRangeGroup.deleteTimeRange(timeRangeTwo);
    Assert.assertEquals(expectedTimeRangesAfterDelete, actualTimeRangesAfterDelete);

    // Creates a new time range that is 10 seconds shorter than the original time range.
    Instant timeRangeOneEndNew = timeRangeOneEnd.minusSeconds(10);
    TimeRange timeRangeOneNew = TimeRange.fromStartEnd(timeRangeOneStart, timeRangeOneEndNew);

    List<TimeRange> timeRangesAfterModify =
        timeRangeGroup.modifyTimeRange(
            /* originalFreeTimeRange= */ timeRangeOne, /* newFreeTimeRange= */ timeRangeOneNew);
    Assert.assertEquals(timeRangesAfterModify.get(0).end(), timeRangeOneNew.end());
  }

  /**
   * Tests updating an existing free time into a time range that is not part of the original time
   * range.
   */
  @Test(expected = IllegalArgumentException.class)
  public void modifyTimeRangeWrongBoundary() {
    // Possible: |---------------------------|
    // Free:     |---|       |---|       |---|
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

    // Creates a new time range that is 10 seconds longer than the original time range,
    // which makes this modification not valid.
    Instant timeRangeOneEndNew = timeRangeOneEnd.plusSeconds(10);
    TimeRange timeRangeOneNew = TimeRange.fromStartEnd(timeRangeOneStart, timeRangeOneEndNew);

    List<TimeRange> timeRangesAfterModify =
        timeRangeGroup.modifyTimeRange(
            /* originalFreeTimeRange= */ timeRangeOne, /* newFreeTimeRange= */ timeRangeOneNew);
  }
}
