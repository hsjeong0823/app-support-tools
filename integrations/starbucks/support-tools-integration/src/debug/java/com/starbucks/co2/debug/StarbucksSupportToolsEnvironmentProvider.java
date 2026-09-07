package com.starbucks.co2.debug;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.hsjeong.supporttools.SupportTools;
import com.starbucks.co.R;
import com.starbucks.co2.constants.NewUriAuthorityResolver;

public final class StarbucksSupportToolsEnvironmentProvider extends ContentProvider {
    @Override
    public boolean onCreate() {
        Context context = getContext();
        if (context != null && SupportTools.loadEnvironmentConfig(
                context, R.raw.support_tools_environments)) {
            Context appContext = context.getApplicationContext();
            NewUriAuthorityResolver.install(
                    original -> SupportTools.resolveAuthority(appContext, original));
        }
        return true;
    }

    @Override public Cursor query(
            Uri uri,
            String[] projection,
            String selection,
            String[] selectionArgs,
            String sortOrder
    ) { return null; }

    @Override public String getType(Uri uri) { return null; }

    @Override public Uri insert(Uri uri, ContentValues values) { return null; }

    @Override public int delete(Uri uri, String selection, String[] selectionArgs) { return 0; }

    @Override public int update(
            Uri uri,
            ContentValues values,
            String selection,
            String[] selectionArgs
    ) { return 0; }
}
