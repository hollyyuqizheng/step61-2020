package com.google.sps.data;

/**
 * This Enum includes the scenarios for a scheduled task's completeness. A scheduled task can either
 * be completely scheduled, partially scheduled, or not be able to be scheduled at all.
 */
public enum SchedulingCompleteness {
  NOT_SCHEDULED(0),
  PARTIALLY_SCHEDULED(1),
  COMPLETELY_SCHEDULED(2);

  private final int value;

  SchedulingCompleteness(int value) {
    this.value = value;
  }

  public int getValue() {
    return this.value;
  }
}
