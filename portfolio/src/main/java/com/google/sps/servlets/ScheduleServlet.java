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

package com.google.sps.servlets;

import com.google.gson.Gson;
import com.google.sps.data.*;
import com.google.sps.data.BaseTaskScheduler.SchedulingAlgorithmType;
import com.google.sps.data.CalendarEvent;
import com.google.sps.data.Task;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet("/schedule")
public class ScheduleServlet extends HttpServlet {
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String jsonInput = request.getReader().lines().collect(Collectors.joining());
    JSONObject jsonFromRequest = new JSONObject(jsonInput);

    JSONArray eventsArray = jsonFromRequest.getJSONArray("events");
    JSONArray tasksArray = jsonFromRequest.getJSONArray("tasks");
    String workHoursStartTimeString = jsonFromRequest.getString("startTime");
    String workHoursEndTimeString = jsonFromRequest.getString("endTime");
    Instant workHoursStartTime = Instant.parse(workHoursStartTimeString);
    Instant workHoursEndTime = Instant.parse(workHoursEndTimeString);
    String algorithmTypeString = jsonFromRequest.getString("algorithmType");
    Collection<CalendarEvent> events = collectEventsFromJsonArray(eventsArray);
    Collection<Task> tasks = collectTasksFromJsonArray(tasksArray);

    // These are null for initialization purposes. They will either be set or
    // the program will thrown an error.
    SchedulingAlgorithmType schedulingAlgorithmType = null;
    BaseTaskScheduler algorithm = null;

    // This will take the string representation of the algorithm that is passed
    // in and match it to something in the enum under BaseTaskScheduler.
    try {
      schedulingAlgorithmType = SchedulingAlgorithmType.valueOf(algorithmTypeString);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Algorithm type does not exist.");
    }

    // Here we should add a case for each new algorithm that is implemented.
    switch (schedulingAlgorithmType) {
      case SHORTEST_TASK_FIRST:
        algorithm = new ShortestTaskFirst();
    }
    Collection<ScheduledTask> scheduledTasks =
        algorithm.schedule(events, tasks, workHoursStartTime, workHoursEndTime);

    Gson gson = new Gson();
    String resultJson = gson.toJson(scheduledTasks);

    response.setContentType("application/json");
    response.getWriter().println(resultJson);
  }

  // TODO(tomasalvarez): Add tests for this method.
  private static Collection<CalendarEvent> collectEventsFromJsonArray(JSONArray eventsArray) {
    Collection<CalendarEvent> events = new ArrayList<CalendarEvent>();
    for (Object object : eventsArray) {
      if (object instanceof JSONObject) {
        JSONObject eventJsonObject = (JSONObject) object;
        String name = eventJsonObject.getString("name");
        Instant startTime = Instant.parse(eventJsonObject.getString("startTime"));
        Instant endTime = Instant.parse(eventJsonObject.getString("endTime"));
        CalendarEvent newEvent = new CalendarEvent(name, startTime, endTime);
        events.add(newEvent);
      }
    }
    return events;
  }

  // TODO(tomasalvarez): Add tests for this method.
  private static Collection<Task> collectTasksFromJsonArray(JSONArray tasksArray) {
    Collection<Task> tasks = new ArrayList<Task>();
    for (Object object : tasksArray) {
      if (object instanceof JSONObject) {
        JSONObject taskJsonObject = (JSONObject) object;
        String name = taskJsonObject.getString("name");
        String description = taskJsonObject.getString("description");
        long durationMinutes = taskJsonObject.getLong("duration");
        Duration duration = Duration.ofMinutes(durationMinutes);
        int priorityInt = taskJsonObject.getInt("taskPriority");
        TaskPriority priority = new TaskPriority(priorityInt);
        Task newTask = new Task(name, description, duration, priority);
        tasks.add(newTask);
      }
    }
    return tasks;
  }
}
