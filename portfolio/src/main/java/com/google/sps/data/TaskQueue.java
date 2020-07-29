package com.google.sps.data;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * This class holds a List of tasks and includes functions to sort
 * the List and retrieve elements.
 */
public class TaskGroup {
  private final static int DEFAULT_QUEUE_SIZE = 15;

  private final PriorityQueue<Task> tasks;

  // Comparators declared here are those used by different scheduling algorithms.
  private static final Comparator<Task> sortByTaskDurationThenName =
      Comparator.comparing(Task::getDuration).thenComparing(Task::getName);

  /**
   * The TaskGroup constructor takes in an unsorted list of Tasks and
   * the SchedulingAlgorithmType which it then uses to create a PriorityQueue
   * using a Comparator specifically for the algorithm type. Once the
   * PriorityQueue is created, all the tasks are added.
   */
  public TaskGroup(List<Task> taskList, SchedulingAlgorithmType schedulingAlgorithmType) {
    if (taskList == null) {
      throw new IllegalArgumentException("Tasklist cannot be null");
    }
    if (schedulingAlgorithmType == null) {
      throw new IllegalArgumentException("SchedulingAlgorithmType must be passed in at construction");
    }

    this.tasks = getQueueFromAlgorithmType(schedulingAlgorithmType);
    this.tasks.addAll(taskList);
  }

  /**
   * This method takes in an enumarated SchedulingAlgorithmType and returns
   * a PriorityQueue constructed using the Comparator appropriate for the
   * SchedulingAlgorithmType that was passed in.
   */
  private PriorityQueue<Task> getQueueFromAlgorithmType(SchedulingAlgorithmType schedulingAlgorithmType) {
    switch(schedulingAlgorithmType) {
      case SHORTEST_TASK_FIRST:
        return new PriorityQueue<Task>(DEFAULT_QUEUE_SIZE, sortByTaskDurationThenName);
      default: 
        throw new IllegalArgumentException("SchedulingAlgorithmType not recognized");
    }
  }

  public Task peek() {
    return tasks.peek();
  }

  public void remove() {
    tasks.poll();
  }

  public boolean isEmpty() {
    return tasks.isEmpty();
  }
}
