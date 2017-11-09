/**
 * SubmittedTaskInfoExt.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package cz.it4i.fiji.haas_java_client.proxy;

public class SubmittedTaskInfoExt  implements java.io.Serializable {
    private java.lang.Long id;

    private java.lang.String name;

    private cz.it4i.fiji.haas_java_client.proxy.TaskStateExt state;

    private java.lang.Double allocatedTime;

    private java.lang.String allocatedCoreIds;

    private java.util.Calendar startTime;

    private java.util.Calendar endTime;

    private java.lang.String errorMessage;

    private java.lang.String allParameters;

    public SubmittedTaskInfoExt() {
    }

    public SubmittedTaskInfoExt(
           java.lang.Long id,
           java.lang.String name,
           cz.it4i.fiji.haas_java_client.proxy.TaskStateExt state,
           java.lang.Double allocatedTime,
           java.lang.String allocatedCoreIds,
           java.util.Calendar startTime,
           java.util.Calendar endTime,
           java.lang.String errorMessage,
           java.lang.String allParameters) {
           this.id = id;
           this.name = name;
           this.state = state;
           this.allocatedTime = allocatedTime;
           this.allocatedCoreIds = allocatedCoreIds;
           this.startTime = startTime;
           this.endTime = endTime;
           this.errorMessage = errorMessage;
           this.allParameters = allParameters;
    }


    /**
     * Gets the id value for this SubmittedTaskInfoExt.
     * 
     * @return id
     */
    public java.lang.Long getId() {
        return id;
    }


    /**
     * Sets the id value for this SubmittedTaskInfoExt.
     * 
     * @param id
     */
    public void setId(java.lang.Long id) {
        this.id = id;
    }


    /**
     * Gets the name value for this SubmittedTaskInfoExt.
     * 
     * @return name
     */
    public java.lang.String getName() {
        return name;
    }


    /**
     * Sets the name value for this SubmittedTaskInfoExt.
     * 
     * @param name
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }


    /**
     * Gets the state value for this SubmittedTaskInfoExt.
     * 
     * @return state
     */
    public cz.it4i.fiji.haas_java_client.proxy.TaskStateExt getState() {
        return state;
    }


    /**
     * Sets the state value for this SubmittedTaskInfoExt.
     * 
     * @param state
     */
    public void setState(cz.it4i.fiji.haas_java_client.proxy.TaskStateExt state) {
        this.state = state;
    }


    /**
     * Gets the allocatedTime value for this SubmittedTaskInfoExt.
     * 
     * @return allocatedTime
     */
    public java.lang.Double getAllocatedTime() {
        return allocatedTime;
    }


    /**
     * Sets the allocatedTime value for this SubmittedTaskInfoExt.
     * 
     * @param allocatedTime
     */
    public void setAllocatedTime(java.lang.Double allocatedTime) {
        this.allocatedTime = allocatedTime;
    }


    /**
     * Gets the allocatedCoreIds value for this SubmittedTaskInfoExt.
     * 
     * @return allocatedCoreIds
     */
    public java.lang.String getAllocatedCoreIds() {
        return allocatedCoreIds;
    }


    /**
     * Sets the allocatedCoreIds value for this SubmittedTaskInfoExt.
     * 
     * @param allocatedCoreIds
     */
    public void setAllocatedCoreIds(java.lang.String allocatedCoreIds) {
        this.allocatedCoreIds = allocatedCoreIds;
    }


    /**
     * Gets the startTime value for this SubmittedTaskInfoExt.
     * 
     * @return startTime
     */
    public java.util.Calendar getStartTime() {
        return startTime;
    }


    /**
     * Sets the startTime value for this SubmittedTaskInfoExt.
     * 
     * @param startTime
     */
    public void setStartTime(java.util.Calendar startTime) {
        this.startTime = startTime;
    }


    /**
     * Gets the endTime value for this SubmittedTaskInfoExt.
     * 
     * @return endTime
     */
    public java.util.Calendar getEndTime() {
        return endTime;
    }


    /**
     * Sets the endTime value for this SubmittedTaskInfoExt.
     * 
     * @param endTime
     */
    public void setEndTime(java.util.Calendar endTime) {
        this.endTime = endTime;
    }


    /**
     * Gets the errorMessage value for this SubmittedTaskInfoExt.
     * 
     * @return errorMessage
     */
    public java.lang.String getErrorMessage() {
        return errorMessage;
    }


    /**
     * Sets the errorMessage value for this SubmittedTaskInfoExt.
     * 
     * @param errorMessage
     */
    public void setErrorMessage(java.lang.String errorMessage) {
        this.errorMessage = errorMessage;
    }


    /**
     * Gets the allParameters value for this SubmittedTaskInfoExt.
     * 
     * @return allParameters
     */
    public java.lang.String getAllParameters() {
        return allParameters;
    }


    /**
     * Sets the allParameters value for this SubmittedTaskInfoExt.
     * 
     * @param allParameters
     */
    public void setAllParameters(java.lang.String allParameters) {
        this.allParameters = allParameters;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SubmittedTaskInfoExt)) return false;
        SubmittedTaskInfoExt other = (SubmittedTaskInfoExt) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.id==null && other.getId()==null) || 
             (this.id!=null &&
              this.id.equals(other.getId()))) &&
            ((this.name==null && other.getName()==null) || 
             (this.name!=null &&
              this.name.equals(other.getName()))) &&
            ((this.state==null && other.getState()==null) || 
             (this.state!=null &&
              this.state.equals(other.getState()))) &&
            ((this.allocatedTime==null && other.getAllocatedTime()==null) || 
             (this.allocatedTime!=null &&
              this.allocatedTime.equals(other.getAllocatedTime()))) &&
            ((this.allocatedCoreIds==null && other.getAllocatedCoreIds()==null) || 
             (this.allocatedCoreIds!=null &&
              this.allocatedCoreIds.equals(other.getAllocatedCoreIds()))) &&
            ((this.startTime==null && other.getStartTime()==null) || 
             (this.startTime!=null &&
              this.startTime.equals(other.getStartTime()))) &&
            ((this.endTime==null && other.getEndTime()==null) || 
             (this.endTime!=null &&
              this.endTime.equals(other.getEndTime()))) &&
            ((this.errorMessage==null && other.getErrorMessage()==null) || 
             (this.errorMessage!=null &&
              this.errorMessage.equals(other.getErrorMessage()))) &&
            ((this.allParameters==null && other.getAllParameters()==null) || 
             (this.allParameters!=null &&
              this.allParameters.equals(other.getAllParameters())));
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
        if (getId() != null) {
            _hashCode += getId().hashCode();
        }
        if (getName() != null) {
            _hashCode += getName().hashCode();
        }
        if (getState() != null) {
            _hashCode += getState().hashCode();
        }
        if (getAllocatedTime() != null) {
            _hashCode += getAllocatedTime().hashCode();
        }
        if (getAllocatedCoreIds() != null) {
            _hashCode += getAllocatedCoreIds().hashCode();
        }
        if (getStartTime() != null) {
            _hashCode += getStartTime().hashCode();
        }
        if (getEndTime() != null) {
            _hashCode += getEndTime().hashCode();
        }
        if (getErrorMessage() != null) {
            _hashCode += getErrorMessage().hashCode();
        }
        if (getAllParameters() != null) {
            _hashCode += getAllParameters().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SubmittedTaskInfoExt.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "SubmittedTaskInfoExt"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("id");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "id"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "long"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("name");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "name"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("state");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "state"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "TaskStateExt"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("allocatedTime");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "allocatedTime"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("allocatedCoreIds");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "allocatedCoreIds"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("startTime");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "startTime"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("endTime");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "endTime"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("errorMessage");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "errorMessage"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("allParameters");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "allParameters"));
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
