/**
 * ResourceUsageExt.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package cz.it4i.fiji.haas_java_client.proxy;

public class ResourceUsageExt  implements java.io.Serializable {
    private cz.it4i.fiji.haas_java_client.proxy.ClusterNodeTypeExt nodeType;

    private java.lang.Integer coresUsed;

    private cz.it4i.fiji.haas_java_client.proxy.ResourceLimitationExt limitation;

    public ResourceUsageExt() {
    }

    public ResourceUsageExt(
           cz.it4i.fiji.haas_java_client.proxy.ClusterNodeTypeExt nodeType,
           java.lang.Integer coresUsed,
           cz.it4i.fiji.haas_java_client.proxy.ResourceLimitationExt limitation) {
           this.nodeType = nodeType;
           this.coresUsed = coresUsed;
           this.limitation = limitation;
    }


    /**
     * Gets the nodeType value for this ResourceUsageExt.
     * 
     * @return nodeType
     */
    public cz.it4i.fiji.haas_java_client.proxy.ClusterNodeTypeExt getNodeType() {
        return nodeType;
    }


    /**
     * Sets the nodeType value for this ResourceUsageExt.
     * 
     * @param nodeType
     */
    public void setNodeType(cz.it4i.fiji.haas_java_client.proxy.ClusterNodeTypeExt nodeType) {
        this.nodeType = nodeType;
    }


    /**
     * Gets the coresUsed value for this ResourceUsageExt.
     * 
     * @return coresUsed
     */
    public java.lang.Integer getCoresUsed() {
        return coresUsed;
    }


    /**
     * Sets the coresUsed value for this ResourceUsageExt.
     * 
     * @param coresUsed
     */
    public void setCoresUsed(java.lang.Integer coresUsed) {
        this.coresUsed = coresUsed;
    }


    /**
     * Gets the limitation value for this ResourceUsageExt.
     * 
     * @return limitation
     */
    public cz.it4i.fiji.haas_java_client.proxy.ResourceLimitationExt getLimitation() {
        return limitation;
    }


    /**
     * Sets the limitation value for this ResourceUsageExt.
     * 
     * @param limitation
     */
    public void setLimitation(cz.it4i.fiji.haas_java_client.proxy.ResourceLimitationExt limitation) {
        this.limitation = limitation;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ResourceUsageExt)) return false;
        ResourceUsageExt other = (ResourceUsageExt) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.nodeType==null && other.getNodeType()==null) || 
             (this.nodeType!=null &&
              this.nodeType.equals(other.getNodeType()))) &&
            ((this.coresUsed==null && other.getCoresUsed()==null) || 
             (this.coresUsed!=null &&
              this.coresUsed.equals(other.getCoresUsed()))) &&
            ((this.limitation==null && other.getLimitation()==null) || 
             (this.limitation!=null &&
              this.limitation.equals(other.getLimitation())));
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
        if (getNodeType() != null) {
            _hashCode += getNodeType().hashCode();
        }
        if (getCoresUsed() != null) {
            _hashCode += getCoresUsed().hashCode();
        }
        if (getLimitation() != null) {
            _hashCode += getLimitation().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ResourceUsageExt.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "ResourceUsageExt"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("nodeType");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "nodeType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "ClusterNodeTypeExt"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("coresUsed");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "coresUsed"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("limitation");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "limitation"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "ResourceLimitationExt"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
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
