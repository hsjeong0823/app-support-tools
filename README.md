# app-support-tools

## Runtime environment integration

- Add the library with `debugImplementation` so support tooling stays out of release builds.
- The selected environment is stored in the SupportTools preference.
- Generate the debug environment JSON from the host's build-time source of truth, such as `uri.properties`; do not maintain a second set of URLs in the library.
- Connect the resolver once, at the host URL builder's final authority decision point.
- Install the environment config and resolver from a debug-only `ContentProvider` that runs before the host `Application`.
- Saving an environment restarts the host app. The library intentionally does not clear sessions, tokens, or cookies.
- Release variants must exclude the Provider, generated JSON, and SupportTools dependency. Missing, invalid, or unmatched debug configuration falls back to the host's original authority.
