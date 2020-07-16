package com.google.sps.data;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class TimeRangeTest {

  private static Duration DURATION_30_MINUTES = Duration.ofSeconds(30 * 60);

  private static Instant TIME_0900 = Instant.parse("2020-06-25T09:00:00Z");
  private static Instant TIME_0930 = TIME_0900.plus(DURATION_30_MINUTES);



  /** Makes sure that the start() method works properly. */
  @Test
  public void startMethod() {
    TimeRange timeRange = TimeRange.fromStartEnd(TIME_0900, TIME_0930);
    Assert.assertEquals(TIME_0900, timeRange.start());
  }

  /** Makes sure that the duration() method works properly. */
  @Test
  public void durationMethod() {
    TimeRange timeRange = TimeRange.fromStartEnd(TIME_0900, TIME_0930);
    Assert.assertEquals(DURATION_30_MINUTES, timeRange.duration());
  }

  /** Makes sure that the end() method works properly. */
  @Test
  public void endMethod() {
    TimeRange timeRange = TimeRange.fromStartEnd(TIME_0900, TIME_0930);
    Assert.assertEquals(TIME_0930, timeRange.end());
  }

  /** 
   * Makes sure that a.equals(b) works properly for two TimeRange's.
   * This also tests TimeRange.equals(a,b) because that method is called from
   * a.equals(b).
   */
  @Test
  public void equalsMethod_correctClass() {
    TimeRange timeRangeOne = TimeRange.fromStartEnd(TIME_0900, TIME_0930);
    TimeRange timeRangeTwo = TimeRange.fromStartEnd(TIME_0900, TIME_0930);
    Assert.assertTrue(timeRangeOne.equals(timeRangeTwo));
  }

  /** Makes sure that a.equals(b) works properly when one Object is not a TimeRange*/
  @Test
  public void equalsMethod_incorrectClass() {
    TimeRange timeRange = TimeRange.fromStartEnd(TIME_0900, TIME_0930);
    CalendarEvent calendarEvent = new CalendarEvent("Event 1", TIME_0900, TIME_0930);
    Assert.assertFalse(timeRange.equals(calendarEvent));
  }

  /** Makes sure that toString() works properly.*/
  @Test
  public void toStringMethod() {
    TimeRange timeRange = TimeRange.fromStartEnd(TIME_0900, TIME_0930);
    String actualString = timeRange.toString();
    String expectedString = "Range: [2020-06-25T09:00:00Z, 2020-06-25T09:30:00Z]";
    Assert.assertEquals(expectedString, actualString);
  }
}
