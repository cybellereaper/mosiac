package imageboard.providers.gelbooru;

import imageboard.auth.AuthConfig;
import imageboard.internal.HttpExecutor;
import imageboard.model.ProviderCapabilities;
import imageboard.providers.dapi.AbstractDapiXmlAdapter;

public final class GelbooruAdapter extends AbstractDapiXmlAdapter {
    public GelbooruAdapter(String baseUrl, HttpExecutor executor, AuthConfig auth) {
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
        return "gelbooru";
    }
}
