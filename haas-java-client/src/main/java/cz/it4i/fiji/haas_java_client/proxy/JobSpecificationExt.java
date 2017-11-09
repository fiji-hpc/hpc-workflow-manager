/**
 * JobSpecificationExt.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package cz.it4i.fiji.haas_java_client.proxy;

public class JobSpecificationExt  implements java.io.Serializable {
    private java.lang.String name;

    private java.lang.Integer minCores;

    private java.lang.Integer maxCores;

    private cz.it4i.fiji.haas_java_client.proxy.JobPriorityExt priority;

    private java.lang.String project;

    private java.lang.Integer waitingLimit;

    private java.lang.Integer walltimeLimit;

    private java.lang.String notificationEmail;

    private java.lang.String phoneNumber;

    private java.lang.Boolean notifyOnAbort;

    private java.lang.Boolean notifyOnFinish;

    private java.lang.Boolean notifyOnStart;

    private java.lang.Long clusterNodeTypeId;

    private cz.it4i.fiji.haas_java_client.proxy.EnvironmentVariableExt[] environmentVariables;

    private cz.it4i.fiji.haas_java_client.proxy.TaskSpecificationExt[] tasks;

    public JobSpecificationExt() {
    }

    public JobSpecificationExt(
           java.lang.String name,
           java.lang.Integer minCores,
           java.lang.Integer maxCores,
           cz.it4i.fiji.haas_java_client.proxy.JobPriorityExt priority,
           java.lang.String project,
           java.lang.Integer waitingLimit,
           java.lang.Integer walltimeLimit,
           java.lang.String notificationEmail,
           java.lang.String phoneNumber,
           java.lang.Boolean notifyOnAbort,
           java.lang.Boolean notifyOnFinish,
           java.lang.Boolean notifyOnStart,
           java.lang.Long clusterNodeTypeId,
           cz.it4i.fiji.haas_java_client.proxy.EnvironmentVariableExt[] environmentVariables,
           cz.it4i.fiji.haas_java_client.proxy.TaskSpecificationExt[] tasks) {
           this.name = name;
           this.minCores = minCores;
           this.maxCores = maxCores;
           this.priority = priority;
           this.project = project;
           this.waitingLimit = waitingLimit;
           this.walltimeLimit = walltimeLimit;
           this.notificationEmail = notificationEmail;
           this.phoneNumber = phoneNumber;
           this.notifyOnAbort = notifyOnAbort;
           this.notifyOnFinish = notifyOnFinish;
           this.notifyOnStart = notifyOnStart;
           this.clusterNodeTypeId = clusterNodeTypeId;
           this.environmentVariables = environmentVariables;
           this.tasks = tasks;
    }


    /**
     * Gets the name value for this JobSpecificationExt.
     * 
     * @return name
     */
    public java.lang.String getName() {
        return name;
    }


    /**
     * Sets the name value for this JobSpecificationExt.
     * 
     * @param name
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }


    /**
     * Gets the minCores value for this JobSpecificationExt.
     * 
     * @return minCores
     */
    public java.lang.Integer getMinCores() {
        return minCores;
    }


    /**
     * Sets the minCores value for this JobSpecificationExt.
     * 
     * @param minCores
     */
    public void setMinCores(java.lang.Integer minCores) {
        this.minCores = minCores;
    }


    /**
     * Gets the maxCores value for this JobSpecificationExt.
     * 
     * @return maxCores
     */
    public java.lang.Integer getMaxCores() {
        return maxCores;
    }


    /**
     * Sets the maxCores value for this JobSpecificationExt.
     * 
     * @param maxCores
     */
    public void setMaxCores(java.lang.Integer maxCores) {
        this.maxCores = maxCores;
    }


    /**
     * Gets the priority value for this JobSpecificationExt.
     * 
     * @return priority
     */
    public cz.it4i.fiji.haas_java_client.proxy.JobPriorityExt getPriority() {
        return priority;
    }


    /**
     * Sets the priority value for this JobSpecificationExt.
     * 
     * @param priority
     */
    public void setPriority(cz.it4i.fiji.haas_java_client.proxy.JobPriorityExt priority) {
        this.priority = priority;
    }


    /**
     * Gets the project value for this JobSpecificationExt.
     * 
     * @return project
     */
    public java.lang.String getProject() {
        return project;
    }


    /**
     * Sets the project value for this JobSpecificationExt.
     * 
     * @param project
     */
    public void setProject(java.lang.String project) {
        this.project = project;
    }


    /**
     * Gets the waitingLimit value for this JobSpecificationExt.
     * 
     * @return waitingLimit
     */
    public java.lang.Integer getWaitingLimit() {
        return waitingLimit;
    }


    /**
     * Sets the waitingLimit value for this JobSpecificationExt.
     * 
     * @param waitingLimit
     */
    public void setWaitingLimit(java.lang.Integer waitingLimit) {
        this.waitingLimit = waitingLimit;
    }


    /**
     * Gets the walltimeLimit value for this JobSpecificationExt.
     * 
     * @return walltimeLimit
     */
    public java.lang.Integer getWalltimeLimit() {
        return walltimeLimit;
    }


    /**
     * Sets the walltimeLimit value for this JobSpecificationExt.
     * 
     * @param walltimeLimit
     */
    public void setWalltimeLimit(java.lang.Integer walltimeLimit) {
        this.walltimeLimit = walltimeLimit;
    }


    /**
     * Gets the notificationEmail value for this JobSpecificationExt.
     * 
     * @return notificationEmail
     */
    public java.lang.String getNotificationEmail() {
        return notificationEmail;
    }


    /**
     * Sets the notificationEmail value for this JobSpecificationExt.
     * 
     * @param notificationEmail
     */
    public void setNotificationEmail(java.lang.String notificationEmail) {
        this.notificationEmail = notificationEmail;
    }


    /**
     * Gets the phoneNumber value for this JobSpecificationExt.
     * 
     * @return phoneNumber
     */
    public java.lang.String getPhoneNumber() {
        return phoneNumber;
    }


    /**
     * Sets the phoneNumber value for this JobSpecificationExt.
     * 
     * @param phoneNumber
     */
    public void setPhoneNumber(java.lang.String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }


    /**
     * Gets the notifyOnAbort value for this JobSpecificationExt.
     * 
     * @return notifyOnAbort
     */
    public java.lang.Boolean getNotifyOnAbort() {
        return notifyOnAbort;
    }


    /**
     * Sets the notifyOnAbort value for this JobSpecificationExt.
     * 
     * @param notifyOnAbort
     */
    public void setNotifyOnAbort(java.lang.Boolean notifyOnAbort) {
        this.notifyOnAbort = notifyOnAbort;
    }


    /**
     * Gets the notifyOnFinish value for this JobSpecificationExt.
     * 
     * @return notifyOnFinish
     */
    public java.lang.Boolean getNotifyOnFinish() {
        return notifyOnFinish;
    }


    /**
     * Sets the notifyOnFinish value for this JobSpecificationExt.
     * 
     * @param notifyOnFinish
     */
    public void setNotifyOnFinish(java.lang.Boolean notifyOnFinish) {
        this.notifyOnFinish = notifyOnFinish;
    }


    /**
     * Gets the notifyOnStart value for this JobSpecificationExt.
     * 
     * @return notifyOnStart
     */
    public java.lang.Boolean getNotifyOnStart() {
        return notifyOnStart;
    }


    /**
     * Sets the notifyOnStart value for this JobSpecificationExt.
     * 
     * @param notifyOnStart
     */
    public void setNotifyOnStart(java.lang.Boolean notifyOnStart) {
        this.notifyOnStart = notifyOnStart;
    }


    /**
     * Gets the clusterNodeTypeId value for this JobSpecificationExt.
     * 
     * @return clusterNodeTypeId
     */
    public java.lang.Long getClusterNodeTypeId() {
        return clusterNodeTypeId;
    }


    /**
     * Sets the clusterNodeTypeId value for this JobSpecificationExt.
     * 
     * @param clusterNodeTypeId
     */
    public void setClusterNodeTypeId(java.lang.Long clusterNodeTypeId) {
        this.clusterNodeTypeId = clusterNodeTypeId;
    }


    /**
     * Gets the environmentVariables value for this JobSpecificationExt.
     * 
     * @return environmentVariables
     */
    public cz.it4i.fiji.haas_java_client.proxy.EnvironmentVariableExt[] getEnvironmentVariables() {
        return environmentVariables;
    }


    /**
     * Sets the environmentVariables value for this JobSpecificationExt.
     * 
     * @param environmentVariables
     */
    public void setEnvironmentVariables(cz.it4i.fiji.haas_java_client.proxy.EnvironmentVariableExt[] environmentVariables) {
        this.environmentVariables = environmentVariables;
    }


    /**
     * Gets the tasks value for this JobSpecificationExt.
     * 
     * @return tasks
     */
    public cz.it4i.fiji.haas_java_client.proxy.TaskSpecificationExt[] getTasks() {
        return tasks;
    }


    /**
     * Sets the tasks value for this JobSpecificationExt.
     * 
     * @param tasks
     */
    public void setTasks(cz.it4i.fiji.haas_java_client.proxy.TaskSpecificationExt[] tasks) {
        this.tasks = tasks;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof JobSpecificationExt)) return false;
        JobSpecificationExt other = (JobSpecificationExt) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.name==null && other.getName()==null) || 
             (this.name!=null &&
              this.name.equals(other.getName()))) &&
            ((this.minCores==null && other.getMinCores()==null) || 
             (this.minCores!=null &&
              this.minCores.equals(other.getMinCores()))) &&
            ((this.maxCores==null && other.getMaxCores()==null) || 
             (this.maxCores!=null &&
              this.maxCores.equals(other.getMaxCores()))) &&
            ((this.priority==null && other.getPriority()==null) || 
             (this.priority!=null &&
              this.priority.equals(other.getPriority()))) &&
            ((this.project==null && other.getProject()==null) || 
             (this.project!=null &&
              this.project.equals(other.getProject()))) &&
            ((this.waitingLimit==null && other.getWaitingLimit()==null) || 
             (this.waitingLimit!=null &&
              this.waitingLimit.equals(other.getWaitingLimit()))) &&
            ((this.walltimeLimit==null && other.getWalltimeLimit()==null) || 
             (this.walltimeLimit!=null &&
              this.walltimeLimit.equals(other.getWalltimeLimit()))) &&
            ((this.notificationEmail==null && other.getNotificationEmail()==null) || 
             (this.notificationEmail!=null &&
              this.notificationEmail.equals(other.getNotificationEmail()))) &&
            ((this.phoneNumber==null && other.getPhoneNumber()==null) || 
             (this.phoneNumber!=null &&
              this.phoneNumber.equals(other.getPhoneNumber()))) &&
            ((this.notifyOnAbort==null && other.getNotifyOnAbort()==null) || 
             (this.notifyOnAbort!=null &&
              this.notifyOnAbort.equals(other.getNotifyOnAbort()))) &&
            ((this.notifyOnFinish==null && other.getNotifyOnFinish()==null) || 
             (this.notifyOnFinish!=null &&
              this.notifyOnFinish.equals(other.getNotifyOnFinish()))) &&
            ((this.notifyOnStart==null && other.getNotifyOnStart()==null) || 
             (this.notifyOnStart!=null &&
              this.notifyOnStart.equals(other.getNotifyOnStart()))) &&
            ((this.clusterNodeTypeId==null && other.getClusterNodeTypeId()==null) || 
             (this.clusterNodeTypeId!=null &&
              this.clusterNodeTypeId.equals(other.getClusterNodeTypeId()))) &&
            ((this.environmentVariables==null && other.getEnvironmentVariables()==null) || 
             (this.environmentVariables!=null &&
              java.util.Arrays.equals(this.environmentVariables, other.getEnvironmentVariables()))) &&
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
        if (getName() != null) {
            _hashCode += getName().hashCode();
        }
        if (getMinCores() != null) {
            _hashCode += getMinCores().hashCode();
        }
        if (getMaxCores() != null) {
            _hashCode += getMaxCores().hashCode();
        }
        if (getPriority() != null) {
            _hashCode += getPriority().hashCode();
        }
        if (getProject() != null) {
            _hashCode += getProject().hashCode();
        }
        if (getWaitingLimit() != null) {
            _hashCode += getWaitingLimit().hashCode();
        }
        if (getWalltimeLimit() != null) {
            _hashCode += getWalltimeLimit().hashCode();
        }
        if (getNotificationEmail() != null) {
            _hashCode += getNotificationEmail().hashCode();
        }
        if (getPhoneNumber() != null) {
            _hashCode += getPhoneNumber().hashCode();
        }
        if (getNotifyOnAbort() != null) {
            _hashCode += getNotifyOnAbort().hashCode();
        }
        if (getNotifyOnFinish() != null) {
            _hashCode += getNotifyOnFinish().hashCode();
        }
        if (getNotifyOnStart() != null) {
            _hashCode += getNotifyOnStart().hashCode();
        }
        if (getClusterNodeTypeId() != null) {
            _hashCode += getClusterNodeTypeId().hashCode();
        }
        if (getEnvironmentVariables() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getEnvironmentVariables());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getEnvironmentVariables(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
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
        new org.apache.axis.description.TypeDesc(JobSpecificationExt.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "JobSpecificationExt"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("name");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "name"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("minCores");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "minCores"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("maxCores");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "maxCores"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
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
        elemField.setFieldName("waitingLimit");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "waitingLimit"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("walltimeLimit");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "walltimeLimit"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("notificationEmail");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "notificationEmail"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("phoneNumber");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "phoneNumber"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("notifyOnAbort");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "notifyOnAbort"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("notifyOnFinish");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "notifyOnFinish"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("notifyOnStart");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "notifyOnStart"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("clusterNodeTypeId");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "clusterNodeTypeId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "long"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("environmentVariables");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "environmentVariables"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "EnvironmentVariableExt"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "EnvironmentVariableExt"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("tasks");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "tasks"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "TaskSpecificationExt"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "TaskSpecificationExt"));
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
