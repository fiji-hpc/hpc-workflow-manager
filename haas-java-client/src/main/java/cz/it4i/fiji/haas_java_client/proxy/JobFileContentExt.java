/**
 * JobFileContentExt.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package cz.it4i.fiji.haas_java_client.proxy;

public class JobFileContentExt  implements java.io.Serializable {
    private java.lang.String content;

    private java.lang.String relativePath;

    private java.lang.Long offset;

    private cz.it4i.fiji.haas_java_client.proxy.SynchronizableFilesExt fileType;

    private java.lang.Long submittedTaskInfoId;

    public JobFileContentExt() {
    }

    public JobFileContentExt(
           java.lang.String content,
           java.lang.String relativePath,
           java.lang.Long offset,
           cz.it4i.fiji.haas_java_client.proxy.SynchronizableFilesExt fileType,
           java.lang.Long submittedTaskInfoId) {
           this.content = content;
           this.relativePath = relativePath;
           this.offset = offset;
           this.fileType = fileType;
           this.submittedTaskInfoId = submittedTaskInfoId;
    }


    /* (non-Javadoc)
	 * @see cz.it4i.fiji.haas_java_client.proxy.Aaa#getContent()
	 */
    public java.lang.String getContent() {
        return content;
    }


    /**
     * Sets the content value for this JobFileContentExt.
     * 
     * @param content
     */
    public void setContent(java.lang.String content) {
        this.content = content;
    }


    /* (non-Javadoc)
	 * @see cz.it4i.fiji.haas_java_client.proxy.Aaa#getRelativePath()
	 */
    public java.lang.String getRelativePath() {
        return relativePath;
    }


    /**
     * Sets the relativePath value for this JobFileContentExt.
     * 
     * @param relativePath
     */
    public void setRelativePath(java.lang.String relativePath) {
        this.relativePath = relativePath;
    }


    /* (non-Javadoc)
	 * @see cz.it4i.fiji.haas_java_client.proxy.Aaa#getOffset()
	 */
    public java.lang.Long getOffset() {
        return offset;
    }


    /**
     * Sets the offset value for this JobFileContentExt.
     * 
     * @param offset
     */
    public void setOffset(java.lang.Long offset) {
        this.offset = offset;
    }


    /* (non-Javadoc)
	 * @see cz.it4i.fiji.haas_java_client.proxy.Aaa#getFileType()
	 */
    public cz.it4i.fiji.haas_java_client.proxy.SynchronizableFilesExt getFileType() {
        return fileType;
    }


    /**
     * Sets the fileType value for this JobFileContentExt.
     * 
     * @param fileType
     */
    public void setFileType(cz.it4i.fiji.haas_java_client.proxy.SynchronizableFilesExt fileType) {
        this.fileType = fileType;
    }


    /* (non-Javadoc)
	 * @see cz.it4i.fiji.haas_java_client.proxy.Aaa#getSubmittedTaskInfoId()
	 */
    public java.lang.Long getSubmittedTaskInfoId() {
        return submittedTaskInfoId;
    }


    /**
     * Sets the submittedTaskInfoId value for this JobFileContentExt.
     * 
     * @param submittedTaskInfoId
     */
    public void setSubmittedTaskInfoId(java.lang.Long submittedTaskInfoId) {
        this.submittedTaskInfoId = submittedTaskInfoId;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof JobFileContentExt)) return false;
        JobFileContentExt other = (JobFileContentExt) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.content==null && other.getContent()==null) || 
             (this.content!=null &&
              this.content.equals(other.getContent()))) &&
            ((this.relativePath==null && other.getRelativePath()==null) || 
             (this.relativePath!=null &&
              this.relativePath.equals(other.getRelativePath()))) &&
            ((this.offset==null && other.getOffset()==null) || 
             (this.offset!=null &&
              this.offset.equals(other.getOffset()))) &&
            ((this.fileType==null && other.getFileType()==null) || 
             (this.fileType!=null &&
              this.fileType.equals(other.getFileType()))) &&
            ((this.submittedTaskInfoId==null && other.getSubmittedTaskInfoId()==null) || 
             (this.submittedTaskInfoId!=null &&
              this.submittedTaskInfoId.equals(other.getSubmittedTaskInfoId())));
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
        if (getContent() != null) {
            _hashCode += getContent().hashCode();
        }
        if (getRelativePath() != null) {
            _hashCode += getRelativePath().hashCode();
        }
        if (getOffset() != null) {
            _hashCode += getOffset().hashCode();
        }
        if (getFileType() != null) {
            _hashCode += getFileType().hashCode();
        }
        if (getSubmittedTaskInfoId() != null) {
            _hashCode += getSubmittedTaskInfoId().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(JobFileContentExt.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "JobFileContentExt"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("content");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "content"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("relativePath");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "relativePath"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("offset");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "offset"));
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
        elemField.setFieldName("submittedTaskInfoId");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "submittedTaskInfoId"));
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
