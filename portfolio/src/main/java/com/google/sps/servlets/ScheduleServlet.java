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
import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
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
    Collection<CalendarEvent> events = ServletHelper.collectEventsFromJsonArray(eventsArray);
    Collection<Task> tasks = ServletHelper.collectTasksFromJsonArray(tasksArray);

    Optional<SchedulingAlgorithmType> schedulingAlgorithmTypeOptional =
        SchedulingAlgorithmReference.getSchedulingAlgorithmTypeOptional(algorithmTypeString);
    if (!schedulingAlgorithmTypeOptional.isPresent()) {
      response.sendError(
          HttpServletResponse.SC_BAD_REQUEST,
          "The request by the client was syntactically incorrect. The algorithm could not be determined.");
      // Here I am returning the empty schedule instead of null to not mess up
      // any front end code expecting some array.
      ServletHelper.returnEmptyArrayResponse(response);
      return;
    }

    Optional<TaskScheduler> taskSchedulerOptional =
        SchedulingAlgorithmReference.getTaskSchedulerOptional(schedulingAlgorithmTypeOptional);
    if (!taskSchedulerOptional.isPresent()) {
      response.sendError(
          HttpServletResponse.SC_BAD_REQUEST,
          "The request by the client was syntactically incorrect. The algorithm could not be determined.");
      ServletHelper.returnEmptyArrayResponse(response);
      return;
    }

    Collection<ScheduledTask> scheduledTasks =
        taskSchedulerOptional.get().schedule(events, tasks, workHoursStartTime, workHoursEndTime);

    Gson gson = new Gson();
    String resultJson = gson.toJson(scheduledTasks);

    response.setContentType("application/json");
    response.getWriter().println(resultJson);
  }
}
