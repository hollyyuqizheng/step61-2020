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

/**
 * Class representing a span of time, enforcing properties (e.g. start comes before end) and
 * providing methods to make ranges easier to work with (e.g. {@code overlaps}).
 */
public final class TimeRange {
  private final long start;
  private final long duration;

  private TimeRange(long start, long duration) {
    this.start = start;
    this.duration = duration;
  }

  /** Returns the start of the range in minutes. */
  public long start() {
    return start;
  }

  /** Returns the number of minutes between the start and end. */
  public long duration() {
    return duration;
  }

  /** Returns the end of the range. This ending value is the closing exclusive bound. */
  public long end() {
    return start + duration;
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof TimeRange && equals(this, (TimeRange) other);
  }

  @Override
  public int hashCode() {
    return Long.hashCode(start) ^ Long.hashCode(duration);
  }

  @Override
  public String toString() {
    return String.format("Range: [%d, %d)", start, start + duration);
  }

  private static boolean equals(TimeRange a, TimeRange b) {
    return a.start == b.start && a.duration == b.duration;
  }

  /**
   * Creates a {@code TimeRange} from {@code start} to {@code end}. Whether or not {@code end} is
   * included in the range will depend on {@code inclusive}. If {@code inclusive} is {@code true},
   * then @{code end} will be in the range.
   */
  public static TimeRange fromStartEnd(long start, long end, boolean inclusive) {
    return inclusive ? new TimeRange(start, end - start + 1) : new TimeRange(start, end - start);
  }

  /**
   * Create a {@code TimeRange} starting at {@code start} with a duration equal to {@code duration}.
   */
  public static TimeRange fromStartDuration(long start, long duration) {
    return new TimeRange(start, duration);
  }
}
