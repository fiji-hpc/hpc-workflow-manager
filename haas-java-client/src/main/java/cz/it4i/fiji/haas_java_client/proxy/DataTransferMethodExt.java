/**
 * DataTransferMethodExt.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package cz.it4i.fiji.haas_java_client.proxy;

public class DataTransferMethodExt  implements java.io.Serializable {
    private java.lang.Long submittedJobId;

    private byte[] ipAddress;

    private java.lang.Integer port;

    public DataTransferMethodExt() {
    }

    public DataTransferMethodExt(
           java.lang.Long submittedJobId,
           byte[] ipAddress,
           java.lang.Integer port) {
           this.submittedJobId = submittedJobId;
           this.ipAddress = ipAddress;
           this.port = port;
    }


    /**
     * Gets the submittedJobId value for this DataTransferMethodExt.
     * 
     * @return submittedJobId
     */
    public java.lang.Long getSubmittedJobId() {
        return submittedJobId;
    }


    /**
     * Sets the submittedJobId value for this DataTransferMethodExt.
     * 
     * @param submittedJobId
     */
    public void setSubmittedJobId(java.lang.Long submittedJobId) {
        this.submittedJobId = submittedJobId;
    }


    /**
     * Gets the ipAddress value for this DataTransferMethodExt.
     * 
     * @return ipAddress
     */
    public byte[] getIpAddress() {
        return ipAddress;
    }


    /**
     * Sets the ipAddress value for this DataTransferMethodExt.
     * 
     * @param ipAddress
     */
    public void setIpAddress(byte[] ipAddress) {
        this.ipAddress = ipAddress;
    }


    /**
     * Gets the port value for this DataTransferMethodExt.
     * 
     * @return port
     */
    public java.lang.Integer getPort() {
        return port;
    }


    /**
     * Sets the port value for this DataTransferMethodExt.
     * 
     * @param port
     */
    public void setPort(java.lang.Integer port) {
        this.port = port;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof DataTransferMethodExt)) return false;
        DataTransferMethodExt other = (DataTransferMethodExt) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.submittedJobId==null && other.getSubmittedJobId()==null) || 
             (this.submittedJobId!=null &&
              this.submittedJobId.equals(other.getSubmittedJobId()))) &&
            ((this.ipAddress==null && other.getIpAddress()==null) || 
             (this.ipAddress!=null &&
              java.util.Arrays.equals(this.ipAddress, other.getIpAddress()))) &&
            ((this.port==null && other.getPort()==null) || 
             (this.port!=null &&
              this.port.equals(other.getPort())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getSubmittedJobId() != null) {
            _hashCode += getSubmittedJobId().hashCode();
        }
        if (getIpAddress() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getIpAddress());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getIpAddress(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getPort() != null) {
            _hashCode += getPort().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(DataTransferMethodExt.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "DataTransferMethodExt"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("submittedJobId");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "submittedJobId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "long"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("ipAddress");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "ipAddress"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "byte"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "byte"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("port");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "port"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
