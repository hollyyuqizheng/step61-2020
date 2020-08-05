package com.google.sps.data;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** This class models a scheduling algorithm that prioritizes scheduling longer tasks first. */
public class LongestTaskFirstScheduler implements TaskScheduler {

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

    TaskQueue taskQueue = new TaskQueue(tasksList, getSchedulingAlgorithmType());

    CalendarEventsGroup calendarEventsGroup =
        new CalendarEventsGroup(eventsList, workHoursStartTime, workHoursEndTime);

    // Create a TimeRangeGroup class for the free time ranges.
    List<TimeRange> availableTimes = calendarEventsGroup.getFreeTimeRanges();
    TimeRangeGroup availableTimesGroup = new ArrayListTimeRangeGroup(availableTimes);

    List<ScheduledTask> scheduledTasks = new ArrayList<ScheduledTask>();

    while (!taskQueue.isEmpty()) {
      Task task = taskQueue.peek();
      List<ScheduledTask> currentScheduledTasks = scheduleOneTask(task, availableTimesGroup);
      currentScheduledTasks.forEach(scheduledTasks::add);
      taskQueue.remove();
    }

    return scheduledTasks;
  }

  /** Returns the scheduler's type, which is Longest Task First. */
  public SchedulingAlgorithmType getSchedulingAlgorithmType() {
    return SchedulingAlgorithmType.LONGEST_TASK_FIRST;
  }

  /** Constructs a list for the updated available time ranges after deletion. */
  private List<TimeRange> constructAvailableTimeRanges(TimeRangeGroup updatedTimeRangeGroup) {
    List<TimeRange> updatedAvailableTimes = new ArrayList();
    updatedTimeRangeGroup.iterator().forEachRemaining(updatedAvailableTimes::add);
    return updatedAvailableTimes;
  }

  /**
   * Tries to schedule for a single task by iterating through the currently available time ranges.
   * Keeps splitting up the task to schedule the currently avaible free time ranges until all of the
   * task is scheduled across the different time ranges.
   *
   * <p>When this method is called, the task may or may not be complete scheduled. The task may be
   * scheduled into one entire free time range, or the task could be split into multiple time
   * ranges. When a task is split, new tasks are created to model the segment of the current task
   * and these new tasks are added to the list of all newly scheduled tasks.
   *
   * @return a list of newly scheduled tasks. If this list is empty, then it means the current task
   *     cannot be scheduled at all.
   */
  private List<ScheduledTask> scheduleOneTask(Task task, TimeRangeGroup availableTimesGroup) {
    List<TimeRange> currentAvailableTimes = constructAvailableTimeRanges(availableTimesGroup);
    List<ScheduledTask> newScheduledTasks = new ArrayList<ScheduledTask>();

    // If there is no available time ranges anymore,
    // return the empty list.
    if (currentAvailableTimes.isEmpty()) {
      return newScheduledTasks;
    }

    Duration taskDuration = task.getDuration();
    int taskSegmentCount = 1;

    for (TimeRange currentFreeTimeRange : currentAvailableTimes) {
      // If the task has been entirely scheduled, return the list of new scheduled tasks.
      if (taskDuration.getSeconds() == 0) {
        return newScheduledTasks;
      }

      Instant scheduledTime = currentFreeTimeRange.start();

      // Reconstruct the task segment's name.
      String taskName = task.getName() + " (Part " + taskSegmentCount + ")";

      String taskDescription = task.getDescription().orElse("");
      TaskPriority taskPriority = task.getPriority();

      // If task's current duration is longer than the free time's,
      // this means the entirety of the free time range is scheduled to this task.
      if (taskDuration.compareTo(currentFreeTimeRange.duration()) > 0) {
        // Constructs a new task object with the current free time range's duration.
        Task taskWithActualScheduledDuration =
            new Task(taskName, taskDescription, currentFreeTimeRange.duration(), taskPriority);
        TimeRange scheduledTaskTimeRange =
            scheduleTaskSegment(
                taskWithActualScheduledDuration,
                scheduledTime,
                newScheduledTasks,
                availableTimesGroup);
        taskDuration = taskDuration.minus(scheduledTaskTimeRange.duration());
      } else {
        // Otherwise, only part of the free time range is needed to schedule this task.
        // In this case, if the count of segment is 1, then the task can be scheduled in its
        // entirety. Retrieve the task's original name without the "(Part 1)" suffix.
        if (taskSegmentCount == 1) {
          taskName = task.getName();
        }

        Task taskWithActualScheduledDuration =
            new Task(taskName, taskDescription, taskDuration, taskPriority);
        TimeRange scheduledTaskTimeRange =
            scheduleTaskSegment(
                taskWithActualScheduledDuration,
                scheduledTime,
                newScheduledTasks,
                availableTimesGroup);

        // This is the last free time range that is needed to complete the scheduling
        // for the current task, so return the list of new scheduled tasks here.
        // These new scheduled task segments are marked with "true" for completeness tag,
        // and these tags do not need to be changed.
        return newScheduledTasks;
      }
      taskSegmentCount++;
    }

    // If iterating through all current time ranges finishes, and the task still isn't
    // completely scheduled, then this task is only partially scheduled.
    // Go through all the segments for this task, and set their completeness to partially scheduled.
    Optional<Integer> schedulingCompletenessInt =
        Optional.of(SchedulingCompleteness.PARTIALLY_SCHEDULED.getValue());

    for (ScheduledTask taskSegment : newScheduledTasks) {
      taskSegment.setCompleteness(schedulingCompletenessInt);
    }
    return newScheduledTasks;
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

    Optional<Integer> schedulingCompletenessInt =
        Optional.of(SchedulingCompleteness.COMPLETELY_SCHEDULED.getValue());

    // Assumes this task can be scheduled for now.
    // If not, the true tag will be overwritten at the end of the scheduleOneTask method.
    ScheduledTask scheduledTask = new ScheduledTask(task, scheduledTime, schedulingCompletenessInt);
    scheduledTasks.add(scheduledTask);

    Instant scheduledTaskEndTime = scheduledTime.plus(taskDuration);
    TimeRange scheduledTaskTimeRange = TimeRange.fromStartEnd(scheduledTime, scheduledTaskEndTime);
    availableTimesGroup.deleteTimeRange(scheduledTaskTimeRange);

    return scheduledTaskTimeRange;
  }
}
