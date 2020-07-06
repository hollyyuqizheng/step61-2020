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
import com.google.sps.data.CalendarEvent;
import com.google.sps.data.ScheduleRequest;
import com.google.sps.data.Task;
import java.io.IOException;
import java.time.Duration;
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
    String workHoursStartTimeString = (String) jsonFromRequest.get("startTime");
    String workHoursEndTimeString = (String) jsonFromRequest.get("endTime");

    Collection<CalendarEvent> events = collectEventsFromJsonArray(eventsArray);
    Collection<Task> tasks = collectTasksFromJsonArray(tasksArray);

    ScheduleRequest scheduleRequest =
        new ScheduleRequest(events, tasks, workHoursStartTimeString, workHoursEndTimeString);
    Collection<ScheduledTask> scheduledTasks =
        FindSchedule.shortestTaskFirst(
            scheduleRequest.getEvents(),
            scheduleRequest.getTasks(),
            scheduleRequest.getWorkHoursStartTimeInstant(),
            scheduleRequest.getWorkHoursEndTimeInstant());

    Gson gson = new Gson();
    String resultJson = gson.toJson(scheduledTasks);

    response.setContentType("application/json");
    response.getWriter().println(resultJson);
  }

  private static Collection<CalendarEvent> collectEventsFromJsonArray(JSONArray eventsArray) {
    Collection<CalendarEvent> events = new ArrayList<CalendarEvent>();
    for (Object object: eventsArray) {
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

  private static Collection<Task> collectTasksFromJsonArray(JSONArray tasksArray) {
    Collection<Task> tasks = new ArrayList<Task>();
    for (Object object: tasksArray) {
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
