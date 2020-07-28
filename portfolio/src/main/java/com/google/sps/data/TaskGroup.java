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
  private int tasksIndex;

  // Comparators declared here are those used by different scheduling algorithms.
  private static final Comparator<Task> sortByTaskDurationThenName =
      Comparator.comparing(Task::getDuration).thenComparing(Task::getName);

  /**
   * The TaskGroup constructor takes in an unsorted list of Tasks and
   * sets tasksIndex variable to 0 as to start the user's retrieval of 
   * objects at the beginning of the list.
   */
  public TaskGroup(List<Task> taskList, SchedulingAlgorithmType schedulingAlgorithmType) {
    if (taskList == null) {
      throw new IllegalArgumentException("Tasklist cannot be null");
    }
    if (schedulingAlgorithmType == null) {
      throw new IllegalArgumentException("SchedulingAlgorithmType must be passed in at construction");
    }

    this.tasksIndex = 0;
    this.tasks = getQueueFromAlgorithmType(schedulingAlgorithmType);
    this.tasks.addAll(taskList);
  }

  /**
   * This method takes in an enumarated SchedulingAlgorithmType and sorts
   * the Task list to the algorithm's needs using the Comparators declared
   * at the beginning of this class.
  */
public PriorityQueue<Task> getQueueFromAlgorithmType(SchedulingAlgorithmType schedulingAlgorithmType) {
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
    return tasks.size() == 0;
  }
}