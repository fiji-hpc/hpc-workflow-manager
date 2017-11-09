/**
 * DigitalSignatureCredentialsExt.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package cz.it4i.fiji.haas_java_client.proxy;

public class DigitalSignatureCredentialsExt  extends cz.it4i.fiji.haas_java_client.proxy.AuthenticationCredentialsExt  implements java.io.Serializable {
    private java.lang.String noise;

    private byte[] digitalSignature;

    public DigitalSignatureCredentialsExt() {
    }

    public DigitalSignatureCredentialsExt(
           java.lang.String username,
           java.lang.String noise,
           byte[] digitalSignature) {
        super(
            username);
        this.noise = noise;
        this.digitalSignature = digitalSignature;
    }


    /**
     * Gets the noise value for this DigitalSignatureCredentialsExt.
     * 
     * @return noise
     */
    public java.lang.String getNoise() {
        return noise;
    }


    /**
     * Sets the noise value for this DigitalSignatureCredentialsExt.
     * 
     * @param noise
     */
    public void setNoise(java.lang.String noise) {
        this.noise = noise;
    }


    /**
     * Gets the digitalSignature value for this DigitalSignatureCredentialsExt.
     * 
     * @return digitalSignature
     */
    public byte[] getDigitalSignature() {
        return digitalSignature;
    }


    /**
     * Sets the digitalSignature value for this DigitalSignatureCredentialsExt.
     * 
     * @param digitalSignature
     */
    public void setDigitalSignature(byte[] digitalSignature) {
        this.digitalSignature = digitalSignature;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof DigitalSignatureCredentialsExt)) return false;
        DigitalSignatureCredentialsExt other = (DigitalSignatureCredentialsExt) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = super.equals(obj) && 
            ((this.noise==null && other.getNoise()==null) || 
             (this.noise!=null &&
              this.noise.equals(other.getNoise()))) &&
            ((this.digitalSignature==null && other.getDigitalSignature()==null) || 
             (this.digitalSignature!=null &&
              java.util.Arrays.equals(this.digitalSignature, other.getDigitalSignature())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = super.hashCode();
        if (getNoise() != null) {
            _hashCode += getNoise().hashCode();
        }
        if (getDigitalSignature() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getDigitalSignature());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getDigitalSignature(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(DigitalSignatureCredentialsExt.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "DigitalSignatureCredentialsExt"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("noise");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "noise"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("digitalSignature");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "digitalSignature"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "byte"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "byte"));
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
