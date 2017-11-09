/**
 * ClusterNodeTypeExt.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package cz.it4i.fiji.haas_java_client.proxy;

public class ClusterNodeTypeExt  implements java.io.Serializable {
    private java.lang.Long id;

    private java.lang.String name;

    private java.lang.String description;

    private java.lang.Integer numberOfNodes;

    private java.lang.Integer coresPerNode;

    private java.lang.Integer maxWalltime;

    private cz.it4i.fiji.haas_java_client.proxy.CommandTemplateExt[] possibleCommands;

    public ClusterNodeTypeExt() {
    }

    public ClusterNodeTypeExt(
           java.lang.Long id,
           java.lang.String name,
           java.lang.String description,
           java.lang.Integer numberOfNodes,
           java.lang.Integer coresPerNode,
           java.lang.Integer maxWalltime,
           cz.it4i.fiji.haas_java_client.proxy.CommandTemplateExt[] possibleCommands) {
           this.id = id;
           this.name = name;
           this.description = description;
           this.numberOfNodes = numberOfNodes;
           this.coresPerNode = coresPerNode;
           this.maxWalltime = maxWalltime;
           this.possibleCommands = possibleCommands;
    }


    /**
     * Gets the id value for this ClusterNodeTypeExt.
     * 
     * @return id
     */
    public java.lang.Long getId() {
        return id;
    }


    /**
     * Sets the id value for this ClusterNodeTypeExt.
     * 
     * @param id
     */
    public void setId(java.lang.Long id) {
        this.id = id;
    }


    /**
     * Gets the name value for this ClusterNodeTypeExt.
     * 
     * @return name
     */
    public java.lang.String getName() {
        return name;
    }


    /**
     * Sets the name value for this ClusterNodeTypeExt.
     * 
     * @param name
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }


    /**
     * Gets the description value for this ClusterNodeTypeExt.
     * 
     * @return description
     */
    public java.lang.String getDescription() {
        return description;
    }


    /**
     * Sets the description value for this ClusterNodeTypeExt.
     * 
     * @param description
     */
    public void setDescription(java.lang.String description) {
        this.description = description;
    }


    /**
     * Gets the numberOfNodes value for this ClusterNodeTypeExt.
     * 
     * @return numberOfNodes
     */
    public java.lang.Integer getNumberOfNodes() {
        return numberOfNodes;
    }


    /**
     * Sets the numberOfNodes value for this ClusterNodeTypeExt.
     * 
     * @param numberOfNodes
     */
    public void setNumberOfNodes(java.lang.Integer numberOfNodes) {
        this.numberOfNodes = numberOfNodes;
    }


    /**
     * Gets the coresPerNode value for this ClusterNodeTypeExt.
     * 
     * @return coresPerNode
     */
    public java.lang.Integer getCoresPerNode() {
        return coresPerNode;
    }


    /**
     * Sets the coresPerNode value for this ClusterNodeTypeExt.
     * 
     * @param coresPerNode
     */
    public void setCoresPerNode(java.lang.Integer coresPerNode) {
        this.coresPerNode = coresPerNode;
    }


    /**
     * Gets the maxWalltime value for this ClusterNodeTypeExt.
     * 
     * @return maxWalltime
     */
    public java.lang.Integer getMaxWalltime() {
        return maxWalltime;
    }


    /**
     * Sets the maxWalltime value for this ClusterNodeTypeExt.
     * 
     * @param maxWalltime
     */
    public void setMaxWalltime(java.lang.Integer maxWalltime) {
        this.maxWalltime = maxWalltime;
    }


    /**
     * Gets the possibleCommands value for this ClusterNodeTypeExt.
     * 
     * @return possibleCommands
     */
    public cz.it4i.fiji.haas_java_client.proxy.CommandTemplateExt[] getPossibleCommands() {
        return possibleCommands;
    }


    /**
     * Sets the possibleCommands value for this ClusterNodeTypeExt.
     * 
     * @param possibleCommands
     */
    public void setPossibleCommands(cz.it4i.fiji.haas_java_client.proxy.CommandTemplateExt[] possibleCommands) {
        this.possibleCommands = possibleCommands;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ClusterNodeTypeExt)) return false;
        ClusterNodeTypeExt other = (ClusterNodeTypeExt) obj;
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
            ((this.description==null && other.getDescription()==null) || 
             (this.description!=null &&
              this.description.equals(other.getDescription()))) &&
            ((this.numberOfNodes==null && other.getNumberOfNodes()==null) || 
             (this.numberOfNodes!=null &&
              this.numberOfNodes.equals(other.getNumberOfNodes()))) &&
            ((this.coresPerNode==null && other.getCoresPerNode()==null) || 
             (this.coresPerNode!=null &&
              this.coresPerNode.equals(other.getCoresPerNode()))) &&
            ((this.maxWalltime==null && other.getMaxWalltime()==null) || 
             (this.maxWalltime!=null &&
              this.maxWalltime.equals(other.getMaxWalltime()))) &&
            ((this.possibleCommands==null && other.getPossibleCommands()==null) || 
             (this.possibleCommands!=null &&
              java.util.Arrays.equals(this.possibleCommands, other.getPossibleCommands())));
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
        if (getDescription() != null) {
            _hashCode += getDescription().hashCode();
        }
        if (getNumberOfNodes() != null) {
            _hashCode += getNumberOfNodes().hashCode();
        }
        if (getCoresPerNode() != null) {
            _hashCode += getCoresPerNode().hashCode();
        }
        if (getMaxWalltime() != null) {
            _hashCode += getMaxWalltime().hashCode();
        }
        if (getPossibleCommands() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getPossibleCommands());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getPossibleCommands(), i);
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
        new org.apache.axis.description.TypeDesc(ClusterNodeTypeExt.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "ClusterNodeTypeExt"));
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
        elemField.setFieldName("description");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "description"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("numberOfNodes");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "numberOfNodes"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("coresPerNode");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "coresPerNode"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("maxWalltime");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "maxWalltime"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("possibleCommands");
        elemField.setXmlName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "possibleCommands"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "CommandTemplateExt"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "CommandTemplateExt"));
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
