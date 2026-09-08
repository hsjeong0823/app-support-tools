package com.starbucks.co2.network.core;

import android.content.Context;

import okhttp3.OkHttpClient;

public final class SupportToolsNetworkBridge {
    public interface Installer {
        void install(Context context, OkHttpClient.Builder builder);
    }

    private static final Installer NO_OP = (context, builder) -> {};
    private static volatile Installer installer = NO_OP;

    private SupportToolsNetworkBridge() {}

    public static void install(Installer value) {
        installer = value != null ? value : NO_OP;
    }

    public static void apply(Context context, OkHttpClient.Builder builder) {
        try {
            installer.install(context, builder);
        } catch (Throwable ignored) {
            // Host networking must remain available when support-tools is unavailable.
        }
    }
}
