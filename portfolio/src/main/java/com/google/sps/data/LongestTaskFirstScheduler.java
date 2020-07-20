package com.google.sps.data;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LongestTaskFirstScheduler implements TaskScheduler {

  // Comparator for sorting tasks by duration in ascending order,
  // and then by name in alphabetical ascending order.
  public static final Comparator<Task> sortByTaskDurationAscendingThenName =
      Comparator.comparing(Task::getDuration).thenComparing(Task::getName);

  /**
   * Schedules the tasks so that the longest tasks are scheduled to the first possible free time
   * range of the day.
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
    Collections.sort(tasksList, Collections.reverseOrder(sortByTaskDurationAscendingThenName));

    CalendarEventsGroup calendarEventsGroup =
        new CalendarEventsGroup(eventsList, workHoursStartTime, workHoursEndTime);
    List<TimeRange> availableTimes = calendarEventsGroup.getFreeTimeRanges();

    // Create a TimeRangeGroup class for the free time ranges.
    // The comparator is by time range duration ascending.
    TimeRangeGroup availableTimesGroup =
        new TimeRangeGroupArrayList(
            availableTimes, TimeRange.sortByTimeRangeDurationAscending, /* ascending= */ true);

    List<ScheduledTask> scheduledTasks = new ArrayList<ScheduledTask>();

    // Instant indicating the start time we are currently trying to schedule
    // events in.
    Instant currentScheduleTime = workHoursStartTime;

    for (int taskIndex = 0; taskIndex < tasksList.size(); taskIndex++) {
      Task task = tasksList.get(taskIndex);
      Duration taskDuration = task.getDuration();

      // Find the first free time range that is longer than
      for (int freeRangeIndex = 0; freeRangeIndex < availableTimes.size(); freeRangeIndex++) {
        TimeRange currentFreeTimeRange = availableTimes.get(freeRangeIndex);
        // Check if current free time range's duration is equal or larger than tasks' duration.
        if (currentFreeTimeRange.duration().comparesTo(taskDuration) >= 0) {
          Instant scheduledTime = currentFreeTimeRange.start();
          ScheduledTask scheduledTask = new ScheduledTask(task, currentScheduleTime);
          scheduledTasks.add(scheduledTask);

          // Delete the amount of time that is scheduled for this task from
          // the original free time range.
          TimeRange scheduledTimeRange =
              TimeRange.fromStartEnd(scheduledTime, scheduledTime.plus(taskDuration));
          availableTimes = availableTimesGroup.deleteTimeRange(scheduledTime);

          // Breakt out of the inner for loop, as the current task
          // is scheduled and the outer loops needs to move to the next task.
          break;
        }
      }
    }

    return scheduledTasks;
  }

  public SchedulingAlgorithmType getSchedulingAlgorithmType() {
    return SchedulingAlgorithmType.LONGEST_TASK_FIRST;
  }
}
