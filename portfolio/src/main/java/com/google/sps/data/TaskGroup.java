package com.google.sps.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * This class holds a List of tasks and includes functions to sort
 * the List and retrieve elements.
 */
public class TaskGroup {
  private final List<Task> tasks;
  private int tasksIndex;

  // Comparators declared here are those used by different scheduling algorithms.
  private static final Comparator<Task> sortByTaskDurationThenName =
      Comparator.comparing(Task::getDuration).thenComparing(Task::getName);

  /**
   * The TaskGroup constructor takes in an unsorted list of Tasks and
   * sets tasksIndex variable to 0 as to start the user's retrieval of 
   * objects at the beginning of the list.
   */
  public TaskGroup(List<Task> taskList) {
    if (taskList == null) {
      throw new IllegalArgumentException("Tasklist cannot be null");
    }
    this.tasksIndex = 0;
    this.tasks = new ArrayList<Task>(taskList);
  }

  /**
   * This method takes in an enumarated SchedulingAlgorithmType and sorts
   * the Task list to the algorithm's needs using the Comparators declared
   * at the beginning of this class.
  */
  public void sortTasksWithAlgorithmType(SchedulingAlgorithmType schedulingAlgorithmType) {
    switch(schedulingAlgorithmType) {
      case SHORTEST_TASK_FIRST:
        Collections.sort(this.tasks, sortByTaskDurationThenName);
        break;
      default: 
        throw new IllegalArgumentException("SchedulingAlgorithmType not recognized");
    }
  }

  public Task getTask() {
    return tasks.get(tasksIndex);
  }

  public void incIndex() {
    tasksIndex++;
  }

  public boolean hasNext() {
    return tasksIndex < tasks.size();
  }
}