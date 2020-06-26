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
import java.util.Arrays;
import java.util.Collection;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/schedule")
public class ScheduleServlet extends HttpServlet {
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Gson gson = new Gson();

    // Convert the JSON to an instance of ScheduleRequest.
    ScheduleRequest scheduleRequest = gson.fromJson(request.getReader(), ScheduleRequest.class);
    // Handle the data unpacking here instead of in the algorithm
    Collection<CalendarEvent> events = scheduleRequest.getEvents();
    Collection<Task> tasks = scheduleRequest.getTasks();
    long startWorkHours = scheduleRequest.getWorkHoursStartTimeLong();
    long endWorkHours = scheduleRequest.getWorkHoursEndTimeLong();

    // Find the possible meeting times.

    FindSchedule findScheduleQuery = new FindSchedule();
    Collection<Task> answer = findScheduleQuery.greedy(events, tasks, startWorkHours, endWorkHours);

    // Convert the times to JSON
    String jsonResponse = gson.toJson(answer);

    // Send the JSON back as the response
    response.setContentType("application/json");
    response.getWriter().println(jsonResponse);
  }
}
