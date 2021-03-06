// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: ../wire-runtime/src/test/proto/unknown_fields.proto at 24:1
package com.squareup.wire.protos.unknownfields;

import com.squareup.wire.FieldEncoding;
import com.squareup.wire.Message;
import com.squareup.wire.ProtoAdapter;
import com.squareup.wire.ProtoReader;
import com.squareup.wire.ProtoWriter;
import java.io.IOException;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.util.List;
import okio.ByteString;

public final class VersionTwo extends Message<VersionTwo, VersionTwo.Builder> {
  public static final ProtoAdapter<VersionTwo> ADAPTER = new ProtoAdapter<VersionTwo>(FieldEncoding.LENGTH_DELIMITED, VersionTwo.class) {
    @Override
    public int encodedSize(VersionTwo value) {
      return (value.i != null ? ProtoAdapter.INT32.encodedSizeWithTag(1, value.i) : 0)
          + (value.v2_i != null ? ProtoAdapter.INT32.encodedSizeWithTag(2, value.v2_i) : 0)
          + (value.v2_s != null ? ProtoAdapter.STRING.encodedSizeWithTag(3, value.v2_s) : 0)
          + (value.v2_f32 != null ? ProtoAdapter.FIXED32.encodedSizeWithTag(4, value.v2_f32) : 0)
          + (value.v2_f64 != null ? ProtoAdapter.FIXED64.encodedSizeWithTag(5, value.v2_f64) : 0)
          + ProtoAdapter.STRING.asRepeated().encodedSizeWithTag(6, value.v2_rs)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, VersionTwo value) throws IOException {
      if (value.i != null) ProtoAdapter.INT32.encodeWithTag(writer, 1, value.i);
      if (value.v2_i != null) ProtoAdapter.INT32.encodeWithTag(writer, 2, value.v2_i);
      if (value.v2_s != null) ProtoAdapter.STRING.encodeWithTag(writer, 3, value.v2_s);
      if (value.v2_f32 != null) ProtoAdapter.FIXED32.encodeWithTag(writer, 4, value.v2_f32);
      if (value.v2_f64 != null) ProtoAdapter.FIXED64.encodeWithTag(writer, 5, value.v2_f64);
      if (value.v2_rs != null) ProtoAdapter.STRING.asRepeated().encodeWithTag(writer, 6, value.v2_rs);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public VersionTwo decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.i(ProtoAdapter.INT32.decode(reader)); break;
          case 2: builder.v2_i(ProtoAdapter.INT32.decode(reader)); break;
          case 3: builder.v2_s(ProtoAdapter.STRING.decode(reader)); break;
          case 4: builder.v2_f32(ProtoAdapter.FIXED32.decode(reader)); break;
          case 5: builder.v2_f64(ProtoAdapter.FIXED64.decode(reader)); break;
          case 6: builder.v2_rs.add(ProtoAdapter.STRING.decode(reader)); break;
          default: {
            FieldEncoding fieldEncoding = reader.peekFieldEncoding();
            Object value = fieldEncoding.rawProtoAdapter().decode(reader);
            builder.addUnknownField(tag, fieldEncoding, value);
          }
        }
      }
      reader.endMessage(token);
      return builder.build();
    }

    @Override
    public VersionTwo redact(VersionTwo value) {
      Builder builder = value.newBuilder();
      builder.clearUnknownFields();
      return builder.build();
    }
  };

  private static final long serialVersionUID = 0L;

  public static final Integer DEFAULT_I = 0;

  public static final Integer DEFAULT_V2_I = 0;

  public static final String DEFAULT_V2_S = "";

  public static final Integer DEFAULT_V2_F32 = 0;

  public static final Long DEFAULT_V2_F64 = 0L;

  public final Integer i;

  public final Integer v2_i;

  public final String v2_s;

  public final Integer v2_f32;

  public final Long v2_f64;

  public final List<String> v2_rs;

  public VersionTwo(Integer i, Integer v2_i, String v2_s, Integer v2_f32, Long v2_f64, List<String> v2_rs) {
    this(i, v2_i, v2_s, v2_f32, v2_f64, v2_rs, ByteString.EMPTY);
  }

  public VersionTwo(Integer i, Integer v2_i, String v2_s, Integer v2_f32, Long v2_f64, List<String> v2_rs, ByteString unknownFields) {
    super(unknownFields);
    this.i = i;
    this.v2_i = v2_i;
    this.v2_s = v2_s;
    this.v2_f32 = v2_f32;
    this.v2_f64 = v2_f64;
    this.v2_rs = immutableCopyOf(v2_rs);
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.i = i;
    builder.v2_i = v2_i;
    builder.v2_s = v2_s;
    builder.v2_f32 = v2_f32;
    builder.v2_f64 = v2_f64;
    builder.v2_rs = copyOf(v2_rs);
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof VersionTwo)) return false;
    VersionTwo o = (VersionTwo) other;
    return equals(unknownFields(), o.unknownFields())
        && equals(i, o.i)
        && equals(v2_i, o.v2_i)
        && equals(v2_s, o.v2_s)
        && equals(v2_f32, o.v2_f32)
        && equals(v2_f64, o.v2_f64)
        && equals(v2_rs, o.v2_rs);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + (i != null ? i.hashCode() : 0);
      result = result * 37 + (v2_i != null ? v2_i.hashCode() : 0);
      result = result * 37 + (v2_s != null ? v2_s.hashCode() : 0);
      result = result * 37 + (v2_f32 != null ? v2_f32.hashCode() : 0);
      result = result * 37 + (v2_f64 != null ? v2_f64.hashCode() : 0);
      result = result * 37 + (v2_rs != null ? v2_rs.hashCode() : 1);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (i != null) builder.append(", i=").append(i);
    if (v2_i != null) builder.append(", v2_i=").append(v2_i);
    if (v2_s != null) builder.append(", v2_s=").append(v2_s);
    if (v2_f32 != null) builder.append(", v2_f32=").append(v2_f32);
    if (v2_f64 != null) builder.append(", v2_f64=").append(v2_f64);
    if (v2_rs != null) builder.append(", v2_rs=").append(v2_rs);
    return builder.replace(0, 2, "VersionTwo{").append('}').toString();
  }

  public static final class Builder extends com.squareup.wire.Message.Builder<VersionTwo, Builder> {
    public Integer i;

    public Integer v2_i;

    public String v2_s;

    public Integer v2_f32;

    public Long v2_f64;

    public List<String> v2_rs;

    public Builder() {
      v2_rs = newMutableList();
    }

    public Builder i(Integer i) {
      this.i = i;
      return this;
    }

    public Builder v2_i(Integer v2_i) {
      this.v2_i = v2_i;
      return this;
    }

    public Builder v2_s(String v2_s) {
      this.v2_s = v2_s;
      return this;
    }

    public Builder v2_f32(Integer v2_f32) {
      this.v2_f32 = v2_f32;
      return this;
    }

    public Builder v2_f64(Long v2_f64) {
      this.v2_f64 = v2_f64;
      return this;
    }

    public Builder v2_rs(List<String> v2_rs) {
      checkElementsNotNull(v2_rs);
      this.v2_rs = v2_rs;
      return this;
    }

    @Override
    public VersionTwo build() {
      return new VersionTwo(i, v2_i, v2_s, v2_f32, v2_f64, v2_rs, buildUnknownFields());
    }
  }
}
