package com.google.sps.data;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class LinkedListTimeRangeGroupTest {

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
    LinkedListTimeRangeGroup timeRangeGroup = new LinkedListTimeRangeGroup(timeRanges);

    Instant timeRangeThreeStart = timeRangeOneStart.plusSeconds(200);
    Instant timeRangeThreeEnd = timeRangeThreeStart.plusSeconds(200);
    TimeRange timeRangeThree = TimeRange.fromStartEnd(timeRangeThreeStart, timeRangeThreeEnd);

    Instant timeRangeFourStart = timeRangeOneStart.plusSeconds(1200);
    Instant timeRangeFourEnd = timeRangeFourStart.plusSeconds(500);
    TimeRange timeRangeFour = TimeRange.fromStartEnd(timeRangeFourStart, timeRangeFourEnd);

    Instant timeRangeFiveStart = timeRangeTwoEnd.plusSeconds(200);
    Instant timeRangeFiveEnd = timeRangeFiveStart.plusSeconds(200);
    TimeRange timeRangeFive = TimeRange.fromStartEnd(timeRangeFiveStart, timeRangeFiveEnd);

    Assert.assertTrue(timeRangeGroup.hasTimeRange(timeRangeOne));
    Assert.assertTrue(timeRangeGroup.hasTimeRange(timeRangeTwo));
    Assert.assertTrue(timeRangeGroup.hasTimeRange(timeRangeThree));
    Assert.assertFalse(timeRangeGroup.hasTimeRange(timeRangeFour));
    Assert.assertFalse(timeRangeGroup.hasTimeRange(timeRangeFive));
  }

  /** Tests for merging two overlapping ranges. */
  @Test
  public void testMergeTwoRanges() {
    // Time Ranges: |-----A-----|
    //                |-----B-----|
    // Result:     |--------------|
    Instant timeRangeOneStart = Instant.now();
    Instant timeRangeOneEnd = timeRangeOneStart.plusSeconds(1000);
    Instant timeRangeTwoStart = timeRangeOneStart.plusSeconds(500);
    Instant timeRangeTwoEnd = timeRangeTwoStart.plusSeconds(1000);
    TimeRange timeRangeOne = TimeRange.fromStartEnd(timeRangeOneStart, timeRangeOneEnd);
    TimeRange timeRangeTwo = TimeRange.fromStartEnd(timeRangeTwoStart, timeRangeTwoEnd);

    Instant timeRangeNewStart = timeRangeOneStart;
    Instant timeRangeNewEnd = timeRangeTwoEnd;
    TimeRange expected = TimeRange.fromStartEnd(timeRangeNewStart, timeRangeNewEnd);

    TimeRange actual = ArrayListTimeRangeGroup.mergeTwoTimeRanges(timeRangeOne, timeRangeTwo);
    Assert.assertEquals(expected, actual);
  }

  /** Tests for merging two overlapping ranges. */
  @Test
  public void testMergeTwoRangesContinuous() {
    // Time Ranges: |-----A-----|
    //                         |-----B-----|
    // Result:     |-----------------------|
    Instant timeRangeOneStart = Instant.now();
    Instant timeRangeOneEnd = timeRangeOneStart.plusSeconds(1000);
    Instant timeRangeTwoStart = timeRangeOneEnd;
    Instant timeRangeTwoEnd = timeRangeTwoStart.plusSeconds(1000);
    TimeRange timeRangeOne = TimeRange.fromStartEnd(timeRangeOneStart, timeRangeOneEnd);
    TimeRange timeRangeTwo = TimeRange.fromStartEnd(timeRangeTwoStart, timeRangeTwoEnd);

    Instant timeRangeNewStart = timeRangeOneStart;
    Instant timeRangeNewEnd = timeRangeTwoEnd;
    TimeRange expected = TimeRange.fromStartEnd(timeRangeNewStart, timeRangeNewEnd);

    TimeRange actual = ArrayListTimeRangeGroup.mergeTwoTimeRanges(timeRangeOne, timeRangeTwo);
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void testAddNonOverlappingRange() {
    // Time Ranges:  |-----A-----|                |----C----|
    // To add:                     |-----B-----|
    // Result:       |----A------| |-----B-----|  |----C----|
    Instant timeRangeOneStart = Instant.now();
    Instant timeRangeOneEnd = timeRangeOneStart.plusSeconds(1000);
    Instant timeRangeTwoStart = timeRangeOneEnd.plusSeconds(3000);
    Instant timeRangeTwoEnd = timeRangeTwoStart.plusSeconds(1000);

    TimeRange timeRangeOne = TimeRange.fromStartEnd(timeRangeOneStart, timeRangeOneEnd);
    TimeRange timeRangeTwo = TimeRange.fromStartEnd(timeRangeTwoStart, timeRangeTwoEnd);

    List<TimeRange> originalTimeRanges = Arrays.asList(timeRangeOne, timeRangeTwo);
    LinkedListTimeRangeGroup timeRangeGroup = new LinkedListTimeRangeGroup(originalTimeRanges);

    Instant timeRangeToAddStart = timeRangeOneEnd.plusSeconds(500);
    Instant timeRangeToAddEnd = timeRangeToAddStart.plusSeconds(1000);
    TimeRange timeRangeToAdd = TimeRange.fromStartEnd(timeRangeToAddStart, timeRangeToAddEnd);

    Instant timeRangeCombinedStart = timeRangeToAddStart;
    Instant timeRangeCombinedEnd = timeRangeToAddEnd;
    TimeRange timeRangeNew = TimeRange.fromStartEnd(timeRangeCombinedStart, timeRangeCombinedEnd);
    List<TimeRange> expected = Arrays.asList(timeRangeOne, timeRangeNew, timeRangeTwo);

    timeRangeGroup.addTimeRange(timeRangeToAdd);
    Iterator<TimeRange> actualIterator = timeRangeGroup.getAllTimeRangesIterator();

    List<TimeRange> actual = new LinkedList();
    while (actualIterator.hasNext()) {
      actual.add(actualIterator.next());
    }

    Collections.sort(actual, TimeRange.SORT_BY_TIME_RANGE_DURATION_ASCENDING_THEN_START_TIME);
    Assert.assertEquals(expected, actual);
  }

  /** Tests for adding a time range that overlaps another existing one. */
  @Test
  public void testAddOverlappingRange() {
    // Time Ranges:  |-----A-----|     |----C----|
    // To add:          |-----B-----|
    // Result:       |----A&B-------|  |----C----|
    // After sorting:|----C---|    |-----A&B-----|
    Instant timeRangeOneStart = Instant.now();
    Instant timeRangeOneEnd = timeRangeOneStart.plusSeconds(1000);
    Instant timeRangeTwoStart = timeRangeOneEnd.plusSeconds(3000);
    Instant timeRangeTwoEnd = timeRangeTwoStart.plusSeconds(1000);

    TimeRange timeRangeOne = TimeRange.fromStartEnd(timeRangeOneStart, timeRangeOneEnd);
    TimeRange timeRangeTwo = TimeRange.fromStartEnd(timeRangeTwoStart, timeRangeTwoEnd);

    List<TimeRange> originalTimeRanges = Arrays.asList(timeRangeOne, timeRangeTwo);
    LinkedListTimeRangeGroup timeRangeGroup = new LinkedListTimeRangeGroup(originalTimeRanges);

    Instant timeRangeToAddStart = timeRangeOneStart.plusSeconds(500);
    Instant timeRangeToAddEnd = timeRangeToAddStart.plusSeconds(1000);
    TimeRange timeRangeToAdd = TimeRange.fromStartEnd(timeRangeToAddStart, timeRangeToAddEnd);

    // Constructs the expected merged time range for combining A and B.
    Instant timeRangeCombinedStart = timeRangeOneStart;
    Instant timeRangeCombinedEnd = timeRangeToAddEnd;
    TimeRange timeRangeNew = TimeRange.fromStartEnd(timeRangeCombinedStart, timeRangeCombinedEnd);
    List<TimeRange> expected = Arrays.asList(timeRangeTwo, timeRangeNew);

    timeRangeGroup.addTimeRange(timeRangeToAdd);
    Iterator<TimeRange> actualIterator = timeRangeGroup.getAllTimeRangesIterator();

    List<TimeRange> actual = new LinkedList();
    while (actualIterator.hasNext()) {
      actual.add(actualIterator.next());
    }

    Collections.sort(actual, TimeRange.SORT_BY_TIME_RANGE_DURATION_ASCENDING_THEN_START_TIME);
    Assert.assertEquals(expected, actual);
  }

  /** Tests for adding a time range that overlaps another existing one. */
  @Test
  public void testAddOverlappingRangeTwo() {
    // Time Ranges: |-----A-----|     |----C----|
    // To add:                            |-----B-----|
    // Result:      |-----------|     |---------------|
    Instant timeRangeOneStart = Instant.now();
    Instant timeRangeOneEnd = timeRangeOneStart.plusSeconds(1000);
    Instant timeRangeTwoStart = timeRangeOneEnd.plusSeconds(3000);
    Instant timeRangeTwoEnd = timeRangeTwoStart.plusSeconds(1000);

    TimeRange timeRangeOne = TimeRange.fromStartEnd(timeRangeOneStart, timeRangeOneEnd);
    TimeRange timeRangeTwo = TimeRange.fromStartEnd(timeRangeTwoStart, timeRangeTwoEnd);

    List<TimeRange> originalTimeRanges = Arrays.asList(timeRangeTwo, timeRangeOne);
    LinkedListTimeRangeGroup timeRangeGroup = new LinkedListTimeRangeGroup(originalTimeRanges);

    Instant timeRangeToAddStart = timeRangeTwoStart.plusSeconds(500);
    Instant timeRangeToAddEnd = timeRangeTwoEnd.plusSeconds(1000);
    TimeRange timeRangeToAdd = TimeRange.fromStartEnd(timeRangeToAddStart, timeRangeToAddEnd);

    // Constructs the expected merged time range for combining B and C.
    Instant timeRangeCombinedStart = timeRangeTwoStart;
    Instant timeRangeCombinedEnd = timeRangeToAddEnd;
    TimeRange timeRangeNew = TimeRange.fromStartEnd(timeRangeCombinedStart, timeRangeCombinedEnd);
    List<TimeRange> expected = Arrays.asList(timeRangeOne, timeRangeNew);

    timeRangeGroup.addTimeRange(timeRangeToAdd);
    Iterator<TimeRange> actualIterator = timeRangeGroup.getAllTimeRangesIterator();

    List<TimeRange> actual = new LinkedList();
    while (actualIterator.hasNext()) {
      actual.add(actualIterator.next());
    }

    Collections.sort(actual, TimeRange.SORT_BY_TIME_RANGE_DURATION_ASCENDING_THEN_START_TIME);
    Assert.assertEquals(expected, actual);
  }

  /** Tests for adding a time range that overlaps with several other existing ones. */
  @Test
  public void testAddCompletelyOverlappingRange() {
    // Time Ranges: |-----A-----|     |----B----|    |----C----|
    // To add:         |-----D----------|
    // Result:     |---------------------------|     |---------|
    Instant timeRangeOneStart = Instant.now();
    Instant timeRangeOneEnd = timeRangeOneStart.plusSeconds(1000);
    Instant timeRangeTwoStart = timeRangeOneEnd.plusSeconds(3000);
    Instant timeRangeTwoEnd = timeRangeTwoStart.plusSeconds(1000);
    Instant timeRangeThreeStart = timeRangeTwoEnd.plusSeconds(1000);
    Instant timeRangeThreeEnd = timeRangeThreeStart.plusSeconds(1000);

    TimeRange timeRangeOne = TimeRange.fromStartEnd(timeRangeOneStart, timeRangeOneEnd);
    TimeRange timeRangeTwo = TimeRange.fromStartEnd(timeRangeTwoStart, timeRangeTwoEnd);
    TimeRange timeRangeThree = TimeRange.fromStartEnd(timeRangeThreeStart, timeRangeThreeEnd);

    List<TimeRange> originalTimeRanges = Arrays.asList(timeRangeOne, timeRangeThree, timeRangeTwo);
    LinkedListTimeRangeGroup timeRangeGroup = new LinkedListTimeRangeGroup(originalTimeRanges);

    Instant timeRangeToAddStart = timeRangeOneStart.plusSeconds(500);
    Instant timeRangeToAddEnd = timeRangeTwoStart.plusSeconds(500);
    TimeRange timeRangeToAdd = TimeRange.fromStartEnd(timeRangeToAddStart, timeRangeToAddEnd);

    // Constructs the expected merged time range for combining A and B.
    Instant timeRangeCombinedStart = timeRangeOneStart;
    Instant timeRangeCombinedEnd = timeRangeTwoEnd;
    TimeRange timeRangeNew = TimeRange.fromStartEnd(timeRangeCombinedStart, timeRangeCombinedEnd);
    List<TimeRange> expected = Arrays.asList(timeRangeThree, timeRangeNew);

    timeRangeGroup.addTimeRange(timeRangeToAdd);
    Iterator<TimeRange> actualIterator = timeRangeGroup.getAllTimeRangesIterator();

    List<TimeRange> actual = new LinkedList();
    while (actualIterator.hasNext()) {
      actual.add(actualIterator.next());
    }

    Collections.sort(actual, TimeRange.SORT_BY_TIME_RANGE_DURATION_ASCENDING_THEN_START_TIME);
    Assert.assertEquals(expected, actual);
  }

  /** Tests for adding a time range that overlaps with all other existing ones. */
  @Test
  public void testAddCompletelyOverlappingMultipleRanges() {
    // Time Ranges: |-----A-----|     |----B----|   |------C-----|
    // To add:         |--------------D----------------|
    // Result:     |---------------------------------------------|
    Instant timeRangeOneStart = Instant.now();
    Instant timeRangeOneEnd = timeRangeOneStart.plusSeconds(1000);
    Instant timeRangeTwoStart = timeRangeOneEnd.plusSeconds(3000);
    Instant timeRangeTwoEnd = timeRangeTwoStart.plusSeconds(1000);
    Instant timeRangeThreeStart = timeRangeTwoEnd.plusSeconds(1000);
    Instant timeRangeThreeEnd = timeRangeThreeStart.plusSeconds(1000);

    TimeRange timeRangeOne = TimeRange.fromStartEnd(timeRangeOneStart, timeRangeOneEnd);
    TimeRange timeRangeTwo = TimeRange.fromStartEnd(timeRangeTwoStart, timeRangeTwoEnd);
    TimeRange timeRangeThree = TimeRange.fromStartEnd(timeRangeThreeStart, timeRangeThreeEnd);

    List<TimeRange> originalTimeRanges = Arrays.asList(timeRangeThree, timeRangeOne, timeRangeTwo);
    LinkedListTimeRangeGroup timeRangeGroup = new LinkedListTimeRangeGroup(originalTimeRanges);

    Instant timeRangeToAddStart = timeRangeOneStart.plusSeconds(500);
    Instant timeRangeToAddEnd = timeRangeThreeStart.plusSeconds(500);
    TimeRange timeRangeToAdd = TimeRange.fromStartEnd(timeRangeToAddStart, timeRangeToAddEnd);

    // Constructs the expected merged time range for combining A, B, and C.
    Instant timeRangeCombinedStart = timeRangeOneStart;
    Instant timeRangeCombinedEnd = timeRangeThreeEnd;
    TimeRange timeRangeNew = TimeRange.fromStartEnd(timeRangeCombinedStart, timeRangeCombinedEnd);
    List<TimeRange> expected = Arrays.asList(timeRangeNew);

    timeRangeGroup.addTimeRange(timeRangeToAdd);
    Iterator<TimeRange> actualIterator = timeRangeGroup.getAllTimeRangesIterator();

    List<TimeRange> actual = new LinkedList();
    while (actualIterator.hasNext()) {
      actual.add(actualIterator.next());
    }

    Collections.sort(actual, TimeRange.SORT_BY_TIME_RANGE_DURATION_ASCENDING_THEN_START_TIME);
    Assert.assertEquals(expected, actual);
  }

  /** Tests for deleting part of a time range. */
  @Test
  public void testDelete() {
    // Time Ranges: |-----A-----|   |---B---|
    // To delete:     |--C--|
    // Results:     |-|     |---|   |-------|
    Instant timeRangeOneStart = Instant.now();
    Instant timeRangeOneEnd = timeRangeOneStart.plusSeconds(1000);
    Instant timeRangeTwoStart = timeRangeOneEnd.plusSeconds(500);
    Instant timeRangeTwoEnd = timeRangeTwoStart.plusSeconds(1000);

    TimeRange timeRangeOne = TimeRange.fromStartEnd(timeRangeOneStart, timeRangeOneEnd);
    TimeRange timeRangeTwo = TimeRange.fromStartEnd(timeRangeTwoStart, timeRangeTwoEnd);

    List<TimeRange> timeRanges = Arrays.asList(timeRangeTwo, timeRangeOne);
    LinkedListTimeRangeGroup timeRangeGroup = new LinkedListTimeRangeGroup(timeRanges);

    TimeRange timeRangeToDelete =
        TimeRange.fromStartEnd(
            timeRangeOneStart.plusSeconds(300), timeRangeOneStart.plusSeconds(600));

    TimeRange timeRangeNewOne =
        TimeRange.fromStartEnd(timeRangeOneStart, timeRangeOneStart.plusSeconds(300));
    TimeRange timeRangeNewTwo =
        TimeRange.fromStartEnd(timeRangeOneStart.plusSeconds(600), timeRangeOneEnd);
    List<TimeRange> expected = Arrays.asList(timeRangeNewOne, timeRangeNewTwo, timeRangeTwo);

    timeRangeGroup.deleteTimeRange(timeRangeToDelete);
    Iterator<TimeRange> actualIterator = timeRangeGroup.getAllTimeRangesIterator();

    List<TimeRange> actual = new LinkedList();
    while (actualIterator.hasNext()) {
      actual.add(actualIterator.next());
    }

    Collections.sort(actual, TimeRange.SORT_BY_TIME_RANGE_DURATION_ASCENDING_THEN_START_TIME);
    Assert.assertEquals(expected, actual);
  }

  /** Tests for deleting a time range that overlaps with some existing time ranges. */
  @Test
  public void testDeleteOverlappingTimeRange() {
    // Time Ranges: |-----A-----|   |---B---|
    // To delete:          |-----C------|
    // Results:     |------|            |---|
    Instant timeRangeOneStart = Instant.now();
    Instant timeRangeOneEnd = timeRangeOneStart.plusSeconds(1000);
    Instant timeRangeTwoStart = timeRangeOneEnd.plusSeconds(500);
    Instant timeRangeTwoEnd = timeRangeTwoStart.plusSeconds(1000);

    TimeRange timeRangeOne = TimeRange.fromStartEnd(timeRangeOneStart, timeRangeOneEnd);
    TimeRange timeRangeTwo = TimeRange.fromStartEnd(timeRangeTwoStart, timeRangeTwoEnd);

    List<TimeRange> timeRanges = Arrays.asList(timeRangeOne, timeRangeTwo);
    LinkedListTimeRangeGroup timeRangeGroup = new LinkedListTimeRangeGroup(timeRanges);

    TimeRange timeRangeToDelete =
        TimeRange.fromStartEnd(
            timeRangeOneStart.plusSeconds(500), timeRangeOneStart.plusSeconds(2000));

    TimeRange timeRangeNewOne =
        TimeRange.fromStartEnd(timeRangeOneStart, timeRangeOneStart.plusSeconds(500));
    TimeRange timeRangeNewTwo =
        TimeRange.fromStartEnd(timeRangeTwoStart.plusSeconds(500), timeRangeTwoEnd);
    List<TimeRange> expectedTimeRangesAfterDelete = Arrays.asList(timeRangeNewOne, timeRangeNewTwo);

    timeRangeGroup.deleteTimeRange(timeRangeToDelete);
    Iterator<TimeRange> actualIterator = timeRangeGroup.getAllTimeRangesIterator();

    List<TimeRange> actual = new LinkedList();
    while (actualIterator.hasNext()) {
      actual.add(actualIterator.next());
    }

    Collections.sort(actual, TimeRange.SORT_BY_TIME_RANGE_DURATION_ASCENDING_THEN_START_TIME);
    Assert.assertEquals(expectedTimeRangesAfterDelete, actual);
  }

  /** Tests for deleting a time range that overlaps with multiple existing time ranges. */
  @Test
  public void testDeleteOverlappingMultipleTimeRange() {
    // Time Ranges: |-----A-----|   |---B---|  |---C---|
    // To delete:          |-----------D----------|
    // Results:     |------|                      |----|
    Instant timeRangeOneStart = Instant.now();
    Instant timeRangeOneEnd = timeRangeOneStart.plusSeconds(1000);
    Instant timeRangeTwoStart = timeRangeOneEnd.plusSeconds(500);
    Instant timeRangeTwoEnd = timeRangeTwoStart.plusSeconds(1000);
    Instant timeRangeThreeStart = timeRangeTwoEnd.plusSeconds(500);
    Instant timeRangeThreeEnd = timeRangeThreeStart.plusSeconds(1000);

    TimeRange timeRangeOne = TimeRange.fromStartEnd(timeRangeOneStart, timeRangeOneEnd);
    TimeRange timeRangeTwo = TimeRange.fromStartEnd(timeRangeTwoStart, timeRangeTwoEnd);
    TimeRange timeRangeThree = TimeRange.fromStartEnd(timeRangeThreeStart, timeRangeThreeEnd);

    List<TimeRange> timeRanges = Arrays.asList(timeRangeOne, timeRangeThree, timeRangeTwo);
    LinkedListTimeRangeGroup timeRangeGroup = new LinkedListTimeRangeGroup(timeRanges);

    TimeRange timeRangeToDelete =
        TimeRange.fromStartEnd(
            timeRangeOneStart.plusSeconds(500), timeRangeThreeStart.plusSeconds(100));

    TimeRange timeRangeNewOne =
        TimeRange.fromStartEnd(timeRangeOneStart, timeRangeOneStart.plusSeconds(500));
    TimeRange timeRangeNewTwo =
        TimeRange.fromStartEnd(timeRangeThreeStart.plusSeconds(100), timeRangeThreeEnd);
    List<TimeRange> expectedTimeRangesAfterDelete = Arrays.asList(timeRangeNewOne, timeRangeNewTwo);

    timeRangeGroup.deleteTimeRange(timeRangeToDelete);
    Iterator<TimeRange> actualIterator = timeRangeGroup.getAllTimeRangesIterator();

    List<TimeRange> actual = new LinkedList();
    while (actualIterator.hasNext()) {
      actual.add(actualIterator.next());
    }

    Collections.sort(actual, TimeRange.SORT_BY_TIME_RANGE_DURATION_ASCENDING_THEN_START_TIME);
    Assert.assertEquals(expectedTimeRangesAfterDelete, actual);
  }

  /**
   * Tests for deleting a time range that overlaps with some existing time ranges and is before the
   * first one.
   */
  @Test
  public void testDeleteOverlappingTimeRangeStart() {
    // Time Ranges:        |-----A-----|   |---B---|
    // To delete:   |-----C------|
    // Results:                  |------|  |-------|
    Instant timeRangeOneStart = Instant.now();
    Instant timeRangeOneEnd = timeRangeOneStart.plusSeconds(1000);
    Instant timeRangeTwoStart = timeRangeOneEnd.plusSeconds(500);
    Instant timeRangeTwoEnd = timeRangeTwoStart.plusSeconds(1000);

    TimeRange timeRangeOne = TimeRange.fromStartEnd(timeRangeOneStart, timeRangeOneEnd);
    TimeRange timeRangeTwo = TimeRange.fromStartEnd(timeRangeTwoStart, timeRangeTwoEnd);

    List<TimeRange> timeRanges = Arrays.asList(timeRangeOne, timeRangeTwo);
    LinkedListTimeRangeGroup timeRangeGroup = new LinkedListTimeRangeGroup(timeRanges);

    TimeRange timeRangeToDelete =
        TimeRange.fromStartEnd(
            timeRangeOneStart.minusSeconds(500), timeRangeOneStart.plusSeconds(100));

    TimeRange timeRangeNew =
        TimeRange.fromStartEnd(timeRangeOneStart.plusSeconds(100), timeRangeOneEnd);

    List<TimeRange> expected = Arrays.asList(timeRangeNew, timeRangeTwo);

    timeRangeGroup.deleteTimeRange(timeRangeToDelete);
    Iterator<TimeRange> actualIterator = timeRangeGroup.getAllTimeRangesIterator();

    List<TimeRange> actual = new LinkedList();
    while (actualIterator.hasNext()) {
      actual.add(actualIterator.next());
    }

    Collections.sort(actual, TimeRange.SORT_BY_TIME_RANGE_DURATION_ASCENDING_THEN_START_TIME);
    Assert.assertEquals(expected, actual);
  }

  /** Tests for deleting a time range that does not exist in original list. */
  @Test
  public void testNonExistingTimeRange() {
    // Time Ranges: |-----A-----|           |---B---|
    // To delete:                 |---C---|
    // Results:     |-----A-----|           |---B---|
    Instant timeRangeOneStart = Instant.now();
    Instant timeRangeOneEnd = timeRangeOneStart.plusSeconds(1000);
    Instant timeRangeTwoStart = timeRangeOneEnd.plusSeconds(1000);
    Instant timeRangeTwoEnd = timeRangeTwoStart.plusSeconds(1000);

    TimeRange timeRangeOne = TimeRange.fromStartEnd(timeRangeOneStart, timeRangeOneEnd);
    TimeRange timeRangeTwo = TimeRange.fromStartEnd(timeRangeTwoStart, timeRangeTwoEnd);

    List<TimeRange> timeRanges = Arrays.asList(timeRangeOne, timeRangeTwo);
    LinkedListTimeRangeGroup timeRangeGroup = new LinkedListTimeRangeGroup(timeRanges);

    TimeRange timeRangeToDelete =
        TimeRange.fromStartEnd(
            timeRangeOneEnd.plusSeconds(100), timeRangeTwoStart.minusSeconds(100));

    timeRangeGroup.deleteTimeRange(timeRangeToDelete);
    Iterator<TimeRange> actualIterator = timeRangeGroup.getAllTimeRangesIterator();

    List<TimeRange> actual = new LinkedList();
    while (actualIterator.hasNext()) {
      actual.add(actualIterator.next());
    }

    Collections.sort(actual, TimeRange.SORT_BY_TIME_RANGE_DURATION_ASCENDING_THEN_START_TIME);
    Assert.assertEquals(timeRanges, actual);
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
    LinkedListTimeRangeGroup timeRangeGroup = new LinkedListTimeRangeGroup(timeRanges);

    TimeRange timeRangeToDelete =
        TimeRange.fromStartEnd(timeRangeOneStart, timeRangeOneStart.plusSeconds(1000));

    List<TimeRange> expectedTimeRangesAfterDelete = Arrays.asList(timeRangeTwo);

    timeRangeGroup.deleteTimeRange(timeRangeToDelete);
    Iterator<TimeRange> actualIterator = timeRangeGroup.getAllTimeRangesIterator();

    List<TimeRange> actual = new LinkedList();
    while (actualIterator.hasNext()) {
      actual.add(actualIterator.next());
    }

    Collections.sort(actual, TimeRange.SORT_BY_TIME_RANGE_DURATION_ASCENDING_THEN_START_TIME);
    Assert.assertEquals(expectedTimeRangesAfterDelete, actual);
  }

  /** Tests for time ranges that are covered by the range to delete. */
  @Test
  public void testDeleteTimeRangeThatCoversOriginal() {
    // Time Ranges: |---A---|    |---B---|
    // To delete:              |------C-----|
    // Results:     |-------|
    Instant timeRangeOneStart = Instant.now();
    Instant timeRangeOneEnd = timeRangeOneStart.plusSeconds(1000);
    Instant timeRangeTwoStart = timeRangeOneEnd.plusSeconds(1000);
    Instant timeRangeTwoEnd = timeRangeTwoStart.plusSeconds(1000);

    TimeRange timeRangeOne = TimeRange.fromStartEnd(timeRangeOneStart, timeRangeOneEnd);
    TimeRange timeRangeTwo = TimeRange.fromStartEnd(timeRangeTwoStart, timeRangeTwoEnd);

    List<TimeRange> timeRanges = Arrays.asList(timeRangeOne, timeRangeTwo);
    LinkedListTimeRangeGroup timeRangeGroup = new LinkedListTimeRangeGroup(timeRanges);

    TimeRange timeRangeToDelete =
        TimeRange.fromStartEnd(
            timeRangeTwoStart.minusSeconds(100), timeRangeTwoEnd.plusSeconds(100));

    List<TimeRange> expectedTimeRangesAfterDelete = Arrays.asList(timeRangeOne);

    timeRangeGroup.deleteTimeRange(timeRangeToDelete);
    Iterator<TimeRange> actualIterator = timeRangeGroup.getAllTimeRangesIterator();

    List<TimeRange> actual = new LinkedList();
    while (actualIterator.hasNext()) {
      actual.add(actualIterator.next());
    }

    Collections.sort(actual, TimeRange.SORT_BY_TIME_RANGE_DURATION_ASCENDING_THEN_START_TIME);
    Assert.assertEquals(expectedTimeRangesAfterDelete, actual);
  }
}
