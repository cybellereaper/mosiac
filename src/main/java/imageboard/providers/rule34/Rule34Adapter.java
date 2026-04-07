package imageboard.providers.rule34;

import imageboard.auth.AuthConfig;
import imageboard.internal.HttpExecutor;
import imageboard.model.ProviderCapabilities;
import imageboard.providers.dapi.AbstractDapiXmlAdapter;

public final class Rule34Adapter extends AbstractDapiXmlAdapter {
    public Rule34Adapter(String baseUrl, HttpExecutor executor, AuthConfig auth) {
        super(baseUrl, executor);
    }

    @Override
    public String providerId() {
        return providerKey();
    }

    @Override
    public ProviderCapabilities capabilities() {
        return DAPI_CAPABILITIES;
    }

    @Override
    protected String providerKey() {
        return "rule34";
    }
}
