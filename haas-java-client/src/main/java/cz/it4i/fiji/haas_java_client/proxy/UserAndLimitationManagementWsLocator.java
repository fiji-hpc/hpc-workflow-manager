/**
 * UserAndLimitationManagementWsLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package cz.it4i.fiji.haas_java_client.proxy;

public class UserAndLimitationManagementWsLocator extends org.apache.axis.client.Service implements cz.it4i.fiji.haas_java_client.proxy.UserAndLimitationManagementWs {

    public UserAndLimitationManagementWsLocator() {
    }


    public UserAndLimitationManagementWsLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public UserAndLimitationManagementWsLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for UserAndLimitationManagementWsSoap
    private java.lang.String UserAndLimitationManagementWsSoap_address = "http://haas.vsb.cz/HaasWsFiji/UserAndLimitationManagementWs.asmx";

    public java.lang.String getUserAndLimitationManagementWsSoapAddress() {
        return UserAndLimitationManagementWsSoap_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String UserAndLimitationManagementWsSoapWSDDServiceName = "UserAndLimitationManagementWsSoap";

    public java.lang.String getUserAndLimitationManagementWsSoapWSDDServiceName() {
        return UserAndLimitationManagementWsSoapWSDDServiceName;
    }

    public void setUserAndLimitationManagementWsSoapWSDDServiceName(java.lang.String name) {
        UserAndLimitationManagementWsSoapWSDDServiceName = name;
    }

    public cz.it4i.fiji.haas_java_client.proxy.UserAndLimitationManagementWsSoap getUserAndLimitationManagementWsSoap() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(UserAndLimitationManagementWsSoap_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getUserAndLimitationManagementWsSoap(endpoint);
    }

    public cz.it4i.fiji.haas_java_client.proxy.UserAndLimitationManagementWsSoap getUserAndLimitationManagementWsSoap(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            cz.it4i.fiji.haas_java_client.proxy.UserAndLimitationManagementWsSoapStub _stub = new cz.it4i.fiji.haas_java_client.proxy.UserAndLimitationManagementWsSoapStub(portAddress, this);
            _stub.setPortName(getUserAndLimitationManagementWsSoapWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setUserAndLimitationManagementWsSoapEndpointAddress(java.lang.String address) {
        UserAndLimitationManagementWsSoap_address = address;
    }


    // Use to get a proxy class for UserAndLimitationManagementWsSoap12
    private java.lang.String UserAndLimitationManagementWsSoap12_address = "http://haas.vsb.cz/HaasWsFiji/UserAndLimitationManagementWs.asmx";

    public java.lang.String getUserAndLimitationManagementWsSoap12Address() {
        return UserAndLimitationManagementWsSoap12_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String UserAndLimitationManagementWsSoap12WSDDServiceName = "UserAndLimitationManagementWsSoap12";

    public java.lang.String getUserAndLimitationManagementWsSoap12WSDDServiceName() {
        return UserAndLimitationManagementWsSoap12WSDDServiceName;
    }

    public void setUserAndLimitationManagementWsSoap12WSDDServiceName(java.lang.String name) {
        UserAndLimitationManagementWsSoap12WSDDServiceName = name;
    }

    public cz.it4i.fiji.haas_java_client.proxy.UserAndLimitationManagementWsSoap getUserAndLimitationManagementWsSoap12() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(UserAndLimitationManagementWsSoap12_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getUserAndLimitationManagementWsSoap12(endpoint);
    }

    public cz.it4i.fiji.haas_java_client.proxy.UserAndLimitationManagementWsSoap getUserAndLimitationManagementWsSoap12(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            cz.it4i.fiji.haas_java_client.proxy.UserAndLimitationManagementWsSoap12Stub _stub = new cz.it4i.fiji.haas_java_client.proxy.UserAndLimitationManagementWsSoap12Stub(portAddress, this);
            _stub.setPortName(getUserAndLimitationManagementWsSoap12WSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setUserAndLimitationManagementWsSoap12EndpointAddress(java.lang.String address) {
        UserAndLimitationManagementWsSoap12_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     * This service has multiple ports for a given interface;
     * the proxy implementation returned may be indeterminate.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (cz.it4i.fiji.haas_java_client.proxy.UserAndLimitationManagementWsSoap.class.isAssignableFrom(serviceEndpointInterface)) {
                cz.it4i.fiji.haas_java_client.proxy.UserAndLimitationManagementWsSoapStub _stub = new cz.it4i.fiji.haas_java_client.proxy.UserAndLimitationManagementWsSoapStub(new java.net.URL(UserAndLimitationManagementWsSoap_address), this);
                _stub.setPortName(getUserAndLimitationManagementWsSoapWSDDServiceName());
                return _stub;
            }
            if (cz.it4i.fiji.haas_java_client.proxy.UserAndLimitationManagementWsSoap.class.isAssignableFrom(serviceEndpointInterface)) {
                cz.it4i.fiji.haas_java_client.proxy.UserAndLimitationManagementWsSoap12Stub _stub = new cz.it4i.fiji.haas_java_client.proxy.UserAndLimitationManagementWsSoap12Stub(new java.net.URL(UserAndLimitationManagementWsSoap12_address), this);
                _stub.setPortName(getUserAndLimitationManagementWsSoap12WSDDServiceName());
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
        if ("UserAndLimitationManagementWsSoap".equals(inputPortName)) {
            return getUserAndLimitationManagementWsSoap();
        }
        else if ("UserAndLimitationManagementWsSoap12".equals(inputPortName)) {
            return getUserAndLimitationManagementWsSoap12();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "UserAndLimitationManagementWs");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "UserAndLimitationManagementWsSoap"));
            ports.add(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "UserAndLimitationManagementWsSoap12"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("UserAndLimitationManagementWsSoap".equals(portName)) {
            setUserAndLimitationManagementWsSoapEndpointAddress(address);
        }
        else 
if ("UserAndLimitationManagementWsSoap12".equals(portName)) {
            setUserAndLimitationManagementWsSoap12EndpointAddress(address);
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
