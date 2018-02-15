/**
 * JobManagementWsLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package cz.it4i.fiji.haas_java_client.proxy;

public class JobManagementWsLocator extends org.apache.axis.client.Service implements cz.it4i.fiji.haas_java_client.proxy.JobManagementWs {

    public JobManagementWsLocator() {
    }


    public JobManagementWsLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public JobManagementWsLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for JobManagementWsSoap
    private java.lang.String JobManagementWsSoap_address = "http://haas.vsb.cz/HaasWsFiji/JobManagementWS.asmx";

    public java.lang.String getJobManagementWsSoapAddress() {
        return JobManagementWsSoap_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String JobManagementWsSoapWSDDServiceName = "JobManagementWsSoap";

    public java.lang.String getJobManagementWsSoapWSDDServiceName() {
        return JobManagementWsSoapWSDDServiceName;
    }

    public void setJobManagementWsSoapWSDDServiceName(java.lang.String name) {
        JobManagementWsSoapWSDDServiceName = name;
    }

    public cz.it4i.fiji.haas_java_client.proxy.JobManagementWsSoap getJobManagementWsSoap() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(JobManagementWsSoap_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getJobManagementWsSoap(endpoint);
    }

    public cz.it4i.fiji.haas_java_client.proxy.JobManagementWsSoap getJobManagementWsSoap(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            cz.it4i.fiji.haas_java_client.proxy.JobManagementWsSoapStub _stub = new cz.it4i.fiji.haas_java_client.proxy.JobManagementWsSoapStub(portAddress, this);
            _stub.setPortName(getJobManagementWsSoapWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setJobManagementWsSoapEndpointAddress(java.lang.String address) {
        JobManagementWsSoap_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (cz.it4i.fiji.haas_java_client.proxy.JobManagementWsSoap.class.isAssignableFrom(serviceEndpointInterface)) {
                cz.it4i.fiji.haas_java_client.proxy.JobManagementWsSoapStub _stub = new cz.it4i.fiji.haas_java_client.proxy.JobManagementWsSoapStub(new java.net.URL(JobManagementWsSoap_address), this);
                _stub.setPortName(getJobManagementWsSoapWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("JobManagementWsSoap".equals(inputPortName)) {
            return getJobManagementWsSoap();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "JobManagementWs");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "JobManagementWsSoap"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("JobManagementWsSoap".equals(portName)) {
            setJobManagementWsSoapEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
