package com.vurses.offlineauth;

import com.zenith.plugin.api.Plugin;
import com.zenith.plugin.api.PluginAPI;
import com.zenith.plugin.api.ZenithProxyPlugin;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import com.vurses.offlineauth.module.OfflineAuthModule;

@Plugin(
    id = BuildConstants.PLUGIN_ID,
    version = BuildConstants.VERSION,
    description = "Offline Auth Plugin",
    url = "https://github.com/rfresh2/ZenithProxyOfflineAuthPlugin",
    authors = {"rfresh2"},
    mcVersions = {BuildConstants.MC_VERSION} // to indicate any MC version: @Plugin(mcVersions = "*")
)
public class OfflineAuthPlugin implements ZenithProxyPlugin {
    // public static for simple access from modules and commands
    // or alternatively, you could pass these around in constructors
    public static OfflineAuthConfig PLUGIN_CONFIG;
    public static ComponentLogger LOG;

    @Override
    public void onLoad(PluginAPI pluginAPI) {
        LOG = pluginAPI.getLogger();
        LOG.info("Offline Auth Plugin loading...");
        // initialize any configurations before modules or commands might need to read them
        PLUGIN_CONFIG = pluginAPI.registerConfig(BuildConstants.PLUGIN_ID, OfflineAuthConfig.class);
        pluginAPI.registerModule(new OfflineAuthModule());

        LOG.info("Offline Auth Plugin loaded!");
    }
}
