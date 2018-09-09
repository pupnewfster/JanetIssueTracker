package gg.galaxygaming.janet.Discord;

import gg.galaxygaming.janet.Config;
import gg.galaxygaming.janet.Janet;
import gg.galaxygaming.janet.api.AbstractIntegration;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.server.Server;

import javax.annotation.Nonnull;

/**
 * An implementation of {@link gg.galaxygaming.janet.api.Integration} to connect to the Discord server.
 */
public final class DiscordIntegration extends AbstractIntegration {
    private final long serverID, devChannel;
    private final String authMessage;
    private DiscordApi api;
    private DiscordListener listeners;
    private Server server;

    public DiscordIntegration() {
        super();
        Config config = Janet.getConfig();
        String token = config.getOrDefault("DISCORD_TOKEN", "token");
        this.serverID = config.getOrDefault("DISCORD_SERVER", -1L);
        this.authMessage = config.getOrDefault("DISCORD_AUTH_MESSAGE", "Go authenticate your account.");
        this.devChannel = config.getOrDefault("DISCORD_DEV_CHANNEL", -1L);
        if (token.equals("token") || this.serverID < 0 || this.devChannel < 0) {
            Janet.getLogger().error("Failed to load needed configs for Discord Integration");
            return;
        }
        new DiscordApiBuilder().setToken(token).login().thenAccept(api -> {
            this.api = api;
            //Login successful
            finishConnect();
        });
    }

    public void stop() {
        super.stop();
        this.api.disconnect();
    }

    private void finishConnect() {
        Janet.getLogger().info("Discord connected, registering listeners...");
        this.listeners = new DiscordListener();
        this.api.addMessageCreateListener(this.listeners);
        this.api.addServerMemberJoinListener(this.listeners);
        Janet.getLogger().info("Discord listeners registered.");
        this.api.getServerById(this.serverID).ifPresent(server -> this.server = server);
        this.api.updateActivity(ActivityType.WATCHING, "over the autists.");
        this.mysql = new DiscordMySQL();
    }

    /**
     * @return The id of the developer channel.
     */
    public long getDevChannel() {
        return this.devChannel;
    }

    /**
     * Retrieves the {@link Server} instance this {@link DiscordIntegration} is connected to.
     * @return The {@link Server} instance this {@link DiscordIntegration} is connected to.
     */
    public Server getServer() {
        return this.server;
    }

    /**
     * Retrieves the {@link DiscordApi} this {@link DiscordIntegration} is logged in as.
     * @return The {@link DiscordApi} this {@link DiscordIntegration} is logged in as.
     */
    public DiscordApi getApi() {
        return this.api;
    }

    /**
     * Retrieves the message to send new users when they join telling them to authenticate.
     * @return The message to send new users when they join telling them to authenticate.
     */
    @Nonnull
    public String getAuthMessage() {
        return this.authMessage;
    }
}