package com.google.sps.data;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class TaskQueueTest {
  private static final SchedulingAlgorithmType SHORTEST_TASK_FIRST =
      SchedulingAlgorithmType.SHORTEST_TASK_FIRST;
  private static final SchedulingAlgorithmType HIGHEST_PRIORITY_FIRST =
      SchedulingAlgorithmType.HIGHEST_PRIORITY_FIRST;

  private static Duration DURATION_15_MINUTES = Duration.ofSeconds(15 * 60);
  private static Duration DURATION_20_MINUTES = Duration.ofSeconds(20 * 60);
  private static Duration DURATION_30_MINUTES = Duration.ofSeconds(30 * 60);
  private static Duration DURATION_45_MINUTES = Duration.ofSeconds(45 * 60);
  private static Duration DURATION_60_MINUTES = Duration.ofSeconds(60 * 60);

  private static final int DEFAULT_QUEUE_SIZE = 15;

  private final TaskPriority LOW_PRIORITY = new TaskPriority(1);

  /**
   * Expect the TaskQueue constructor to throw an IllegalArgumentException if null is passed in
   * place of a list of Tasks.
   */
  @Test(expected = IllegalArgumentException.class)
  public void passNullForList() {
    TaskQueue taskQueue = new TaskQueue(null, SHORTEST_TASK_FIRST);
  }

  /**
   * Expect the TaskQueue constructor to throw an IllegalArgumentException if null is passed in
   * place of a SchedulingAlgorithmType.
   */
  @Test(expected = IllegalArgumentException.class)
  public void passNullForAlgorithmType() {
    Task task1 = new Task("Task 1", "First task", DURATION_30_MINUTES, LOW_PRIORITY);
    Task task2 = new Task("Task 2", "Second task", DURATION_30_MINUTES, LOW_PRIORITY);
    Task task3 = new Task("Task 3", "Third task", DURATION_30_MINUTES, LOW_PRIORITY);

    List<Task> taskList = Arrays.asList(task1, task2, task3);
    TaskQueue taskQueue = new TaskQueue(taskList, null);
  }

  /** Ensure that the PriorityQueue is correctly sorting the tasks. */
  @Test
  public void checkCorrectSortingShortestTaskFirst() {
    Task task1 = new Task("Task 1", "First task", DURATION_15_MINUTES, LOW_PRIORITY);
    Task task2 = new Task("Task 2", "Second task", DURATION_20_MINUTES, LOW_PRIORITY);
    Task task3 = new Task("Task 3", "Third task", DURATION_30_MINUTES, LOW_PRIORITY);
    Task task4 = new Task("Task 4", "Fourth task", DURATION_45_MINUTES, LOW_PRIORITY);
    Task task5 = new Task("Task 5", "Fifth task", DURATION_60_MINUTES, LOW_PRIORITY);

    List<Task> taskList = Arrays.asList(task3, task1, task2, task5, task4);

    List<Task> expected = Arrays.asList(task1, task2, task3, task4, task5);
    List<Task> actual = new ArrayList<Task>();

    TaskQueue taskQueue = new TaskQueue(taskList, SHORTEST_TASK_FIRST);

    for (Task task : expected) {
      actual.add(taskQueue.peek());
      taskQueue.remove();
    }

    Assert.assertEquals(expected, actual);
  }

  /**
   * Ensure that the PriorityQueue is correctly sorting the tasks by Priority using the
   * HIGHEST_PRIORITY_FIRST comparator.
   */
  @Test
  public void checkCorrectPrioritySortingHighestPriorityFirst() {
    Task task1 =
        new Task("Task 1", "First task", DURATION_30_MINUTES, SchedulerTestUtil.PRIORITY_ONE);
    Task task2 =
        new Task("Task 2", "Second task", DURATION_30_MINUTES, SchedulerTestUtil.PRIORITY_FIVE);
    Task task3 =
        new Task("Task 3", "Third task", DURATION_30_MINUTES, SchedulerTestUtil.PRIORITY_TWO);
    Task task4 =
        new Task("Task 4", "Fourth task", DURATION_30_MINUTES, SchedulerTestUtil.PRIORITY_FOUR);
    Task task5 =
        new Task("Task 5", "Fifth task", DURATION_30_MINUTES, SchedulerTestUtil.PRIORITY_THREE);

    List<Task> taskList = Arrays.asList(task3, task1, task2, task5, task4);

    List<Task> expected = Arrays.asList(task2, task4, task5, task3, task1);
    List<Task> actual = new ArrayList<Task>();

    TaskQueue taskQueue = new TaskQueue(taskList, HIGHEST_PRIORITY_FIRST);

    for (Task task : expected) {
      actual.add(taskQueue.peek());
      taskQueue.remove();
    }

    Assert.assertEquals(expected, actual);
  }

  /**
   * Ensure that the PriorityQueue is correctly sorting the tasks from shortest to longest using the
   * HIGHEST_PRIORITY_FIRST comparator.
   */
  @Test
  public void checkCorrectSamePrioritySortingHighestPriorityFirst() {
    Task task1 =
        new Task(
            "Task 1",
            "First task",
            SchedulerTestUtil.DURATION_5_MINUTES,
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
            SchedulerTestUtil.DURATION_10_MINUTES,
            SchedulerTestUtil.PRIORITY_ONE);
    Task task4 =
        new Task(
            "Task 4",
            "Fourth task",
            SchedulerTestUtil.DURATION_15_MINUTES,
            SchedulerTestUtil.PRIORITY_ONE);
    Task task5 =
        new Task(
            "Task 5",
            "Fifth task",
            SchedulerTestUtil.DURATION_20_MINUTES,
            SchedulerTestUtil.PRIORITY_ONE);

    List<Task> taskList = Arrays.asList(task3, task1, task2, task5, task4);

    List<Task> expected = Arrays.asList(task1, task3, task4, task5, task2);
    List<Task> actual = new ArrayList<Task>();

    TaskQueue taskQueue = new TaskQueue(taskList, HIGHEST_PRIORITY_FIRST);

    for (Task task : expected) {
      actual.add(taskQueue.peek());
      taskQueue.remove();
    }

    Assert.assertEquals(expected, actual);
  }

  /**
   * Ensure that the PriorityQueue is correctly sorting the tasks from shortest to longest and
   * highest priority to lowest using the HIGHEST_PRIORITY_FIRST comparator.
   */
  @Test
  public void checkCorrectSortingHighestPriorityFirst() {
    Task task1 =
        new Task(
            "Task 1",
            "First task",
            SchedulerTestUtil.DURATION_20_MINUTES,
            SchedulerTestUtil.PRIORITY_TWO);
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
            SchedulerTestUtil.PRIORITY_FOUR);
    Task task4 =
        new Task(
            "Task 4",
            "Fourth task",
            SchedulerTestUtil.DURATION_30_MINUTES,
            SchedulerTestUtil.PRIORITY_THREE);
    Task task5 =
        new Task(
            "Task 5",
            "Fifth task",
            SchedulerTestUtil.DURATION_30_MINUTES,
            SchedulerTestUtil.PRIORITY_FOUR);
    Task task6 =
        new Task(
            "Task 6",
            "Sixth task",
            SchedulerTestUtil.DURATION_30_MINUTES,
            SchedulerTestUtil.PRIORITY_FIVE);

    List<Task> taskList = Arrays.asList(task3, task1, task2, task5, task4, task6);

    List<Task> expected = Arrays.asList(task6, task3, task5, task2, task4, task1);
    List<Task> actual = new ArrayList<Task>();

    TaskQueue taskQueue = new TaskQueue(taskList, HIGHEST_PRIORITY_FIRST);

    for (Task task : expected) {
      actual.add(taskQueue.peek());
      taskQueue.remove();
    }

    Assert.assertEquals(expected, actual);
  }
}
