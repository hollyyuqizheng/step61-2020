package com.google.sps.data;

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
