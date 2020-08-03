package com.google.sps.data;

import java.util.Arrays;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class HighestPriorityFirstSchedulerTest {

  /** Makes sure we return an empty list in the case where no tasks are passed. */
  @Test
  public void noTasksScheduled() {
    HighestPriorityFirstScheduler highestPriorityFirstScheduler =
        new HighestPriorityFirstScheduler();

    Collection<CalendarEvent> events =
        Arrays.asList(
            new CalendarEvent("Event 1", SchedulerTestUtil.TIME_1030, SchedulerTestUtil.TIME_1130));
    Collection<Task> tasks = Arrays.asList();

    Collection<ScheduledTask> actual =
        highestPriorityFirstScheduler.schedule(
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
    HighestPriorityFirstScheduler highestPriorityFirstScheduler =
        new HighestPriorityFirstScheduler();

    Collection<CalendarEvent> events = Arrays.asList();
    Task task1 =
        new Task(
            "Task 1",
            "First task",
            SchedulerTestUtil.DURATION_15_MINUTES,
            SchedulerTestUtil.PRIORITY_ONE);
    Task task2 =
        new Task(
            "Task 2",
            "Second task",
            SchedulerTestUtil.DURATION_30_MINUTES,
            SchedulerTestUtil.PRIORITY_TWO);
    Task task3 =
        new Task(
            "Task 3",
            "Third task",
            SchedulerTestUtil.DURATION_20_MINUTES,
            SchedulerTestUtil.PRIORITY_THREE);
    Collection<Task> tasks = Arrays.asList(task1, task2, task3);
    ScheduledTask scheduledTask1 =
        new ScheduledTask(
            task1, SchedulerTestUtil.TIME_0950, SchedulerTestUtil.completelyScheduled);
    ScheduledTask scheduledTask2 =
        new ScheduledTask(
            task2, SchedulerTestUtil.TIME_0920, SchedulerTestUtil.completelyScheduled);
    ScheduledTask scheduledTask3 =
        new ScheduledTask(
            task3, SchedulerTestUtil.TIME_0900, SchedulerTestUtil.completelyScheduled);

    Collection<ScheduledTask> actual =
        highestPriorityFirstScheduler.schedule(
            events, tasks, SchedulerTestUtil.TIME_0900, SchedulerTestUtil.TIME_1700);
    Collection<ScheduledTask> expected =
        Arrays.asList(scheduledTask3, scheduledTask2, scheduledTask1);
    Assert.assertEquals(expected, actual);
  }

  /** Makes sure that tasks are being scheduled from highest to lowest priority. */
  @Test
  public void sortingTasksCorrectly() {
    HighestPriorityFirstScheduler highestPriorityFirstScheduler =
        new HighestPriorityFirstScheduler();

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
            SchedulerTestUtil.PRIORITY_TWO);
    Task task3 =
        new Task(
            "Task 3",
            "Third task",
            SchedulerTestUtil.DURATION_30_MINUTES,
            SchedulerTestUtil.PRIORITY_THREE);
    Task task4 =
        new Task(
            "Task 4",
            "Fourth task",
            SchedulerTestUtil.DURATION_30_MINUTES,
            SchedulerTestUtil.PRIORITY_FOUR);
    Task task5 =
        new Task(
            "Task 5",
            "Fifth task",
            SchedulerTestUtil.DURATION_30_MINUTES,
            SchedulerTestUtil.PRIORITY_FIVE);
    Collection<Task> tasks = Arrays.asList(task1, task2, task3, task4, task5);
    // Scheduled tasks throughout the file are meant to correspond with the
    // same numbered regular task, they are not numbered by their startTime.
    ScheduledTask scheduledTask1 =
        new ScheduledTask(
            task1, SchedulerTestUtil.TIME_1100, SchedulerTestUtil.completelyScheduled);
    ScheduledTask scheduledTask2 =
        new ScheduledTask(
            task2, SchedulerTestUtil.TIME_1030, SchedulerTestUtil.completelyScheduled);
    ScheduledTask scheduledTask3 =
        new ScheduledTask(
            task3, SchedulerTestUtil.TIME_1000, SchedulerTestUtil.completelyScheduled);
    ScheduledTask scheduledTask4 =
        new ScheduledTask(
            task4, SchedulerTestUtil.TIME_0930, SchedulerTestUtil.completelyScheduled);
    ScheduledTask scheduledTask5 =
        new ScheduledTask(
            task5, SchedulerTestUtil.TIME_0900, SchedulerTestUtil.completelyScheduled);

    Collection<ScheduledTask> actual =
        highestPriorityFirstScheduler.schedule(
            events, tasks, SchedulerTestUtil.TIME_0900, SchedulerTestUtil.TIME_1700);
    Collection<ScheduledTask> expected =
        Arrays.asList(
            scheduledTask5, scheduledTask4, scheduledTask3, scheduledTask2, scheduledTask1);

    Assert.assertEquals(expected, actual);
  }

  /**
   * Makes sure that tasks are being scheduled only during periods of time that contain no events.
   */
  @Test
  public void schedulingOnlyInAvailableTimes() {
    HighestPriorityFirstScheduler highestPriorityFirstScheduler =
        new HighestPriorityFirstScheduler();
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
            SchedulerTestUtil.PRIORITY_TWO);
    Collection<Task> tasks = Arrays.asList(task1, task2);
    ScheduledTask scheduledTask1 =
        new ScheduledTask(
            task1, SchedulerTestUtil.TIME_0900, SchedulerTestUtil.completelyScheduled);
    ScheduledTask scheduledTask2 =
        new ScheduledTask(
            task2, SchedulerTestUtil.TIME_1000, SchedulerTestUtil.completelyScheduled);

    Collection<ScheduledTask> actual =
        highestPriorityFirstScheduler.schedule(
            events, tasks, SchedulerTestUtil.TIME_0900, SchedulerTestUtil.TIME_1700);
    Collection<ScheduledTask> expected = Arrays.asList(scheduledTask1, scheduledTask2);
    Assert.assertEquals(expected, actual);
  }

  /** Makes sure that no errors are thrown for events fully outside working hours. */
  @Test
  public void eventTotallyOutsideWorkingHours() {
    HighestPriorityFirstScheduler highestPriorityFirstScheduler =
        new HighestPriorityFirstScheduler();

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
        highestPriorityFirstScheduler.schedule(
            events, tasks, SchedulerTestUtil.TIME_0900, SchedulerTestUtil.TIME_1700);
    Collection<ScheduledTask> expected = Arrays.asList(scheduledTask1);
    Assert.assertEquals(expected, actual);
  }

  /** Makes sure that no errors are thrown for events partially outside working hours. */
  @Test
  public void eventPartiallyOutsideWorkingHours() {
    HighestPriorityFirstScheduler highestPriorityFirstScheduler =
        new HighestPriorityFirstScheduler();

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
        highestPriorityFirstScheduler.schedule(
            events, tasks, SchedulerTestUtil.TIME_0900, SchedulerTestUtil.TIME_1700);
    Collection<ScheduledTask> expected = Arrays.asList(scheduledTask1);
    Assert.assertEquals(expected, actual);
  }

  /** Makes sure that no errors are thrown if working hours is longer than a 24 hour period. */
  @Test
  public void workingPeriodExceedsTwentyFourHours() {
    HighestPriorityFirstScheduler highestPriorityFirstScheduler =
        new HighestPriorityFirstScheduler();

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
        highestPriorityFirstScheduler.schedule(
            events, tasks, SchedulerTestUtil.TIME_0900, SchedulerTestUtil.THREE_DAYS_LATER);
    Collection<ScheduledTask> expected = Arrays.asList(scheduledTask1);
    Assert.assertEquals(expected, actual);
  }

  /** Expected normal behavior if not all tasks can be scheduled. */
  @Test
  public void tooManyTasksShouldNotThrowError() {
    HighestPriorityFirstScheduler highestPriorityFirstScheduler =
        new HighestPriorityFirstScheduler();

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
            SchedulerTestUtil.PRIORITY_TWO);
    Task task3 =
        new Task(
            "Task 3",
            "Third task",
            SchedulerTestUtil.DURATION_20_MINUTES,
            SchedulerTestUtil.PRIORITY_FOUR);
    Task task4 =
        new Task(
            "Task 4",
            "Fourth task",
            SchedulerTestUtil.DURATION_45_MINUTES,
            SchedulerTestUtil.PRIORITY_THREE);
    Collection<Task> tasks = Arrays.asList(task1, task2, task3, task4);
    ScheduledTask scheduledTask2 =
        new ScheduledTask(
            task2, SchedulerTestUtil.TIME_0920, SchedulerTestUtil.completelyScheduled);
    ScheduledTask scheduledTask3 =
        new ScheduledTask(
            task3, SchedulerTestUtil.TIME_0900, SchedulerTestUtil.completelyScheduled);

    Collection<ScheduledTask> actual =
        highestPriorityFirstScheduler.schedule(
            events, tasks, SchedulerTestUtil.TIME_0900, SchedulerTestUtil.TIME_1000);
    Collection<ScheduledTask> expected = Arrays.asList(scheduledTask3, scheduledTask2);

    Assert.assertEquals(expected, actual);
  }
}
