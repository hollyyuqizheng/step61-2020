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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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

    Collection<CalendarEvent> events = collectEventsFromJson(eventsArray);
    Collection<Task> tasks = collectTasksFromJson(tasksArray);

    ScheduleRequest scheduleRequest =
        new ScheduleRequest(events, tasks, workHoursStartTimeString, workHoursEndTimeString);
    FindSchedule findSchedule = new FindSchedule();
    Collection<Task> scheduledTasks =
        findSchedule.greedy(scheduleRequest.getEvents(), scheduleRequest.getTasks(),
            scheduleRequest.getWorkHoursStartTimeLong(), scheduleRequest.getWorkHoursEndTimeLong());

    Gson gson = new Gson();
    String resultJSON = gson.toJson(scheduledTasks);
    System.out.println(resultJSON);

    response.setContentType("application/json");
    response.getWriter().println(resultJSON);
  }

  private static Collection<CalendarEvent> collectEventsFromJson(JSONArray eventsArray) {
    Collection<CalendarEvent> events = new ArrayList<CalendarEvent>();
    for (int i = 0; i < eventsArray.length(); i++) {
      JSONObject eventAsJSONObject = eventsArray.getJSONObject(i);
      String name = (String) eventAsJSONObject.get("name");
      String startTime = (String) eventAsJSONObject.get("startTime");
      String endTime = (String) eventAsJSONObject.get("endTime");
      CalendarEvent newEvent = new CalendarEvent(name, startTime, endTime);
      events.add(newEvent);
    }
    return events;
  }

  private static Collection<Task> collectTasksFromJson(JSONArray tasksArray) {
    Collection<Task> tasks = new ArrayList<Task>();
    for (int i = 0; i < tasksArray.length(); i++) {
      JSONObject taskAsJSONArray = tasksArray.getJSONObject(i);
      String name = (String) taskAsJSONArray.get("name");
      String description = (String) taskAsJSONArray.get("description");
      long duration = Long.valueOf((int) taskAsJSONArray.get("durationMinutes"));
      int priorityInt = (int) taskAsJSONArray.get("priority");
      TaskPriority priority = new TaskPriority(priorityInt);
      Task newTask = new Task(name, description, duration, priority);
      tasks.add(newTask);
    }
    return tasks;
  }
}
