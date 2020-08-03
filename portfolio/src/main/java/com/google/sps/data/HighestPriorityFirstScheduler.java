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
    int rangeIndex = 0;

    Instant currentScheduleTime = workHoursStartTime;

    Boolean getNextTimeRange = true;
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
        availableTimesGroup.deleteTimeRange(
            TimeRange.fromStartEnd(
                scheduledTask.getStartTime(),
                scheduledTask
                    .getStartTime()
                    .plusSeconds(scheduledTask.getTask().getDuration().getSeconds())));
        currentScheduleTime = currentScheduleTime.plusSeconds(task.getDuration().getSeconds());
        taskQueue.remove();
        if (!taskQueue.isEmpty()
            && taskQueue.peek().getPriority().getPriority() != task.getPriority().getPriority()) {
          availableTimesIterator = availableTimesGroup.iterator();
          currentScheduleTime = workHoursStartTime;
          getNextTimeRange = true;
        } else {
          getNextTimeRange = false;
        }
      } else if (!availableTimesIterator.hasNext()) {
        taskQueue.remove();
        if (!taskQueue.isEmpty()
            && taskQueue.peek().getPriority().getPriority() != task.getPriority().getPriority()) {
          availableTimesIterator = availableTimesGroup.iterator();
        }
      } else {
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
