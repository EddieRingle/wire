/*
 * Copyright 2013 Square Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squareup.wire;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import okio.ByteString;

/**
 * Superclass for protocol buffer messages.
 */
public abstract class Message implements Serializable {
  private static final long serialVersionUID = 0L;

  // Hidden Wire instance that can perform work that does not require knowledge of extensions.
  static final Wire WIRE = new Wire();

  /**
   * A protocol buffer data type.
   */
  public enum Datatype {
    INT32(1), INT64(2), UINT32(3), UINT64(4), SINT32(5),
    SINT64(6), BOOL(7), ENUM(8), STRING(9), BYTES(10),
    MESSAGE(11), FIXED32(12), SFIXED32(13), FIXED64(14),
    SFIXED64(15), FLOAT(16), DOUBLE(17);

    private final int value;

    Datatype(int value) {
      this.value = value;
    }

    public int value() {
      return value;
    }

    WireType wireType() {
      switch (this) {
        case INT32: case INT64: case UINT32: case UINT64:
        case SINT32: case SINT64: case BOOL: case ENUM:
          return WireType.VARINT;
        case FIXED32: case SFIXED32: case FLOAT:
          return WireType.FIXED32;
        case FIXED64: case SFIXED64: case DOUBLE:
          return WireType.FIXED64;
        case STRING: case BYTES: case MESSAGE:
          return WireType.LENGTH_DELIMITED;
        default:
          throw new AssertionError("No wiretype for datatype " + this);
      }
    }
  }

  /**
   * A protocol buffer label. We treat "packed" as a label of its own that implies "repeated."
   */
  public enum Label {
    REQUIRED(32), OPTIONAL(64), REPEATED(128), PACKED(256), ONE_OF(512);

    private final int value;

    Label(int value) {
      this.value = value;
    }

    public int value() {
      return value;
    }

    boolean isRepeated() {
      return this == REPEATED || this == PACKED;
    }

    boolean isPacked() {
      return this == PACKED;
    }

    boolean isOneOf() {
      return this == ONE_OF;
    }
  }

  /** Set to null until a field is added. */
  protected transient UnknownFieldMap unknownFields;

  /** If not {@code -1} then the serialized size of this message. */
  protected transient int size = -1;

  /** If non-zero, the hash code of this message. Accessed by generated code. */
  protected transient int hashCode = 0;

  protected Message() {
  }

  /**
   * Initializes any unknown field data to that stored in the given {@code Builder}.
   */
  protected void setBuilder(Builder builder) {
    if (builder.unknownFieldMap != null) {
      unknownFields = new UnknownFieldMap(builder.unknownFieldMap);
    }
  }

  // Increase visibility for testing
  protected Collection<List<UnknownFieldMap.FieldValue>> unknownFields() {
    return unknownFields == null ? Collections.<List<UnknownFieldMap.FieldValue>>emptySet()
        : unknownFields.fieldMap.values();
  }

  /**
   * Utility method to return a mutable copy of a given List. Used by generated code.
   */
  protected static <T> List<T> copyOf(List<T> source) {
    return source == null ? null : new ArrayList<T>(source);
  }

  /**
   * Utility method to return an immutable copy of a given List. Used by generated code.
   * If {@code source} is null, {@link Collections#emptyList()} is returned.
   */
  protected static <T> List<T> immutableCopyOf(List<T> source) {
    if (source == Collections.emptyList()) {
      return source;
    }
    if (source instanceof MessageAdapter.ImmutableList) {
      return source;
    }
    return Collections.unmodifiableList(new ArrayList<T>(source));
  }

  /**
   * Returns the enumerated value tagged with the given integer value for the
   * given enum class. If no enum value in the given class is initialized
   * with the given integer tag value, an exception will be thrown.
   *
   * @param <E> the enum class type
   */
  public static <E extends Enum & ProtoEnum> E enumFromInt(Class<E> enumClass, int value) {
    EnumAdapter<E> adapter = WIRE.enumAdapter(enumClass);
    return adapter.fromInt(value);
  }

  void writeUnknownFieldMap(WireOutput output) throws IOException {
    if (unknownFields != null) {
      unknownFields.write(output);
    }
  }

  protected int unknownFieldsSize() {
    return unknownFields == null ? 0 : unknownFields.size();
  }

  protected static boolean equals(Object a, Object b) {
    return a == b || (a != null && a.equals(b));
  }

  @SuppressWarnings("unchecked")
  @Override public String toString() {
    return WIRE.messageAdapter((Class<Message>) getClass()).toString(this);
  }

  private Object writeReplace() throws ObjectStreamException {
    return new MessageSerializedForm(this, getClass());
  }

  protected abstract int size();

  /**
   * Superclass for protocol buffer message builders.
   */
  public abstract static class Builder<T extends Message> {

    UnknownFieldMap unknownFieldMap;

    /**
     * Constructs a Builder with no unknown field data.
     */
    public Builder() {
    }

    /**
     * Constructs a Builder with unknown field data initialized to a copy of any unknown
     * field data in the given {@link Message}.
     */
    public Builder(Message message) {
      if (message != null && message.unknownFields != null) {
        this.unknownFieldMap = new UnknownFieldMap(message.unknownFields);
      }
    }

    /**
     * Adds a {@code varint} value to the unknown field set with the given tag number.
     */
    public void addVarint(int tag, long value) {
      try {
        ensureUnknownFieldMap().addVarint(tag, value);
      } catch (IOException e) {
        throw new IllegalArgumentException(e.getMessage());
      }
    }

    /**
     * Adds a {@code fixed32} value to the unknown field set with the given tag number.
     */
    public void addFixed32(int tag, int value) {
      try {
        ensureUnknownFieldMap().addFixed32(tag, value);
      } catch (IOException e) {
        throw new IllegalArgumentException(e.getMessage());
      }
    }

    /**
     * Adds a {@code fixed64} value to the unknown field set with the given tag number.
     */
    public void addFixed64(int tag, long value) {
      try {
        ensureUnknownFieldMap().addFixed64(tag, value);
      } catch (IOException e) {
        throw new IllegalArgumentException(e.getMessage());
      }
    }

    /**
     * Adds a length delimited value to the unknown field set with the given tag number.
     */
    public void addLengthDelimited(int tag, ByteString value) {
      try {
        ensureUnknownFieldMap().addLengthDelimited(tag, value);
      } catch (IOException e) {
        throw new IllegalArgumentException(e.getMessage());
      }
    }

    UnknownFieldMap ensureUnknownFieldMap() {
      if (unknownFieldMap == null) {
        unknownFieldMap = new UnknownFieldMap();
      }
      return unknownFieldMap;
    }

    /**
     * Create an exception for missing required fields.
     *
     * @param args Alternating field value and field name pairs.
     */
    protected IllegalStateException missingRequiredFields(Object... args) {
      StringBuilder sb = new StringBuilder();
      String plural = "";
      for (int i = 0, size = args.length; i < size; i += 2) {
        if (args[i] == null) {
          if (sb.length() > 0) {
            plural = "s"; // Found more than one missing field
          }
          sb.append("\n  ");
          sb.append(args[i + 1]);
        }
      }
      throw new IllegalStateException("Required field" + plural + " not set:" + sb);
    }

    /**
     * If {@code list} is null it will be replaced with {@link Collections#emptyList()}.
     * Otherwise look for null items and throw {@link NullPointerException} if one is found.
     */
    protected static <T> List<T> canonicalizeList(List<T> list) {
      if (list == null) {
        return Collections.emptyList();
      }
      for (int i = 0, size = list.size(); i < size; i++) {
        T element = list.get(i);
        if (element == null) {
          throw new NullPointerException("Element at index " + i + " is null");
        }
      }
      return list;
    }

    /**
     * Returns an immutable {@link com.squareup.wire.Message} based on the fields that have been set
     * in this builder.
     */
    public abstract T build();
  }

  protected static int sizeOfBool(int tag, boolean value) {
    return WireOutput.tagSize(tag) + 1;
  }

  protected static int sizeOfInt32(int tag, int value) {
    return WireOutput.tagSize(tag) + (value < 0 ? 10 : WireOutput.varint32Size(value));
  }

  protected static int sizeOfUint32(int tag, int value) {
    return WireOutput.tagSize(tag) + WireOutput.varint32Size(value);
  }

  protected static int sizeOfSint32(int tag, int value) {
    return WireOutput.tagSize(tag) + WireOutput.varint32ZigZagSize(value);
  }

  protected static int sizeOfFixed32(int tag, int value) {
    return WireOutput.tagSize(tag) + 4;
  }

  protected static int sizeOfSfixed32(int tag, int value) {
    return WireOutput.tagSize(tag) + 4;
  }

  protected static int sizeOfFloat(int tag, float value) {
    return WireOutput.tagSize(tag) + 4;
  }

  protected static int sizeOfInt64(int tag, long value) {
    return WireOutput.tagSize(tag) + WireOutput.varint64Size(value);
  }

  protected static int sizeOfUint64(int tag, long value) {
    return WireOutput.tagSize(tag) + WireOutput.varint64Size(value);
  }

  protected static int sizeOfSint64(int tag, long value) {
    return WireOutput.tagSize(tag) + WireOutput.varint64ZigZagSize(value);
  }

  protected static int sizeOfFixed64(int tag, long value) {
    return WireOutput.tagSize(tag) + 8;
  }

  protected static int sizeOfSfixed64(int tag, long value) {
    return WireOutput.tagSize(tag) + 8;
  }

  protected static int sizeOfDouble(int tag, double value) {
    return WireOutput.tagSize(tag) + 8;
  }

  protected static int sizeOfString(int tag, String value) {
    int size = 0;
    for (int i = 0, length = value.length(); i < length; i++) {
      char ch = value.charAt(i);
      if (ch <= 0x7F) {
        size++;
      } else if (ch <= 0x7FF) {
        size += 2;
      } else if (Character.isHighSurrogate(ch)) {
        size += 4;
        ++i;
      } else {
        size += 3;
      }
    }
    return WireOutput.tagSize(tag) + WireOutput.varint32Size(size) + size;
  }

  protected  static int sizeOfBytes(int tag, ByteString value) {
    return WireOutput.tagSize(tag) + WireOutput.varint32Size(value.size()) + value.size();
  }

  protected static int sizeOfEnum(int tag, ProtoEnum value) {
    return WireOutput.tagSize(tag) + WireOutput.varint32Size(value.getValue());
  }

  protected static int sizeOfMessage(int tag, Message value) {
    return WireOutput.tagSize(tag) + value.size();
  }
  
  protected static int sizeOfRepeatedInt32(int tag, List<Integer> values) {
    int count = values.size();
    int size = count * WireOutput.tagSize(tag);
    for (int i = 0; i < count; i++) {
      Integer value = values.get(i);
      size += value < 0 ? 10 : WireOutput.varint32Size(value);
    }
    return size;
  }

  protected static int sizeOfRepeatedUint32(int tag, List<Integer> values) {
    int count = values.size();
    int size = count * WireOutput.tagSize(tag);
    for (int i = 0; i < count; i++) {
      size += WireOutput.varint32Size(values.get(i));
    }
    return size;
  }

  protected static int sizeOfRepeatedSint32(int tag, List<Integer> values) {
    int count = values.size();
    int size = count * WireOutput.tagSize(tag);
    for (int i = 0; i < count; i++) {
      size += WireOutput.varint32ZigZagSize(values.get(i));
    }
    return size;
  }

  protected static int sizeOfRepeatedFixed32(int tag, List<Integer> values) {
    return values.size() * (WireOutput.tagSize(tag) + 4);
  }

  protected static int sizeOfRepeatedSfixed32(int tag, List<Integer> values) {
    return values.size() * (WireOutput.tagSize(tag) + 4);
  }

  protected static int sizeOfRepeatedInt64(int tag, List<Long> values) {
    int count = values.size();
    int size = count * WireOutput.tagSize(tag);
    for (int i = 0; i < count; i++) {
      size += WireOutput.varint64Size(values.get(i));
    }
    return size;
  }

  protected static int sizeOfRepeatedUint64(int tag, List<Long> values) {
    int count = values.size();
    int size = count * WireOutput.tagSize(tag);
    for (int i = 0; i < count; i++) {
      size += WireOutput.varint64Size(values.get(i));
    }
    return size;
  }

  protected static int sizeOfRepeatedSint64(int tag, List<Long> values) {
    int count = values.size();
    int size = count * WireOutput.tagSize(tag);
    for (int i = 0; i < count; i++) {
      size += WireOutput.varint64ZigZagSize(values.get(i));
    }
    return size;
  }

  protected static int sizeOfRepeatedFixed64(int tag, List<Long> values) {
    return values.size() * (WireOutput.tagSize(tag) + 8);
  }

  protected static int sizeOfRepeatedSfixed64(int tag, List<Long> values) {
    return values.size() * (WireOutput.tagSize(tag) + 8);
  }

  protected static int sizeOfRepeatedBool(int tag, List<Boolean> values) {
    return values.size() * (WireOutput.tagSize(tag) + 1);
  }

  protected static int sizeOfRepeatedFloat(int tag, List<Float> values) {
    return values.size() * (WireOutput.tagSize(tag) + 4);
  }

  protected static int sizeOfRepeatedDouble(int tag, List<Double> values) {
    return values.size() * (WireOutput.tagSize(tag) + 8);
  }

  protected static int sizeOfRepeatedString(int tag, List<String> values) {
    int count = values.size();
    int size = count * WireOutput.tagSize(tag);
    for (int i = 0; i < count; i++) {
      String value = values.get(i);
      size += WireOutput.varint32Size(value.length()) + value.length();
    }
    return size;
  }

  protected static int sizeOfRepeatedBytes(int tag, List<ByteString> values) {
    int count = values.size();
    int size = count * WireOutput.tagSize(tag);
    for (int i = 0; i < count; i++) {
      ByteString value = values.get(i);
      size += WireOutput.varint32Size(value.size()) + value.size();
    }
    return size;
  }

  protected static int sizeOfRepeatedEnum(int tag, List<? extends ProtoEnum> values) {
    int count = values.size();
    int size = count * WireOutput.tagSize(tag);
    for (int i = 0; i < count; i++) {
      ProtoEnum value = values.get(i);
      size += WireOutput.varint32Size(value.getValue());
    }
    return size;
  }

  protected static int sizeOfRepeatedMessage(int tag, List<? extends Message> values) {
    int count = values.size();
    int size = count * WireOutput.tagSize(tag);
    for (int i = 0; i < count; i++) {
      size += values.get(i).size();
    }
    return size;
  }

  protected static int sizeOfPackedInt32(int tag, List<Integer> values) {
    int count = values.size();
    int size = packedTagSize(tag) + WireOutput.varint32Size(count);
    for (int i = 0; i < count; i++) {
      Integer value = values.get(i);
      size += value < 0 ? 10 : WireOutput.varint32Size(value);
    }
    return size;
  }

  protected static int sizeOfPackedUint32(int tag, List<Integer> values) {
    int count = values.size();
    int size = packedTagSize(tag) + WireOutput.varint32Size(count);
    for (int i = 0; i < count; i++) {
      size += WireOutput.varint32Size(values.get(i));
    }
    return size;
  }

  protected static int sizeOfPackedSint32(int tag, List<Integer> values) {
    int count = values.size();
    int size = packedTagSize(tag) + WireOutput.varint32Size(count);
    for (int i = 0; i < count; i++) {
      size += WireOutput.varint32ZigZagSize(values.get(i));
    }
    return size;
  }

  protected static int sizeOfPackedFixed32(int tag, List<Integer> values) {
    int count = values.size();
    return packedTagSize(tag) + WireOutput.varint32Size(count) + (count * 4);
  }

  protected static int sizeOfPackedSfixed32(int tag, List<Integer> values) {
    int count = values.size();
    return packedTagSize(tag) + WireOutput.varint32Size(count) + (count * 4);
  }

  protected static int sizeOfPackedInt64(int tag, List<Long> values) {
    int count = values.size();
    int size = packedTagSize(tag) + WireOutput.varint32Size(count);
    for (int i = 0; i < count; i++) {
      size += WireOutput.varint64Size(values.get(i));
    }
    return size;
  }

  protected static int sizeOfPackedUint64(int tag, List<Long> values) {
    int count = values.size();
    int size = packedTagSize(tag) + WireOutput.varint32Size(count);
    for (int i = 0; i < count; i++) {
      size += WireOutput.varint64Size(values.get(i));
    }
    return size;
  }

  protected static int sizeOfPackedSint64(int tag, List<Long> values) {
    int count = values.size();
    int size = packedTagSize(tag) + WireOutput.varint32Size(count);
    for (int i = 0; i < count; i++) {
      size += WireOutput.varint64ZigZagSize(values.get(i));
    }
    return size;
  }

  protected static int sizeOfPackedFixed64(int tag, List<Long> values) {
    int count = values.size();
    return packedTagSize(tag) + WireOutput.varint32Size(count) + (count * 8);
  }

  protected static int sizeOfPackedSfixed64(int tag, List<Long> values) {
    int count = values.size();
    return packedTagSize(tag) + WireOutput.varint32Size(count) + (count * 8);
  }

  protected static int sizeOfPackedBool(int tag, List<Boolean> values) {
    int count = values.size();
    return packedTagSize(tag) + WireOutput.varint32Size(count) + count;
  }

  protected static int sizeOfPackedFloat(int tag, List<Float> values) {
    int count = values.size();
    return packedTagSize(tag) + WireOutput.varint32Size(count) + (count * 4);
  }

  protected static int sizeOfPackedDouble(int tag, List<Double> values) {
    int count = values.size();
    return packedTagSize(tag) + WireOutput.varint32Size(count) + (count * 8);
  }

  protected static int sizeOfPackedEnum(int tag, List<? extends ProtoEnum> values) {
    int count = values.size();
    int size = packedTagSize(tag) + WireOutput.varint32Size(count);
    for (int i = 0; i < count; i++) {
      size += WireOutput.varint32Size(values.get(i).getValue());
    }
    return size;
  }

  private static int packedTagSize(int tag) {
    return WireOutput.varint32Size(WireOutput.makeTag(tag, WireType.LENGTH_DELIMITED));
  }
}
