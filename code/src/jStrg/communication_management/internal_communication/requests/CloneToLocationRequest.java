package jStrg.communication_management.internal_communication.requests;

import jStrg.communication_management.internal_communication.core.InternalRequest;
import jStrg.network_management.storage_management.internal.StorageServer;

/**
 * Request format to make the subserver migrate to another storage cell or location by master
 */
public class CloneToLocationRequest extends InternalRequest {
    public CloneToLocationRequest(InternalRequest.type _type, String _username, String _password, StorageServer _sub_server) {
        super(_type, _sub_server);
    }
}
