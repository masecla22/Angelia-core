package com.github.maxopoly.angeliacore.libs.yaml.config;

import java.io.File;

import org.apache.logging.log4j.Logger;

import com.github.maxopoly.angeliacore.connection.ServerConnection;
import com.github.maxopoly.angeliacore.model.block.ChunkHolder;

public class GlobalConfig extends YAMLFileConfig {

	private ServerConnection connection;

	public GlobalConfig(ServerConnection connection, Logger logger, File dataFolder) {
		super(logger, new File(dataFolder, "globalConfig.yml"), "/globalDefaultConfig.yml");
		this.connection = connection;
		if (!configFile.exists()) {
			logger.info("Global config file did not exist, creating it");
			saveDefaultConfig();
		}
		reloadConfig();
	}

	public long getAuthReconnectDelay() {
		return config.getInt("connection.reconnect_delay", 30 * 1000);
	}

	public long getTokenRefreshDelay() {
		return config.getInt("auth.refresh_delay", 60 * 5 * 1000);
	}

	public boolean holdBlockModel() {
		return config.getBoolean("block.hold_model", false);
	}

	public void setHoldBlockModel(boolean state) {
		if (holdBlockModel() == state) {
			return;
		}
		config.putBoolean("block.hold_model", state);
		ChunkHolder chunkHolder = connection.getChunkHolder();
		if (chunkHolder == null) {
			// connection isnt setup yet
			return;
		}
		chunkHolder.setActivationState(state);
		setDirty(true);
	}
	
	public double getPhysicsDelta() {
		return config.getDouble("physics.delta", 1E-8);
	}
	
	public double getGravity() {
		return config.getDouble("physics.gravity", 32);
	}
	
	public double getPhysicsDrag() {
		return config.getDouble("physics.drag", 0.02);
	}
	
	public double getTerminalVelocity() {
		return config.getDouble("physics.terminal_velocity", -20);
	}
	
	public double getPhysicsWalkingSpeed() {
		return config.getDouble("physics.walking_speed", 4.317);
	}
	
	public double getSprintingSpeed() {
		return getPhysicsWalkingSpeed() * getPhysicsSprintingMultiplier();
	}
	
	public double getPhysicsSprintingMultiplier() {
		return config.getDouble("physics.sprinting_multiplier", 1.3);
	}
	
	public double getPhysicsPlayerHeight() {
		return config.getDouble("physics.player_height", 1.74);
	}
	
	public double getPlayerAABBRadius() {
		return config.getDouble("physics.player_radius", 0.3);
	}

	public boolean useAutoReconnect() {
		return config.getBoolean("connection.auto_reconnect", false);
	}
	
	public boolean getPacketDebug() {
		return config.getBoolean("dev.packet_debug", false);
	}

}
