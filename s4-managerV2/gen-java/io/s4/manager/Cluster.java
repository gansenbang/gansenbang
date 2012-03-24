/**
 * Autogenerated by Thrift Compiler (0.8.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package io.s4.manager;

import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;

import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Cluster implements org.apache.thrift.TBase<Cluster, Cluster._Fields>, java.io.Serializable, Cloneable {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("Cluster");

  private static final org.apache.thrift.protocol.TField CLUSTERNAME_FIELD_DESC = new org.apache.thrift.protocol.TField("clustername", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField ZK_ADDRESS_FIELD_DESC = new org.apache.thrift.protocol.TField("zkAddress", org.apache.thrift.protocol.TType.STRING, (short)2);
  private static final org.apache.thrift.protocol.TField NUMBER_FIELD_DESC = new org.apache.thrift.protocol.TField("number", org.apache.thrift.protocol.TType.I32, (short)3);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new ClusterStandardSchemeFactory());
    schemes.put(TupleScheme.class, new ClusterTupleSchemeFactory());
  }

  public String clustername; // required
  public String zkAddress; // required
  public int number; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    CLUSTERNAME((short)1, "clustername"),
    ZK_ADDRESS((short)2, "zkAddress"),
    NUMBER((short)3, "number");

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
        case 1: // CLUSTERNAME
          return CLUSTERNAME;
        case 2: // ZK_ADDRESS
          return ZK_ADDRESS;
        case 3: // NUMBER
          return NUMBER;
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
  private static final int __NUMBER_ISSET_ID = 0;
  private BitSet __isset_bit_vector = new BitSet(1);
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.CLUSTERNAME, new org.apache.thrift.meta_data.FieldMetaData("clustername", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.ZK_ADDRESS, new org.apache.thrift.meta_data.FieldMetaData("zkAddress", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.NUMBER, new org.apache.thrift.meta_data.FieldMetaData("number", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(Cluster.class, metaDataMap);
  }

  public Cluster() {
  }

  public Cluster(
    String clustername,
    String zkAddress,
    int number)
  {
    this();
    this.clustername = clustername;
    this.zkAddress = zkAddress;
    this.number = number;
    setNumberIsSet(true);
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public Cluster(Cluster other) {
    __isset_bit_vector.clear();
    __isset_bit_vector.or(other.__isset_bit_vector);
    if (other.isSetClustername()) {
      this.clustername = other.clustername;
    }
    if (other.isSetZkAddress()) {
      this.zkAddress = other.zkAddress;
    }
    this.number = other.number;
  }

  public Cluster deepCopy() {
    return new Cluster(this);
  }

  @Override
  public void clear() {
    this.clustername = null;
    this.zkAddress = null;
    setNumberIsSet(false);
    this.number = 0;
  }

  public String getClustername() {
    return this.clustername;
  }

  public Cluster setClustername(String clustername) {
    this.clustername = clustername;
    return this;
  }

  public void unsetClustername() {
    this.clustername = null;
  }

  /** Returns true if field clustername is set (has been assigned a value) and false otherwise */
  public boolean isSetClustername() {
    return this.clustername != null;
  }

  public void setClusternameIsSet(boolean value) {
    if (!value) {
      this.clustername = null;
    }
  }

  public String getZkAddress() {
    return this.zkAddress;
  }

  public Cluster setZkAddress(String zkAddress) {
    this.zkAddress = zkAddress;
    return this;
  }

  public void unsetZkAddress() {
    this.zkAddress = null;
  }

  /** Returns true if field zkAddress is set (has been assigned a value) and false otherwise */
  public boolean isSetZkAddress() {
    return this.zkAddress != null;
  }

  public void setZkAddressIsSet(boolean value) {
    if (!value) {
      this.zkAddress = null;
    }
  }

  public int getNumber() {
    return this.number;
  }

  public Cluster setNumber(int number) {
    this.number = number;
    setNumberIsSet(true);
    return this;
  }

  public void unsetNumber() {
    __isset_bit_vector.clear(__NUMBER_ISSET_ID);
  }

  /** Returns true if field number is set (has been assigned a value) and false otherwise */
  public boolean isSetNumber() {
    return __isset_bit_vector.get(__NUMBER_ISSET_ID);
  }

  public void setNumberIsSet(boolean value) {
    __isset_bit_vector.set(__NUMBER_ISSET_ID, value);
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case CLUSTERNAME:
      if (value == null) {
        unsetClustername();
      } else {
        setClustername((String)value);
      }
      break;

    case ZK_ADDRESS:
      if (value == null) {
        unsetZkAddress();
      } else {
        setZkAddress((String)value);
      }
      break;

    case NUMBER:
      if (value == null) {
        unsetNumber();
      } else {
        setNumber((Integer)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case CLUSTERNAME:
      return getClustername();

    case ZK_ADDRESS:
      return getZkAddress();

    case NUMBER:
      return Integer.valueOf(getNumber());

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case CLUSTERNAME:
      return isSetClustername();
    case ZK_ADDRESS:
      return isSetZkAddress();
    case NUMBER:
      return isSetNumber();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof Cluster)
      return this.equals((Cluster)that);
    return false;
  }

  public boolean equals(Cluster that) {
    if (that == null)
      return false;

    boolean this_present_clustername = true && this.isSetClustername();
    boolean that_present_clustername = true && that.isSetClustername();
    if (this_present_clustername || that_present_clustername) {
      if (!(this_present_clustername && that_present_clustername))
        return false;
      if (!this.clustername.equals(that.clustername))
        return false;
    }

    boolean this_present_zkAddress = true && this.isSetZkAddress();
    boolean that_present_zkAddress = true && that.isSetZkAddress();
    if (this_present_zkAddress || that_present_zkAddress) {
      if (!(this_present_zkAddress && that_present_zkAddress))
        return false;
      if (!this.zkAddress.equals(that.zkAddress))
        return false;
    }

    boolean this_present_number = true;
    boolean that_present_number = true;
    if (this_present_number || that_present_number) {
      if (!(this_present_number && that_present_number))
        return false;
      if (this.number != that.number)
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  public int compareTo(Cluster other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;
    Cluster typedOther = (Cluster)other;

    lastComparison = Boolean.valueOf(isSetClustername()).compareTo(typedOther.isSetClustername());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetClustername()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.clustername, typedOther.clustername);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetZkAddress()).compareTo(typedOther.isSetZkAddress());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetZkAddress()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.zkAddress, typedOther.zkAddress);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetNumber()).compareTo(typedOther.isSetNumber());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetNumber()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.number, typedOther.number);
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
    StringBuilder sb = new StringBuilder("Cluster(");
    boolean first = true;

    sb.append("clustername:");
    if (this.clustername == null) {
      sb.append("null");
    } else {
      sb.append(this.clustername);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("zkAddress:");
    if (this.zkAddress == null) {
      sb.append("null");
    } else {
      sb.append(this.zkAddress);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("number:");
    sb.append(this.number);
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
      // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
      __isset_bit_vector = new BitSet(1);
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class ClusterStandardSchemeFactory implements SchemeFactory {
    public ClusterStandardScheme getScheme() {
      return new ClusterStandardScheme();
    }
  }

  private static class ClusterStandardScheme extends StandardScheme<Cluster> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, Cluster struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // CLUSTERNAME
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.clustername = iprot.readString();
              struct.setClusternameIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // ZK_ADDRESS
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.zkAddress = iprot.readString();
              struct.setZkAddressIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // NUMBER
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.number = iprot.readI32();
              struct.setNumberIsSet(true);
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

    public void write(org.apache.thrift.protocol.TProtocol oprot, Cluster struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.clustername != null) {
        oprot.writeFieldBegin(CLUSTERNAME_FIELD_DESC);
        oprot.writeString(struct.clustername);
        oprot.writeFieldEnd();
      }
      if (struct.zkAddress != null) {
        oprot.writeFieldBegin(ZK_ADDRESS_FIELD_DESC);
        oprot.writeString(struct.zkAddress);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldBegin(NUMBER_FIELD_DESC);
      oprot.writeI32(struct.number);
      oprot.writeFieldEnd();
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class ClusterTupleSchemeFactory implements SchemeFactory {
    public ClusterTupleScheme getScheme() {
      return new ClusterTupleScheme();
    }
  }

  private static class ClusterTupleScheme extends TupleScheme<Cluster> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, Cluster struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      BitSet optionals = new BitSet();
      if (struct.isSetClustername()) {
        optionals.set(0);
      }
      if (struct.isSetZkAddress()) {
        optionals.set(1);
      }
      if (struct.isSetNumber()) {
        optionals.set(2);
      }
      oprot.writeBitSet(optionals, 3);
      if (struct.isSetClustername()) {
        oprot.writeString(struct.clustername);
      }
      if (struct.isSetZkAddress()) {
        oprot.writeString(struct.zkAddress);
      }
      if (struct.isSetNumber()) {
        oprot.writeI32(struct.number);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, Cluster struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      BitSet incoming = iprot.readBitSet(3);
      if (incoming.get(0)) {
        struct.clustername = iprot.readString();
        struct.setClusternameIsSet(true);
      }
      if (incoming.get(1)) {
        struct.zkAddress = iprot.readString();
        struct.setZkAddressIsSet(true);
      }
      if (incoming.get(2)) {
        struct.number = iprot.readI32();
        struct.setNumberIsSet(true);
      }
    }
  }

}
