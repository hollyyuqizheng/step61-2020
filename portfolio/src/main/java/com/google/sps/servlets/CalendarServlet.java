package com.google.sps.servlets;

import com.google.gson.Gson;
import com.google.sps.data.CalendarEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that handles calendar event creation. */
@WebServlet("/calendarServlet")
public class CalendarServlet extends HttpServlet {

  public List<CalendarEvent> events;

  public CalendarServlet() {
    events = new ArrayList<CalendarEvent>();
  }

  /**
   * Retrieves the new calendar event's information from the POST request. Creates a new instance of
   * the CalendarEvent class and adds it to a list of calendar events.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Gson gson = new Gson();
    List<String> allEvents = gson.fromJson(request.getReader(), List.class);
    allEvents.forEach(
        (eventString) -> {
          CalendarEvent event = gson.fromJson(eventString, CalendarEvent.class);
          events.add(event);
        });
  }
}
