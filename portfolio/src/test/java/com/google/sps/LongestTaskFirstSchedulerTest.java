package com.google.sps.data;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class LongestTaskFirstSchedulerTest {

  /** Tests that the comparator sorts tasks by duration in descending order. */
  @Test
  public void testTaskComparator() {
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
            SchedulerTestUtil.DURATION_15_MINUTES,
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
    List<Task> tasks = Arrays.asList(task1, task2, task3, task4);

    Collections.sort(tasks, LongestTaskFirstScheduler.sortByTaskDurationDescendingThenName);

    List<Task> expected = Arrays.asList(task4, task1, task3, task2);
    Assert.assertEquals(expected, tasks);
  }

  /**
   * Tests for a basic scenario where all free time ranges can be scheduled. Tests for the ordering
   * of the test (longest task first, and then alphabetical order).
   */
  @Test
  public void testLongestFirst() {
    // Working hours:   |-----------------------------------------|
    // Events:               |---|     |--------------------------|
    // Scheduled tasks: |-A--|   |-B-|C|
    Collection<CalendarEvent> events =
        Arrays.asList(
            new CalendarEvent("Event 1", SchedulerTestUtil.TIME_0920, SchedulerTestUtil.TIME_1000),
            new CalendarEvent("Event 2", SchedulerTestUtil.TIME_1030, SchedulerTestUtil.TIME_1700));

    Task task1 =
        new Task(
            "Task A", "A", SchedulerTestUtil.DURATION_20_MINUTES, SchedulerTestUtil.PRIORITY_ONE);
    Task task2 =
        new Task(
            "Task B", "B", SchedulerTestUtil.DURATION_10_MINUTES, SchedulerTestUtil.PRIORITY_ONE);
    Task task3 =
        new Task(
            "Task C", "C", SchedulerTestUtil.DURATION_20_MINUTES, SchedulerTestUtil.PRIORITY_ONE);
    Collection<Task> tasks = Arrays.asList(task1, task2, task3);

    LongestTaskFirstScheduler longestTaskFirstScheduler = new LongestTaskFirstScheduler();
    Collection<ScheduledTask> actual =
        longestTaskFirstScheduler.schedule(
            events, tasks, SchedulerTestUtil.TIME_0900, SchedulerTestUtil.TIME_1700);

    ScheduledTask scheduledTask1 = new ScheduledTask(task1, SchedulerTestUtil.TIME_0900);
    ScheduledTask scheduledTask3 = new ScheduledTask(task3, SchedulerTestUtil.TIME_1000);
    ScheduledTask scheduledTask2 = new ScheduledTask(task2, SchedulerTestUtil.TIME_1020);
    Collection<ScheduledTask> expected =
        Arrays.asList(scheduledTask1, scheduledTask3, scheduledTask2);

    Assert.assertEquals(actual, expected);
  }

  /**
   * This scenario contains a task that is too long to be scheduled. The other tasks are scheduled
   * to the first time slot that has enough time for this task.
   */
  @Test
  public void testMaxTimeScheduled() {
    // Working hours:   |---------------------------------------------------------|
    // Events:                     |---|          |--------|    |-----------------|
    // Scheduled tasks: |-D-|-E-|      |--C--|             |-B--|
    Collection<CalendarEvent> events =
        Arrays.asList(
            new CalendarEvent("Event 1", SchedulerTestUtil.TIME_0930, SchedulerTestUtil.TIME_1000),
            new CalendarEvent("Event 2", SchedulerTestUtil.TIME_1130, SchedulerTestUtil.TIME_1200),
            new CalendarEvent("Event 3", SchedulerTestUtil.TIME_1300, SchedulerTestUtil.TIME_1700));

    Task task1 =
        new Task("Task A", "A", SchedulerTestUtil.DURATION_2_HOURS, SchedulerTestUtil.PRIORITY_ONE);
    Task task2 =
        new Task(
            "Task B", "B", SchedulerTestUtil.DURATION_60_MINUTES, SchedulerTestUtil.PRIORITY_ONE);
    Task task3 =
        new Task(
            "Task C", "C", SchedulerTestUtil.DURATION_80_MINUTES, SchedulerTestUtil.PRIORITY_ONE);
    Task task4 =
        new Task(
            "Task D", "D", SchedulerTestUtil.DURATION_15_MINUTES, SchedulerTestUtil.PRIORITY_ONE);
    Task task5 =
        new Task(
            "Task E", "E", SchedulerTestUtil.DURATION_5_MINUTES, SchedulerTestUtil.PRIORITY_ONE);
    Collection<Task> tasks = Arrays.asList(task1, task2, task3, task4, task5);

    LongestTaskFirstScheduler longestTaskFirstScheduler = new LongestTaskFirstScheduler();
    Collection<ScheduledTask> actual =
        longestTaskFirstScheduler.schedule(
            events, tasks, SchedulerTestUtil.TIME_0900, SchedulerTestUtil.TIME_1700);

    // Task A is too long to be scheduled.

    ScheduledTask scheduledTask2 = new ScheduledTask(task2, SchedulerTestUtil.TIME_1200);
    ScheduledTask scheduledTask3 = new ScheduledTask(task3, SchedulerTestUtil.TIME_1000);
    ScheduledTask scheduledTask4 = new ScheduledTask(task4, SchedulerTestUtil.TIME_0900);
    ScheduledTask scheduledTask5 = new ScheduledTask(task5, SchedulerTestUtil.TIME_0915);
    Collection<ScheduledTask> expected =
        Arrays.asList(scheduledTask3, scheduledTask2, scheduledTask4, scheduledTask5);
    Assert.assertEquals(actual, expected);
  }

  /**
   * Tests for tasks that are scheduled chronologically if there are multiple free time ranges of
   * the same length.
   */
  @Test
  public void testScheduleOrderByFreeTimeStart() {
    // Working hours:   |--------------------------------------------------------|
    // Events:                  |----|       |-----|           |-----------------|
    // Scheduled tasks: |---A---|    |---B---|     |---C---|
    Collection<CalendarEvent> events =
        Arrays.asList(
            new CalendarEvent("Event 1", SchedulerTestUtil.TIME_0930, SchedulerTestUtil.TIME_1000),
            new CalendarEvent("Event 2", SchedulerTestUtil.TIME_1030, SchedulerTestUtil.TIME_1100),
            new CalendarEvent("Event 3", SchedulerTestUtil.TIME_1200, SchedulerTestUtil.TIME_1700));

    Task task1 =
        new Task(
            "Task A", "A", SchedulerTestUtil.DURATION_30_MINUTES, SchedulerTestUtil.PRIORITY_ONE);
    Task task2 =
        new Task(
            "Task B", "B", SchedulerTestUtil.DURATION_30_MINUTES, SchedulerTestUtil.PRIORITY_ONE);
    Task task3 =
        new Task(
            "Task C", "C", SchedulerTestUtil.DURATION_30_MINUTES, SchedulerTestUtil.PRIORITY_ONE);
    Collection<Task> tasks = Arrays.asList(task1, task2, task3);

    LongestTaskFirstScheduler longestTaskFirstScheduler = new LongestTaskFirstScheduler();
    Collection<ScheduledTask> actual =
        longestTaskFirstScheduler.schedule(
            events, tasks, SchedulerTestUtil.TIME_0900, SchedulerTestUtil.TIME_1700);

    ScheduledTask scheduledTask1 = new ScheduledTask(task1, SchedulerTestUtil.TIME_0900);
    ScheduledTask scheduledTask2 = new ScheduledTask(task2, SchedulerTestUtil.TIME_1000);
    ScheduledTask scheduledTask3 = new ScheduledTask(task3, SchedulerTestUtil.TIME_1100);
    Collection<ScheduledTask> expected =
        Arrays.asList(scheduledTask1, scheduledTask2, scheduledTask3);

    Assert.assertEquals(actual, expected);
  }

  /** Tests for the scenario where none of the tasks can be scheduled. */
  @Test
  public void testNoTaskCanBeScheduled() {
    // Working hours:   |-----------------------------------------------------|
    // Events:                 |---|          |--------|    |-----------------|
    Collection<CalendarEvent> events =
        Arrays.asList(
            new CalendarEvent("Event 1", SchedulerTestUtil.TIME_0930, SchedulerTestUtil.TIME_1000),
            new CalendarEvent("Event 2", SchedulerTestUtil.TIME_1130, SchedulerTestUtil.TIME_1200),
            new CalendarEvent("Event 3", SchedulerTestUtil.TIME_1300, SchedulerTestUtil.TIME_1700));

    Task task1 =
        new Task("Task A", "A", SchedulerTestUtil.DURATION_2_HOURS, SchedulerTestUtil.PRIORITY_ONE);
    Task task2 =
        new Task(
            "Task B", "B", SchedulerTestUtil.DURATION_100_MINUTES, SchedulerTestUtil.PRIORITY_ONE);
    Collection<Task> tasks = Arrays.asList(task1, task2);

    LongestTaskFirstScheduler longestTaskFirstScheduler = new LongestTaskFirstScheduler();
    Collection<ScheduledTask> actual =
        longestTaskFirstScheduler.schedule(
            events, tasks, SchedulerTestUtil.TIME_0900, SchedulerTestUtil.TIME_1700);

    // None of the tasks can actually be scheduled.
    Assert.assertTrue(actual.isEmpty());
  }
}
