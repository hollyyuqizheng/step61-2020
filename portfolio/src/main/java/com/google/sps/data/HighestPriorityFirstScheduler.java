package com.google.sps.data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class HighestPriorityFirstScheduler implements TaskScheduler {
  private static final Comparator<ScheduledTask> sortByScheduledStartTimeAscending =
      Comparator.comparing(ScheduledTask::getStartTime);

  /** This method schedules tasks from highest to lowest priority and */
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

    List<ScheduledTask> scheduledTasks = new ArrayList<ScheduledTask>();

    Iterator<TimeRange> availableTimesIterator = availableTimesGroup.iterator();

    Instant currentScheduleTime = workHoursStartTime;

    Boolean getNextTimeRange = true;

    // Set the first availableTimeRange using the first list value
    // in order to avoid getting the value from the availableTimesIterator
    // which would prevent the while loop from running if there were to
    // only be a single availableTimeRange.
    TimeRange availableTimeRange = availableTimes.get(0);

    while (availableTimesIterator.hasNext() && !taskQueue.isEmpty()) {
      if (getNextTimeRange) {
        availableTimeRange = availableTimesIterator.next();
      }
      Task task = taskQueue.peek();

      if (availableTimeRange.start().isAfter(currentScheduleTime)) {
        currentScheduleTime = availableTimeRange.start();
      }

      if (!currentScheduleTime
          .plusSeconds(task.getDuration().getSeconds())
          .isAfter(availableTimeRange.end())) {
        ScheduledTask scheduledTask =
            new ScheduledTask(task, currentScheduleTime, Optional.of(true));
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
        // then reset the availableTimesIterator, currentScheduleTime, and make the loop
        // grab a TimeRange. We go back to the first availableTimeRange in order to schedule
        // more tasks towards the beginning of the availableTimes.
        if (!taskQueue.isEmpty()
            && taskQueue.peek().getPriority().getPriority() != task.getPriority().getPriority()) {
          availableTimesIterator = availableTimesGroup.iterator();
          currentScheduleTime = workHoursStartTime;
          getNextTimeRange = true;
        } else {
          // If the task's priority is not different, then we can simply continue running the
          // the loop without retrieving the next availableTimeRange since there might be
          // some time left over towards the end of the current one.
          getNextTimeRange = false;
        }
      } else if (!availableTimesIterator.hasNext()) {
        // If we've reached the end of the availableTimeRange group and we can't schedule a task,
        // then we can remove all the remaining tasks of equal priority since they will all be
        // longer in duration therefore, they will not be able to be scheduled either.
        if (!taskQueue.isEmpty()) {
          availableTimesIterator = availableTimesGroup.iterator();
          while (!taskQueue.isEmpty()
              && taskQueue.peek().getPriority().getPriority() == task.getPriority().getPriority()) {
            taskQueue.remove();
          }
        }
      } else {
        // Get the next TimeRange if the Task cannot be scheduled but we aren't at the final
        // TimeRange.
        getNextTimeRange = true;
      }
    }

    Collections.sort(scheduledTasks, sortByScheduledStartTimeAscending);
    return scheduledTasks;
  }

  /** Returns the scheduler's type, which is Highest Priority First. */
  public SchedulingAlgorithmType getSchedulingAlgorithmType() {
    return SchedulingAlgorithmType.HIGHEST_PRIORITY_FIRST;
  }
}
