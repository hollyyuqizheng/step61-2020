/**
 * Models a task that is displayed on the UI.
 * This class is useful when the task information needs to be sent
 * to the task servlet through a POST request.
 * This class contains all the task information that can be easily
 * converted into a JSON string.
 */
class Task {
  // TODO(raulcruise): could you add the type for each param please? 
  
  /**
   * All fields are required except description, which will be null
   * when not set.
   * @param name: name of task
   * @param description: description of task, can be null
   * @param durationMinutes: duration of task in minutes
   * @param priority: priority level of task, range 1-5 inclusive. 
   */
  constructor(name, description, durationMinutes, priority) {
    this.name = name;
    this.description = description;
    this.durationMinutes = durationMinutes;
    this.priority = priority;
  }
}
