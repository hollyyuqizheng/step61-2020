package com.google.sps.data;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class LongestTaskFirstScheduler implements TaskScheduler {

  // Comparator for sorting tasks by duration in ascending order
  public static final Comparator<Task> sortByTaskDurationAscending =
      Comparator.comparing(Task::getDuration);

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
    Collections.sort(
        tasksList,
        Collections.reverseOrder(sortByTaskDurationAscending).thenComparing(Task::getName));

    CalendarEventsGroup calendarEventsGroup =
        new CalendarEventsGroup(eventsList, workHoursStartTime, workHoursEndTime);
    List<TimeRange> availableTimes = calendarEventsGroup.getFreeTimeRanges();

    // Create a TimeRangeGroup class for the free time ranges.
    TimeRangeGroup availableTimesGroup = new ArrayListTimeRangeGroup(availableTimes);

    List<ScheduledTask> scheduledTasks = new ArrayList<ScheduledTask>();

    for (int taskIndex = 0; taskIndex < tasksList.size(); taskIndex++) {
      Task task = tasksList.get(taskIndex);
      Duration taskDuration = task.getDuration();

      // Find the first free time range that is longer than the current task's duration.
      for (int freeRangeIndex = 0; freeRangeIndex < availableTimes.size(); freeRangeIndex++) {
        TimeRange currentFreeTimeRange = availableTimes.get(freeRangeIndex);

        if (currentFreeTimeRange.duration().compareTo(taskDuration) >= 0) {
          // Construct the scheduled task based on the start time of this current time range.
          Instant scheduledTime = currentFreeTimeRange.start();
          ScheduledTask scheduledTask = new ScheduledTask(task, scheduledTime);
          scheduledTasks.add(scheduledTask);

          // Delete the amount of time that is scheduled for this task from
          // the original free time range.
          TimeRange scheduledTimeRange =
              TimeRange.fromStartEnd(scheduledTime, scheduledTime.plus(taskDuration));
          availableTimesGroup.deleteTimeRange(scheduledTimeRange);
          Iterator<TimeRange> updatedAvailableTimesGroupIterator =
              availableTimesGroup.getAllTimeRangesIterator();

          // Reconstruct availableTimes list based on the updated iterator after delete.
          availableTimes = new ArrayList();
          while (updatedAvailableTimesGroupIterator.hasNext()) {
            availableTimes.add(updatedAvailableTimesGroupIterator.next());
          }
          Collections.sort(
              availableTimes, TimeRange.SORT_BY_TIME_RANGE_DURATION_ASCENDING_THEN_START_TIME);

          // Break out of the inner for loop, as the current task
          // is scheduled and the outer loops needs to move to the next task.
          break;
        }
      }
    }

    return scheduledTasks;
  }

  /** Returns the scheduler's type, which is Longest Task First. */
  public SchedulingAlgorithmType getSchedulingAlgorithmType() {
    return SchedulingAlgorithmType.LONGEST_TASK_FIRST;
  }
}
