package com.google.sps.data;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


@RunWith(JUnit4.class)
public final class TaskQueueTest {
  private static final SchedulingAlgorithmType ALGORITHM_TYPE =
      SchedulingAlgorithmType.SHORTEST_TASK_FIRST;

  private static Duration DURATION_30_MINUTES = Duration.ofSeconds(30 * 60);

  private static final int DEFAULT_QUEUE_SIZE = 15;

  private static final Comparator<Task> sortByTaskDurationThenName =
      Comparator.comparing(Task::getDuration).thenComparing(Task::getName);
  
  private final TaskPriority LOW_PRIORITY = new TaskPriority(1);



  /**
   * Expect the TaskQueue constructor to throw an IllegalArgumentException if 
   * null is passed in place of a list of Tasks.
   */
  @Test(expected = IllegalArgumentException.class)
  public void passNullForList() {
    TaskQueue taskQueue = new TaskQueue(null, ALGORITHM_TYPE);
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
}
