/**
 * TaskSpecificationExt.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package cz.it4i.fiji.haas_java_client.proxy;

public class TaskSpecificationExt  implements java.io.Serializable {
    private java.lang.String name;

    private java.lang.Integer minCores;

    private java.lang.Integer maxCores;

    private java.lang.Integer walltimeLimit;

    private java.lang.String requiredNodes;

    private java.lang.Boolean isExclusive;

    private java.lang.Boolean isRerunnable;

    private java.lang.String standardInputFile;

    private java.lang.String standardOutputFile;

    private java.lang.String standardErrorFile;

    private java.lang.String progressFile;

    private java.lang.String logFile;

    private java.lang.String clusterTaskSubdirectory;

    private java.lang.Long commandTemplateId;

    private cz.it4i.fiji.haas_java_client.proxy.EnvironmentVariableExt[] environmentVariables;

    private cz.it4i.fiji.haas_java_client.proxy.TaskSpecificationExt[] dependsOn;

    private cz.it4i.fiji.haas_java_client.proxy.CommandTemplateParameterValueExt[] templateParameterValues;

    public TaskSpecificationExt() {
    }

    public TaskSpecificationExt(
           java.lang.String name,
           java.lang.Integer minCores,
           java.lang.Integer maxCores,
           java.lang.Integer walltimeLimit,
           java.lang.String requiredNodes,
           java.lang.Boolean isExclusive,
           java.lang.Boolean isRerunnable,
           java.lang.String standardInputFile,
           java.lang.String standardOutputFile,
           java.lang.String standardErrorFile,
           java.lang.String progressFile,
           java.lang.String logFile,
           java.lang.String clusterTaskSubdirectory,
           java.lang.Long commandTemplateId,
           cz.it4i.fiji.haas_java_client.proxy.EnvironmentVariableExt[] environmentVariables,
           cz.it4i.fiji.haas_java_client.proxy.TaskSpecificationExt[] dependsOn,
           cz.it4i.fiji.haas_java_client.proxy.CommandTemplateParameterValueExt[] templateParameterValues) {
           this.name = name;
           this.minCores = minCores;
           this.maxCores = maxCores;
           this.walltimeLimit = walltimeLimit;
           this.requiredNodes = requiredNodes;
           this.isExclusive = isExclusive;
           this.isRerunnable = isRerunnable;
           this.standardInputFile = standardInputFile;
           this.standardOutputFile = standardOutputFile;
           this.standardErrorFile = standardErrorFile;
           this.progressFile = progressFile;
           this.logFile = logFile;
           this.clusterTaskSubdirectory = clusterTaskSubdirectory;
           this.commandTemplateId = commandTemplateId;
           this.environmentVariables = environmentVariables;
           this.dependsOn = dependsOn;
           this.templateParameterValues = templateParameterValues;
    }


    /**
     * Gets the name value for this TaskSpecificationExt.
     * 
     * @return name
     */
    public java.lang.String getName() {
        return name;
    }


    /**
     * Sets the name value for this TaskSpecificationExt.
     * 
     * @param name
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }


    /**
     * Gets the minCores value for this TaskSpecificationExt.
     * 
     * @return minCores
     */
    public java.lang.Integer getMinCores() {
        return minCores;
    }


    /**
     * Sets the minCores value for this TaskSpecificationExt.
     * 
     * @param minCores
     */
    public void setMinCores(java.lang.Integer minCores) {
        this.minCores = minCores;
    }


    /**
     * Gets the maxCores value for this TaskSpecificationExt.
     * 
     * @return maxCores
     */
    public java.lang.Integer getMaxCores() {
        return maxCores;
    }


    /**
     * Sets the maxCores value for this TaskSpecificationExt.
     * 
     * @param maxCores
     */
    public void setMaxCores(java.lang.Integer maxCores) {
        this.maxCores = maxCores;
    }


    /**
     * Gets the walltimeLimit value for this TaskSpecificationExt.
     * 
     * @return walltimeLimit
     */
    public java.lang.Integer getWalltimeLimit() {
        return walltimeLimit;
    }


    /**
     * Sets the walltimeLimit value for this TaskSpecificationExt.
     * 
     * @param walltimeLimit
     */
    public void setWalltimeLimit(java.lang.Integer walltimeLimit) {
        this.walltimeLimit = walltimeLimit;
    }


    /**
     * Gets the requiredNodes value for this TaskSpecificationExt.
     * 
     * @return requiredNodes
     */
    public java.lang.String getRequiredNodes() {
        return requiredNodes;
    }


    /**
     * Sets the requiredNodes value for this TaskSpecificationExt.
     * 
     * @param requiredNodes
     */
    public void setRequiredNodes(java.lang.String requiredNodes) {
        this.requiredNodes = requiredNodes;
    }


    /**
     * Gets the isExclusive value for this TaskSpecificationExt.
     * 
     * @return isExclusive
     */
    public java.lang.Boolean getIsExclusive() {
        return isExclusive;
    }


    /**
     * Sets the isExclusive value for this TaskSpecificationExt.
     * 
     * @param isExclusive
     */
    public void setIsExclusive(java.lang.Boolean isExclusive) {
        this.isExclusive = isExclusive;
    }


    /**
     * Gets the isRerunnable value for this TaskSpecificationExt.
     * 
     * @return isRerunnable
     */
    public java.lang.Boolean getIsRerunnable() {
        return isRerunnable;
    }


    /**
     * Sets the isRerunnable value for this TaskSpecificationExt.
     * 
     * @param isRerunnable
     */
    public void setIsRerunnable(java.lang.Boolean isRerunnable) {
        this.isRerunnable = isRerunnable;
    }


    /**
     * Gets the standardInputFile value for this TaskSpecificationExt.
     * 
     * @return standardInputFile
     */
    public java.lang.String getStandardInputFile() {
        return standardInputFile;
    }


    /**
     * Sets the standardInputFile value for this TaskSpecificationExt.
     * 
     * @param standardInputFile
     */
    public void setStandardInputFile(java.lang.String standardInputFile) {
        this.standardInputFile = standardInputFile;
    }


    /**
     * Gets the standardOutputFile value for this TaskSpecificationExt.
     * 
     * @return standardOutputFile
     */
    public java.lang.String getStandardOutputFile() {
        return standardOutputFile;
    }


    /**
     * Sets the standardOutputFile value for this TaskSpecificationExt.
     * 
     * @param standardOutputFile
     */
    public void setStandardOutputFile(java.lang.String standardOutputFile) {
        this.standardOutputFile = standardOutputFile;
    }


    /**
     * Gets the standardErrorFile value for this TaskSpecificationExt.
     * 
     * @return standardErrorFile
     */
    public java.lang.String getStandardErrorFile() {
        return standardErrorFile;
    }


    /**
     * Sets the standardErrorFile value for this TaskSpecificationExt.
     * 
     * @param standardErrorFile
     */
    public void setStandardErrorFile(java.lang.String standardErrorFile) {
        this.standardErrorFile = standardErrorFile;
    }


    /**
     * Gets the progressFile value for this TaskSpecificationExt.
     * 
     * @return progressFile
     */
    public java.lang.String getProgressFile() {
        return progressFile;
    }


    /**
     * Sets the progressFile value for this TaskSpecificationExt.
     * 
     * @param progressFile
     */
    public void setProgressFile(java.lang.String progressFile) {
        this.progressFile = progressFile;
    }


    /**
     * Gets the logFile value for this TaskSpecificationExt.
     * 
     * @return logFile
     */
    public java.lang.String getLogFile() {
        return logFile;
    }


    /**
     * Sets the logFile value for this TaskSpecificationExt.
     * 
     * @param logFile
     */
    public void setLogFile(java.lang.String logFile) {
        this.logFile = logFile;
    }


    /**
     * Gets the clusterTaskSubdirectory value for this TaskSpecificationExt.
     * 
     * @return clusterTaskSubdirectory
     */
    public java.lang.String getClusterTaskSubdirectory() {
        return clusterTaskSubdirectory;
    }


    /**
     * Sets the clusterTaskSubdirectory value for this TaskSpecificationExt.
     * 
     * @param clusterTaskSubdirectory
     */
    public void setClusterTaskSubdirectory(java.lang.String clusterTaskSubdirectory) {
        this.clusterTaskSubdirectory = clusterTaskSubdirectory;
    }


    /**
     * Gets the commandTemplateId value for this TaskSpecificationExt.
     * 
     * @return commandTemplateId
     */
    public java.lang.Long getCommandTemplateId() {
        return commandTemplateId;
    }


    /**
     * Sets the commandTemplateId value for this TaskSpecificationExt.
     * 
     * @param commandTemplateId
     */
    public void setCommandTemplateId(java.lang.Long commandTemplateId) {
        this.commandTemplateId = commandTemplateId;
    }


    /**
     * Gets the environmentVariables value for this TaskSpecificationExt.
     * 
     * @return environmentVariables
     */
    public cz.it4i.fiji.haas_java_client.proxy.EnvironmentVariableExt[] getEnvironmentVariables() {
        return environmentVariables;
    }


    /**
     * Sets the environmentVariables value for this TaskSpecificationExt.
     * 
     * @param environmentVariables
     */
    public void setEnvironmentVariables(cz.it4i.fiji.haas_java_client.proxy.EnvironmentVariableExt[] environmentVariables) {
        this.environmentVariables = environmentVariables;
    }


    /**
     * Gets the dependsOn value for this TaskSpecificationExt.
     * 
     * @return dependsOn
     */
    public cz.it4i.fiji.haas_java_client.proxy.TaskSpecificationExt[] getDependsOn() {
        return dependsOn;
    }


    /**
     * Sets the dependsOn value for this TaskSpecificationExt.
     * 
     * @param dependsOn
     */
    public void setDependsOn(cz.it4i.fiji.haas_java_client.proxy.TaskSpecificationExt[] dependsOn) {
        this.dependsOn = dependsOn;
    }


    /**
     * Gets the templateParameterValues value for this TaskSpecificationExt.
     * 
     * @return templateParameterValues
     */
    public cz.it4i.fiji.haas_java_client.proxy.CommandTemplateParameterValueExt[] getTemplateParameterValues() {
        return templateParameterValues;
    }


    /**
     * Sets the templateParameterValues value for this TaskSpecificationExt.
     * 
     * @param templateParameterValues
     */
    public void setTemplateParameterValues(cz.it4i.fiji.haas_java_client.proxy.CommandTemplateParameterValueExt[] templateParameterValues) {
        this.templateParameterValues = templateParameterValues;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof TaskSpecificationExt)) return false;
        TaskSpecificationExt other = (TaskSpecificationExt) obj;
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
            ((this.walltimeLimit==null && other.getWalltimeLimit()==null) || 
             (this.walltimeLimit!=null &&
              this.walltimeLimit.equals(other.getWalltimeLimit()))) &&
            ((this.requiredNodes==null && other.getRequiredNodes()==null) || 
             (this.requiredNodes!=null &&
              this.requiredNodes.equals(other.getRequiredNodes()))) &&
            ((this.isExclusive==null && other.getIsExclusive()==null) || 
             (this.isExclusive!=null &&
              this.isExclusive.equals(other.getIsExclusive()))) &&
            ((this.isRerunnable==null && other.getIsRerunnable()==null) || 
             (this.isRerunnable!=null &&
              this.isRerunnable.equals(other.getIsRerunnable()))) &&
            ((this.standardInputFile==null && other.getStandardInputFile()==null) || 
             (this.standardInputFile!=null &&
              this.standardInputFile.equals(other.getStandardInputFile()))) &&
            ((this.standardOutputFile==null && other.getStandardOutputFile()==null) || 
             (this.standardOutputFile!=null &&
              this.standardOutputFile.equals(other.getStandardOutputFile()))) &&
            ((this.standardErrorFile==null && other.getStandardErrorFile()==null) || 
             (this.standardErrorFile!=null &&
              this.standardErrorFile.equals(other.getStandardErrorFile()))) &&
            ((this.progressFile==null && other.getProgressFile()==null) || 
             (this.progressFile!=null &&
              this.progressFile.equals(other.getProgressFile()))) &&
            ((this.logFile==null && other.getLogFile()==null) || 
             (this.logFile!=null &&
              this.logFile.equals(other.getLogFile()))) &&
            ((this.clusterTaskSubdirectory==null && other.getClusterTaskSubdirectory()==null) || 
             (this.clusterTaskSubdirectory!=null &&
              this.clusterTaskSubdirectory.equals(other.getClusterTaskSubdirectory()))) &&
            ((this.commandTemplateId==null && other.getCommandTemplateId()==null) || 
             (this.commandTemplateId!=null &&
              this.commandTemplateId.equals(other.getCommandTemplateId()))) &&
            ((this.environmentVariables==null && other.getEnvironmentVariables()==null) || 
             (this.environmentVariables!=null &&
              java.util.Arrays.equals(this.environmentVariables, other.getEnvironmentVariables()))) &&
            ((this.dependsOn==null && other.getDependsOn()==null) || 
             (this.dependsOn!=null &&
              java.util.Arrays.equals(this.dependsOn, other.getDependsOn()))) &&
            ((this.templateParameterValues==null && other.getTemplateParameterValues()==null) || 
             (this.templateParameterValues!=null &&
              java.util.Arrays.equals(this.templateParameterValues, other.getTemplateParameterValues())));
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
        if (getWalltimeLimit() != null) {
            _hashCode += getWalltimeLimit().hashCode();
        }
        if (getRequiredNodes() != null) {
            _hashCode += getRequiredNodes().hashCode();
        }
        if (getIsExclusive() != null) {
            _hashCode += getIsExclusive().hashCode();
        }
        if (getIsRerunnable() != null) {
            _hashCode += getIsRerunnable().hashCode();
        }
        if (getStandardInputFile() != null) {
            _hashCode += getStandardInputFile().hashCode();
        }
        if (getStandardOutputFile() != null) {
            _hashCode += getStandardOutputFile().hashCode();
        }
        if (getStandardErrorFile() != null) {
            _hashCode += getStandardErrorFile().hashCode();
        }
        if (getProgressFile() != null) {
            _hashCode += getProgressFile().hashCode();
        }
        if (getLogFile() != null) {
            _hashCode += getLogFile().hashCode();
        }
        if (getClusterTaskSubdirectory() != null) {
            _hashCode += getClusterTaskSubdirectory().hashCode();
        }
        if (getCommandTemplateId() != null) {
            _hashCode += getCommandTemplateId().hashCode();
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
        if (getDependsOn() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getDependsOn());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getDependsOn(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getTemplateParameterValues() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getTemplateParameterValues());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getTemplateParameterValues(), i);
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
        new org.apache.axis.description.TypeDesc(TaskSpecificationExt.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "TaskSpecificationExt"));
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
        elemField.setFieldName("walltimeLimit");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "walltimeLimit"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("requiredNodes");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "requiredNodes"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("isExclusive");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "isExclusive"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("isRerunnable");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "isRerunnable"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("standardInputFile");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "standardInputFile"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("standardOutputFile");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "standardOutputFile"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("standardErrorFile");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "standardErrorFile"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("progressFile");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "progressFile"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("logFile");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "logFile"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("clusterTaskSubdirectory");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "clusterTaskSubdirectory"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("commandTemplateId");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "commandTemplateId"));
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
        elemField.setFieldName("dependsOn");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "dependsOn"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "TaskSpecificationExt"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "TaskSpecificationExt"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("templateParameterValues");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "templateParameterValues"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "CommandTemplateParameterValueExt"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "CommandTemplateParameterValueExt"));
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
