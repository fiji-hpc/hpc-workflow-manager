/**
 * CommandTemplateParameterValueExt.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package cz.it4i.fiji.haas_java_client.proxy;

public class CommandTemplateParameterValueExt  implements java.io.Serializable {
    private java.lang.String commandParameterIdentifier;

    private java.lang.String parameterValue;

    public CommandTemplateParameterValueExt() {
    }

    public CommandTemplateParameterValueExt(
           java.lang.String commandParameterIdentifier,
           java.lang.String parameterValue) {
           this.commandParameterIdentifier = commandParameterIdentifier;
           this.parameterValue = parameterValue;
    }


    /**
     * Gets the commandParameterIdentifier value for this CommandTemplateParameterValueExt.
     * 
     * @return commandParameterIdentifier
     */
    public java.lang.String getCommandParameterIdentifier() {
        return commandParameterIdentifier;
    }


    /**
     * Sets the commandParameterIdentifier value for this CommandTemplateParameterValueExt.
     * 
     * @param commandParameterIdentifier
     */
    public void setCommandParameterIdentifier(java.lang.String commandParameterIdentifier) {
        this.commandParameterIdentifier = commandParameterIdentifier;
    }


    /**
     * Gets the parameterValue value for this CommandTemplateParameterValueExt.
     * 
     * @return parameterValue
     */
    public java.lang.String getParameterValue() {
        return parameterValue;
    }


    /**
     * Sets the parameterValue value for this CommandTemplateParameterValueExt.
     * 
     * @param parameterValue
     */
    public void setParameterValue(java.lang.String parameterValue) {
        this.parameterValue = parameterValue;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof CommandTemplateParameterValueExt)) return false;
        CommandTemplateParameterValueExt other = (CommandTemplateParameterValueExt) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.commandParameterIdentifier==null && other.getCommandParameterIdentifier()==null) || 
             (this.commandParameterIdentifier!=null &&
              this.commandParameterIdentifier.equals(other.getCommandParameterIdentifier()))) &&
            ((this.parameterValue==null && other.getParameterValue()==null) || 
             (this.parameterValue!=null &&
              this.parameterValue.equals(other.getParameterValue())));
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
        if (getCommandParameterIdentifier() != null) {
            _hashCode += getCommandParameterIdentifier().hashCode();
        }
        if (getParameterValue() != null) {
            _hashCode += getParameterValue().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(CommandTemplateParameterValueExt.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "CommandTemplateParameterValueExt"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("commandParameterIdentifier");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "commandParameterIdentifier"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("parameterValue");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "parameterValue"));
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
