/**
 * TaskFileOffsetExt.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package cz.it4i.fiji.haas_java_client.proxy;

public class TaskFileOffsetExt  implements java.io.Serializable {
    private java.lang.Long submittedTaskInfoId;

    private cz.it4i.fiji.haas_java_client.proxy.SynchronizableFilesExt fileType;

    private java.lang.Long offset;

    public TaskFileOffsetExt() {
    }

    public TaskFileOffsetExt(
           java.lang.Long submittedTaskInfoId,
           cz.it4i.fiji.haas_java_client.proxy.SynchronizableFilesExt fileType,
           java.lang.Long offset) {
           this.submittedTaskInfoId = submittedTaskInfoId;
           this.fileType = fileType;
           this.offset = offset;
    }


    /**
     * Gets the submittedTaskInfoId value for this TaskFileOffsetExt.
     * 
     * @return submittedTaskInfoId
     */
    public java.lang.Long getSubmittedTaskInfoId() {
        return submittedTaskInfoId;
    }


    /**
     * Sets the submittedTaskInfoId value for this TaskFileOffsetExt.
     * 
     * @param submittedTaskInfoId
     */
    public void setSubmittedTaskInfoId(java.lang.Long submittedTaskInfoId) {
        this.submittedTaskInfoId = submittedTaskInfoId;
    }


    /**
     * Gets the fileType value for this TaskFileOffsetExt.
     * 
     * @return fileType
     */
    public cz.it4i.fiji.haas_java_client.proxy.SynchronizableFilesExt getFileType() {
        return fileType;
    }


    /**
     * Sets the fileType value for this TaskFileOffsetExt.
     * 
     * @param fileType
     */
    public void setFileType(cz.it4i.fiji.haas_java_client.proxy.SynchronizableFilesExt fileType) {
        this.fileType = fileType;
    }


    /**
     * Gets the offset value for this TaskFileOffsetExt.
     * 
     * @return offset
     */
    public java.lang.Long getOffset() {
        return offset;
    }


    /**
     * Sets the offset value for this TaskFileOffsetExt.
     * 
     * @param offset
     */
    public void setOffset(java.lang.Long offset) {
        this.offset = offset;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof TaskFileOffsetExt)) return false;
        TaskFileOffsetExt other = (TaskFileOffsetExt) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.submittedTaskInfoId==null && other.getSubmittedTaskInfoId()==null) || 
             (this.submittedTaskInfoId!=null &&
              this.submittedTaskInfoId.equals(other.getSubmittedTaskInfoId()))) &&
            ((this.fileType==null && other.getFileType()==null) || 
             (this.fileType!=null &&
              this.fileType.equals(other.getFileType()))) &&
            ((this.offset==null && other.getOffset()==null) || 
             (this.offset!=null &&
              this.offset.equals(other.getOffset())));
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
        if (getSubmittedTaskInfoId() != null) {
            _hashCode += getSubmittedTaskInfoId().hashCode();
        }
        if (getFileType() != null) {
            _hashCode += getFileType().hashCode();
        }
        if (getOffset() != null) {
            _hashCode += getOffset().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(TaskFileOffsetExt.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "TaskFileOffsetExt"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("submittedTaskInfoId");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "submittedTaskInfoId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "long"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("fileType");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "fileType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "SynchronizableFilesExt"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("offset");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "offset"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "long"));
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
