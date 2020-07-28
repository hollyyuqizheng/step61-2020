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
  public void testTaskComparatorDuration() {
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

    Collections.sort(tasks, LongestTaskFirstScheduler.sortByTaskDurationDescendingThenPriority);

    List<Task> expected = Arrays.asList(task4, task1, task3, task2);
    Assert.assertEquals(expected, tasks);
  }

  /** Tests that the comparator also sorts tasks by priority in descending order. */
  @Test
  public void testTaskComparatorPriority() {
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
            SchedulerTestUtil.DURATION_20_MINUTES,
            SchedulerTestUtil.PRIORITY_THREE);
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
            SchedulerTestUtil.DURATION_20_MINUTES,
            SchedulerTestUtil.PRIORITY_TWO);
    List<Task> tasks = Arrays.asList(task1, task2, task3, task4);

    Collections.sort(tasks, LongestTaskFirstScheduler.sortByTaskDurationDescendingThenPriority);

    List<Task> expected = Arrays.asList(task1, task2, task4, task3);
    Assert.assertEquals(expected, tasks);
  }

  /**
   * Tests for a basic scenario where all free time ranges can be scheduled. Tests for the ordering
   * of the test (longest task first).
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

  /** Tests for the scenario where one of the tasks is split, and the other can't be scheduled. */
  @Test
  public void testOneTaskSplit() {
    // Working hours:   |-----------------------------------------------------|
    // Events:                 |---|          |--------|    |-----------------|
    // Scheduled:       |--A---|   |-----A----|

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

    // These are the shortened segments of task A.
    Task task1A =
        new Task(
            "Task A", "A", SchedulerTestUtil.DURATION_30_MINUTES, SchedulerTestUtil.PRIORITY_ONE);
    Task task1B =
        new Task(
            "Task A", "A", SchedulerTestUtil.DURATION_90_MINUTES, SchedulerTestUtil.PRIORITY_ONE);

    ScheduledTask scheduledTask1 = new ScheduledTask(task1A, SchedulerTestUtil.TIME_0900);
    ScheduledTask scheduledTask2 = new ScheduledTask(task1B, SchedulerTestUtil.TIME_1000);
    Collection<ScheduledTask> expected = Arrays.asList(scheduledTask1, scheduledTask2);

    Assert.assertEquals(actual, expected);
  }

  /**
   * In this scenario, A is split up into 2 blocks, C is split up into 2 blocks, B cannot be
   * scheduled, D is scheduled as a whole, and E cannot be scheduled in the end.
   */
  @Test
  public void testMaxTimeScheduled() {
    // Working hours:   |--------------------------------------------------------------------|
    // Events:                 |---|          |--------|         |--------|           |------|
    // Scheduled tasks: |--A---|   |----A-----|        |----C----|        |-C-|-D-|
    Collection<CalendarEvent> events =
        Arrays.asList(
            new CalendarEvent("Event 1", SchedulerTestUtil.TIME_0930, SchedulerTestUtil.TIME_1000),
            new CalendarEvent("Event 2", SchedulerTestUtil.TIME_1130, SchedulerTestUtil.TIME_1200),
            new CalendarEvent("Event 3", SchedulerTestUtil.TIME_1300, SchedulerTestUtil.TIME_1500),
            new CalendarEvent("Event 4", SchedulerTestUtil.TIME_1600, SchedulerTestUtil.TIME_1700));

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
            "Task D", "D", SchedulerTestUtil.DURATION_30_MINUTES, SchedulerTestUtil.PRIORITY_ONE);
    Task task5 =
        new Task(
            "Task E", "E", SchedulerTestUtil.DURATION_20_MINUTES, SchedulerTestUtil.PRIORITY_ONE);
    Collection<Task> tasks = Arrays.asList(task1, task2, task3, task4, task5);

    LongestTaskFirstScheduler longestTaskFirstScheduler = new LongestTaskFirstScheduler();
    Collection<ScheduledTask> actual =
        longestTaskFirstScheduler.schedule(
            events, tasks, SchedulerTestUtil.TIME_0900, SchedulerTestUtil.TIME_1700);

    Task task1A =
        new Task(
            "Task A", "A", SchedulerTestUtil.DURATION_30_MINUTES, SchedulerTestUtil.PRIORITY_ONE);
    Task task1B =
        new Task(
            "Task A", "A", SchedulerTestUtil.DURATION_90_MINUTES, SchedulerTestUtil.PRIORITY_ONE);
    Task task3A =
        new Task(
            "Task C", "C", SchedulerTestUtil.DURATION_60_MINUTES, SchedulerTestUtil.PRIORITY_ONE);
    Task task3B =
        new Task(
            "Task C", "C", SchedulerTestUtil.DURATION_20_MINUTES, SchedulerTestUtil.PRIORITY_ONE);
    ScheduledTask scheduledTask1 = new ScheduledTask(task1A, SchedulerTestUtil.TIME_0900);
    ScheduledTask scheduledTask2 = new ScheduledTask(task1B, SchedulerTestUtil.TIME_1000);
    ScheduledTask scheduledTask3 = new ScheduledTask(task3A, SchedulerTestUtil.TIME_1200);
    ScheduledTask scheduledTask4 = new ScheduledTask(task3B, SchedulerTestUtil.TIME_1500);
    ScheduledTask scheduledTask5 = new ScheduledTask(task4, SchedulerTestUtil.TIME_1520);
    Collection<ScheduledTask> expected =
        Arrays.asList(
            scheduledTask1, scheduledTask2, scheduledTask3, scheduledTask4, scheduledTask5);

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

  /** Tests for scenario where tasks of equal duration will be scheduled based on priority. */
  @Test
  public void testEqualDurationPriorityFirst() {
    // Working hours:   |-----------------------------------------|
    // Events:               |---|     |--------------------------|
    // Scheduled tasks: |-C--|   |--A--|
    Collection<CalendarEvent> events =
        Arrays.asList(
            new CalendarEvent("Event 1", SchedulerTestUtil.TIME_0930, SchedulerTestUtil.TIME_1000),
            new CalendarEvent("Event 2", SchedulerTestUtil.TIME_1030, SchedulerTestUtil.TIME_1700));

    Task task1 =
        new Task(
            "Task A", "A", SchedulerTestUtil.DURATION_30_MINUTES, SchedulerTestUtil.PRIORITY_ONE);
    Task task2 =
        new Task(
            "Task B", "B", SchedulerTestUtil.DURATION_10_MINUTES, SchedulerTestUtil.PRIORITY_ONE);
    Task task3 =
        new Task(
            "Task C", "C", SchedulerTestUtil.DURATION_30_MINUTES, SchedulerTestUtil.PRIORITY_THREE);
    Collection<Task> tasks = Arrays.asList(task1, task2, task3);

    LongestTaskFirstScheduler longestTaskFirstScheduler = new LongestTaskFirstScheduler();
    Collection<ScheduledTask> actual =
        longestTaskFirstScheduler.schedule(
            events, tasks, SchedulerTestUtil.TIME_0900, SchedulerTestUtil.TIME_1700);

    ScheduledTask scheduledTask3 = new ScheduledTask(task3, SchedulerTestUtil.TIME_0900);
    ScheduledTask scheduledTask1 = new ScheduledTask(task1, SchedulerTestUtil.TIME_1000);
    // Task B cannot be scheduled.

    Collection<ScheduledTask> expected = Arrays.asList(scheduledTask3, scheduledTask1);

    Assert.assertEquals(actual, expected);
  }

  /** Tests for the scenario where no task can be scheduled. */
  @Test
  public void testNoTaskScheduled() {
    // Working hours:   |-----------------------------------------|
    // Events:               |---|     |--------------------------|
    // Scheduled tasks:
    Collection<CalendarEvent> events =
        Arrays.asList(
            new CalendarEvent("Event 1", SchedulerTestUtil.TIME_0930, SchedulerTestUtil.TIME_1000),
            new CalendarEvent("Event 2", SchedulerTestUtil.TIME_1030, SchedulerTestUtil.TIME_1700));

    Task task1 =
        new Task(
            "Task A", "A", SchedulerTestUtil.DURATION_80_MINUTES, SchedulerTestUtil.PRIORITY_ONE);
    Collection<Task> tasks = Arrays.asList(task1);

    LongestTaskFirstScheduler longestTaskFirstScheduler = new LongestTaskFirstScheduler();
    Collection<ScheduledTask> actual =
        longestTaskFirstScheduler.schedule(
            events, tasks, SchedulerTestUtil.TIME_0900, SchedulerTestUtil.TIME_1700);

    Assert.assertTrue(actual.isEmpty());
  }
}
