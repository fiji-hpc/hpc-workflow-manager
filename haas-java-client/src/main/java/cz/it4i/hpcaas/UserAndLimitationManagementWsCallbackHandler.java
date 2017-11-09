
/**
 * UserAndLimitationManagementWsCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.7.6  Built on : Jul 30, 2017 (09:08:31 BST)
 */

    package cz.it4i.hpcaas;

    /**
     *  UserAndLimitationManagementWsCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class UserAndLimitationManagementWsCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public UserAndLimitationManagementWsCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public UserAndLimitationManagementWsCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
           /**
            * auto generated Axis2 call back method for authenticateUserDigitalSignature method
            * override this method for handling normal response from authenticateUserDigitalSignature operation
            */
           public void receiveResultauthenticateUserDigitalSignature(
                    cz.it4i.hpcaas.AuthenticateUserDigitalSignatureResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from authenticateUserDigitalSignature operation
           */
            public void receiveErrorauthenticateUserDigitalSignature(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getCurrentUsageAndLimitationsForCurrentUser method
            * override this method for handling normal response from getCurrentUsageAndLimitationsForCurrentUser operation
            */
           public void receiveResultgetCurrentUsageAndLimitationsForCurrentUser(
                    cz.it4i.hpcaas.GetCurrentUsageAndLimitationsForCurrentUserResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getCurrentUsageAndLimitationsForCurrentUser operation
           */
            public void receiveErrorgetCurrentUsageAndLimitationsForCurrentUser(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for authenticateUserPassword method
            * override this method for handling normal response from authenticateUserPassword operation
            */
           public void receiveResultauthenticateUserPassword(
                    cz.it4i.hpcaas.AuthenticateUserPasswordResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from authenticateUserPassword operation
           */
            public void receiveErrorauthenticateUserPassword(java.lang.Exception e) {
            }
                


    }
    