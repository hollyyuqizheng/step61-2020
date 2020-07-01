package com.google.sps;

import com.google.sps.data.CalendarEvent;
import java.time.Instant;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class CalendarEventTest {

  /**
   * Testing calendar event initialization and conversion between String and Instant for the start
   * and end times.
   */
  @Test
  public void testTimeStringConversion() {
    CalendarEvent event =
        new CalendarEvent(
            /* name= */ "test",
            /* startTime= */ Instant.parse("2020-06-24T13:00:00.000Z"),
            /* endTime= */ Instant.parse("2020-06-25T00:00:00.000Z"));

    Instant expectedStartTimeInstant = Instant.parse("2020-06-24T13:00:00.000Z");
    Instant actualStartTimeInstant = event.getStartTimeInstant();
    Assert.assertEquals(expectedStartTimeInstant, actualStartTimeInstant);
  }
}
