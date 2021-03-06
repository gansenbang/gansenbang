/**
 * Autogenerated by Thrift Compiler (0.8.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package io.s4.manager.thrift;

import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;

import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;

public class Machine implements org.apache.thrift.TBase<Machine, Machine._Fields>, java.io.Serializable, Cloneable {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("Machine");

  private static final org.apache.thrift.protocol.TField HOSTPORT_FIELD_DESC = new org.apache.thrift.protocol.TField("hostport", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField STATUS_FIELD_DESC = new org.apache.thrift.protocol.TField("status", org.apache.thrift.protocol.TType.STRING, (short)2);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new MachineStandardSchemeFactory());
    schemes.put(TupleScheme.class, new MachineTupleSchemeFactory());
  }

  public String hostport; // required
  public String status; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    HOSTPORT((short)1, "hostport"),
    STATUS((short)2, "status");

    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // HOSTPORT
          return HOSTPORT;
        case 2: // STATUS
          return STATUS;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final String _fieldName;

    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.HOSTPORT, new org.apache.thrift.meta_data.FieldMetaData("hostport", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.STATUS, new org.apache.thrift.meta_data.FieldMetaData("status", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(Machine.class, metaDataMap);
  }

  public Machine() {
  }

  public Machine(
    String hostport,
    String status)
  {
    this();
    this.hostport = hostport;
    this.status = status;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public Machine(Machine other) {
    if (other.isSetHostport()) {
      this.hostport = other.hostport;
    }
    if (other.isSetStatus()) {
      this.status = other.status;
    }
  }

  public Machine deepCopy() {
    return new Machine(this);
  }

  @Override
  public void clear() {
    this.hostport = null;
    this.status = null;
  }

  public String getHostport() {
    return this.hostport;
  }

  public Machine setHostport(String hostport) {
    this.hostport = hostport;
    return this;
  }

  public void unsetHostport() {
    this.hostport = null;
  }

  /** Returns true if field hostport is set (has been assigned a value) and false otherwise */
  public boolean isSetHostport() {
    return this.hostport != null;
  }

  public void setHostportIsSet(boolean value) {
    if (!value) {
      this.hostport = null;
    }
  }

  public String getStatus() {
    return this.status;
  }

  public Machine setStatus(String status) {
    this.status = status;
    return this;
  }

  public void unsetStatus() {
    this.status = null;
  }

  /** Returns true if field status is set (has been assigned a value) and false otherwise */
  public boolean isSetStatus() {
    return this.status != null;
  }

  public void setStatusIsSet(boolean value) {
    if (!value) {
      this.status = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case HOSTPORT:
      if (value == null) {
        unsetHostport();
      } else {
        setHostport((String)value);
      }
      break;

    case STATUS:
      if (value == null) {
        unsetStatus();
      } else {
        setStatus((String)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case HOSTPORT:
      return getHostport();

    case STATUS:
      return getStatus();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case HOSTPORT:
      return isSetHostport();
    case STATUS:
      return isSetStatus();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof Machine)
      return this.equals((Machine)that);
    return false;
  }

  public boolean equals(Machine that) {
    if (that == null)
      return false;

    boolean this_present_hostport = true && this.isSetHostport();
    boolean that_present_hostport = true && that.isSetHostport();
    if (this_present_hostport || that_present_hostport) {
      if (!(this_present_hostport && that_present_hostport))
        return false;
      if (!this.hostport.equals(that.hostport))
        return false;
    }

    boolean this_present_status = true && this.isSetStatus();
    boolean that_present_status = true && that.isSetStatus();
    if (this_present_status || that_present_status) {
      if (!(this_present_status && that_present_status))
        return false;
      if (!this.status.equals(that.status))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  public int compareTo(Machine other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;
    Machine typedOther = (Machine)other;

    lastComparison = Boolean.valueOf(isSetHostport()).compareTo(typedOther.isSetHostport());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetHostport()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.hostport, typedOther.hostport);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetStatus()).compareTo(typedOther.isSetStatus());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetStatus()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.status, typedOther.status);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("Machine(");
    boolean first = true;

    sb.append("hostport:");
    if (this.hostport == null) {
      sb.append("null");
    } else {
      sb.append(this.hostport);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("status:");
    if (this.status == null) {
      sb.append("null");
    } else {
      sb.append(this.status);
    }
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    try {
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class MachineStandardSchemeFactory implements SchemeFactory {
    public MachineStandardScheme getScheme() {
      return new MachineStandardScheme();
    }
  }

  private static class MachineStandardScheme extends StandardScheme<Machine> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, Machine struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // HOSTPORT
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.hostport = iprot.readString();
              struct.setHostportIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // STATUS
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.status = iprot.readString();
              struct.setStatusIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, Machine struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.hostport != null) {
        oprot.writeFieldBegin(HOSTPORT_FIELD_DESC);
        oprot.writeString(struct.hostport);
        oprot.writeFieldEnd();
      }
      if (struct.status != null) {
        oprot.writeFieldBegin(STATUS_FIELD_DESC);
        oprot.writeString(struct.status);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class MachineTupleSchemeFactory implements SchemeFactory {
    public MachineTupleScheme getScheme() {
      return new MachineTupleScheme();
    }
  }

  private static class MachineTupleScheme extends TupleScheme<Machine> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, Machine struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      BitSet optionals = new BitSet();
      if (struct.isSetHostport()) {
        optionals.set(0);
      }
      if (struct.isSetStatus()) {
        optionals.set(1);
      }
      oprot.writeBitSet(optionals, 2);
      if (struct.isSetHostport()) {
        oprot.writeString(struct.hostport);
      }
      if (struct.isSetStatus()) {
        oprot.writeString(struct.status);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, Machine struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      BitSet incoming = iprot.readBitSet(2);
      if (incoming.get(0)) {
        struct.hostport = iprot.readString();
        struct.setHostportIsSet(true);
      }
      if (incoming.get(1)) {
        struct.status = iprot.readString();
        struct.setStatusIsSet(true);
      }
    }
  }

}

