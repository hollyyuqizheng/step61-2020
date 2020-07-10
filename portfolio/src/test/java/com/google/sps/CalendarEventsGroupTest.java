package com.google.sps;

import com.google.common.collect.ImmutableList;
import com.google.sps.data.CalendarEvent;
import com.google.sps.data.CalendarEventsGroup;
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
public final class CalendarEventsGroupTest {

  /** Start time of the scheduling block must not be later then end time. */
  @Test(expected = IllegalArgumentException.class)
  public void startTimeAfterEndTime() {
    Instant startTime = Instant.now();
    Instant endTimeBefore = startTime.minusSeconds(1000);
    Instant endTimeAfter = startTime.plusSeconds(1000);

    List<CalendarEvent> events =
        ImmutableList.of(new CalendarEvent("event", startTime, endTimeAfter));

    CalendarEventsGroup eventGroup = new CalendarEventsGroup(events, startTime, endTimeBefore);
  }

  /** Tests for correct free times with non-overlapping events. */
  @Test
  public void freeTimeRangesNonOverlappingEvents() {
    // Events:       |--One--|   |--Two--|
    // Possible: |---------------------------|
    // Free:     |---|       |---|       |---|
    Instant eventOneStart = Instant.now();
    Instant eventOneEnd = eventOneStart.plusSeconds(1000);
    Instant eventTwoStart = eventOneEnd.plusSeconds(1000);
    Instant eventTwoEnd = eventTwoStart.plusSeconds(1000);

    Instant startTime = eventOneStart.minusSeconds(1000);
    Instant endTime = eventTwoEnd.plusSeconds(1000);

    List<CalendarEvent> events =
        ImmutableList.of(
            new CalendarEvent("event one", eventOneStart, eventOneEnd),
            new CalendarEvent("event two", eventTwoStart, eventTwoEnd));

    CalendarEventsGroup eventGroup = new CalendarEventsGroup(events, startTime, endTime);

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
    Instant startTime = Instant.now();
    Instant endTime = startTime.plusSeconds(5000);

    Instant eventOneStart = startTime.plusSeconds(1000);
    Instant eventOneEnd = eventOneStart.plusSeconds(1000);
    Instant eventTwoStart = startTime.plusSeconds(1500);
    Instant eventTwoEnd = eventTwoStart.plusSeconds(1000);

    List<CalendarEvent> events =
        ImmutableList.of(
            new CalendarEvent("event one", eventOneStart, eventOneEnd),
            new CalendarEvent("event two", eventTwoStart, eventTwoEnd));

    CalendarEventsGroup eventGroup = new CalendarEventsGroup(events, startTime, endTime);

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
    Instant startTime = Instant.now();
    Instant endTime = startTime.plusSeconds(5000);

    Instant eventOneStart = startTime.plusSeconds(2000);
    Instant eventOneEnd = eventOneStart.plusSeconds(1000);
    Instant eventTwoStart = startTime.plusSeconds(1000);
    Instant eventTwoEnd = eventTwoStart.plusSeconds(3000);

    List<CalendarEvent> events =
        ImmutableList.of(
            new CalendarEvent("event one", eventOneStart, eventOneEnd),
            new CalendarEvent("event two", eventTwoStart, eventTwoEnd));

    CalendarEventsGroup eventGroup = new CalendarEventsGroup(events, startTime, endTime);

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
    Instant startTime = Instant.now();
    Instant endTime = startTime.plusSeconds(2000);

    Instant eventOneStart = startTime;
    Instant eventOneEnd = eventOneStart.plusSeconds(1000);
    Instant eventTwoStart = eventOneEnd;
    Instant eventTwoEnd = eventTwoStart.plusSeconds(1000);

    List<CalendarEvent> events =
        ImmutableList.of(
            new CalendarEvent("event one", eventOneStart, eventOneEnd),
            new CalendarEvent("event two", eventTwoStart, eventTwoEnd));

    CalendarEventsGroup eventGroup = new CalendarEventsGroup(events, startTime, endTime);
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

    CalendarEventsGroup eventGroup = new CalendarEventsGroup(events, startTime, endTime);

    List<TimeRange> expectedFreeTimeRanges = Arrays.asList(targetFreeTime);
    List<TimeRange> actualFreeTimeRanges = eventGroup.getFreeTimeRanges();
    Assert.assertEquals(expectedFreeTimeRanges, actualFreeTimeRanges);
  }

  /**
   * If the event lies outside the working hours, the entire possible scheduling time range should
   * be returned.
   */
  @Test
  public void eventAfterOverallEndTime() {
    // Events:                                  |-A-|
    // Possible: |---------------------------|
    // Free:     |---------------------------|
    Instant overallStartTime = Instant.now();
    Instant overallEndTime = overallStartTime.plusSeconds(10000);
    TimeRange targetFreeTime = TimeRange.fromStartEnd(overallStartTime, overallEndTime);

    Instant eventStartTime = overallEndTime.plusSeconds(5000);
    Instant eventEndTime = eventStartTime.plusSeconds(5000);
    List<CalendarEvent> events =
        ImmutableList.of(new CalendarEvent("event", eventStartTime, eventEndTime));
    CalendarEventsGroup eventGroup =
        new CalendarEventsGroup(events, overallStartTime, overallEndTime);

    List<TimeRange> expectedFreeTimeRanges = Arrays.asList(targetFreeTime);
    List<TimeRange> actualFreeTimeRanges = eventGroup.getFreeTimeRanges();
    Assert.assertEquals(expectedFreeTimeRanges, actualFreeTimeRanges);
  }

  /** The event is partially within scheduling hours. */
  @Test
  public void eventPartiallyOverlapWorkHoursEnd() {
    // Events:            |------------A-----------|
    // Possible: |---------------------------|
    // Free:     |--------|
    Instant overallStartTime = Instant.now();
    Instant overallEndTime = overallStartTime.plusSeconds(10000);

    Instant eventStartTime = overallStartTime.plusSeconds(1000);
    Instant eventEndTime = eventStartTime.plusSeconds(10000);
    List<CalendarEvent> events =
        ImmutableList.of(new CalendarEvent("event", eventStartTime, eventEndTime));
    CalendarEventsGroup eventGroup =
        new CalendarEventsGroup(events, overallStartTime, overallEndTime);

    TimeRange targetFreeTime = TimeRange.fromStartEnd(overallStartTime, eventStartTime);

    List<TimeRange> expectedFreeTimeRanges = Arrays.asList(targetFreeTime);
    List<TimeRange> actualFreeTimeRanges = eventGroup.getFreeTimeRanges();
    Assert.assertEquals(expectedFreeTimeRanges, actualFreeTimeRanges);
  }

  /** The event is partially within scheduling hours. */
  @Test
  public void eventPartiallyOverlapWorkHoursStart() {
    // Events:   |------------A-----------|
    // Possible:      |---------------------------|
    // Free:                              |-------|
    Instant overallStartTime = Instant.now();
    Instant overallEndTime = overallStartTime.plusSeconds(10000);

    Instant eventStartTime = overallStartTime.minusSeconds(1000);
    Instant eventEndTime = eventStartTime.plusSeconds(10000);
    List<CalendarEvent> events =
        ImmutableList.of(new CalendarEvent("event", eventStartTime, eventEndTime));
    CalendarEventsGroup eventGroup =
        new CalendarEventsGroup(events, overallStartTime, overallEndTime);

    TimeRange targetFreeTime = TimeRange.fromStartEnd(eventEndTime, overallEndTime);

    List<TimeRange> expectedFreeTimeRanges = Arrays.asList(targetFreeTime);
    List<TimeRange> actualFreeTimeRanges = eventGroup.getFreeTimeRanges();
    Assert.assertEquals(expectedFreeTimeRanges, actualFreeTimeRanges);
  }

  /** Events that overlap and whose overall time is outside working hours. */
  @Test
  public void overlappingEventsOutsideWorkHours() {
    // Events:   |---------A---------|
    //                      |-------B-------|
    // Possible:      |---------------------------|
    // Free:                                |-----|
    Instant overallStartTime = Instant.now();
    Instant overallEndTime = overallStartTime.plusSeconds(10000);

    Instant eventOneStartTime = overallStartTime.minusSeconds(1000);
    Instant eventOneEndTime = eventOneStartTime.plusSeconds(3000);
    Instant eventTwoStartTime = overallStartTime.plusSeconds(1000);
    Instant eventTwoEndTime = eventTwoStartTime.plusSeconds(2000);

    List<CalendarEvent> events =
        ImmutableList.of(
            new CalendarEvent("event one", eventOneStartTime, eventOneEndTime),
            new CalendarEvent("event two", eventTwoStartTime, eventTwoEndTime));
    CalendarEventsGroup eventGroup =
        new CalendarEventsGroup(events, overallStartTime, overallEndTime);

    TimeRange targetFreeTime = TimeRange.fromStartEnd(eventTwoEndTime, overallEndTime);

    List<TimeRange> expectedFreeTimeRanges = Arrays.asList(targetFreeTime);
    List<TimeRange> actualFreeTimeRanges = eventGroup.getFreeTimeRanges();
    Assert.assertEquals(expectedFreeTimeRanges, actualFreeTimeRanges);
  }
}
