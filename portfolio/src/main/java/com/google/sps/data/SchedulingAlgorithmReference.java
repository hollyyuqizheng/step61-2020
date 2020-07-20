package com.google.sps.data;

import java.util.Optional;

/** This class includes the SchedulingAlgorithmType methods that are used by ScheduleServlet.java */
public class SchedulingAlgorithmReference {

  public static Optional<SchedulingAlgorithmType> getSchedulingAlgorithmTypeOptional(
      String algorithmTypeString) {
    // Here we should add a case for each new algorithm that is implemented.
    switch (algorithmTypeString) {
      case "SHORTEST_TASK_FIRST":
        return Optional.of(SchedulingAlgorithmType.SHORTEST_TASK_FIRST);
    }
    return Optional.empty();
  }

  public static Optional<TaskScheduler> getTaskSchedulerOptional(
      Optional<SchedulingAlgorithmType> schedulingAlgorithmTypeOptional) {
    // This will always be present because in the doPost we return the method
    // before the code gets to call this method if this Optional is not
    // present.
    SchedulingAlgorithmType schedulingAlgorithmType = schedulingAlgorithmTypeOptional.get();
    // Here we should add a case for each new algorithm that is implemented.
    switch (schedulingAlgorithmType) {
      case SHORTEST_TASK_FIRST:
        return Optional.of(new ShortestTaskFirstScheduler());
    }
    return Optional.empty();
  }
}
