/**
 * Models the requests sent to the algorithm for scheduling. This class is
 * useful when the request information needs to be sent to the scheduling
 * servlet through a POST request. This class contains all the request
 * information that can be easily converted into a JSON string.
 */
class ScheduleRequest {
  constructor(events, tasks, startTime, endTime) {
    this.events = events;
    this.tasks = tasks;
    this.startTime = startTime;
    this.endTime = endTime;
  }
}

// TODO(tomasalvarez): write function to fetch results and display results
