/**
 * Models a task that is displayed on the UI.
 * This class is useful when the task information needs to be sent
 * to the task servlet through a POST request.
 * This class contains all the task information that can be easily
 * converted into a JSON string.
 */
class Task {
  /**
   * All fields are required except description, which will be null
   * when not set.
   * @param name: name of task as String
   * @param description: description of task as String, can be null
   * @param duration: duration of task as Duration class
   * @param priority: priority level of task, range 1-5 inclusive. 
   */
  constructor(name, description, duration, priority) {
    this.name = name;
    this.description = description;
    this.duration = duration;
    this.priority = priority;
  }
}
