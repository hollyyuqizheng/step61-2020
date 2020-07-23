package com.google.sps.data;

import java.time.Duration;
import java.time.Instant;

public class SchedulerTestUtil {
  public static final Instant BEGINNING_OF_DAY = Instant.parse("2020-06-25T00:00:00Z");
  public static final Instant END_OF_DAY = Instant.parse("2020-06-25T23:59:59Z");
  public static final Instant THREE_DAYS_LATER = Instant.parse("2020-06-28T00:00:00Z");

  public static final Duration DURATION_5_MINUTES = Duration.ofSeconds(5 * 60);
  public static final Duration DURATION_10_MINUTES = Duration.ofSeconds(10 * 60);
  public static final Duration DURATION_15_MINUTES = Duration.ofSeconds(15 * 60);
  public static final Duration DURATION_20_MINUTES = Duration.ofSeconds(20 * 60);
  public static final Duration DURATION_30_MINUTES = Duration.ofSeconds(30 * 60);
  public static final Duration DURATION_45_MINUTES = Duration.ofSeconds(45 * 60);
  public static final Duration DURATION_60_MINUTES = Duration.ofSeconds(60 * 60);
  public static final Duration DURATION_80_MINUTES = Duration.ofSeconds(80 * 60);
  public static final Duration DURATION_100_MINUTES = Duration.ofSeconds(100 * 60);
  public static final Duration DURATION_2_HOURS = Duration.ofSeconds(120 * 60);

  public static final Instant TIME_0830 = Instant.parse("2020-06-25T08:30:00Z");
  public static final Instant TIME_0900 = TIME_0830.plus(DURATION_30_MINUTES);
  public static final Instant TIME_0920 = TIME_0900.plus(DURATION_20_MINUTES);
  public static final Instant TIME_0930 = TIME_0900.plus(DURATION_30_MINUTES);
  public static final Instant TIME_0950 = TIME_0900.plus(DURATION_20_MINUTES);
  public static final Instant TIME_1000 = TIME_0900.plus(DURATION_60_MINUTES);
  public static final Instant TIME_1020 = TIME_1000.plus(DURATION_20_MINUTES);
  public static final Instant TIME_1030 = TIME_1000.plus(DURATION_30_MINUTES);
  public static final Instant TIME_1100 = TIME_1000.plus(DURATION_60_MINUTES);
  public static final Instant TIME_1120 = TIME_1100.plus(DURATION_20_MINUTES);
  public static final Instant TIME_1130 = TIME_1030.plus(DURATION_60_MINUTES);
  public static final Instant TIME_1200 = TIME_1100.plus(DURATION_60_MINUTES);
  public static final Instant TIME_1300 = TIME_1200.plus(DURATION_60_MINUTES);
  public static final Instant TIME_1400 = TIME_1300.plus(DURATION_60_MINUTES);
  public static final Instant TIME_1500 = TIME_1400.plus(DURATION_60_MINUTES);
  public static final Instant TIME_1600 = TIME_1500.plus(DURATION_60_MINUTES);
  public static final Instant TIME_1700 = TIME_1600.plus(DURATION_60_MINUTES);
  public static final Instant TIME_1800 = TIME_1700.plus(DURATION_60_MINUTES);
  public static final Instant TIME_1900 = TIME_1800.plus(DURATION_60_MINUTES);
  public static final Instant TIME_2000 = TIME_1900.plus(DURATION_60_MINUTES);

  public static final TaskPriority PRIORITY_ONE = new TaskPriority(1);
  public static final TaskPriority PRIORITY_TWO = new TaskPriority(2);
  public static final TaskPriority PRIORITY_THREE = new TaskPriority(3);
  public static final TaskPriority PRIORITY_FOUR = new TaskPriority(4);
  public static final TaskPriority PRIORITY_FIVE = new TaskPriority(5);
}
