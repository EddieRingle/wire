/*
 * Copyright (C) 2015 Square, Inc.
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
package com.squareup.wire.schema;

import com.google.common.collect.ImmutableList;
import com.squareup.wire.schema.internal.parser.MessageElement;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;

import static com.google.common.base.Preconditions.checkNotNull;

public final class MessageType extends Type {
  private final ProtoType protoType;
  private final MessageElement element;
  private final ImmutableList<Field> declaredFields;
  private final List<Field> extensionFields;
  private final ImmutableList<OneOf> oneOfs;
  private final ImmutableList<Type> nestedTypes;
  private final ImmutableList<Extensions> extensionsList;
  private final Options options;

  MessageType(ProtoType protoType, MessageElement element, ImmutableList<Field> declaredFields,
      List<Field> extensionFields, ImmutableList<OneOf> oneOfs, ImmutableList<Type> nestedTypes,
      ImmutableList<Extensions> extensionsList, Options options) {
    this.protoType = protoType;
    this.element = element;
    this.declaredFields = declaredFields;
    this.extensionFields = extensionFields;
    this.oneOfs = oneOfs;
    this.nestedTypes = nestedTypes;
    this.extensionsList = extensionsList;
    this.options = checkNotNull(options);
  }

  @Override public Location location() {
    return element.location();
  }

  @Override public ProtoType name() {
    return protoType;
  }

  @Override public String documentation() {
    return element.documentation();
  }

  @Override public ImmutableList<Type> nestedTypes() {
    return nestedTypes;
  }

  @Override public Options options() {
    return options;
  }

  public ImmutableList<Field> fields() {
    return ImmutableList.<Field>builder()
        .addAll(declaredFields)
        .addAll(extensionFields)
        .build();
  }

  public ImmutableList<Field> extensionFields() {
    return ImmutableList.copyOf(extensionFields);
  }

  public ImmutableList<Field> getRequiredFields() {
    ImmutableList.Builder<Field> required = ImmutableList.builder();
    for (Field field : fieldsAndOneOfFields()) {
      if (field.isRequired()) {
        required.add(field);
      }
    }
    return required.build();
  }

  public ImmutableList<Field> fieldsAndOneOfFields() {
    ImmutableList.Builder<Field> result = ImmutableList.builder();
    result.addAll(declaredFields);
    result.addAll(extensionFields);
    for (OneOf oneOf : oneOfs) {
      result.addAll(oneOf.fields());
    }
    return result.build();
  }

  /** Returns the field named {@code name}, or null if this type has no such field. */
  public Field field(String name) {
    for (Field field : declaredFields) {
      if (field.name().equals(name)) {
        return field;
      }
    }
    return null;
  }

  /**
   * Returns the field with the qualified name {@code qualifiedName}, or null if this type has no
   * such field.
   */
  public Field extensionField(String qualifiedName) {
    for (Field field : extensionFields) {
      if (field.qualifiedName().equals(qualifiedName)) {
        return field;
      }
    }
    return null;
  }

  /** Returns the field tagged {@code tag}, or null if this type has no such field. */
  public Field field(int tag) {
    for (Field field : declaredFields) {
      if (field.tag() == tag) {
        return field;
      }
    }
    for (Field field : extensionFields) {
      if (field.tag() == tag) {
        return field;
      }
    }
    return null;
  }

  public ImmutableList<OneOf> oneOfs() {
    return oneOfs;
  }

  public ImmutableList<Extensions> extensions() {
    return extensionsList;
  }

  Map<String, Field> extensionFieldsMap() {
    // TODO(jwilson): simplify this to just resolve field values directly.
    Map<String, Field> extensionsForType = new LinkedHashMap<>();
    for (Field field : extensionFields) {
      extensionsForType.put(field.qualifiedName(), field);
    }
    return extensionsForType;
  }

  void validate(Linker linker) {
    linker = linker.withContext(this);
    linker.validateFields(fieldsAndOneOfFields());
    linker.validateEnumConstantNameUniqueness(nestedTypes);
    for (Field field : fieldsAndOneOfFields()) {
      field.validate(linker);
    }
    for (Type type : nestedTypes) {
      type.validate(linker);
    }
    for (Extensions extensions : extensionsList) {
      extensions.validate(linker);
    }
  }

  void link(Linker linker) {
    linker = linker.withContext(this);
    for (Field field : declaredFields) {
      field.link(linker);
    }
    for (Field field : extensionFields) {
      field.link(linker);
    }
    for (OneOf oneOf : oneOfs) {
      oneOf.link(linker);
    }
    for (Type type : nestedTypes) {
      type.link(linker);
    }
  }

  void linkOptions(Linker linker) {
    linker = linker.withContext(this);
    for (Type type : nestedTypes) {
      type.linkOptions(linker);
    }
    for (Field field : declaredFields) {
      field.linkOptions(linker);
    }
    for (Field field : extensionFields) {
      field.linkOptions(linker);
    }
    for (OneOf oneOf : oneOfs) {
      oneOf.linkOptions(linker);
    }
    options.link(linker);
  }

  @Override Type retainAll(NavigableSet<String> identifiers) {
    ImmutableList.Builder<Type> retainedNestedTypesBuilder = ImmutableList.builder();
    for (Type nestedType : nestedTypes) {
      Type retainedNestedType = nestedType.retainAll(identifiers);
      if (retainedNestedType != null) {
        retainedNestedTypesBuilder.add(retainedNestedType);
      }
    }

    String typeName = protoType.toString();

    // If this type is not retained, and none of its nested types are retained, prune it.
    ImmutableList<Type> retainedNestedTypes = retainedNestedTypesBuilder.build();
    if (!identifiers.contains(typeName) && retainedNestedTypes.isEmpty()) {
      return null;
    }

    // If any of our fields are specifically retained, retain only that set.
    ImmutableList<Field> retainedFields = declaredFields;
    ImmutableList<Field> retainedExtensionFields = ImmutableList.copyOf(extensionFields);
    if (Pruner.hasMarkedMember(identifiers, protoType)) {
      retainedFields = retainFields(identifiers, declaredFields);
      retainedExtensionFields = retainFields(identifiers, extensionFields);
    }

    return new MessageType(protoType, element, retainedFields, retainedExtensionFields, oneOfs,
        retainedNestedTypes, extensionsList, options);
  }

  private ImmutableList<Field> retainFields(
      NavigableSet<String> identifiers, Collection<Field> fields) {
    ImmutableList.Builder<Field> retainedFieldsBuilder = ImmutableList.builder();
    for (Field field : fields) {
      if (identifiers.contains(protoType + "#" + field.name())) {
        retainedFieldsBuilder.add(field);
      }
    }
    return retainedFieldsBuilder.build();
  }

  void addExtensionFields(ImmutableList<Field> fields) {
    extensionFields.addAll(fields);
  }
}
