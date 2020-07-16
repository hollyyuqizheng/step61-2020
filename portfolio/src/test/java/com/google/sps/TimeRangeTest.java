package com.google.sps;

import com.google.sps.data.TimeRange;
import java.time.Instant;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class TimeRangeTest {

  /** Tests the helper method for contains. This tests for a time range containing points. */
  @Test
  public void testContainsHelper() {
    // Time Range:    |--------|
    // Points:    B   D     A  C
    Instant timeRangeStart = Instant.now();
    Instant timeRangeEnd = timeRangeStart.plusSeconds(1000);
    TimeRange timeRange = TimeRange.fromStartEnd(timeRangeStart, timeRangeEnd);

    Instant pointInside = timeRangeStart.plusSeconds(100);
    Assert.assertTrue(TimeRange.timeRangeContainsPoint(timeRange, pointInside));

    Instant pointOutside = timeRangeStart.minusSeconds(100);
    Assert.assertTrue(!TimeRange.timeRangeContainsPoint(timeRange, pointOutside));

    Instant pointOnEnd = timeRangeEnd;
    Assert.assertTrue(TimeRange.timeRangeContainsPoint(timeRange, pointOnEnd));

    Instant pointOnStart = timeRangeStart;
    Assert.assertTrue(TimeRange.timeRangeContainsPoint(timeRange, pointOnStart));
  }

  /** Tests for a time range containing another one. */
  @Test
  public void testContains() {
    // Time Range One: |-------|
    // Time Range Two:    |--|
    Instant timeRangeOneStart = Instant.now();
    Instant timeRangeOneEnd = timeRangeOneStart.plusSeconds(1000);
    TimeRange timeRangeOne = TimeRange.fromStartEnd(timeRangeOneStart, timeRangeOneEnd);

    Instant timeRangeTwoStart = timeRangeOneStart.plusSeconds(100);
    Instant timeRangeTwoEnd = timeRangeTwoStart.plusSeconds(200);
    TimeRange timeRangeTwo = TimeRange.fromStartEnd(timeRangeTwoStart, timeRangeTwoEnd);

    Assert.assertTrue(timeRangeOne.contains(timeRangeTwo));
    Assert.assertTrue(!timeRangeTwo.contains(timeRangeOne));
  }

  /** Tests for a time range partially overlapping another one, therefore not containing. */
  @Test
  public void testContainsPartialOverlap() {
    // Time Range One: |-------|
    // Time Range Two:       |----|
    // One does not contain Two.
    Instant timeRangeOneStart = Instant.now();
    Instant timeRangeOneEnd = timeRangeOneStart.plusSeconds(1000);
    TimeRange timeRangeOne = TimeRange.fromStartEnd(timeRangeOneStart, timeRangeOneEnd);

    Instant timeRangeTwoStart = timeRangeOneStart.plusSeconds(800);
    Instant timeRangeTwoEnd = timeRangeTwoStart.plusSeconds(500);
    TimeRange timeRangeTwo = TimeRange.fromStartEnd(timeRangeTwoStart, timeRangeTwoEnd);

    Assert.assertTrue(!timeRangeOne.contains(timeRangeTwo));
  }

  /** Tests for a time range lying on the boundary of another one. */
  @Test
  public void testContainsOnBoundary() {
    // Time Range One: |-------|
    // Time Range Two:    |----|
    Instant timeRangeOneStart = Instant.now();
    Instant timeRangeOneEnd = timeRangeOneStart.plusSeconds(1000);
    TimeRange timeRangeOne = TimeRange.fromStartEnd(timeRangeOneStart, timeRangeOneEnd);

    Instant timeRangeTwoStart = timeRangeOneStart.plusSeconds(800);
    Instant timeRangeTwoEnd = timeRangeTwoStart.plusSeconds(200);
    TimeRange timeRangeTwo = TimeRange.fromStartEnd(timeRangeTwoStart, timeRangeTwoEnd);

    Assert.assertTrue(timeRangeOne.contains(timeRangeTwo));
  }

  /** Tests for a time range containing another point that is on its boundary. */
  @Test
  public void testContainsSinglePoint() {
    // Time Range One: |-------|
    // Time Range Two:         |
    Instant timeRangeOneStart = Instant.now();
    Instant timeRangeOneEnd = timeRangeOneStart.plusSeconds(1000);
    TimeRange timeRangeOne = TimeRange.fromStartEnd(timeRangeOneStart, timeRangeOneEnd);

    // Time range two is a single instant that lies on the end of time range one.
    Instant timeRangeTwoStart = timeRangeOneStart.plusSeconds(1000);
    Instant timeRangeTwoEnd = timeRangeTwoStart;
    TimeRange timeRangeTwo = TimeRange.fromStartEnd(timeRangeTwoStart, timeRangeTwoEnd);

    Assert.assertTrue(timeRangeOne.contains(timeRangeTwo));
  }

  /** Tests for partially overlapping time ranges. */
  @Test
  public void testOverlap() {
    // Time Range One: |-------|
    // Time Range Two:     |-----|
    Instant timeRangeOneStart = Instant.now();
    Instant timeRangeOneEnd = timeRangeOneStart.plusSeconds(1000);
    TimeRange timeRangeOne = TimeRange.fromStartEnd(timeRangeOneStart, timeRangeOneEnd);

    // Time range two is a single instant that lies on the end of time range one.
    Instant timeRangeTwoStart = timeRangeOneStart.plusSeconds(800);
    Instant timeRangeTwoEnd = timeRangeTwoStart.plusSeconds(400);
    TimeRange timeRangeTwo = TimeRange.fromStartEnd(timeRangeTwoStart, timeRangeTwoEnd);

    Assert.assertTrue(timeRangeOne.overlaps(timeRangeTwo));
    Assert.assertTrue(timeRangeTwo.overlaps(timeRangeOne));
  }

  /** Tests for non-overlapping time ranges. */
  @Test
  public void testNonOverlap() {
    // Time Range One: |-------|
    // Time Range Two:           |-----|
    Instant timeRangeOneStart = Instant.now();
    Instant timeRangeOneEnd = timeRangeOneStart.plusSeconds(1000);
    TimeRange timeRangeOne = TimeRange.fromStartEnd(timeRangeOneStart, timeRangeOneEnd);

    // Time range two lies completely outside time range one.
    Instant timeRangeTwoStart = timeRangeOneStart.plusSeconds(2000);
    Instant timeRangeTwoEnd = timeRangeTwoStart.plusSeconds(200);
    TimeRange timeRangeTwo = TimeRange.fromStartEnd(timeRangeTwoStart, timeRangeTwoEnd);

    Assert.assertTrue(!timeRangeOne.overlaps(timeRangeTwo));
    Assert.assertTrue(!timeRangeTwo.overlaps(timeRangeOne));
  }

  /** Tests for time ranges overlapping on their boundary. */
  @Test
  public void testOverlapOnBoundary() {
    // Time Range One: |-------|
    // Time Range Two:         |-----|
    Instant timeRangeOneStart = Instant.now();
    Instant timeRangeOneEnd = timeRangeOneStart.plusSeconds(1000);
    TimeRange timeRangeOne = TimeRange.fromStartEnd(timeRangeOneStart, timeRangeOneEnd);

    // Time range two lies completely outside time range one.
    Instant timeRangeTwoStart = timeRangeOneStart.plusSeconds(1000);
    Instant timeRangeTwoEnd = timeRangeTwoStart.plusSeconds(200);
    TimeRange timeRangeTwo = TimeRange.fromStartEnd(timeRangeTwoStart, timeRangeTwoEnd);

    Assert.assertTrue(timeRangeOne.overlaps(timeRangeTwo));
    Assert.assertTrue(timeRangeTwo.overlaps(timeRangeOne));
  }

  /** Tests for overlapping on a time range that contains another one. */
  @Test
  public void testOverlapButActuallyContains() {
    // Time Range One: |----------|
    // Time Range Two:    |-----|
    Instant timeRangeOneStart = Instant.now();
    Instant timeRangeOneEnd = timeRangeOneStart.plusSeconds(1000);
    TimeRange timeRangeOne = TimeRange.fromStartEnd(timeRangeOneStart, timeRangeOneEnd);

    // Time range two lies completely outside time range one.
    Instant timeRangeTwoStart = timeRangeOneStart.plusSeconds(200);
    Instant timeRangeTwoEnd = timeRangeTwoStart.plusSeconds(200);
    TimeRange timeRangeTwo = TimeRange.fromStartEnd(timeRangeTwoStart, timeRangeTwoEnd);

    Assert.assertTrue(timeRangeOne.overlaps(timeRangeTwo));
    Assert.assertTrue(timeRangeTwo.overlaps(timeRangeOne));
  }
}
