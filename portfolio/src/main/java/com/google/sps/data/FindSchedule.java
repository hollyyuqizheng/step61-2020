// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

// TODO(tomasalvarez): Refactor this class into an abstract class where each of
// the scheduling algorithms can be their own class that extends this one.
// TODO: Add test coverage for scheduling algorithms

public class FindSchedule {
  private static final Comparator<CalendarEvent> sortByEventStartTimeAscending =
      Comparator.comparing(CalendarEvent::getStartTimeInstant);

  private static final Comparator<Task> sortByTaskDurationThenName =
      Comparator.comparing(Task::getDuration).thenComparing(Task::getName);

  /**
   * This method schedules tasks from shortest to longest and returns a ScheduledTask Collection
   * based on the tasks that were able to be scheduled.
   */
  public static Collection<ScheduledTask> shortestTaskFirst(
      Collection<CalendarEvent> events,
      Collection<Task> tasks,
      Instant workHoursStartTime,
      Instant workHoursEndTime) {
    List<CalendarEvent> eventsList = new ArrayList<CalendarEvent>(events);
    List<Task> tasksList = new ArrayList<Task>(tasks);
    Collections.sort(eventsList, sortByEventStartTimeAscending);
    Collections.sort(tasksList, sortByTaskDurationThenName);
    List<TimeRange> availableTimes =
        getEmptyTimeRanges(eventsList, workHoursStartTime, workHoursEndTime);
    List<ScheduledTask> scheduledTasks = new ArrayList<ScheduledTask>();
    int rangeIndex = 0;
    int taskIndex = 0;
    // Instant indicating the start time we are currently trying to schedule
    // events in.
    Instant currentScheduleTime = workHoursStartTime;
    // This will iterate through the time ranges and tasks and if one can be
    // scheduled then it will be (and we move onto the next task) otherwise
    // we move on to the next range (this is because the tasks are sorted by
    // duration so if one task did not fit in the given range then we know no
    // later ones will fit either). We create new Task objects for the result
    // so data structures passed in are never changed.
    while (rangeIndex < availableTimes.size() && taskIndex < tasksList.size()) {
      TimeRange availableTimeRange = availableTimes.get(rangeIndex);
      Task task = tasksList.get(taskIndex);
      // Either time is already past the start of the time range or we should
      // update it (maybe this is our first iteration in the range).
      if (availableTimeRange.start().isAfter(currentScheduleTime)) {
        currentScheduleTime = availableTimeRange.start();
      }
      // The task can be scheduled in the current time range.
      if (!currentScheduleTime
          .plusSeconds(task.getDuration().getSeconds())
          .isAfter(availableTimeRange.end())) {
        ScheduledTask scheduledTask = new ScheduledTask(task, currentScheduleTime);
        scheduledTasks.add(scheduledTask);
        currentScheduleTime = currentScheduleTime.plusSeconds(task.getDuration().getSeconds());
        taskIndex++;
      } else {
        rangeIndex++;
      }
    }

  /**
   * This method returns a List of TimeRange's, in chronological order, that are empty of events and
   * lie completely inside the period in between startTime and endTime. All fields are required for
   * this method and object construction in the servlet enforces that none are null.
   *
   * @param events: A Collection of CalendarEvent objects,
   * @param startTime: An Instant representing the start of the time period,
   * @param endTime: An Instant representing the end of the time period.
   */
  public static List<TimeRange> getEmptyTimeRanges(
      List<CalendarEvent> events, Instant startTime, Instant endTime) {
    List<TimeRange> possibleTimes = new ArrayList<TimeRange>();
    // This represents the earliest time that we can schedule a window for the
    // meeting. As events are processed, this changes to their end times.
    Instant earliestNonScheduledInstant = startTime;
    for (CalendarEvent event : events) {
      // Make sure that there is some time between the events and it is not
      // later than the person's working hours ending time.
      if (event.getStartTimeInstant().isAfter(earliestNonScheduledInstant)
          && !event.getStartTimeInstant().isAfter(endTime)) {
        possibleTimes.add(
            TimeRange.fromStartEnd(
                earliestNonScheduledInstant, event.getStartTimeInstant(), /* inclusive= */ true));
      }
      if (earliestNonScheduledInstant.isBefore(event.getEndTimeInstant())) {
        earliestNonScheduledInstant = event.getEndTimeInstant();
      }
    }
    // The end of the work hours is potentially never included so we check.
    if (endTime.isAfter(earliestNonScheduledInstant)) {
      possibleTimes.add(
          TimeRange.fromStartEnd(earliestNonScheduledInstant, endTime, /* inclusive= */ true));
    }
    return possibleTimes;
  }
}
