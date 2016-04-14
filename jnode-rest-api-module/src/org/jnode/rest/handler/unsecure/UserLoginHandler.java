package org.jnode.rest.handler.unsecure;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.util.NamedParamsRetriever;
import org.jnode.rest.core.CryptoUtils;
import org.jnode.rest.core.RPCError;
import org.jnode.rest.db.RestUser;
import org.jnode.rest.di.Inject;
import org.jnode.rest.di.Named;
import org.jnode.rest.fido.RestUserProxy;
import org.jnode.rest.handler.AbstractHandler;

public class UserLoginHandler extends AbstractHandler {

    @Inject
    @Named("restUserProxy")
    private RestUserProxy restUserProxy;

    @Override
    protected JSONRPC2Response createJsonrpc2Response(Object reqID, NamedParamsRetriever np) throws JSONRPC2Error {

        final String login = np.getString("login", false);

        if (login == null || login.isEmpty()){
            return new JSONRPC2Response(RPCError.EMPTY_LOGIN, reqID);
        }

        RestUser restUser = restUserProxy.findByUserCredentials(login, np.getString("password", false));

        final String pwd = CryptoUtils.randomToken();

        if (restUser == null){
            return new JSONRPC2Response(RPCError.BAD_CREDENTIALS, reqID);
        }

        restUser.setToken(CryptoUtils.makeToken(pwd));
        restUserProxy.update(restUser);

        return new JSONRPC2Response(pwd, reqID);

    }

    @Override
    public String[] handledRequests() {
        return new String[]{"user.login"};
    }

    public void setRestUserProxy(RestUserProxy restUserProxy) {
        this.restUserProxy = restUserProxy;
    }


}
