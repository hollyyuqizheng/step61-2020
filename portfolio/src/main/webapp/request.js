/**
 * Models the requests sent to the algorithm for scheduling. This class is
 * useful when the request information needs to be sent to the scheduling
 * servlet through a POST request. This class contains all the request
 * information that can be easily converted into a JSON string.
 */
class ScheduleRequest {
  /**
   * All fields are required for this object.
   * @param events: events happening the day of the scheduling,
   * @param tasks: the tasks a user would like to schedule on that day
   * @param startTime: a JavaScript Date object representing the start time.
   * @param endTime: a JavaScript Date object representing the end time.
   */
  constructor(events, tasks, startTime, endTime) {
    this.events = events;
    this.tasks = tasks;
    this.startTime = startTime;
    this.endTime = endTime;
  }
}

// TODO(tomasalvarez): write function to fetch results and display results
