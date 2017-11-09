/**
 * JobStateExt.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package cz.it4i.fiji.haas_java_client.proxy;

public class JobStateExt implements java.io.Serializable {
    private java.lang.String _value_;
    private static java.util.HashMap _table_ = new java.util.HashMap();

    // Constructor
    protected JobStateExt(java.lang.String value) {
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
    public static final JobStateExt Configuring = new JobStateExt(_Configuring);
    public static final JobStateExt Submitted = new JobStateExt(_Submitted);
    public static final JobStateExt Queued = new JobStateExt(_Queued);
    public static final JobStateExt Running = new JobStateExt(_Running);
    public static final JobStateExt Finished = new JobStateExt(_Finished);
    public static final JobStateExt Failed = new JobStateExt(_Failed);
    public static final JobStateExt Canceled = new JobStateExt(_Canceled);
    public java.lang.String getValue() { return _value_;}
    public static JobStateExt fromValue(java.lang.String value)
          throws java.lang.IllegalArgumentException {
        JobStateExt enumeration = (JobStateExt)
            _table_.get(value);
        if (enumeration==null) throw new java.lang.IllegalArgumentException();
        return enumeration;
    }
    public static JobStateExt fromString(java.lang.String value)
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
        new org.apache.axis.description.TypeDesc(JobStateExt.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "JobStateExt"));
    }
    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

}
