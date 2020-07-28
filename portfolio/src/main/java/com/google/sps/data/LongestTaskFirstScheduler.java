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

/** This class models a scheduling algorithm that prioritizes scheduling longer tasks first. */
public class LongestTaskFirstScheduler implements TaskScheduler {

  // Comparator for sorting tasks by duration in descending order and then by task priority
  // descending.
  public static final Comparator<Task> sortByTaskDurationDescendingThenPriority =
      Comparator.comparing(Task::getDuration).reversed().thenComparing(Task::getPriority);

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
    // Then sorts the list again by task priority.
    Collections.sort(tasksList, sortByTaskDurationDescendingThenPriority);

    CalendarEventsGroup calendarEventsGroup =
        new CalendarEventsGroup(eventsList, workHoursStartTime, workHoursEndTime);

    // Create a TimeRangeGroup class for the free time ranges.
    List<TimeRange> availableTimes = calendarEventsGroup.getFreeTimeRanges();
    TimeRangeGroup availableTimesGroup = new ArrayListTimeRangeGroup(availableTimes);

    List<ScheduledTask> scheduledTasks = new ArrayList<ScheduledTask>();

    for (Task task : tasksList) {

      Optional<TimeRange> timeRangeForTask = findTimeForTask(task, availableTimesGroup);

      if (!timeRangeForTask.isPresent()) {
        // If the total duration of available free times is longer than the task's duration,
        // then this task can be split up into smaller blocks to be scheduled.
        // Otherwise, there is not enough free time in the day for this task, so this
        // task will not be scheduled.
        if (availableTimesGroup.getTotalDuration().compareTo(task.getDuration()) > 0) {
          splitUpTaskToSchedule(task, availableTimesGroup, scheduledTasks);
        }
      } else {
        // This is the case that the current task can be scheduled entirely to one free time range.
        TimeRange scheduledTimeRange = timeRangeForTask.get();
        ScheduledTask scheduledTask = new ScheduledTask(task, scheduledTimeRange.start());
        scheduledTasks.add(scheduledTask);
        availableTimesGroup.deleteTimeRange(scheduledTimeRange);
      }
    }

    return scheduledTasks;
  }

  /** Returns the scheduler's type, which is Longest Task First. */
  public SchedulingAlgorithmType getSchedulingAlgorithmType() {
    return SchedulingAlgorithmType.LONGEST_TASK_FIRST;
  }

  /** Constructs a list for the updated available time ranges after deletion. */
  private List<TimeRange> constructAvailableTimeRanges(TimeRangeGroup updatedTimeRangeGroup) {
    Iterator<TimeRange> updatedAvailableTimesGroupIterator = updatedTimeRangeGroup.iterator();
    List<TimeRange> updatedAvailableTimes = new ArrayList();
    while (updatedAvailableTimesGroupIterator.hasNext()) {
      updatedAvailableTimes.add(updatedAvailableTimesGroupIterator.next());
    }

    return updatedAvailableTimes;
  }

  /**
   * Tries to find a free time range for a task.
   *
   * @return an Optional that contains the time range scheduled for this task. This Optional object
   *     is empty if the task cannot be scheduled.
   */
  private static Optional<TimeRange> findTimeForTask(
      Task task, TimeRangeGroup availableTimesGroup) {
    List<TimeRange> availableTimes = new ArrayList<TimeRange>();
    Iterator<TimeRange> availableTimesIterator = availableTimesGroup.iterator();
    Duration taskDuration = task.getDuration();
    Optional<TimeRange> scheduledTimeRangeOptional = Optional.empty();

    // Find the first free time range that is longer than the current task's duration.
    while (availableTimesIterator.hasNext()) {
      TimeRange currentFreeTimeRange = availableTimesIterator.next();

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

  /**
   * Keeps splitting up the task to schedule the currently avaible free time ranges until all of the
   * task is scheduled across the different time ranges.
   */
  private void splitUpTaskToSchedule(
      Task task, TimeRangeGroup availableTimesGroup, List<ScheduledTask> scheduledTasks) {
    List<TimeRange> currentAvailableTimes = constructAvailableTimeRanges(availableTimesGroup);
    Duration taskDuration = task.getDuration();

    for (TimeRange currentFreeTimeRange : currentAvailableTimes) {
      if (taskDuration.getSeconds() > 0) {
        // Construct the scheduled task object.
        Instant scheduledTime = currentFreeTimeRange.start();
        ScheduledTask scheduledTask = new ScheduledTask(task, scheduledTime);
        scheduledTasks.add(scheduledTask);

        // If task's current duration is longer than the free time's,
        // this means the entirety of the free time range is scheduled to this task.
        if (taskDuration.compareTo(currentFreeTimeRange.duration()) > 0) {
          availableTimesGroup.deleteTimeRange(currentFreeTimeRange);
          taskDuration = taskDuration.minus(currentFreeTimeRange.duration());
        } else {
          // Otherwise, only part of the free time range is needed to schedule this task.
          Instant scheduledTaskEndTime = scheduledTime.plus(taskDuration);
          TimeRange scheduledTaskTimeRange =
              TimeRange.fromStartEnd(scheduledTime, scheduledTaskEndTime);
          availableTimesGroup.deleteTimeRange(scheduledTaskTimeRange);
          taskDuration = taskDuration.minus(scheduledTaskTimeRange.duration());

          // This is also the last free time range that is needed to complete the scheduling
          // for the current task, so break out of the for loop now.
          break;
        }
      }
    }
  }
}
