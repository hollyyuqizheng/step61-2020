package com.google.sps.data;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class CollectTasksFromJsonArrayTest {

  /** Make sure that it handles no tasks. */
  @Test
  public void noTasks() {
    String taskJson = "{\"tasks\":[]}";
    JSONObject jsonObject = new JSONObject(taskJson);
    JSONArray tasksArray = jsonObject.getJSONArray("tasks");
    Collection<Task> actualTasks = ServletHelper.collectTasksFromJsonArray(tasksArray);
    Collection<Task> expectedTasks = new ArrayList<Task>();
    Assert.assertEquals(expectedTasks, actualTasks);
  }

  /** Make sure that it handles at least one tasks. */
  @Test
  public void someTasks() {
    String taskJson =
        "{\"tasks\":[{\"duration\":15,\"name\":\"Task 1\",\"taskPriority\":5,\"description\":\"First task\"}]}";
    JSONObject jsonObject = new JSONObject(taskJson);
    JSONArray tasksArray = jsonObject.getJSONArray("tasks");
    Collection<Task> actualTasks = ServletHelper.collectTasksFromJsonArray(tasksArray);
    Collection<Task> expectedTasks = new ArrayList<Task>();
    expectedTasks.add(
        new Task("Task 1", "First task", Duration.ofMinutes(15), new TaskPriority(5)));
    Assert.assertEquals(expectedTasks, actualTasks);
  }

  /**
   * We expect the class to throw a JSONException if the formatting is not what we expect in
   * collectTasksFromJsonArray() such as a missing duration in this case.
   */
  @Test(expected = org.json.JSONException.class)
  public void failedTasks() {
    String taskJson =
        "{\"tasks\":[{\"name\":\"Task 1\",\"taskPriority\":5,\"description\":\"First task\"}]}";
    JSONObject jsonObject = new JSONObject(taskJson);
    JSONArray tasksArray = jsonObject.getJSONArray("tasks");
    Collection<Task> actualTasks = ServletHelper.collectTasksFromJsonArray(tasksArray);
  }
}
