/**
 * TaskStateExt.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package cz.it4i.fiji.haas_java_client.proxy;

public class TaskStateExt implements java.io.Serializable {
    private java.lang.String _value_;
    private static java.util.HashMap _table_ = new java.util.HashMap();

    // Constructor
    protected TaskStateExt(java.lang.String value) {
        _value_ = value;
        _table_.put(_value_,this);
    }

    public static final java.lang.String _Configuring = "Configuring";
    public static final java.lang.String _Submitted = "Submitted";
    public static final java.lang.String _Queued = "Queued";
    public static final java.lang.String _Running = "Running";
    public static final java.lang.String _Finished = "Finished";
    public static final java.lang.String _Failed = "Failed";
    public static final java.lang.String _Canceled = "Canceled";
    public static final TaskStateExt Configuring = new TaskStateExt(_Configuring);
    public static final TaskStateExt Submitted = new TaskStateExt(_Submitted);
    public static final TaskStateExt Queued = new TaskStateExt(_Queued);
    public static final TaskStateExt Running = new TaskStateExt(_Running);
    public static final TaskStateExt Finished = new TaskStateExt(_Finished);
    public static final TaskStateExt Failed = new TaskStateExt(_Failed);
    public static final TaskStateExt Canceled = new TaskStateExt(_Canceled);
    public java.lang.String getValue() { return _value_;}
    public static TaskStateExt fromValue(java.lang.String value)
          throws java.lang.IllegalArgumentException {
        TaskStateExt enumeration = (TaskStateExt)
            _table_.get(value);
        if (enumeration==null) throw new java.lang.IllegalArgumentException();
        return enumeration;
    }
    public static TaskStateExt fromString(java.lang.String value)
          throws java.lang.IllegalArgumentException {
        return fromValue(value);
    }
    public boolean equals(java.lang.Object obj) {return (obj == this);}
    public int hashCode() { return toString().hashCode();}
    public java.lang.String toString() { return _value_;}
    public java.lang.Object readResolve() throws java.io.ObjectStreamException { return fromValue(_value_);}
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new org.apache.axis.encoding.ser.EnumSerializer(
            _javaType, _xmlType);
    }
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new org.apache.axis.encoding.ser.EnumDeserializer(
            _javaType, _xmlType);
    }
    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(TaskStateExt.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "TaskStateExt"));
    }
    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

}
