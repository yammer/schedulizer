package com.yammer.schedulizer.resources;

import com.yammer.schedulizer.auth.ExtAppType;
import com.yammer.schedulizer.freemarker.ExtAppConfigView;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Produces({MediaType.TEXT_HTML})
@Path("/ext-app")
public class ExtAppConfigResource {
    private ExtAppType extAppType;
    private String extAppClientId;

    public ExtAppConfigResource(ExtAppType extAppType, String extAppClientId) {
        this.extAppType = extAppType;
        this.extAppClientId = extAppClientId;
    }

    @GET
    @Path("config.html")
    public ExtAppConfigView getConfig() {
        return new ExtAppConfigView(extAppType, extAppClientId);
    }
}
