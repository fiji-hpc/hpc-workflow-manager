
/**
 * ExtensionMapper.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.7.6  Built on : Jul 30, 2017 (09:08:58 BST)
 */

        
            package cz.it4i.hpcaas;
        
            /**
            *  ExtensionMapper class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class ExtensionMapper{

          public static java.lang.Object getTypeObject(java.lang.String namespaceURI,
                                                       java.lang.String typeName,
                                                       javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{

              
                  if (
                  "http://hpcaas.it4i.cz/".equals(namespaceURI) &&
                  "ArrayOfByte".equals(typeName)){
                   
                            return  cz.it4i.hpcaas.ArrayOfByte.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://hpcaas.it4i.cz/".equals(namespaceURI) &&
                  "CommandTemplateExt".equals(typeName)){
                   
                            return  cz.it4i.hpcaas.CommandTemplateExt.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://hpcaas.it4i.cz/".equals(namespaceURI) &&
                  "PasswordCredentialsExt".equals(typeName)){
                   
                            return  cz.it4i.hpcaas.PasswordCredentialsExt.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://hpcaas.it4i.cz/".equals(namespaceURI) &&
                  "ResourceUsageExt".equals(typeName)){
                   
                            return  cz.it4i.hpcaas.ResourceUsageExt.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://hpcaas.it4i.cz/".equals(namespaceURI) &&
                  "ArrayOfResourceUsageExt".equals(typeName)){
                   
                            return  cz.it4i.hpcaas.ArrayOfResourceUsageExt.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://hpcaas.it4i.cz/".equals(namespaceURI) &&
                  "DigitalSignatureCredentialsExt".equals(typeName)){
                   
                            return  cz.it4i.hpcaas.DigitalSignatureCredentialsExt.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://hpcaas.it4i.cz/".equals(namespaceURI) &&
                  "ResourceLimitationExt".equals(typeName)){
                   
                            return  cz.it4i.hpcaas.ResourceLimitationExt.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://hpcaas.it4i.cz/".equals(namespaceURI) &&
                  "AuthenticationCredentialsExt".equals(typeName)){
                   
                            return  cz.it4i.hpcaas.AuthenticationCredentialsExt.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://hpcaas.it4i.cz/".equals(namespaceURI) &&
                  "ArrayOfCommandTemplateExt".equals(typeName)){
                   
                            return  cz.it4i.hpcaas.ArrayOfCommandTemplateExt.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://hpcaas.it4i.cz/".equals(namespaceURI) &&
                  "ClusterNodeTypeExt".equals(typeName)){
                   
                            return  cz.it4i.hpcaas.ClusterNodeTypeExt.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://hpcaas.it4i.cz/".equals(namespaceURI) &&
                  "ArrayOfCommandTemplateParameterExt".equals(typeName)){
                   
                            return  cz.it4i.hpcaas.ArrayOfCommandTemplateParameterExt.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://hpcaas.it4i.cz/".equals(namespaceURI) &&
                  "CommandTemplateParameterExt".equals(typeName)){
                   
                            return  cz.it4i.hpcaas.CommandTemplateParameterExt.Factory.parse(reader);
                        

                  }

              
             throw new org.apache.axis2.databinding.ADBException("Unsupported type " + namespaceURI + " " + typeName);
          }

        }
    