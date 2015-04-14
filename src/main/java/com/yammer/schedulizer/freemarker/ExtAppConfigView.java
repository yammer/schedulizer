package com.yammer.schedulizer.freemarker;

import com.yammer.schedulizer.auth.ExtAppType;
import io.dropwizard.views.View;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ExtAppConfigView extends View {

    private ExtAppType extApp;
    private String extAppClientId;

    public ExtAppConfigView(ExtAppType extApp, String extAppClientId) {
        super("ext_app_config.ftl");
        this.extApp = extApp;
        this.extAppClientId = extAppClientId;
    }

    public String getExtApp() {
        return extApp.toString();
    }

    public List<String> getExtAppTypes() {
        List<ExtAppType> extAppTypes = Arrays.asList(ExtAppType.values());
        return extAppTypes.stream().map(ExtAppType::toString).collect(Collectors.toList());
    }

    public String getExtAppClientId() {
        return extAppClientId;
    }
}
