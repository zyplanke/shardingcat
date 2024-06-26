/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.shardingcat.memory.unsafe.row;


import java.math.BigDecimal;

import io.shardingcat.memory.unsafe.Platform;
import io.shardingcat.memory.unsafe.array.ByteArrayMethods;
import io.shardingcat.memory.unsafe.bitset.BitSetMethods;

/**
 * A helper class to write data into global row buffer using `UnsafeRow` format.
 *
 * It will remember the offset of row buffer which it starts to write, and move the cursor of row
 * buffer while writing.  If new data(can be the input record if this is the outermost writer, or
 * nested struct if this is an inner writer) comes, the starting cursor of row buffer may be
 * changed, so we need to call `UnsafeRowWriter.reset` before writing, to update the
 * `startingOffset` and clear out null bits.
 *
 * Note that if this is the outermost writer, which means we will always write from the very
 * beginning of the global row buffer, we don't need to update `startingOffset` and can just call
 * `zeroOutNullBytes` before writing new data.
 */
public class UnsafeRowWriter {

  private final BufferHolder holder;
  // The offset of the global buffer where we start to write this row.
  private int startingOffset;
  private final int nullBitsSize;
  private final int fixedSize;

  public UnsafeRowWriter(BufferHolder holder,int numFields) {
    this.holder = holder;
    this.nullBitsSize = UnsafeRow.calculateBitSetWidthInBytes(numFields);
    this.fixedSize = nullBitsSize + 8 * numFields;
    this.startingOffset = holder.cursor;
  }

  /**
   * Resets the `startingOffset` according to the current cursor of row buffer, and clear out null
   * bits.  This should be called before we write a new nested struct to the row buffer.
   */
  public void reset() {
    this.startingOffset = holder.cursor;

    // grow the global buffer to make sure it has enough space to write fixed-length data.
    holder.grow(fixedSize);
    holder.cursor += fixedSize;

    zeroOutNullBytes();
  }

  /**
   * Clears out null bits.  This should be called before we write a new row to row buffer.
   */
  public void zeroOutNullBytes() {
    for (int i = 0; i < nullBitsSize; i += 8) {
      Platform.putLong(holder.buffer, startingOffset + i, 0L);
    }
  }

  private void zeroOutPaddingBytes(int numBytes) {
    if ((numBytes & 0x07) > 0) {
      Platform.putLong(holder.buffer, holder.cursor + ((numBytes >> 3) << 3), 0L);
    }
  }

  public BufferHolder holder() { return holder; }

  public boolean isNullAt(int ordinal) {
    return BitSetMethods.isSet(holder.buffer, startingOffset, ordinal);
  }

  public void setNullAt(int ordinal) {
    BitSetMethods.set(holder.buffer, startingOffset, ordinal);
    Platform.putLong(holder.buffer, getFieldOffset(ordinal), 0L);
  }

  public long getFieldOffset(int ordinal) {
    return startingOffset + nullBitsSize + 8 * ordinal;
  }

  public void setOffsetAndSize(int ordinal, long size) {
    setOffsetAndSize(ordinal, holder.cursor, size);
  }

  public void setOffsetAndSize(int ordinal, long currentCursor, long size) {
    final long relativeOffset = currentCursor - startingOffset;
    final long fieldOffset = getFieldOffset(ordinal);
    final long offsetAndSize = (relativeOffset << 32) | size;

    Platform.putLong(holder.buffer, fieldOffset, offsetAndSize);
  }

  // Do word alignment for this row and grow the row buffer if needed.
  // todo: remove this after we make unsafe array data word align.
  public void alignToWords(int numBytes) {
    final int remainder = numBytes & 0x07;

    if (remainder > 0) {
      final int paddingBytes = 8 - remainder;
      holder.grow(paddingBytes);

      for (int i = 0; i < paddingBytes; i++) {
        Platform.putByte(holder.buffer, holder.cursor, (byte) 0);
        holder.cursor++;
      }
    }
  }

  public void write(int ordinal, boolean value) {
    final long offset = getFieldOffset(ordinal);
    Platform.putLong(holder.buffer, offset, 0L);
    Platform.putBoolean(holder.buffer, offset, value);
  }

  public void write(int ordinal, byte value) {
    final long offset = getFieldOffset(ordinal);
    Platform.putLong(holder.buffer, offset, 0L);
    Platform.putByte(holder.buffer, offset, value);
  }

  public void write(int ordinal, short value) {
    final long offset = getFieldOffset(ordinal);
    Platform.putLong(holder.buffer, offset, 0L);
    Platform.putShort(holder.buffer, offset, value);
  }

  public void write(int ordinal, int value) {
    final long offset = getFieldOffset(ordinal);
    Platform.putLong(holder.buffer, offset, 0L);
    Platform.putInt(holder.buffer, offset, value);
  }

  public void write(int ordinal, long value) {
    Platform.putLong(holder.buffer, getFieldOffset(ordinal), value);
  }

  public void write(int ordinal, float value) {
    if (Float.isNaN(value)) {
      value = Float.NaN;
    }
    final long offset = getFieldOffset(ordinal);
    Platform.putLong(holder.buffer, offset, 0L);
    Platform.putFloat(holder.buffer, offset, value);
  }

  public void write(int ordinal, double value) {
    if (Double.isNaN(value)) {
      value = Double.NaN;
    }
    Platform.putDouble(holder.buffer, getFieldOffset(ordinal), value);
  }

  public void write(int ordinal, byte[] input) {
    if(input == null){
      return;
    }
    write(ordinal, input, 0, input.length);
  }

  public void write(int ordinal, byte[] input, int offset, int numBytes) {
    final int roundedSize = ByteArrayMethods.roundNumberOfBytesToNearestWord(numBytes);

    // grow the global buffer before writing data.
    holder.grow(roundedSize);

    zeroOutPaddingBytes(numBytes);

    // Write the bytes to the variable length portion.
    Platform.copyMemory(input, Platform.BYTE_ARRAY_OFFSET + offset,
      holder.buffer, holder.cursor, numBytes);

    setOffsetAndSize(ordinal, numBytes);

    // move the cursor forward.
    holder.cursor += roundedSize;
  }

  	/**
	 * different from Spark, we use java BigDecimal here, 
	 * and we limit the max precision to be 38 because the bytes length limit to be 16
	 * 
	 * @param ordinal
	 * @param input
	 */
	public void write(int ordinal, BigDecimal input) {

		// grow the global buffer before writing data.
		holder.grow(16);

		// zero-out the bytes
		Platform.putLong(holder.buffer, holder.cursor, 0L);
		Platform.putLong(holder.buffer, holder.cursor + 8, 0L);

		// Make sure Decimal object has the same scale as DecimalType.
		// Note that we may pass in null Decimal object to set null for it.
		if (input == null) {
			BitSetMethods.set(holder.buffer, startingOffset, ordinal);
			// keep the offset for future update
			setOffsetAndSize(ordinal, 0L);
		} else {
			final byte[] bytes = input.unscaledValue().toByteArray();
			assert bytes.length <= 16;

			// Write the bytes to the variable length portion.
			Platform.copyMemory(bytes, Platform.BYTE_ARRAY_OFFSET, holder.buffer, holder.cursor, bytes.length);
			setOffsetAndSize(ordinal, bytes.length);
		}

		// move the cursor forward.
		holder.cursor += 16;
	}
  
}
