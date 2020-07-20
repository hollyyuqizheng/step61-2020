package com.google.sps.data;

import com.google.gson.Gson;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

/** This class includes the helper methods used by ScheduleServlet.java */
public class ServletHelper {

  // TODO(tomasalvarez): Add tests for this method.
  public static Collection<CalendarEvent> collectEventsFromJsonArray(JSONArray eventsArray) {
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
  public static Collection<Task> collectTasksFromJsonArray(JSONArray tasksArray) {
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

  public static void returnEmptyArrayResponse(HttpServletResponse response) throws IOException {
    Gson gson = new Gson();
    String resultJson = gson.toJson(Arrays.asList());
    response.setContentType("application/json");
    response.getWriter().println(resultJson);
  }
}
