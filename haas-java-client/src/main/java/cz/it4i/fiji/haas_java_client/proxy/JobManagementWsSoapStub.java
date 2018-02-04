/**
 * JobManagementWsSoapStub.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package cz.it4i.fiji.haas_java_client.proxy;

public class JobManagementWsSoapStub extends org.apache.axis.client.Stub implements cz.it4i.fiji.haas_java_client.proxy.JobManagementWsSoap {
    private java.util.Vector cachedSerClasses = new java.util.Vector();
    private java.util.Vector cachedSerQNames = new java.util.Vector();
    private java.util.Vector cachedSerFactories = new java.util.Vector();
    private java.util.Vector cachedDeserFactories = new java.util.Vector();

    static org.apache.axis.description.OperationDesc [] _operations;

    static {
        _operations = new org.apache.axis.description.OperationDesc[6];
        _initOperationDesc1();
    }

    private static void _initOperationDesc1(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("CreateJob");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "specification"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "JobSpecificationExt"), cz.it4i.fiji.haas_java_client.proxy.JobSpecificationExt.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "sessionCode"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "SubmittedJobInfoExt"));
        oper.setReturnClass(cz.it4i.fiji.haas_java_client.proxy.SubmittedJobInfoExt.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "CreateJobResult"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[0] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("SubmitJob");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "createdJobInfoId"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "long"), long.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "sessionCode"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "SubmittedJobInfoExt"));
        oper.setReturnClass(cz.it4i.fiji.haas_java_client.proxy.SubmittedJobInfoExt.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "SubmitJobResult"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[1] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("CancelJob");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "submittedJobInfoId"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "long"), long.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "sessionCode"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "SubmittedJobInfoExt"));
        oper.setReturnClass(cz.it4i.fiji.haas_java_client.proxy.SubmittedJobInfoExt.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "CancelJobResult"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[2] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("DeleteJob");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "submittedJobInfoId"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "long"), long.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "sessionCode"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[3] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("ListJobsForCurrentUser");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "sessionCode"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "ArrayOfSubmittedJobInfoExt"));
        oper.setReturnClass(cz.it4i.fiji.haas_java_client.proxy.SubmittedJobInfoExt[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "ListJobsForCurrentUserResult"));
        param = oper.getReturnParamDesc();
        param.setItemQName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "SubmittedJobInfoExt"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[4] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("GetCurrentInfoForJob");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "submittedJobInfoId"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "long"), long.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "sessionCode"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "SubmittedJobInfoExt"));
        oper.setReturnClass(cz.it4i.fiji.haas_java_client.proxy.SubmittedJobInfoExt.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "GetCurrentInfoForJobResult"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[5] = oper;

    }

    public JobManagementWsSoapStub() throws org.apache.axis.AxisFault {
         this(null);
    }

    public JobManagementWsSoapStub(java.net.URL endpointURL, javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
         this(service);
         super.cachedEndpoint = endpointURL;
    }

    public JobManagementWsSoapStub(javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
        if (service == null) {
            super.service = new org.apache.axis.client.Service();
        } else {
            super.service = service;
        }
        ((org.apache.axis.client.Service)super.service).setTypeMappingVersion("1.2");
            java.lang.Class cls;
            javax.xml.namespace.QName qName;
            javax.xml.namespace.QName qName2;
            java.lang.Class beansf = org.apache.axis.encoding.ser.BeanSerializerFactory.class;
            java.lang.Class beandf = org.apache.axis.encoding.ser.BeanDeserializerFactory.class;
            java.lang.Class enumsf = org.apache.axis.encoding.ser.EnumSerializerFactory.class;
            java.lang.Class enumdf = org.apache.axis.encoding.ser.EnumDeserializerFactory.class;
            java.lang.Class arraysf = org.apache.axis.encoding.ser.ArraySerializerFactory.class;
            java.lang.Class arraydf = org.apache.axis.encoding.ser.ArrayDeserializerFactory.class;
            java.lang.Class simplesf = org.apache.axis.encoding.ser.SimpleSerializerFactory.class;
            java.lang.Class simpledf = org.apache.axis.encoding.ser.SimpleDeserializerFactory.class;
            java.lang.Class simplelistsf = org.apache.axis.encoding.ser.SimpleListSerializerFactory.class;
            java.lang.Class simplelistdf = org.apache.axis.encoding.ser.SimpleListDeserializerFactory.class;
            qName = new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "ArrayOfCommandTemplateExt");
            cachedSerQNames.add(qName);
            cls = cz.it4i.fiji.haas_java_client.proxy.CommandTemplateExt[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "CommandTemplateExt");
            qName2 = new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "CommandTemplateExt");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "ArrayOfCommandTemplateParameterExt");
            cachedSerQNames.add(qName);
            cls = cz.it4i.fiji.haas_java_client.proxy.CommandTemplateParameterExt[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "CommandTemplateParameterExt");
            qName2 = new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "CommandTemplateParameterExt");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "ArrayOfCommandTemplateParameterValueExt");
            cachedSerQNames.add(qName);
            cls = cz.it4i.fiji.haas_java_client.proxy.CommandTemplateParameterValueExt[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "CommandTemplateParameterValueExt");
            qName2 = new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "CommandTemplateParameterValueExt");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "ArrayOfEnvironmentVariableExt");
            cachedSerQNames.add(qName);
            cls = cz.it4i.fiji.haas_java_client.proxy.EnvironmentVariableExt[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "EnvironmentVariableExt");
            qName2 = new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "EnvironmentVariableExt");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "ArrayOfSubmittedJobInfoExt");
            cachedSerQNames.add(qName);
            cls = cz.it4i.fiji.haas_java_client.proxy.SubmittedJobInfoExt[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "SubmittedJobInfoExt");
            qName2 = new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "SubmittedJobInfoExt");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "ArrayOfSubmittedTaskInfoExt");
            cachedSerQNames.add(qName);
            cls = cz.it4i.fiji.haas_java_client.proxy.SubmittedTaskInfoExt[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "SubmittedTaskInfoExt");
            qName2 = new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "SubmittedTaskInfoExt");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "ArrayOfTaskSpecificationExt");
            cachedSerQNames.add(qName);
            cls = cz.it4i.fiji.haas_java_client.proxy.TaskSpecificationExt[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "TaskSpecificationExt");
            qName2 = new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "TaskSpecificationExt");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "ClusterNodeTypeExt");
            cachedSerQNames.add(qName);
            cls = cz.it4i.fiji.haas_java_client.proxy.ClusterNodeTypeExt.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "CommandTemplateExt");
            cachedSerQNames.add(qName);
            cls = cz.it4i.fiji.haas_java_client.proxy.CommandTemplateExt.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "CommandTemplateParameterExt");
            cachedSerQNames.add(qName);
            cls = cz.it4i.fiji.haas_java_client.proxy.CommandTemplateParameterExt.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "CommandTemplateParameterValueExt");
            cachedSerQNames.add(qName);
            cls = cz.it4i.fiji.haas_java_client.proxy.CommandTemplateParameterValueExt.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "EnvironmentVariableExt");
            cachedSerQNames.add(qName);
            cls = cz.it4i.fiji.haas_java_client.proxy.EnvironmentVariableExt.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "JobPriorityExt");
            cachedSerQNames.add(qName);
            cls = cz.it4i.fiji.haas_java_client.proxy.JobPriorityExt.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "JobSpecificationExt");
            cachedSerQNames.add(qName);
            cls = cz.it4i.fiji.haas_java_client.proxy.JobSpecificationExt.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "JobStateExt");
            cachedSerQNames.add(qName);
            cls = cz.it4i.fiji.haas_java_client.proxy.JobStateExt.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "SubmittedJobInfoExt");
            cachedSerQNames.add(qName);
            cls = cz.it4i.fiji.haas_java_client.proxy.SubmittedJobInfoExt.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "SubmittedTaskInfoExt");
            cachedSerQNames.add(qName);
            cls = cz.it4i.fiji.haas_java_client.proxy.SubmittedTaskInfoExt.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "TaskSpecificationExt");
            cachedSerQNames.add(qName);
            cls = cz.it4i.fiji.haas_java_client.proxy.TaskSpecificationExt.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "TaskStateExt");
            cachedSerQNames.add(qName);
            cls = cz.it4i.fiji.haas_java_client.proxy.TaskStateExt.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

    }

    protected org.apache.axis.client.Call createCall() throws java.rmi.RemoteException {
        try {
            org.apache.axis.client.Call _call = super._createCall();
            if (super.maintainSessionSet) {
                _call.setMaintainSession(super.maintainSession);
            }
            if (super.cachedUsername != null) {
                _call.setUsername(super.cachedUsername);
            }
            if (super.cachedPassword != null) {
                _call.setPassword(super.cachedPassword);
            }
            if (super.cachedEndpoint != null) {
                _call.setTargetEndpointAddress(super.cachedEndpoint);
            }
            if (super.cachedTimeout != null) {
                _call.setTimeout(super.cachedTimeout);
            }
            if (super.cachedPortName != null) {
                _call.setPortName(super.cachedPortName);
            }
            java.util.Enumeration keys = super.cachedProperties.keys();
            while (keys.hasMoreElements()) {
                java.lang.String key = (java.lang.String) keys.nextElement();
                _call.setProperty(key, super.cachedProperties.get(key));
            }
            // All the type mapping information is registered
            // when the first call is made.
            // The type mapping information is actually registered in
            // the TypeMappingRegistry of the service, which
            // is the reason why registration is only needed for the first call.
            synchronized (this) {
                if (firstCall()) {
                    // must set encoding style before registering serializers
                    _call.setEncodingStyle(null);
                    for (int i = 0; i < cachedSerFactories.size(); ++i) {
                        java.lang.Class cls = (java.lang.Class) cachedSerClasses.get(i);
                        javax.xml.namespace.QName qName =
                                (javax.xml.namespace.QName) cachedSerQNames.get(i);
                        java.lang.Object x = cachedSerFactories.get(i);
                        if (x instanceof Class) {
                            java.lang.Class sf = (java.lang.Class)
                                 cachedSerFactories.get(i);
                            java.lang.Class df = (java.lang.Class)
                                 cachedDeserFactories.get(i);
                            _call.registerTypeMapping(cls, qName, sf, df, false);
                        }
                        else if (x instanceof javax.xml.rpc.encoding.SerializerFactory) {
                            org.apache.axis.encoding.SerializerFactory sf = (org.apache.axis.encoding.SerializerFactory)
                                 cachedSerFactories.get(i);
                            org.apache.axis.encoding.DeserializerFactory df = (org.apache.axis.encoding.DeserializerFactory)
                                 cachedDeserFactories.get(i);
                            _call.registerTypeMapping(cls, qName, sf, df, false);
                        }
                    }
                }
            }
            return _call;
        }
        catch (java.lang.Throwable _t) {
            throw new org.apache.axis.AxisFault("Failure trying to get the Call object", _t);
        }
    }

    public cz.it4i.fiji.haas_java_client.proxy.SubmittedJobInfoExt createJob(cz.it4i.fiji.haas_java_client.proxy.JobSpecificationExt specification, java.lang.String sessionCode) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[0]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://hpcaas.it4i.cz/CreateJob");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "CreateJob"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {specification, sessionCode});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (cz.it4i.fiji.haas_java_client.proxy.SubmittedJobInfoExt) _resp;
            } catch (java.lang.Exception _exception) {
                return (cz.it4i.fiji.haas_java_client.proxy.SubmittedJobInfoExt) org.apache.axis.utils.JavaUtils.convert(_resp, cz.it4i.fiji.haas_java_client.proxy.SubmittedJobInfoExt.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public cz.it4i.fiji.haas_java_client.proxy.SubmittedJobInfoExt submitJob(long createdJobInfoId, java.lang.String sessionCode) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[1]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://hpcaas.it4i.cz/SubmitJob");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "SubmitJob"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {new java.lang.Long(createdJobInfoId), sessionCode});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (cz.it4i.fiji.haas_java_client.proxy.SubmittedJobInfoExt) _resp;
            } catch (java.lang.Exception _exception) {
                return (cz.it4i.fiji.haas_java_client.proxy.SubmittedJobInfoExt) org.apache.axis.utils.JavaUtils.convert(_resp, cz.it4i.fiji.haas_java_client.proxy.SubmittedJobInfoExt.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public cz.it4i.fiji.haas_java_client.proxy.SubmittedJobInfoExt cancelJob(long submittedJobInfoId, java.lang.String sessionCode) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[2]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://hpcaas.it4i.cz/CancelJob");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "CancelJob"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {new java.lang.Long(submittedJobInfoId), sessionCode});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (cz.it4i.fiji.haas_java_client.proxy.SubmittedJobInfoExt) _resp;
            } catch (java.lang.Exception _exception) {
                return (cz.it4i.fiji.haas_java_client.proxy.SubmittedJobInfoExt) org.apache.axis.utils.JavaUtils.convert(_resp, cz.it4i.fiji.haas_java_client.proxy.SubmittedJobInfoExt.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public void deleteJob(long submittedJobInfoId, java.lang.String sessionCode) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[3]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://hpcaas.it4i.cz/DeleteJob");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "DeleteJob"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {new java.lang.Long(submittedJobInfoId), sessionCode});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        extractAttachments(_call);
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public cz.it4i.fiji.haas_java_client.proxy.SubmittedJobInfoExt[] listJobsForCurrentUser(java.lang.String sessionCode) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[4]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://hpcaas.it4i.cz/ListJobsForCurrentUser");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "ListJobsForCurrentUser"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {sessionCode});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (cz.it4i.fiji.haas_java_client.proxy.SubmittedJobInfoExt[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (cz.it4i.fiji.haas_java_client.proxy.SubmittedJobInfoExt[]) org.apache.axis.utils.JavaUtils.convert(_resp, cz.it4i.fiji.haas_java_client.proxy.SubmittedJobInfoExt[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public cz.it4i.fiji.haas_java_client.proxy.SubmittedJobInfoExt getCurrentInfoForJob(long submittedJobInfoId, java.lang.String sessionCode) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[5]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://hpcaas.it4i.cz/GetCurrentInfoForJob");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://hpcaas.it4i.cz/", "GetCurrentInfoForJob"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {new java.lang.Long(submittedJobInfoId), sessionCode});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (cz.it4i.fiji.haas_java_client.proxy.SubmittedJobInfoExt) _resp;
            } catch (java.lang.Exception _exception) {
                return (cz.it4i.fiji.haas_java_client.proxy.SubmittedJobInfoExt) org.apache.axis.utils.JavaUtils.convert(_resp, cz.it4i.fiji.haas_java_client.proxy.SubmittedJobInfoExt.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

}
