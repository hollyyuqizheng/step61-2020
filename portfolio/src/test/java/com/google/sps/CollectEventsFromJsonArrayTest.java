package com.google.sps.data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class CollectEventsFromJsonArrayTest {

  /** Make sure that it handles no events and no errors are thrown. */
  @Test
  public void noEvents() {
    String eventsJson = "{\"events\":[]}";
    JSONObject jsonObject = new JSONObject(eventsJson);
    JSONArray eventsArray = jsonObject.getJSONArray("events");
    Collection<CalendarEvent> actualEvents = ServletHelper.collectEventsFromJsonArray(eventsArray);
    Collection<CalendarEvent> expectedEvents = new ArrayList<CalendarEvent>();
    Assert.assertEquals(expectedEvents, actualEvents);
  }

  /** Make sure that it handles at least one events and no errors are thrown. */
  @Test
  public void someEvents() {
    String eventsJson =
        "{\"events\":[{\"name\":\"Event 1\",\"startTime\":\"2020-07-16T19:00:00.000Z\",\"endTime\":\"2020-07-16T21:00:00.000Z\"}]}";
    JSONObject jsonObject = new JSONObject(eventsJson);
    JSONArray eventsArray = jsonObject.getJSONArray("events");
    Collection<CalendarEvent> actualEvents = ServletHelper.collectEventsFromJsonArray(eventsArray);
    Collection<CalendarEvent> expectedEvents = new ArrayList<CalendarEvent>();
    expectedEvents.add(
        new CalendarEvent(
            "Event 1",
            Instant.parse("2020-07-16T19:00:00.000Z"),
            Instant.parse("2020-07-16T21:00:00.000Z")));
    Assert.assertEquals(expectedEvents, actualEvents);
  }

  /**
   * We expect the class to throw a JSONException if the formatting is not what we expect in
   * collectEventsFromJsonArray() such as a missing name in this case.
   */
  @Test(expected = org.json.JSONException.class)
  public void failedEvents() {
    String taskJson =
        "{\"events\":[{\"startTime\":\"2020-07-16T19:00:00.000Z\",\"endTime\":\"2020-07-16T21:00:00.000Z\"}]}";
    JSONObject jsonObject = new JSONObject(taskJson);
    JSONArray tasksArray = jsonObject.getJSONArray("tasks");
    Collection<Task> actualTasks = ServletHelper.collectTasksFromJsonArray(tasksArray);
  }
}
