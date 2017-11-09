/**
 * AsymmetricKeyCredentialsExt.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package cz.it4i.fiji.haas_java_client.proxy;

public class AsymmetricKeyCredentialsExt  extends cz.it4i.fiji.haas_java_client.proxy.AuthenticationCredentialsExt  implements java.io.Serializable {
    private java.lang.String privateKey;

    private java.lang.String publicKey;

    public AsymmetricKeyCredentialsExt() {
    }

    public AsymmetricKeyCredentialsExt(
           java.lang.String username,
           java.lang.String privateKey,
           java.lang.String publicKey) {
        super(
            username);
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }


    /**
     * Gets the privateKey value for this AsymmetricKeyCredentialsExt.
     * 
     * @return privateKey
     */
    public java.lang.String getPrivateKey() {
        return privateKey;
    }


    /**
     * Sets the privateKey value for this AsymmetricKeyCredentialsExt.
     * 
     * @param privateKey
     */
    public void setPrivateKey(java.lang.String privateKey) {
        this.privateKey = privateKey;
    }


    /**
     * Gets the publicKey value for this AsymmetricKeyCredentialsExt.
     * 
     * @return publicKey
     */
    public java.lang.String getPublicKey() {
        return publicKey;
    }


    /**
     * Sets the publicKey value for this AsymmetricKeyCredentialsExt.
     * 
     * @param publicKey
     */
    public void setPublicKey(java.lang.String publicKey) {
        this.publicKey = publicKey;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof AsymmetricKeyCredentialsExt)) return false;
        AsymmetricKeyCredentialsExt other = (AsymmetricKeyCredentialsExt) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = super.equals(obj) && 
            ((this.privateKey==null && other.getPrivateKey()==null) || 
             (this.privateKey!=null &&
              this.privateKey.equals(other.getPrivateKey()))) &&
            ((this.publicKey==null && other.getPublicKey()==null) || 
             (this.publicKey!=null &&
              this.publicKey.equals(other.getPublicKey())));
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
        if (getPrivateKey() != null) {
            _hashCode += getPrivateKey().hashCode();
        }
        if (getPublicKey() != null) {
            _hashCode += getPublicKey().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(AsymmetricKeyCredentialsExt.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "AsymmetricKeyCredentialsExt"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("privateKey");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "privateKey"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("publicKey");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "publicKey"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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
