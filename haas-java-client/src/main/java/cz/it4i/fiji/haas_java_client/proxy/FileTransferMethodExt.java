/**
 * FileTransferMethodExt.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package cz.it4i.fiji.haas_java_client.proxy;

public class FileTransferMethodExt  implements java.io.Serializable {
    private java.lang.String serverHostname;

    private java.lang.String sharedBasepath;

    private cz.it4i.fiji.haas_java_client.proxy.FileTransferProtocolExt protocol;

    private cz.it4i.fiji.haas_java_client.proxy.AsymmetricKeyCredentialsExt credentials;

    public FileTransferMethodExt() {
    }

    public FileTransferMethodExt(
           java.lang.String serverHostname,
           java.lang.String sharedBasepath,
           cz.it4i.fiji.haas_java_client.proxy.FileTransferProtocolExt protocol,
           cz.it4i.fiji.haas_java_client.proxy.AsymmetricKeyCredentialsExt credentials) {
           this.serverHostname = serverHostname;
           this.sharedBasepath = sharedBasepath;
           this.protocol = protocol;
           this.credentials = credentials;
    }


    /**
     * Gets the serverHostname value for this FileTransferMethodExt.
     * 
     * @return serverHostname
     */
    public java.lang.String getServerHostname() {
        return serverHostname;
    }


    /**
     * Sets the serverHostname value for this FileTransferMethodExt.
     * 
     * @param serverHostname
     */
    public void setServerHostname(java.lang.String serverHostname) {
        this.serverHostname = serverHostname;
    }


    /**
     * Gets the sharedBasepath value for this FileTransferMethodExt.
     * 
     * @return sharedBasepath
     */
    public java.lang.String getSharedBasepath() {
        return sharedBasepath;
    }


    /**
     * Sets the sharedBasepath value for this FileTransferMethodExt.
     * 
     * @param sharedBasepath
     */
    public void setSharedBasepath(java.lang.String sharedBasepath) {
        this.sharedBasepath = sharedBasepath;
    }


    /**
     * Gets the protocol value for this FileTransferMethodExt.
     * 
     * @return protocol
     */
    public cz.it4i.fiji.haas_java_client.proxy.FileTransferProtocolExt getProtocol() {
        return protocol;
    }


    /**
     * Sets the protocol value for this FileTransferMethodExt.
     * 
     * @param protocol
     */
    public void setProtocol(cz.it4i.fiji.haas_java_client.proxy.FileTransferProtocolExt protocol) {
        this.protocol = protocol;
    }


    /**
     * Gets the credentials value for this FileTransferMethodExt.
     * 
     * @return credentials
     */
    public cz.it4i.fiji.haas_java_client.proxy.AsymmetricKeyCredentialsExt getCredentials() {
        return credentials;
    }


    /**
     * Sets the credentials value for this FileTransferMethodExt.
     * 
     * @param credentials
     */
    public void setCredentials(cz.it4i.fiji.haas_java_client.proxy.AsymmetricKeyCredentialsExt credentials) {
        this.credentials = credentials;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof FileTransferMethodExt)) return false;
        FileTransferMethodExt other = (FileTransferMethodExt) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.serverHostname==null && other.getServerHostname()==null) || 
             (this.serverHostname!=null &&
              this.serverHostname.equals(other.getServerHostname()))) &&
            ((this.sharedBasepath==null && other.getSharedBasepath()==null) || 
             (this.sharedBasepath!=null &&
              this.sharedBasepath.equals(other.getSharedBasepath()))) &&
            ((this.protocol==null && other.getProtocol()==null) || 
             (this.protocol!=null &&
              this.protocol.equals(other.getProtocol()))) &&
            ((this.credentials==null && other.getCredentials()==null) || 
             (this.credentials!=null &&
              this.credentials.equals(other.getCredentials())));
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
        if (getServerHostname() != null) {
            _hashCode += getServerHostname().hashCode();
        }
        if (getSharedBasepath() != null) {
            _hashCode += getSharedBasepath().hashCode();
        }
        if (getProtocol() != null) {
            _hashCode += getProtocol().hashCode();
        }
        if (getCredentials() != null) {
            _hashCode += getCredentials().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(FileTransferMethodExt.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "FileTransferMethodExt"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("serverHostname");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "serverHostname"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("sharedBasepath");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "sharedBasepath"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("protocol");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "protocol"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "FileTransferProtocolExt"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("credentials");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "credentials"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "AsymmetricKeyCredentialsExt"));
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
