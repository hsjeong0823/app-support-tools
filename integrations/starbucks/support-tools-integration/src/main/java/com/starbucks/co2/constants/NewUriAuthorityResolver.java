package com.starbucks.co2.constants;

public final class NewUriAuthorityResolver {
    public interface Resolver {
        String resolve(String originalAuthority);
    }

    private static final Resolver IDENTITY = value -> value;
    private static volatile Resolver resolver = IDENTITY;

    private NewUriAuthorityResolver() {}

    public static void install(Resolver value) {
        resolver = value != null ? value : IDENTITY;
    }

    public static String resolve(String originalAuthority) {
        try {
            String resolved = resolver.resolve(originalAuthority);
            return resolved != null ? resolved : originalAuthority;
        } catch (Throwable ignored) {
            return originalAuthority;
        }
    }
}
