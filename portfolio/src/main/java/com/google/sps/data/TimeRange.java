// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.data;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;

/**
 * Class representing a span of time, enforcing properties (e.g. start comes before end) and
 * providing methods to make ranges easier to work with (e.g. {@code overlaps}).
 */
public final class TimeRange {
  private final Instant start;
  private final Duration duration;

  // Comparator for sorting time ranges by start time
  public static final Comparator<TimeRange> sortByTimeRangeStartTimeAscending =
      Comparator.comparing(TimeRange::start);

  public static final Comparator<TimeRange> sortByTimeRangeDurationAscending =
      Comparator.comparing(TimeRange::duration);

  public static final Comparator<TimeRange> sortByTimeRangeDurationAscendingThenStartTime =
      Comparator.comparing(TimeRange::duration).thenComparing(TimeRange::start);

  private TimeRange(Instant start, Duration duration) {
    this.start = start;
    this.duration = duration;
  }

  /** Returns the start of the range in minutes. */
  public Instant start() {
    return start;
  }

  /** Returns the number of minutes between the start and end. */
  public Duration duration() {
    return duration;
  }

  /** Returns the end of the range. This ending value is the closing exclusive bound. */
  public Instant end() {
    return start.plus(duration);
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof TimeRange && equals(this, (TimeRange) other);
  }

  @Override
  public String toString() {
    return String.format("Range: [%s, %s]", start.toString(), this.end().toString());
  }

  public static boolean equals(TimeRange a, TimeRange b) {
    return a.start.equals(b.start) && a.duration.equals(b.duration);
  }

  /** Creates a {@code TimeRange} from {@code start} to {@code end}. */
  public static TimeRange fromStartEnd(Instant start, Instant end) {
    return new TimeRange(start, Duration.ofSeconds(end.getEpochSecond() - start.getEpochSecond()));
  }

  /**
   * Checks if this range completely contains another range. This means that {@code otherRange} is a
   * subset of this range. This is an inclusive bounds, meaning that if two ranges are the same,
   * they contain each other.
   */
  public boolean contains(TimeRange otherRange) {
    // If this range has no duration, it cannot contain anything.
    if (duration.getSeconds() <= 0) {
      return false;
    }

    // If the other range has no duration, then it is treated like a point that is
    // anchored in its start time.
    if (otherRange.duration.getSeconds() <= 0) {
      return timeRangeContainsPoint(this, otherRange.start);
    }

    // Checks if the time range contains the other range's start and end points.
    return timeRangeContainsPoint(this, otherRange.start)
        && timeRangeContainsPoint(this, otherRange.end());
  }

  /**
   * Checks if a time range contains a time point. Helper method for contains and overlaps. This
   * method is package-private so that it can be tested.
   */
  static boolean timeRangeContainsPoint(TimeRange range, Instant point) {
    // If a range has no duration, it cannot contain anything.
    if (range.duration.getSeconds() <= 0) {
      return false;
    }

    // If the point comes before the start of the range, the range cannot contain it.
    if (point.getEpochSecond() < range.start.getEpochSecond()) {
      return false;
    }

    // This is to make sure [8 - 8:30] contains 8:30, for example.
    // The end of a time range is considered part of this time range,
    // so that [8 - 8:30] and [8:30 - 9] can be considered as overlapping.
    return point.getEpochSecond() <= range.start.getEpochSecond() + range.duration.getSeconds();
  }

  /**
   * Checks if two ranges overlap. This means that at least some part of one range falls within the
   * bounds of another range.
   */
  public boolean overlaps(TimeRange otherRange) {
    // For two ranges to overlap, one range must contain the start of another range.
    // Case 1: |---| |---|
    //
    // Case 2: |---|
    //            |---|
    // Case 3: |---------|
    //            |---|
    // Case 4:    |--------|
    //         |-----|
    return (timeRangeContainsPoint(this, otherRange.start())
        || timeRangeContainsPoint(otherRange, start));
  }
}
