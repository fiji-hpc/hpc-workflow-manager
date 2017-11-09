/**
 * JobPriorityExt.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package cz.it4i.fiji.haas_java_client.proxy;

public class JobPriorityExt implements java.io.Serializable {
    private java.lang.String _value_;
    private static java.util.HashMap _table_ = new java.util.HashMap();

    // Constructor
    protected JobPriorityExt(java.lang.String value) {
        _value_ = value;
        _table_.put(_value_,this);
    }

    public static final java.lang.String _Lowest = "Lowest";
    public static final java.lang.String _VeryLow = "VeryLow";
    public static final java.lang.String _Low = "Low";
    public static final java.lang.String _BelowAverage = "BelowAverage";
    public static final java.lang.String _Average = "Average";
    public static final java.lang.String _AboveAverage = "AboveAverage";
    public static final java.lang.String _High = "High";
    public static final java.lang.String _VeryHigh = "VeryHigh";
    public static final java.lang.String _Critical = "Critical";
    public static final JobPriorityExt Lowest = new JobPriorityExt(_Lowest);
    public static final JobPriorityExt VeryLow = new JobPriorityExt(_VeryLow);
    public static final JobPriorityExt Low = new JobPriorityExt(_Low);
    public static final JobPriorityExt BelowAverage = new JobPriorityExt(_BelowAverage);
    public static final JobPriorityExt Average = new JobPriorityExt(_Average);
    public static final JobPriorityExt AboveAverage = new JobPriorityExt(_AboveAverage);
    public static final JobPriorityExt High = new JobPriorityExt(_High);
    public static final JobPriorityExt VeryHigh = new JobPriorityExt(_VeryHigh);
    public static final JobPriorityExt Critical = new JobPriorityExt(_Critical);
    public java.lang.String getValue() { return _value_;}
    public static JobPriorityExt fromValue(java.lang.String value)
          throws java.lang.IllegalArgumentException {
        JobPriorityExt enumeration = (JobPriorityExt)
            _table_.get(value);
        if (enumeration==null) throw new java.lang.IllegalArgumentException();
        return enumeration;
    }
    public static JobPriorityExt fromString(java.lang.String value)
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
        new org.apache.axis.description.TypeDesc(JobPriorityExt.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "JobPriorityExt"));
    }
    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

}
