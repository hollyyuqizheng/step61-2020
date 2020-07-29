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

  private static Duration DURATION_15_MINUTES = Duration.ofSeconds(15 * 60);
  private static Duration DURATION_20_MINUTES = Duration.ofSeconds(20 * 60);
  private static Duration DURATION_30_MINUTES = Duration.ofSeconds(30 * 60);
  private static Duration DURATION_45_MINUTES = Duration.ofSeconds(45 * 60);
  private static Duration DURATION_60_MINUTES = Duration.ofSeconds(60 * 60);

  private static final int DEFAULT_QUEUE_SIZE = 15;
  
  private final TaskPriority LOW_PRIORITY = new TaskPriority(1);

  /**
   * Expect the TaskQueue constructor to throw an IllegalArgumentException if 
   * null is passed in place of a list of Tasks.
   */
  @Test(expected = IllegalArgumentException.class)
  public void passNullForList() {
    TaskQueue taskQueue = new TaskQueue(null, SHORTEST_TASK_FIRST);
  }

  /**
   * Expect the TaskQueue constructor to throw an IllegalArgumentException if
   * null is passed in place of a SchedulingAlgorithmType.
   */
  @Test(expected = IllegalArgumentException.class)
  public void passNullForAlgorithmType() {
    Task task1 = new Task("Task 1", "First task", DURATION_30_MINUTES, LOW_PRIORITY);
    Task task2 = new Task("Task 2", "Second task", DURATION_30_MINUTES, LOW_PRIORITY);
    Task task3 = new Task("Task 3", "Third task", DURATION_30_MINUTES, LOW_PRIORITY);

    List<Task> taskList = Arrays.asList(task1, task2, task3);
    TaskQueue taskQueue = new TaskQueue(taskList, null);
  }

  /**
   * Ensure that the PriorityQueue is correctly sorting the tasks.
   */
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

    for (int i = 0; i < expected.size(); i++) {
      actual.add(taskQueue.peek());
      taskQueue.remove();
    }

    Assert.assertEquals(expected, actual);
  }
}
