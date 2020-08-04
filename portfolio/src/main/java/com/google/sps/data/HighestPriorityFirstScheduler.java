package com.google.sps.data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class HighestPriorityFirstScheduler implements TaskScheduler {
  private static final Comparator<ScheduledTask> sortByScheduledStartTimeAscending =
      Comparator.comparing(ScheduledTask::getStartTime);

  /**
   * This method schedules tasks from highest to lowest priority and shortest to longest duration.
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

    List<TimeRange> availableTimes = calendarEventsGroup.getFreeTimeRanges();
    TimeRangeGroup availableTimesGroup = new ArrayListTimeRangeGroup(availableTimes);
    List<TimeRange> currentAvailableTimes = getAvailableTimeRangesList(availableTimesGroup);

    List<ScheduledTask> scheduledTasks = new ArrayList<ScheduledTask>();

    int availableTimesIndex = 0;

    Instant currentScheduleTime = workHoursStartTime;

    while (availableTimesIndex < currentAvailableTimes.size() && !taskQueue.isEmpty()) {
      TimeRange availableTimeRange = currentAvailableTimes.get(availableTimesIndex);
      Task task = taskQueue.peek();

      if (availableTimeRange.start().isAfter(currentScheduleTime)) {
        currentScheduleTime = availableTimeRange.start();
      }

      boolean doesTaskFit =
          !currentScheduleTime
              .plusSeconds(task.getDuration().getSeconds())
              .isAfter(availableTimeRange.end());

      if (doesTaskFit) {
        ScheduledTask scheduledTask =
            new ScheduledTask(
                task, currentScheduleTime, /* isCompletelyScheduled = */ Optional.of(true));
        scheduledTasks.add(scheduledTask);

        // Delete the TimeRange that has been scheduled over so that different priority tasks
        // won't be scheduled over the same time.
        availableTimesGroup.deleteTimeRange(
            TimeRange.fromStartEnd(
                scheduledTask.getStartTime(),
                scheduledTask
                    .getStartTime()
                    .plusSeconds(scheduledTask.getTask().getDuration().getSeconds())));

        // Push the currentScheduleTime back by the amount that was scheduled.
        currentScheduleTime = currentScheduleTime.plusSeconds(task.getDuration().getSeconds());
        taskQueue.remove();

        // If the next task's priority is different from the task that was just scheduled,
        // then reset the availableTimesIndex, currentScheduleTime, and make the loop
        // grab a TimeRange. We go back to the first availableTimeRange in order to schedule
        // more tasks towards the beginning of the availableTimes.
        if (isNextTaskDifferentPriority(taskQueue, task)) {
          availableTimesIndex = 0;
          currentScheduleTime = workHoursStartTime;
          currentAvailableTimes = getAvailableTimeRangesList(availableTimesGroup);
        }
        // If the task's priority is not different, then we can simply continue running the
        // the loop without retrieving the next availableTimeRange since there might be
        // some time left over towards the end of the current one.
      } else if (availableTimesIndex == currentAvailableTimes.size() - 1) {
        // If we've reached the end of the availableTimeRange group and we can't schedule a task,
        // then we can remove all the remaining tasks of equal priority since they will all be
        // longer in duration therefore, they will not be able to be scheduled either.
        if (!taskQueue.isEmpty()) {
          availableTimesIndex = 0;
          removeTasksWithPriority(taskQueue, task.getPriority());
        }
      } else {
        // Get the next TimeRange if the Task cannot be scheduled but we aren't at the final
        // TimeRange.
        availableTimesIndex++;
      }
    }

    Collections.sort(scheduledTasks, sortByScheduledStartTimeAscending);
    return scheduledTasks;
  }

  private void removeTasksWithPriority(TaskQueue taskQueue, TaskPriority taskPriority) {
    while (!taskQueue.isEmpty()
        && taskQueue.peek().getPriority().getPriority() == taskPriority.getPriority()) {
      taskQueue.remove();
    }
  }

  private boolean isNextTaskDifferentPriority(TaskQueue taskQueue, Task task) {
    return !taskQueue.isEmpty()
        && taskQueue.peek().getPriority().getPriority() != task.getPriority().getPriority();
  }

  private List<TimeRange> getAvailableTimeRangesList(TimeRangeGroup updatedTimeRangeGroup) {
    List<TimeRange> availableTimes = new ArrayList();
    updatedTimeRangeGroup.iterator().forEachRemaining(availableTimes::add);
    return availableTimes;
  }

  /** Returns the scheduler's type, which is Highest Priority First. */
  public SchedulingAlgorithmType getSchedulingAlgorithmType() {
    return SchedulingAlgorithmType.HIGHEST_PRIORITY_FIRST;
  }
}
