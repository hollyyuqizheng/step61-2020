/**
 * Models a task that is displayed on the UI.
 * This class is useful when the task information needs to be sent
 * to the task servlet through a POST request.
 * This class contains all the task information that can be easily
 * converted into a JSON string.
 * All fields are required except description, which will be null
 * when not set.
 */
class Task {
  constructor(name, description, durationMinutes, priority) {
    this.name = name;
    this.description = description;
    this.durationMinutes = durationMinutes;
    this.priority = priority;
  }
}
