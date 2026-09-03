package com.hsjeong.supporttools.sample.network;

public final class SampleAuthorityResolverRegistry {
    public interface AuthorityResolver {
        String resolve(String originalAuthority);
    }

    private static final AuthorityResolver IDENTITY = value -> value;
    private static volatile AuthorityResolver resolver = IDENTITY;

    private SampleAuthorityResolverRegistry() {}

    public static void install(AuthorityResolver value) {
        resolver = value != null ? value : IDENTITY;
    }

    public static String resolve(String original) {
        try {
            String resolved = resolver.resolve(original);
            return resolved != null ? resolved : original;
        } catch (Throwable ignored) {
            return original;
        }
    }
}
