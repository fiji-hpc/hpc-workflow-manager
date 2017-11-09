/**
 * SubmittedJobInfoExt.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package cz.it4i.fiji.haas_java_client.proxy;

public class SubmittedJobInfoExt  implements java.io.Serializable {
    private java.lang.Long id;

    private java.lang.String name;

    private cz.it4i.fiji.haas_java_client.proxy.JobStateExt state;

    private cz.it4i.fiji.haas_java_client.proxy.JobPriorityExt priority;

    private java.lang.String project;

    private java.util.Calendar creationTime;

    private java.util.Calendar submitTime;

    private java.util.Calendar startTime;

    private java.util.Calendar endTime;

    private java.lang.Double totalAllocatedTime;

    private java.lang.String allParameters;

    private cz.it4i.fiji.haas_java_client.proxy.ClusterNodeTypeExt nodeType;

    private cz.it4i.fiji.haas_java_client.proxy.SubmittedTaskInfoExt[] tasks;

    public SubmittedJobInfoExt() {
    }

    public SubmittedJobInfoExt(
           java.lang.Long id,
           java.lang.String name,
           cz.it4i.fiji.haas_java_client.proxy.JobStateExt state,
           cz.it4i.fiji.haas_java_client.proxy.JobPriorityExt priority,
           java.lang.String project,
           java.util.Calendar creationTime,
           java.util.Calendar submitTime,
           java.util.Calendar startTime,
           java.util.Calendar endTime,
           java.lang.Double totalAllocatedTime,
           java.lang.String allParameters,
           cz.it4i.fiji.haas_java_client.proxy.ClusterNodeTypeExt nodeType,
           cz.it4i.fiji.haas_java_client.proxy.SubmittedTaskInfoExt[] tasks) {
           this.id = id;
           this.name = name;
           this.state = state;
           this.priority = priority;
           this.project = project;
           this.creationTime = creationTime;
           this.submitTime = submitTime;
           this.startTime = startTime;
           this.endTime = endTime;
           this.totalAllocatedTime = totalAllocatedTime;
           this.allParameters = allParameters;
           this.nodeType = nodeType;
           this.tasks = tasks;
    }


    /**
     * Gets the id value for this SubmittedJobInfoExt.
     * 
     * @return id
     */
    public java.lang.Long getId() {
        return id;
    }


    /**
     * Sets the id value for this SubmittedJobInfoExt.
     * 
     * @param id
     */
    public void setId(java.lang.Long id) {
        this.id = id;
    }


    /**
     * Gets the name value for this SubmittedJobInfoExt.
     * 
     * @return name
     */
    public java.lang.String getName() {
        return name;
    }


    /**
     * Sets the name value for this SubmittedJobInfoExt.
     * 
     * @param name
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }


    /**
     * Gets the state value for this SubmittedJobInfoExt.
     * 
     * @return state
     */
    public cz.it4i.fiji.haas_java_client.proxy.JobStateExt getState() {
        return state;
    }


    /**
     * Sets the state value for this SubmittedJobInfoExt.
     * 
     * @param state
     */
    public void setState(cz.it4i.fiji.haas_java_client.proxy.JobStateExt state) {
        this.state = state;
    }


    /**
     * Gets the priority value for this SubmittedJobInfoExt.
     * 
     * @return priority
     */
    public cz.it4i.fiji.haas_java_client.proxy.JobPriorityExt getPriority() {
        return priority;
    }


    /**
     * Sets the priority value for this SubmittedJobInfoExt.
     * 
     * @param priority
     */
    public void setPriority(cz.it4i.fiji.haas_java_client.proxy.JobPriorityExt priority) {
        this.priority = priority;
    }


    /**
     * Gets the project value for this SubmittedJobInfoExt.
     * 
     * @return project
     */
    public java.lang.String getProject() {
        return project;
    }


    /**
     * Sets the project value for this SubmittedJobInfoExt.
     * 
     * @param project
     */
    public void setProject(java.lang.String project) {
        this.project = project;
    }


    /**
     * Gets the creationTime value for this SubmittedJobInfoExt.
     * 
     * @return creationTime
     */
    public java.util.Calendar getCreationTime() {
        return creationTime;
    }


    /**
     * Sets the creationTime value for this SubmittedJobInfoExt.
     * 
     * @param creationTime
     */
    public void setCreationTime(java.util.Calendar creationTime) {
        this.creationTime = creationTime;
    }


    /**
     * Gets the submitTime value for this SubmittedJobInfoExt.
     * 
     * @return submitTime
     */
    public java.util.Calendar getSubmitTime() {
        return submitTime;
    }


    /**
     * Sets the submitTime value for this SubmittedJobInfoExt.
     * 
     * @param submitTime
     */
    public void setSubmitTime(java.util.Calendar submitTime) {
        this.submitTime = submitTime;
    }


    /**
     * Gets the startTime value for this SubmittedJobInfoExt.
     * 
     * @return startTime
     */
    public java.util.Calendar getStartTime() {
        return startTime;
    }


    /**
     * Sets the startTime value for this SubmittedJobInfoExt.
     * 
     * @param startTime
     */
    public void setStartTime(java.util.Calendar startTime) {
        this.startTime = startTime;
    }


    /**
     * Gets the endTime value for this SubmittedJobInfoExt.
     * 
     * @return endTime
     */
    public java.util.Calendar getEndTime() {
        return endTime;
    }


    /**
     * Sets the endTime value for this SubmittedJobInfoExt.
     * 
     * @param endTime
     */
    public void setEndTime(java.util.Calendar endTime) {
        this.endTime = endTime;
    }


    /**
     * Gets the totalAllocatedTime value for this SubmittedJobInfoExt.
     * 
     * @return totalAllocatedTime
     */
    public java.lang.Double getTotalAllocatedTime() {
        return totalAllocatedTime;
    }


    /**
     * Sets the totalAllocatedTime value for this SubmittedJobInfoExt.
     * 
     * @param totalAllocatedTime
     */
    public void setTotalAllocatedTime(java.lang.Double totalAllocatedTime) {
        this.totalAllocatedTime = totalAllocatedTime;
    }


    /**
     * Gets the allParameters value for this SubmittedJobInfoExt.
     * 
     * @return allParameters
     */
    public java.lang.String getAllParameters() {
        return allParameters;
    }


    /**
     * Sets the allParameters value for this SubmittedJobInfoExt.
     * 
     * @param allParameters
     */
    public void setAllParameters(java.lang.String allParameters) {
        this.allParameters = allParameters;
    }


    /**
     * Gets the nodeType value for this SubmittedJobInfoExt.
     * 
     * @return nodeType
     */
    public cz.it4i.fiji.haas_java_client.proxy.ClusterNodeTypeExt getNodeType() {
        return nodeType;
    }


    /**
     * Sets the nodeType value for this SubmittedJobInfoExt.
     * 
     * @param nodeType
     */
    public void setNodeType(cz.it4i.fiji.haas_java_client.proxy.ClusterNodeTypeExt nodeType) {
        this.nodeType = nodeType;
    }


    /**
     * Gets the tasks value for this SubmittedJobInfoExt.
     * 
     * @return tasks
     */
    public cz.it4i.fiji.haas_java_client.proxy.SubmittedTaskInfoExt[] getTasks() {
        return tasks;
    }


    /**
     * Sets the tasks value for this SubmittedJobInfoExt.
     * 
     * @param tasks
     */
    public void setTasks(cz.it4i.fiji.haas_java_client.proxy.SubmittedTaskInfoExt[] tasks) {
        this.tasks = tasks;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SubmittedJobInfoExt)) return false;
        SubmittedJobInfoExt other = (SubmittedJobInfoExt) obj;
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
            ((this.priority==null && other.getPriority()==null) || 
             (this.priority!=null &&
              this.priority.equals(other.getPriority()))) &&
            ((this.project==null && other.getProject()==null) || 
             (this.project!=null &&
              this.project.equals(other.getProject()))) &&
            ((this.creationTime==null && other.getCreationTime()==null) || 
             (this.creationTime!=null &&
              this.creationTime.equals(other.getCreationTime()))) &&
            ((this.submitTime==null && other.getSubmitTime()==null) || 
             (this.submitTime!=null &&
              this.submitTime.equals(other.getSubmitTime()))) &&
            ((this.startTime==null && other.getStartTime()==null) || 
             (this.startTime!=null &&
              this.startTime.equals(other.getStartTime()))) &&
            ((this.endTime==null && other.getEndTime()==null) || 
             (this.endTime!=null &&
              this.endTime.equals(other.getEndTime()))) &&
            ((this.totalAllocatedTime==null && other.getTotalAllocatedTime()==null) || 
             (this.totalAllocatedTime!=null &&
              this.totalAllocatedTime.equals(other.getTotalAllocatedTime()))) &&
            ((this.allParameters==null && other.getAllParameters()==null) || 
             (this.allParameters!=null &&
              this.allParameters.equals(other.getAllParameters()))) &&
            ((this.nodeType==null && other.getNodeType()==null) || 
             (this.nodeType!=null &&
              this.nodeType.equals(other.getNodeType()))) &&
            ((this.tasks==null && other.getTasks()==null) || 
             (this.tasks!=null &&
              java.util.Arrays.equals(this.tasks, other.getTasks())));
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
        if (getPriority() != null) {
            _hashCode += getPriority().hashCode();
        }
        if (getProject() != null) {
            _hashCode += getProject().hashCode();
        }
        if (getCreationTime() != null) {
            _hashCode += getCreationTime().hashCode();
        }
        if (getSubmitTime() != null) {
            _hashCode += getSubmitTime().hashCode();
        }
        if (getStartTime() != null) {
            _hashCode += getStartTime().hashCode();
        }
        if (getEndTime() != null) {
            _hashCode += getEndTime().hashCode();
        }
        if (getTotalAllocatedTime() != null) {
            _hashCode += getTotalAllocatedTime().hashCode();
        }
        if (getAllParameters() != null) {
            _hashCode += getAllParameters().hashCode();
        }
        if (getNodeType() != null) {
            _hashCode += getNodeType().hashCode();
        }
        if (getTasks() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getTasks());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getTasks(), i);
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
        new org.apache.axis.description.TypeDesc(SubmittedJobInfoExt.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "SubmittedJobInfoExt"));
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
        elemField.setXmlType(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "JobStateExt"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("priority");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "priority"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "JobPriorityExt"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("project");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "project"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("creationTime");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "creationTime"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("submitTime");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "submitTime"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        elemField.setNillable(true);
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
        elemField.setFieldName("totalAllocatedTime");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "totalAllocatedTime"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("allParameters");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "allParameters"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("nodeType");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "nodeType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "ClusterNodeTypeExt"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("tasks");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "tasks"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "SubmittedTaskInfoExt"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "SubmittedTaskInfoExt"));
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
