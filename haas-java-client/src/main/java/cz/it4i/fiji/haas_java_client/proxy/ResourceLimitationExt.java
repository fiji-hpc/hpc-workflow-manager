/**
 * ResourceLimitationExt.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package cz.it4i.fiji.haas_java_client.proxy;

public class ResourceLimitationExt  implements java.io.Serializable {
    private java.lang.Integer totalMaxCores;

    private java.lang.Integer maxCoresPerJob;

    public ResourceLimitationExt() {
    }

    public ResourceLimitationExt(
           java.lang.Integer totalMaxCores,
           java.lang.Integer maxCoresPerJob) {
           this.totalMaxCores = totalMaxCores;
           this.maxCoresPerJob = maxCoresPerJob;
    }


    /**
     * Gets the totalMaxCores value for this ResourceLimitationExt.
     * 
     * @return totalMaxCores
     */
    public java.lang.Integer getTotalMaxCores() {
        return totalMaxCores;
    }


    /**
     * Sets the totalMaxCores value for this ResourceLimitationExt.
     * 
     * @param totalMaxCores
     */
    public void setTotalMaxCores(java.lang.Integer totalMaxCores) {
        this.totalMaxCores = totalMaxCores;
    }


    /**
     * Gets the maxCoresPerJob value for this ResourceLimitationExt.
     * 
     * @return maxCoresPerJob
     */
    public java.lang.Integer getMaxCoresPerJob() {
        return maxCoresPerJob;
    }


    /**
     * Sets the maxCoresPerJob value for this ResourceLimitationExt.
     * 
     * @param maxCoresPerJob
     */
    public void setMaxCoresPerJob(java.lang.Integer maxCoresPerJob) {
        this.maxCoresPerJob = maxCoresPerJob;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ResourceLimitationExt)) return false;
        ResourceLimitationExt other = (ResourceLimitationExt) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.totalMaxCores==null && other.getTotalMaxCores()==null) || 
             (this.totalMaxCores!=null &&
              this.totalMaxCores.equals(other.getTotalMaxCores()))) &&
            ((this.maxCoresPerJob==null && other.getMaxCoresPerJob()==null) || 
             (this.maxCoresPerJob!=null &&
              this.maxCoresPerJob.equals(other.getMaxCoresPerJob())));
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
        if (getTotalMaxCores() != null) {
            _hashCode += getTotalMaxCores().hashCode();
        }
        if (getMaxCoresPerJob() != null) {
            _hashCode += getMaxCoresPerJob().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ResourceLimitationExt.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "ResourceLimitationExt"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("totalMaxCores");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "totalMaxCores"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("maxCoresPerJob");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "maxCoresPerJob"));
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
