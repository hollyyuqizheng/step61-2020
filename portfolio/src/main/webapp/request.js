/**
 * Models the requests sent to the algorithm for scheduling. This class is
 * useful when the request information needs to be sent to the scheduling
 * servlet through a POST request. This class contains all the request
 * information that can be easily converted into a JSON string.
 */
class ScheduleRequest {
  /**
   * All fields are required for this object.
   * @param events: events happening the day of the scheduling. This is an
   *     array of JavaScript CalendarEvent objects.
   * @param tasks: the tasks a user would like to schedule on that day. This is an
   *     array of JavaScript Task objects.
   * @param startTime: a JavaScript Date object representing the start time.
   * @param endTime: a JavaScript Date object representing the end time.
   * @param algorithmType: a string representation of the algorithm type that
   *     the user can select fromt the UI.
   */
  constructor(events, tasks, startTime, endTime, algorithmType) {
    this.events = events;
    this.tasks = tasks;
    this.startTime = startTime;
    this.endTime = endTime;
    this.algorithmType = algorithmType;
  }
}

// TODO(tomasalvarez): write function to fetch results and display results
