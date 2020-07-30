package com.google.sps.data;

import java.util.Arrays;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class ShortestTaskFirstSchedulerTest {

  /** Makes sure we return an empty list in the case where no tasks are passed. */
  @Test
  public void noTasksScheduled() {
    ShortestTaskFirstScheduler shortestTaskFirstScheduler = new ShortestTaskFirstScheduler();

    Collection<CalendarEvent> events =
        Arrays.asList(
            new CalendarEvent(
                "Event 1", SchedulerTestUtil.BEGINNING_OF_DAY, SchedulerTestUtil.END_OF_DAY));
    Collection<Task> tasks = Arrays.asList();

    Collection<ScheduledTask> actual =
        shortestTaskFirstScheduler.schedule(
            events, tasks, SchedulerTestUtil.TIME_0900, SchedulerTestUtil.TIME_1700);
    Collection<ScheduledTask> expected = Arrays.asList();

    Assert.assertEquals(expected, actual);
  }

  /**
   * Makes sure no errors are thrown in the case where no events are passed and we schedule the
   * tasks. It also checks that we begin scheduling after the proposed startTime.
   */
  @Test
  public void noEventsScheduled() {
    ShortestTaskFirstScheduler shortestTaskFirstScheduler = new ShortestTaskFirstScheduler();

    Collection<CalendarEvent> events = Arrays.asList();
    Task task1 =
        new Task(
            "Task 1",
            "First task",
            SchedulerTestUtil.DURATION_30_MINUTES,
            SchedulerTestUtil.PRIORITY_ONE);
    Collection<Task> tasks = Arrays.asList(task1);
    ScheduledTask scheduledTask1 =
        new ScheduledTask(
            task1, SchedulerTestUtil.TIME_0900, SchedulerTestUtil.completelyScheduled);

    Collection<ScheduledTask> actual =
        shortestTaskFirstScheduler.schedule(
            events, tasks, SchedulerTestUtil.TIME_0900, SchedulerTestUtil.TIME_1700);
    Collection<ScheduledTask> expected = Arrays.asList(scheduledTask1);
    Assert.assertEquals(expected, actual);
  }

  /** Makes sure that tasks are being scheduled from shortest to longest. */
  @Test
  public void sortingTasksCorrectly() {
    ShortestTaskFirstScheduler shortestTaskFirstScheduler = new ShortestTaskFirstScheduler();

    Collection<CalendarEvent> events = Arrays.asList();
    Task task1 =
        new Task(
            "Task 1",
            "First task",
            SchedulerTestUtil.DURATION_30_MINUTES,
            SchedulerTestUtil.PRIORITY_ONE);
    Task task2 =
        new Task(
            "Task 2",
            "Second task",
            SchedulerTestUtil.DURATION_30_MINUTES,
            SchedulerTestUtil.PRIORITY_ONE);
    Task task3 =
        new Task(
            "Task 3",
            "Third task",
            SchedulerTestUtil.DURATION_20_MINUTES,
            SchedulerTestUtil.PRIORITY_ONE);
    Task task4 =
        new Task(
            "Task 4",
            "Fourth task",
            SchedulerTestUtil.DURATION_45_MINUTES,
            SchedulerTestUtil.PRIORITY_ONE);
    Collection<Task> tasks = Arrays.asList(task1, task2, task3, task4);
    // Scheduled tasks throughout the file are meant to correspond with the
    // same numbered regular task, they are not numbered by their startTime.
    ScheduledTask scheduledTask1 =
        new ScheduledTask(
            task1, SchedulerTestUtil.TIME_0920, SchedulerTestUtil.completelyScheduled);
    ScheduledTask scheduledTask2 =
        new ScheduledTask(
            task2, SchedulerTestUtil.TIME_0950, SchedulerTestUtil.completelyScheduled);
    ScheduledTask scheduledTask3 =
        new ScheduledTask(
            task3, SchedulerTestUtil.TIME_0900, SchedulerTestUtil.completelyScheduled);
    ScheduledTask scheduledTask4 =
        new ScheduledTask(
            task4, SchedulerTestUtil.TIME_1020, SchedulerTestUtil.completelyScheduled);

    Collection<ScheduledTask> actual =
        shortestTaskFirstScheduler.schedule(
            events, tasks, SchedulerTestUtil.TIME_0900, SchedulerTestUtil.TIME_1700);
    Collection<ScheduledTask> expected =
        Arrays.asList(scheduledTask3, scheduledTask1, scheduledTask2, scheduledTask4);

    Assert.assertEquals(expected, actual);
  }

  /**
   * Makes sure that tasks are being scheduled only during periods of time that contain no events.
   */
  @Test
  public void schedulingOnlyInAvailableTimes() {
    ShortestTaskFirstScheduler shortestTaskFirstScheduler = new ShortestTaskFirstScheduler();
    Collection<CalendarEvent> events =
        Arrays.asList(
            new CalendarEvent("Event 1", SchedulerTestUtil.TIME_0920, SchedulerTestUtil.TIME_1000),
            new CalendarEvent("Event 2", SchedulerTestUtil.TIME_1030, SchedulerTestUtil.TIME_1700));

    Task task1 =
        new Task(
            "Task 1",
            "First task",
            SchedulerTestUtil.DURATION_20_MINUTES,
            SchedulerTestUtil.PRIORITY_ONE);
    Task task2 =
        new Task(
            "Task 2",
            "Second task",
            SchedulerTestUtil.DURATION_30_MINUTES,
            SchedulerTestUtil.PRIORITY_ONE);
    Collection<Task> tasks = Arrays.asList(task1, task2);
    ScheduledTask scheduledTask1 =
        new ScheduledTask(
            task1, SchedulerTestUtil.TIME_0900, SchedulerTestUtil.completelyScheduled);
    ScheduledTask scheduledTask2 =
        new ScheduledTask(
            task2, SchedulerTestUtil.TIME_1000, SchedulerTestUtil.completelyScheduled);

    Collection<ScheduledTask> actual =
        shortestTaskFirstScheduler.schedule(
            events, tasks, SchedulerTestUtil.TIME_0900, SchedulerTestUtil.TIME_1700);
    Collection<ScheduledTask> expected = Arrays.asList(scheduledTask1, scheduledTask2);
    Assert.assertEquals(expected, actual);
  }

  /** Makes sure that no errors are thrown for events fully outside working hours. */
  @Test
  public void eventTotallyOutsideWorkingHours() {
    ShortestTaskFirstScheduler shortestTaskFirstScheduler = new ShortestTaskFirstScheduler();

    Collection<CalendarEvent> events =
        Arrays.asList(
            new CalendarEvent("Event 1", SchedulerTestUtil.TIME_1800, SchedulerTestUtil.TIME_2000));
    Task task1 =
        new Task(
            "Task 1",
            "First task",
            SchedulerTestUtil.DURATION_30_MINUTES,
            SchedulerTestUtil.PRIORITY_ONE);
    Collection<Task> tasks = Arrays.asList(task1);
    ScheduledTask scheduledTask1 =
        new ScheduledTask(
            task1, SchedulerTestUtil.TIME_0900, SchedulerTestUtil.completelyScheduled);
    Collection<ScheduledTask> actual =
        shortestTaskFirstScheduler.schedule(
            events, tasks, SchedulerTestUtil.TIME_0900, SchedulerTestUtil.TIME_1700);
    Collection<ScheduledTask> expected = Arrays.asList(scheduledTask1);
    Assert.assertEquals(expected, actual);
  }

  /** Makes sure that no errors are thrown for events partially outside working hours. */
  @Test
  public void eventPartiallyOutsideWorkingHours() {
    ShortestTaskFirstScheduler shortestTaskFirstScheduler = new ShortestTaskFirstScheduler();

    Collection<CalendarEvent> events =
        Arrays.asList(
            new CalendarEvent("Event 1", SchedulerTestUtil.TIME_0830, SchedulerTestUtil.TIME_0930));
    Task task1 =
        new Task(
            "Task 1",
            "First task",
            SchedulerTestUtil.DURATION_30_MINUTES,
            SchedulerTestUtil.PRIORITY_ONE);
    Collection<Task> tasks = Arrays.asList(task1);
    ScheduledTask scheduledTask1 =
        new ScheduledTask(
            task1, SchedulerTestUtil.TIME_0930, SchedulerTestUtil.completelyScheduled);
    Collection<ScheduledTask> actual =
        shortestTaskFirstScheduler.schedule(
            events, tasks, SchedulerTestUtil.TIME_0900, SchedulerTestUtil.TIME_1700);
    Collection<ScheduledTask> expected = Arrays.asList(scheduledTask1);
    Assert.assertEquals(expected, actual);
  }

  /** Makes sure that no errors are thrown if working hours is longer than a 24 hour period. */
  @Test
  public void workingPeriodExceedsTwentyFourHours() {
    ShortestTaskFirstScheduler shortestTaskFirstScheduler = new ShortestTaskFirstScheduler();

    Collection<CalendarEvent> events = Arrays.asList();
    Task task1 =
        new Task(
            "Task 1",
            "First task",
            SchedulerTestUtil.DURATION_30_MINUTES,
            SchedulerTestUtil.PRIORITY_ONE);
    Collection<Task> tasks = Arrays.asList(task1);
    ScheduledTask scheduledTask1 =
        new ScheduledTask(
            task1, SchedulerTestUtil.TIME_0900, SchedulerTestUtil.completelyScheduled);

    Collection<ScheduledTask> actual =
        shortestTaskFirstScheduler.schedule(
            events, tasks, SchedulerTestUtil.TIME_0900, SchedulerTestUtil.THREE_DAYS_LATER);
    Collection<ScheduledTask> expected = Arrays.asList(scheduledTask1);
    Assert.assertEquals(expected, actual);
  }

  /** Makes sure no errors are thrown if we have more tasks than we can possibly schedule. */
  @Test
  public void tooManyTasksShouldNotThrowError() {
    ShortestTaskFirstScheduler shortestTaskFirstScheduler = new ShortestTaskFirstScheduler();

    Collection<CalendarEvent> events =
        Arrays.asList(
            new CalendarEvent("Event 1", SchedulerTestUtil.TIME_1000, SchedulerTestUtil.TIME_1800));
    Task task1 =
        new Task(
            "Task 1",
            "First task",
            SchedulerTestUtil.DURATION_30_MINUTES,
            SchedulerTestUtil.PRIORITY_ONE);
    Task task2 =
        new Task(
            "Task 2",
            "Second task",
            SchedulerTestUtil.DURATION_30_MINUTES,
            SchedulerTestUtil.PRIORITY_ONE);
    Task task3 =
        new Task(
            "Task 3",
            "Third task",
            SchedulerTestUtil.DURATION_20_MINUTES,
            SchedulerTestUtil.PRIORITY_ONE);
    Task task4 =
        new Task(
            "Task 4",
            "Fourth task",
            SchedulerTestUtil.DURATION_45_MINUTES,
            SchedulerTestUtil.PRIORITY_ONE);
    Collection<Task> tasks = Arrays.asList(task1, task2, task3, task4);
    ScheduledTask scheduledTask1 =
        new ScheduledTask(
            task1, SchedulerTestUtil.TIME_0920, SchedulerTestUtil.completelyScheduled);
    ScheduledTask scheduledTask3 =
        new ScheduledTask(
            task3, SchedulerTestUtil.TIME_0900, SchedulerTestUtil.completelyScheduled);

    Collection<ScheduledTask> actual =
        shortestTaskFirstScheduler.schedule(
            events, tasks, SchedulerTestUtil.TIME_0900, SchedulerTestUtil.TIME_1000);
    Collection<ScheduledTask> expected = Arrays.asList(scheduledTask3, scheduledTask1);
    Assert.assertEquals(expected, actual);
  }
}
