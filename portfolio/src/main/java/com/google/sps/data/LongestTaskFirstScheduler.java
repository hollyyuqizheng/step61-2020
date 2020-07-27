package com.google.sps.data;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class LongestTaskFirstScheduler implements TaskScheduler {

  // Comparator for sorting tasks by duration in descending order and then by task name
  // alphabetically.
  public static final Comparator<Task> sortByTaskDurationDescendingThenName =
      Comparator.comparing(Task::getDuration).reversed().thenComparing(Task::getName);

  /**
   * Schedules the tasks so that the longest tasks are scheduled to the first possible free time
   * range of the day. This approach tries to prioritize long tasks so that they are scheduled.
   */
  public Collection<ScheduledTask> schedule(
      Collection<CalendarEvent> events,
      Collection<Task> tasks,
      Instant workHoursStartTime,
      Instant workHoursEndTime) {

    List<CalendarEvent> eventsList = new ArrayList<CalendarEvent>(events);
    List<Task> tasksList = new ArrayList<Task>(tasks);

    // Sorts the tasks in descending order based on duration.
    // Longest task comes first in the collection.
    // Then sorts the list again by alphabetical order ascending.
    Collections.sort(tasksList, sortByTaskDurationDescendingThenName);

    CalendarEventsGroup calendarEventsGroup =
        new CalendarEventsGroup(eventsList, workHoursStartTime, workHoursEndTime);
    // Create a TimeRangeGroup class for the free time ranges.
    List<TimeRange> availableTimes = calendarEventsGroup.getFreeTimeRanges();
    TimeRangeGroup availableTimesGroup = new ArrayListTimeRangeGroup(availableTimes);

    List<ScheduledTask> scheduledTasks = new ArrayList<ScheduledTask>();

    for (Task task : tasksList) {
      Duration taskDuration = task.getDuration();
      Optional timeRangeForTask = findTimeForTask(task, availableTimesGroup);

      if (!timeRangeForTask.isPresent()) {
        continue;
      }

      // Create and append scheduled tasks.
      TimeRange scheduledTimeRange = (TimeRange) timeRangeForTask.get();
      ScheduledTask scheduledTask = new ScheduledTask(task, scheduledTimeRange.start());
      scheduledTasks.add(scheduledTask);

      // Delete the scheduled time range from the time range group.
      availableTimesGroup.deleteTimeRange(scheduledTimeRange);
    }

    return scheduledTasks;
  }

  /** Returns the scheduler's type, which is Longest Task First. */
  public SchedulingAlgorithmType getSchedulingAlgorithmType() {
    return SchedulingAlgorithmType.LONGEST_TASK_FIRST;
  }

  /**
   * Tries to find a free time range for a task.
   *
   * @return an Optional that contains the time range scheduled for this task. This Optional object
   *     is empty if the task cannot be scheduled.
   */
  private static Optional findTimeForTask(Task task, TimeRangeGroup availableTimesGroup) {
    List<TimeRange> availableTimes = new ArrayList<TimeRange>();
    Iterator<TimeRange> availableTimesIterator = availableTimesGroup.iterator();
    while (availableTimesIterator.hasNext()) {
      availableTimes.add(availableTimesIterator.next());
    }

    Duration taskDuration = task.getDuration();
    Optional scheduledTimeRangeOptional = Optional.empty();

    // Find the first free time range that is longer than the current task's duration.
    for (TimeRange currentFreeTimeRange : availableTimes) {
      if (currentFreeTimeRange.duration().compareTo(taskDuration) < 0) {
        continue;
      }

      // Construct and return the scheduled time range based on the start time of this current time
      // range.
      Instant scheduledTime = currentFreeTimeRange.start();
      TimeRange scheduledTimeRange =
          TimeRange.fromStartEnd(scheduledTime, scheduledTime.plus(taskDuration));
      scheduledTimeRangeOptional = Optional.of(scheduledTimeRange);
      break;
    }

    return scheduledTimeRangeOptional;
  }
}
