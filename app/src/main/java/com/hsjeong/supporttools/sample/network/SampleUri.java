package com.hsjeong.supporttools.sample.network;

public final class SampleUri {
    private SampleUri() {}

    public static final class Api {
        public static final String baseUrl = new Builder()
                .setAuthority("dev-api.example.test")
                .build();
        public static final String DERIVED_URL = baseUrl + "v1/items";

        private Api() {}
    }

    public static final class Builder {
        private String scheme = "https";
        private String authority;
        private String path;

        public Builder setScheme(String scheme) {
            this.scheme = scheme;
            return this;
        }

        public Builder setAuthority(String authority) {
            this.authority = authority;
            return this;
        }

        public Builder setPath(String path) {
            this.path = path;
            return this;
        }

        public String build() {
            StringBuilder uri = new StringBuilder();
            if (scheme != null) {
                uri.append(scheme).append(":");
            }
            if (authority != null) {
                uri.append("//").append(SampleAuthorityResolverRegistry.resolve(authority));
            }
            if (path != null) {
                uri.append(path);
            }
            return uri.toString();
        }
    }
}
