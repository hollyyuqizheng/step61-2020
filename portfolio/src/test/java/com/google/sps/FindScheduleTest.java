package com.google.sps.data;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class FindScheduleTest {
  private static long BEGINNING_OF_DAY = Instant.parse("2020-06-25T00:00:00Z").getEpochSecond();
  private static long END_OF_DAY = Instant.parse("2020-06-25T23:59:59Z").getEpochSecond();

  private static long TIME_15_MINUTES = 900;
  private static long TIME_30_MINUTES = 1800;
  private static long TIME_45_MINUTES = 2700;
  private static long TIME_60_MINUTES = 3600;

  private static long TIME_0900 = BEGINNING_OF_DAY + 9 * TIME_60_MINUTES;
  private static long TIME_1000 = BEGINNING_OF_DAY + 10 * TIME_60_MINUTES;
  private static long TIME_1100 = BEGINNING_OF_DAY + 11 * TIME_60_MINUTES;
  private static long TIME_1200 = BEGINNING_OF_DAY + 12 * TIME_60_MINUTES;
  private static long TIME_1300 = BEGINNING_OF_DAY + 13 * TIME_60_MINUTES;
  private static long TIME_1400 = BEGINNING_OF_DAY + 14 * TIME_60_MINUTES;
  private static long TIME_1500 = BEGINNING_OF_DAY + 15 * TIME_60_MINUTES;
  private static long TIME_1600 = BEGINNING_OF_DAY + 16 * TIME_60_MINUTES;
  private static long TIME_1700 = BEGINNING_OF_DAY + 17 * TIME_60_MINUTES;
  private static long TIME_1800 = BEGINNING_OF_DAY + 18 * TIME_60_MINUTES;

  private static String STRING_0900AM = "2020-06-25T09:00:00Z";
  private static String STRING_1000AM = "2020-06-25T10:00:00Z";
  private static String STRING_1100AM = "2020-06-25T11:00:00Z";
  private static String STRING_1200AM = "2020-06-25T12:00:00Z";
  private static String STRING_1300AM = "2020-06-25T13:00:00Z";
  private static String STRING_1400AM = "2020-06-25T14:00:00Z";
  private static String STRING_1500AM = "2020-06-25T15:00:00Z";
  private static String STRING_1600AM = "2020-06-25T16:00:00Z";
  private static String STRING_1700AM = "2020-06-25T17:00:00Z";

  private final TaskPriority LOW_PRIORITY = new TaskPriority(1);

  private FindSchedule schedule;

  @Before
  public void setUp() {
    schedule = new FindSchedule();
  }

  @Test
  public void noTasksGreedy() {
    Collection<CalendarEvent> events =
        Arrays.asList(new CalendarEvent("Event 1", "2020-06-25T00:00:00Z", "2020-06-26T00:00:00Z"));
    Collection<Task> tasks = Arrays.asList();

    Collection<Task> actual = schedule.greedy(events, tasks, TIME_0900, TIME_1700);
    Collection<Task> expected = Arrays.asList();

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void singleTaskBetweenEventsGreedy() {
    Collection<CalendarEvent> events =
        Arrays.asList(new CalendarEvent("Event 1", "2020-06-25T09:15:00Z", "2020-06-25T09:45:00Z"),
            new CalendarEvent("Event 2", "2020-06-25T10:15:00Z", "2020-06-25T10:45:00Z"));
    Collection<Task> tasks = Arrays.asList(new Task("Task 1", "First task", 30, LOW_PRIORITY));
    Collection<Task> actual = schedule.greedy(events, tasks, TIME_0900, TIME_1700);
    List<Task> expected =
        Arrays.asList(new Task("Task 1", "First task", 30, LOW_PRIORITY, "2020-06-25T09:45:00Z"));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void testSortingNoEventsGreedy() {
    Collection<CalendarEvent> events = Arrays.asList();
    Collection<Task> tasks = Arrays.asList(new Task("Task 1", "First task", 30, LOW_PRIORITY),
        new Task("Task 2", "Second task", 30, LOW_PRIORITY),
        new Task("Task 3", "Second task", 20, LOW_PRIORITY),
        new Task("Task 4", "Second task", 45, LOW_PRIORITY));
    Collection<Task> actual = schedule.greedy(events, tasks, TIME_0900, TIME_1700);
    List<Task> expected =
        Arrays.asList(new Task("Task 3", "Second task", 20, LOW_PRIORITY, STRING_0900AM),
            new Task("Task 1", "First task", 30, LOW_PRIORITY, "2020-06-25T09:20:00Z"),
            new Task("Task 2", "Second task", 30, LOW_PRIORITY, "2020-06-25T09:50:00Z"),
            new Task("Task 4", "Second task", 45, LOW_PRIORITY, "2020-06-25T10:20:00Z"));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void manyEventsAndTasksGreedy() {
    Collection<CalendarEvent> events =
        Arrays.asList(new CalendarEvent("Event 1", "2020-06-25T09:00:00Z", "2020-06-25T09:30:00Z"),
            new CalendarEvent("Event 2", "2020-06-25T10:00:00Z", "2020-06-25T10:45:00Z"),
            new CalendarEvent("Event 3", "2020-06-25T12:00:00Z", "2020-06-25T13:00:00Z"),
            new CalendarEvent("Event 4", "2020-06-25T13:30:00Z", "2020-06-25T14:00:00Z"),
            new CalendarEvent("Event 5", "2020-06-25T15:00:00Z", "2020-06-25T15:30:00Z"),
            new CalendarEvent("Event 6", "2020-06-25T17:30:00Z", "2020-06-25T18:00:00Z"));
    Collection<Task> tasks = Arrays.asList(new Task("Task 1", "First task", 15, LOW_PRIORITY),
        new Task("Task 2", "Second task", 30, LOW_PRIORITY),
        new Task("Task 3", "Third task", 45, LOW_PRIORITY),
        new Task("Task 4", "Fourth task", 15, LOW_PRIORITY),
        new Task("Task 5", "Fifth task", 15, LOW_PRIORITY),
        new Task("Task 6", "Sixth task", 45, LOW_PRIORITY),
        new Task("Task 7", "Seventh task", 30, LOW_PRIORITY),
        new Task("Task 8", "Eigth task", 15, LOW_PRIORITY),
        new Task("Task 9", "Ninth task", 30, LOW_PRIORITY));
    Collection<Task> actual = schedule.greedy(events, tasks, TIME_0900, TIME_1800);

    List<Task> expected =
        Arrays.asList(new Task("Task 1", "First task", 15, LOW_PRIORITY, "2020-06-25T09:30:00Z"),
            new Task("Task 4", "Fourth task", 15, LOW_PRIORITY, "2020-06-25T09:45:00Z"),
            new Task("Task 5", "Fifth task", 15, LOW_PRIORITY, "2020-06-25T10:45:00Z"),
            new Task("Task 8", "Eigth task", 15, LOW_PRIORITY, "2020-06-25T11:00:00Z"),
            new Task("Task 2", "Second task", 30, LOW_PRIORITY, "2020-06-25T11:15:00Z"),
            new Task("Task 7", "Seventh task", 30, LOW_PRIORITY, "2020-06-25T13:00:00Z"),
            new Task("Task 9", "Ninth task", 30, LOW_PRIORITY, "2020-06-25T14:00:00Z"),
            new Task("Task 3", "Third task", 45, LOW_PRIORITY, "2020-06-25T15:30:00Z"),
            new Task("Task 6", "Sixth task", 45, LOW_PRIORITY, "2020-06-25T16:15:00Z"));

    Assert.assertEquals(expected, actual);
  }

  // TODO(tomasalvarez): Test edge cases
  // 1: Events out of the scheduling bounds
  // 2: Scheduling time is more than 24 hours
  // 3: ???
}
