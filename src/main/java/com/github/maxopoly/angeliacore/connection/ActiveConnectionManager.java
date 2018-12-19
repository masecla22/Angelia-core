package com.github.maxopoly.angeliacore.connection;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.github.maxopoly.angeliacore.connection.login.Auth403Exception;
import com.github.maxopoly.angeliacore.event.events.angelia.ReconnectEvent;

public class ActiveConnectionManager {

	private static ActiveConnectionManager instance;
	private Map<String, ServerConnection> activeConnections = new ConcurrentHashMap<String, ServerConnection>();

	public static ActiveConnectionManager getInstance() {
		if (instance == null) {
			instance = new ActiveConnectionManager();
		}
		return instance;
	}

	private ActiveConnectionManager() {
	}

	public void initConnection(ServerConnection newConnection, ServerConnection oldConnection) {
		try {
			newConnection.connect();
		} catch (Auth403Exception e) {
			newConnection.getLogger().error("Auth was invalid, no automated reconnecting possible", e);
			System.exit(0);
		} catch (Exception e) {
			newConnection.getLogger().error("Could not connect to server", e);
			if (oldConnection == null) {
				scheduleConnectionReattempt(newConnection);
			} else {
				scheduleConnectionReattempt(oldConnection);
			}
			return;
		}
		// block until everything is ready
		while (!newConnection.getPlayerStatus().isInitialized()) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// how even
				e.printStackTrace();
			}
		}
		activeConnections.put(newConnection.getPlayerName(), newConnection);
		if (oldConnection != null) {
			oldConnection.getEventHandler().transferListeners(newConnection.getEventHandler());
			oldConnection.getPluginManager().passPluginsOver(newConnection);
		}
		newConnection.getEventHandler().broadcast(new ReconnectEvent(oldConnection, newConnection));
	}

	private void scheduleConnectionReattempt(final ServerConnection failed) {
		if (!failed.getConfig().useAutoReconnect()) {
			failed.getLogger().info("Autoreconnecting is disabled, connection will remain closed");
			System.exit(0);
			return;
		}
		else {
			failed.getLogger().info("Autoreconnecting in " + failed.getConfig().getAuthReconnectDelay() + " ms");
		}
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.schedule(new Runnable() {

			@Override
			public void run() {
				ServerConnection conn = new ServerConnection(failed.getAdress(), failed.getPort(), failed.getLogger(),
						failed.getAuthHandler());
				initConnection(conn, failed);

			}
		}, failed.getConfig().getAuthReconnectDelay(), TimeUnit.MILLISECONDS);
	}

	public ServerConnection getConnection(String playerName) {
		return activeConnections.get(playerName);
	}

	public void reportDisconnect(ServerConnection conn, DisconnectReason reason) {
		synchronized (activeConnections) {
			if (!activeConnections.values().contains(conn)) {
				// disconnect may be reported in multiple layers, we only want the first one to count
				return;
			}
			for (Entry<String, ServerConnection> entry : activeConnections.entrySet()) {
				if (entry.getValue() == conn) {
					activeConnections.remove(entry.getKey());
					break;
				}
			}
		}
		switch (reason) {
			case Critial_Exception:
				conn.getLogger().info("Exiting, because continuing is no longer possible");
				System.exit(1);
				break;
			case Intentional_Disconnect:
				// whoever called this is expected to tell the user why we are exiting
				System.exit(0);
				break;
			case Unknown_Connection_Error:
				conn.getLogger().info(
						"Server connection was closed for unknown reasons!");
				break;
			case Server_Disconnected_Intentionally:
				conn.getLogger().info("Server disconnected us!");
				break;
			case Server_Timed_Out:
				conn.getLogger().info("Server connection timed out!");
				break;
		}
		scheduleConnectionReattempt(conn);
	}
}
