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

      Optional<TimeRange> timeRangeForTask = findOneTimeRangeForTask(task, availableTimesGroup);

      if (!timeRangeForTask.isPresent()) {
        // Try to break up the current task and schedule it as much as possible.
        // TODO(hollyyuqizheng): use this boolean value for UI updates.
        boolean isTaskCompletelyScheduled =
            splitUpTaskToSchedule(task, availableTimesGroup, scheduledTasks);
      } else {
        // This is the case that the current task can be scheduled entirely to one free time range.
        TimeRange scheduledTimeRange = timeRangeForTask.get();
        ScheduledTask scheduledTask =
            new ScheduledTask(task, scheduledTimeRange.start(), /* isCompletelyScheduled= */ true);
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
   * Tries to find a single free time range for a task.
   *
   * @return an Optional that contains the time range scheduled for this task. This Optional object
   *     is empty if the task cannot be scheduled.
   */
  private static Optional<TimeRange> findOneTimeRangeForTask(
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
   * task is scheduled across the different time ranges. When this method is called, the task may or
   * may not be complete scheduled. When a task is split, new tasks are created to model the segment
   * of the current task and these new tasks are added to the list of all scheduled tasks.
   *
   * @return a boolean value representing whether or not the task is completely scheduled.
   */
  private boolean splitUpTaskToSchedule(
      Task task, TimeRangeGroup availableTimesGroup, List<ScheduledTask> scheduledTasks) {
    List<TimeRange> currentAvailableTimes = constructAvailableTimeRanges(availableTimesGroup);
    Duration taskDuration = task.getDuration();
    int taskSegmentCount = 1;
    List<ScheduledTask> taskSegments = new ArrayList<ScheduledTask>();

    for (TimeRange currentFreeTimeRange : currentAvailableTimes) {
      if (taskDuration.getSeconds() == 0) {
        return true;
      }

      Instant scheduledTime = currentFreeTimeRange.start();

      // Reconstruct the task segment's name.
      String taskName = task.getName() + " (Part " + taskSegmentCount + ")";
      taskSegmentCount++;

      String taskDescription = task.getDescription().orElse("");
      TaskPriority taskPriority = task.getPriority();

      // If task's current duration is longer than the free time's,
      // this means the entirety of the free time range is scheduled to this task.
      if (taskDuration.compareTo(currentFreeTimeRange.duration()) > 0) {
        // Constructs a new taks object with the current free time range's duration.
        Task taskWithActualScheduledDuration =
            new Task(taskName, taskDescription, currentFreeTimeRange.duration(), taskPriority);
        TimeRange scheduledTaskTimeRange =
            scheduleTaskSegment(
                taskWithActualScheduledDuration, scheduledTime, taskSegments, availableTimesGroup);
        taskDuration = taskDuration.minus(scheduledTaskTimeRange.duration());
      } else {
        // Otherwise, only part of the free time range is needed to schedule this task.
        Task taskWithActualScheduledDuration =
            new Task(taskName, taskDescription, taskDuration, taskPriority);
        TimeRange scheduledTaskTimeRange =
            scheduleTaskSegment(
                taskWithActualScheduledDuration, scheduledTime, taskSegments, availableTimesGroup);

        // Move all task segments into scheduledTasks.
        // These task segments are marked with "true" for completeness tag,
        // and these tags do not need to be changed.
        for (ScheduledTask taskSegment : taskSegments) {
          scheduledTasks.add(taskSegment);
        }
        // taskDuration = taskDuration.minus(scheduledTaskTimeRange.duration());

        // This is also the last free time range that is needed to complete the scheduling
        // for the current task, so return true.
        return true;
      }
    }
    // If iterating through all current time ranges finishes, and the task still isn't
    // completely scheduled, then this task is only partially scheduled.
    // Go through all the segments for this task, and set their completeness to false.
    // Return false to indicate that this task is not completely scheduled.
    for (ScheduledTask taskSegment : taskSegments) {
      taskSegment.setCompleteness(false);
      scheduledTasks.add(taskSegment);
    }

    return false;
  }

  /**
   * Schedules a task segment based on a scheduled time. Deletes the scheduled time range from
   * availabe time range group.
   */
  private TimeRange scheduleTaskSegment(
      Task task,
      Instant scheduledTime,
      List<ScheduledTask> scheduledTasks,
      TimeRangeGroup availableTimesGroup) {
    Duration taskDuration = task.getDuration();

    // Assumes this task can be scheduled for now.
    // If not, the true tag will be overwritten at the end of the splitUpTaskToSchedule method.
    ScheduledTask scheduledTask =
        new ScheduledTask(task, scheduledTime, /* isCompletelyScheduled= */ true);
    scheduledTasks.add(scheduledTask);

    Instant scheduledTaskEndTime = scheduledTime.plus(taskDuration);
    TimeRange scheduledTaskTimeRange = TimeRange.fromStartEnd(scheduledTime, scheduledTaskEndTime);
    availableTimesGroup.deleteTimeRange(scheduledTaskTimeRange);

    return scheduledTaskTimeRange;
  }
}
