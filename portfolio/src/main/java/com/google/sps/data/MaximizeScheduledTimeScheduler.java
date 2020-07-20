package com.google.sps.data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MaximizeScheduledTimeScheduler implements TaskScheduler {

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
    Collections.sort(tasksList, Collections.reverseOrder(sortByTaskDurationAscendingThenName));
    // Collections.sort(tasksList, sortByTaskDurationAscendingThenName);

    // Find out what the free time ranges are and initialize a TimeRangeGroup for those free ranges.
    CalendarEventsGroup calendarEventsGroup =
        new CalendarEventsGroup(eventsList, workHoursStartTime, workHoursEndTime);
    List<TimeRange> availableTimes = calendarEventsGroup.getFreeTimeRanges();
    TimeRangeGroup availableTimesGroup = new TimeRangeGroupArrayList(availableTimes);

    List<ScheduledTask> scheduledTasks = new ArrayList<ScheduledTask>();
    int rangeIndex = 0;
    int taskIndex = 0;

    // Instant indicating the start time we are currently trying to schedule
    // events in.
    Instant currentScheduleTime = workHoursStartTime;

    return scheduledTasks;
  }

  public SchedulingAlgorithmType getSchedulingAlgorithmType() {
    return SchedulingAlgorithmType.LONGEST_TASK_FIRST;
  }
}
