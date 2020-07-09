package com.google.sps;

import com.google.sps.data.CalendarEvent;
import com.google.sps.data.CalendarGroup;
import com.google.sps.data.TimeRange;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class CalendarGroupTest {

  /** Start time of the scheduling block must not be later then end time. */
  @Test(expected = IllegalArgumentException.class)
  public void startTimeAfterEndTime() {
    Instant startTime = Instant.now();
    Instant endTimeBefore = startTime.minusSeconds(1000);
    Instant endTimeAfter = startTime.plusSeconds(1000);

    List<CalendarEvent> events = new ArrayList<CalendarEvent>();
    CalendarEvent event = new CalendarEvent("event", startTime, endTimeAfter);
    events.add(event);

    CalendarGroup eventGroup = new CalendarGroup(events, startTime, endTimeBefore);
  }

  /** Tests for deleting a time range that doesn't exsit in the list of all free time ranges. */
  @Test(expected = IllegalArgumentException.class)
  public void deleteTimeRangeNonExistent() {
    // Events:       |--One--|   |--Two--|
    // Possible: |---------------------------|
    // Free:     |---|       |---|       |---|
    List<CalendarEvent> events = new ArrayList<CalendarEvent>();

    Instant eventOneStart = Instant.now();
    Instant eventOneEnd = eventOneStart.plusSeconds(1000);
    Instant eventTwoStart = eventOneEnd.plusSeconds(1000);
    Instant eventTwoEnd = eventTwoStart.plusSeconds(1000);

    Instant startTime = eventOneStart.minusSeconds(1000);
    Instant endTime = eventTwoEnd.plusSeconds(1000);

    CalendarEvent eventOne = new CalendarEvent("event one", eventOneStart, eventOneEnd);
    CalendarEvent eventTwo = new CalendarEvent("event two", eventTwoStart, eventTwoEnd);

    events.add(eventOne);
    events.add(eventTwo);

    CalendarGroup eventGroup = new CalendarGroup(events, startTime, endTime);

    Instant wrongStart = eventOneStart.minusSeconds(50);
    Instant wrongEnd = eventOneStart;

    eventGroup.deleteFreeTimeRange(TimeRange.fromStartEnd(wrongStart, wrongEnd));
  }

  /** Tests for correct free times with non-overlapping events. */
  @Test
  public void freeTimeRangesNonOverlappingEvents() {
    // Events:       |--One--|   |--Two--|
    // Possible: |---------------------------|
    // Free:     |---|       |---|       |---|
    List<CalendarEvent> events = new ArrayList<CalendarEvent>();

    Instant eventOneStart = Instant.now();
    Instant eventOneEnd = eventOneStart.plusSeconds(1000);
    Instant eventTwoStart = eventOneEnd.plusSeconds(1000);
    Instant eventTwoEnd = eventTwoStart.plusSeconds(1000);

    Instant startTime = eventOneStart.minusSeconds(1000);
    Instant endTime = eventTwoEnd.plusSeconds(1000);

    CalendarEvent eventOne = new CalendarEvent("event one", eventOneStart, eventOneEnd);
    CalendarEvent eventTwo = new CalendarEvent("event two", eventTwoStart, eventTwoEnd);

    events.add(eventOne);
    events.add(eventTwo);

    CalendarGroup eventGroup = new CalendarGroup(events, startTime, endTime);

    Instant freeTimeRangeOneStart = startTime;
    Instant freeTimeRangeOneEnd = eventOneStart;
    Instant freeTimeRangeTwoStart = eventOneEnd;
    Instant freeTimeRangeTwoEnd = eventTwoStart;
    Instant freeTimeRangeThreeStart = eventTwoEnd;
    Instant freeTimeRangeThreeEnd = endTime;

    TimeRange freeTimeRangeOne = TimeRange.fromStartEnd(freeTimeRangeOneStart, freeTimeRangeOneEnd);
    TimeRange freeTimeRangeTwo = TimeRange.fromStartEnd(freeTimeRangeTwoStart, freeTimeRangeTwoEnd);
    TimeRange freeTimeRangeThree =
        TimeRange.fromStartEnd(freeTimeRangeThreeStart, freeTimeRangeThreeEnd);

    List<TimeRange> expectedFreeTimeRanges =
        Arrays.asList(freeTimeRangeOne, freeTimeRangeTwo, freeTimeRangeThree);
    List<TimeRange> actualFreeTimeRanges = eventGroup.getFreeTimeRanges();
    Assert.assertEquals(expectedFreeTimeRanges, actualFreeTimeRanges);
  }

  /** Tests for getting the correct free times with overlapping events. */
  @Test
  public void freeTimeRangesOverlappingEvents() {
    // Events:       |--One--|
    // Events:          |---Two--|
    // Possible: |-----------------------|
    // Free:     |---|           |-------|
    List<CalendarEvent> events = new ArrayList<CalendarEvent>();
    Instant startTime = Instant.now();
    Instant endTime = startTime.plusSeconds(5000);

    Instant eventOneStart = startTime.plusSeconds(1000);
    Instant eventOneEnd = eventOneStart.plusSeconds(1000);
    Instant eventTwoStart = startTime.plusSeconds(1500);
    Instant eventTwoEnd = eventTwoStart.plusSeconds(1000);

    CalendarEvent eventOne = new CalendarEvent("event one", eventOneStart, eventOneEnd);
    CalendarEvent eventTwo = new CalendarEvent("event two", eventTwoStart, eventTwoEnd);
    events.add(eventOne);
    events.add(eventTwo);

    CalendarGroup eventGroup = new CalendarGroup(events, startTime, endTime);

    Instant freeTimeRangeOneStart = startTime;
    Instant freeTimeRangeOneEnd = startTime.plusSeconds(1000);
    Instant freeTimeRangeTwoStart = startTime.plusSeconds(2500);
    Instant freeTimeRangeTwoEnd = endTime;
    TimeRange freeTimeRangeOne = TimeRange.fromStartEnd(freeTimeRangeOneStart, freeTimeRangeOneEnd);
    TimeRange freeTimeRangeTwo = TimeRange.fromStartEnd(freeTimeRangeTwoStart, freeTimeRangeTwoEnd);

    List<TimeRange> expectedFreeTimeRanges = Arrays.asList(freeTimeRangeOne, freeTimeRangeTwo);
    List<TimeRange> actualFreeTimeRanges = eventGroup.getFreeTimeRanges();
    Assert.assertEquals(expectedFreeTimeRanges, actualFreeTimeRanges);
  }

  /** Tests for getting the correct free times with completely overlapping events. */
  @Test
  public void freeTimeRangesCompletelyOverlappingEvents() {
    // Events:         |--One--|
    // Events:       |------Two-----|
    // Possible: |-----------------------|
    // Free:     |---|               |---|
    List<CalendarEvent> events = new ArrayList<CalendarEvent>();
    Instant startTime = Instant.now();
    Instant endTime = startTime.plusSeconds(5000);

    Instant eventOneStart = startTime.plusSeconds(2000);
    Instant eventOneEnd = eventOneStart.plusSeconds(1000);
    Instant eventTwoStart = startTime.plusSeconds(1000);
    Instant eventTwoEnd = eventTwoStart.plusSeconds(3000);

    CalendarEvent eventOne = new CalendarEvent("event one", eventOneStart, eventOneEnd);
    CalendarEvent eventTwo = new CalendarEvent("event two", eventTwoStart, eventTwoEnd);
    events.add(eventOne);
    events.add(eventTwo);

    CalendarGroup eventGroup = new CalendarGroup(events, startTime, endTime);

    Instant freeTimeRangeOneStart = startTime;
    Instant freeTimeRangeOneEnd = startTime.plusSeconds(1000);
    Instant freeTimeRangeTwoStart = startTime.plusSeconds(4000);
    Instant freeTimeRangeTwoEnd = endTime;
    TimeRange freeTimeRangeOne = TimeRange.fromStartEnd(freeTimeRangeOneStart, freeTimeRangeOneEnd);
    TimeRange freeTimeRangeTwo = TimeRange.fromStartEnd(freeTimeRangeTwoStart, freeTimeRangeTwoEnd);

    List<TimeRange> expectedFreeTimeRanges = Arrays.asList(freeTimeRangeOne, freeTimeRangeTwo);
    List<TimeRange> actualFreeTimeRanges = eventGroup.getFreeTimeRanges();
    Assert.assertEquals(expectedFreeTimeRanges, actualFreeTimeRanges);
  }

  /** Tests for scenario when there is no free time. */
  @Test
  public void noFreeTime() {
    // Events:   |--One--||--------Two-------|
    // Possible: |---------------------------|
    // Free:

    List<CalendarEvent> events = new ArrayList<CalendarEvent>();
    Instant startTime = Instant.now();
    Instant endTime = startTime.plusSeconds(2000);

    Instant eventOneStart = startTime;
    Instant eventOneEnd = eventOneStart.plusSeconds(1000);
    Instant eventTwoStart = eventOneEnd;
    Instant eventTwoEnd = eventTwoStart.plusSeconds(1000);

    CalendarEvent eventOne = new CalendarEvent("event one", eventOneStart, eventOneEnd);
    CalendarEvent eventTwo = new CalendarEvent("event two", eventTwoStart, eventTwoEnd);
    events.add(eventOne);
    events.add(eventTwo);

    CalendarGroup eventGroup = new CalendarGroup(events, startTime, endTime);
    List<TimeRange> freeTimeRanges = eventGroup.getFreeTimeRanges();
    Assert.assertTrue(freeTimeRanges.isEmpty());
  }

  /**
   * Tests for scenario where there is no calendar event. Entirety of possible time range is the
   * free time range.
   */
  @Test
  public void allFreeTime() {
    // Events:
    // Possible: |---------------------------|
    // Free:     |---------------------------|

    List<CalendarEvent> events = new ArrayList<CalendarEvent>();
    Instant startTime = Instant.now();
    Instant endTime = startTime.plusSeconds(10000);
    TimeRange targetFreeTime = TimeRange.fromStartEnd(startTime, endTime);

    CalendarGroup eventGroup = new CalendarGroup(events, startTime, endTime);

    List<TimeRange> expectedFreeTimeRanges = Arrays.asList(targetFreeTime);
    List<TimeRange> actualFreeTimeRanges = eventGroup.getFreeTimeRanges();
    Assert.assertEquals(expectedFreeTimeRanges, actualFreeTimeRanges);
  }

  /** Tests deleting and modifying a free time range. */
  @Test
  public void deleteAndModifyTimeRange() {
    // Events:       |--One--|   |--Two--|
    // Possible: |---------------------------|
    // Free:     |---|       |---|       |---|
    List<CalendarEvent> events = new ArrayList<CalendarEvent>();

    Instant eventOneStart = Instant.now();
    Instant eventOneEnd = eventOneStart.plusSeconds(1000);
    Instant eventTwoStart = eventOneEnd.plusSeconds(1000);
    Instant eventTwoEnd = eventTwoStart.plusSeconds(1000);

    Instant startTime = eventOneStart.minusSeconds(1000);
    Instant endTime = eventTwoEnd.plusSeconds(1000);

    CalendarEvent eventOne = new CalendarEvent("event one", eventOneStart, eventOneEnd);
    CalendarEvent eventTwo = new CalendarEvent("event two", eventTwoStart, eventTwoEnd);

    events.add(eventOne);
    events.add(eventTwo);

    CalendarGroup eventGroup = new CalendarGroup(events, startTime, endTime);
    List<TimeRange> freeTimeRanges = eventGroup.getFreeTimeRanges();

    Instant freeTimeRangeOneStart = startTime;
    Instant freeTimeRangeOneEnd = eventOneStart;
    Instant freeTimeRangeTwoStart = eventOneEnd;
    Instant freeTimeRangeTwoEnd = eventTwoStart;
    Instant freeTimeRangeThreeStart = eventTwoEnd;
    Instant freeTimeRangeThreeEnd = endTime;

    TimeRange freeTimeRangeOne = TimeRange.fromStartEnd(freeTimeRangeOneStart, freeTimeRangeOneEnd);
    TimeRange freeTimeRangeTwo = TimeRange.fromStartEnd(freeTimeRangeTwoStart, freeTimeRangeTwoEnd);
    TimeRange freeTimeRangeThree =
        TimeRange.fromStartEnd(freeTimeRangeThreeStart, freeTimeRangeThreeEnd);

    List<TimeRange> expectedFreeTimeRangesAfterDelete =
        Arrays.asList(freeTimeRangeOne, freeTimeRangeThree);
    List<TimeRange> actualFreeTimeRangesAfterDelete =
        eventGroup.deleteFreeTimeRange(freeTimeRangeTwo);
    Assert.assertEquals(expectedFreeTimeRangesAfterDelete, actualFreeTimeRangesAfterDelete);

    // Creates a new time range that is 10 seconds shorter than free time One.
    Instant freeTimeRangeOneEndNew = freeTimeRangeOneEnd.minusSeconds(10);
    TimeRange freeTimeRangeOneNew =
        TimeRange.fromStartEnd(freeTimeRangeOneStart, freeTimeRangeOneEndNew);

    List<TimeRange> freeTimeRangesAfterModify =
        eventGroup.modifyFreeTimeRange(
            /* originalFreeTimeRange= */ freeTimeRangeOne,
            /* newFreeTimeRange= */ freeTimeRangeOneNew);
    Assert.assertEquals(freeTimeRangesAfterModify.get(0).end(), freeTimeRangeOneNew.end());
  }

  /**
   * Tests updating an existing free time into a time range that is not part of the original time
   * range.
   */
  @Test(expected = IllegalArgumentException.class)
  public void modifyTimeRangeWrongBoundary() {
    // Events:       |--One--|   |--Two--|
    // Possible: |---------------------------|
    // Free:     |---|       |---|       |---|
    List<CalendarEvent> events = new ArrayList<CalendarEvent>();

    Instant eventOneStart = Instant.now();
    Instant eventOneEnd = eventOneStart.plusSeconds(1000);
    Instant eventTwoStart = eventOneEnd.plusSeconds(1000);
    Instant eventTwoEnd = eventTwoStart.plusSeconds(1000);

    Instant startTime = eventOneStart.minusSeconds(1000);
    Instant endTime = eventTwoEnd.plusSeconds(1000);

    CalendarEvent eventOne = new CalendarEvent("event one", eventOneStart, eventOneEnd);
    CalendarEvent eventTwo = new CalendarEvent("event two", eventTwoStart, eventTwoEnd);

    events.add(eventOne);
    events.add(eventTwo);

    CalendarGroup eventGroup = new CalendarGroup(events, startTime, endTime);
    List<TimeRange> freeTimeRanges = eventGroup.getFreeTimeRanges();

    Instant freeTimeRangeOneStart = startTime;
    Instant freeTimeRangeOneEnd = eventOneStart;
    Instant freeTimeRangeTwoStart = eventOneEnd;
    Instant freeTimeRangeTwoEnd = eventTwoStart;
    Instant freeTimeRangeThreeStart = eventTwoEnd;
    Instant freeTimeRangeThreeEnd = endTime;

    TimeRange freeTimeRangeOne = TimeRange.fromStartEnd(freeTimeRangeOneStart, freeTimeRangeOneEnd);
    TimeRange freeTimeRangeTwo = TimeRange.fromStartEnd(freeTimeRangeTwoStart, freeTimeRangeTwoEnd);
    TimeRange freeTimeRangeThree =
        TimeRange.fromStartEnd(freeTimeRangeThreeStart, freeTimeRangeThreeEnd);

    Instant freeTimeRangeOneEndNew = freeTimeRangeOneEnd.plusSeconds(10);

    // This new time range is 10 seconds longer than the original one,
    // so this modification is not valid.
    TimeRange freeTimeRangeOneNew =
        TimeRange.fromStartEnd(freeTimeRangeOneStart, freeTimeRangeOneEndNew);
    List<TimeRange> freeTimeRangesAfterModify =
        eventGroup.modifyFreeTimeRange(
            /* originalFreeTimeRange= */ freeTimeRangeOne,
            /* newFreeTimeRange= */ freeTimeRangeOneNew);
  }
}
