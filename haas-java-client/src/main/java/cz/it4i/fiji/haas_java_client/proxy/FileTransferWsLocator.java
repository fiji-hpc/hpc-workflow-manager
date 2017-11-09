/**
 * FileTransferWsLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package cz.it4i.fiji.haas_java_client.proxy;

public class FileTransferWsLocator extends org.apache.axis.client.Service implements cz.it4i.fiji.haas_java_client.proxy.FileTransferWs {

    public FileTransferWsLocator() {
    }


    public FileTransferWsLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public FileTransferWsLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for FileTransferWsSoap
    private java.lang.String FileTransferWsSoap_address = "http://utepstorage.it4i.cz/HaasWsFiji/FileTransferWs.asmx";

    public java.lang.String getFileTransferWsSoapAddress() {
        return FileTransferWsSoap_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String FileTransferWsSoapWSDDServiceName = "FileTransferWsSoap";

    public java.lang.String getFileTransferWsSoapWSDDServiceName() {
        return FileTransferWsSoapWSDDServiceName;
    }

    public void setFileTransferWsSoapWSDDServiceName(java.lang.String name) {
        FileTransferWsSoapWSDDServiceName = name;
    }

    public cz.it4i.fiji.haas_java_client.proxy.FileTransferWsSoap getFileTransferWsSoap() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(FileTransferWsSoap_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getFileTransferWsSoap(endpoint);
    }

    public cz.it4i.fiji.haas_java_client.proxy.FileTransferWsSoap getFileTransferWsSoap(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            cz.it4i.fiji.haas_java_client.proxy.FileTransferWsSoapStub _stub = new cz.it4i.fiji.haas_java_client.proxy.FileTransferWsSoapStub(portAddress, this);
            _stub.setPortName(getFileTransferWsSoapWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setFileTransferWsSoapEndpointAddress(java.lang.String address) {
        FileTransferWsSoap_address = address;
    }


    // Use to get a proxy class for FileTransferWsSoap12
    private java.lang.String FileTransferWsSoap12_address = "http://utepstorage.it4i.cz/HaasWsFiji/FileTransferWs.asmx";

    public java.lang.String getFileTransferWsSoap12Address() {
        return FileTransferWsSoap12_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String FileTransferWsSoap12WSDDServiceName = "FileTransferWsSoap12";

    public java.lang.String getFileTransferWsSoap12WSDDServiceName() {
        return FileTransferWsSoap12WSDDServiceName;
    }

    public void setFileTransferWsSoap12WSDDServiceName(java.lang.String name) {
        FileTransferWsSoap12WSDDServiceName = name;
    }

    public cz.it4i.fiji.haas_java_client.proxy.FileTransferWsSoap getFileTransferWsSoap12() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(FileTransferWsSoap12_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getFileTransferWsSoap12(endpoint);
    }

    public cz.it4i.fiji.haas_java_client.proxy.FileTransferWsSoap getFileTransferWsSoap12(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            cz.it4i.fiji.haas_java_client.proxy.FileTransferWsSoap12Stub _stub = new cz.it4i.fiji.haas_java_client.proxy.FileTransferWsSoap12Stub(portAddress, this);
            _stub.setPortName(getFileTransferWsSoap12WSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setFileTransferWsSoap12EndpointAddress(java.lang.String address) {
        FileTransferWsSoap12_address = address;
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
            if (cz.it4i.fiji.haas_java_client.proxy.FileTransferWsSoap.class.isAssignableFrom(serviceEndpointInterface)) {
                cz.it4i.fiji.haas_java_client.proxy.FileTransferWsSoapStub _stub = new cz.it4i.fiji.haas_java_client.proxy.FileTransferWsSoapStub(new java.net.URL(FileTransferWsSoap_address), this);
                _stub.setPortName(getFileTransferWsSoapWSDDServiceName());
                return _stub;
            }
            if (cz.it4i.fiji.haas_java_client.proxy.FileTransferWsSoap.class.isAssignableFrom(serviceEndpointInterface)) {
                cz.it4i.fiji.haas_java_client.proxy.FileTransferWsSoap12Stub _stub = new cz.it4i.fiji.haas_java_client.proxy.FileTransferWsSoap12Stub(new java.net.URL(FileTransferWsSoap12_address), this);
                _stub.setPortName(getFileTransferWsSoap12WSDDServiceName());
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
        if ("FileTransferWsSoap".equals(inputPortName)) {
            return getFileTransferWsSoap();
        }
        else if ("FileTransferWsSoap12".equals(inputPortName)) {
            return getFileTransferWsSoap12();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "FileTransferWs");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "FileTransferWsSoap"));
            ports.add(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "FileTransferWsSoap12"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("FileTransferWsSoap".equals(portName)) {
            setFileTransferWsSoapEndpointAddress(address);
        }
        else 
if ("FileTransferWsSoap12".equals(portName)) {
            setFileTransferWsSoap12EndpointAddress(address);
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
