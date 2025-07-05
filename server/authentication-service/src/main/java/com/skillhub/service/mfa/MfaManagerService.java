package com.skillhub.service.mfa;

import com.skillhub.entity.MfaMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MfaManagerService {
    private final Map<MfaMethod, MfaProvider> providers;

    /**
     * Constructor that initializes the MfaManagerService with a list of MfaProvider instances.
     * The providers are collected into a map where the key is the MfaMethod and the value is the MfaProvider.
     *
     * @param providersList a list of MfaProvider instances to be managed
     */
    @Autowired
    public MfaManagerService(List<MfaProvider> providersList) {
        this.providers = providersList.stream().collect(Collectors.toMap(MfaProvider::getMethod, provider -> provider));
    }

    /**
     * Retrieves the MfaProvider for the specified MfaMethod.
     *
     * @param method the MfaMethod for which to retrieve the provider
     * @return the MfaProvider associated with the specified method
     */
    public MfaProvider getProvider(MfaMethod method) {
        return providers.get(method);
    }
}
