package com.github.maxopoly.angeliacore.model;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import com.github.maxopoly.angeliacore.connection.ServerConnection;
import com.github.maxopoly.angeliacore.event.events.player.XPChangeEvent;
import com.github.maxopoly.angeliacore.model.entity.AABB;
import com.github.maxopoly.angeliacore.model.entity.LivingEntity;
import com.github.maxopoly.angeliacore.model.inventory.Inventory;
import com.github.maxopoly.angeliacore.model.inventory.PlayerInventory;
import com.github.maxopoly.angeliacore.model.location.DirectedLocation;
import com.github.maxopoly.angeliacore.model.location.Vector;
import com.github.maxopoly.angeliacore.model.player.GameMode;

public class ThePlayer extends LivingEntity {

	public static final double HEAD_DELTA = 1.62; // how far the center of the head is above the player location
	private static final Vector headOffSet = new Vector(0, HEAD_DELTA, 0);

	private boolean initialized = false;
	private Map<Byte, Inventory> openInventories;

	private int selectedHotbarSlot;

	private int totalEXP;
	private float xpProgress;
	private int level;

	private AABB boundingBox;

	private int hunger;
	private float saturation;
	private boolean isSprinting;
	private boolean isSneaking;
	private GameMode gamemode;
	private boolean hardCore;
	private boolean isDead;

	private ServerConnection connection;

	public ThePlayer(ServerConnection connection, String uuid) {
		super(0, convertUUIDFromDashless(uuid), new DirectedLocation(), new Vector());
		this.openInventories = new TreeMap<>();
		this.openInventories.put((byte) 0, new PlayerInventory());
		this.connection = connection;
		double playerRadius = connection.getConfig().getPlayerAABBRadius();
		this.boundingBox = new AABB(-playerRadius, playerRadius, 0, connection.getConfig().getPhysicsPlayerHeight(),
				-playerRadius, playerRadius);
	}

	public void addInventory(Inventory inv, byte id) {
		openInventories.put(id, inv);
	}

	public DirectedLocation getHeadLocation() {
		return new DirectedLocation(location.add(headOffSet), location.getYaw(), location.getPitch());
	}

	/**
	 * @return Player hunger
	 */
	public int getHunger() {
		return hunger;
	}

	public Inventory getInventory(byte id) {
		if (id == -1) {
			// -1 is used to access the cursor slot, so we can just use the
			// player inventory, which will always be
			// present
			id = 0;
		}
		return openInventories.get(id);
	}

	/**
	 * @return Current player level
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * @return Players inventory
	 */
	public PlayerInventory getPlayerInventory() {
		return (PlayerInventory) openInventories.get((byte) 0);
	}

	/**
	 * @return Players hunger saturation
	 */
	public float getSaturation() {
		return saturation;
	}

	/**
	 * @return Slot the player has selected
	 */
	public int getSelectedHotbarSlot() {
		return selectedHotbarSlot;
	}

	/**
	 * @return Total amount of XP units the player has
	 */
	public int getTotalXP() {
		return totalEXP;
	}

	/**
	 * @return How close the player is to leveling up in a range from 0.0 to 1.0
	 */
	public float getXPProgress() {
		return xpProgress;
	}

	/**
	 * @return Whether the location was initialized yet
	 */
	public boolean isInitialized() {
		return initialized;
	}

	public void removeOpenInventory(byte id) {
		// always keep player inventory
		if (id != 0) {
			openInventories.remove(id);
		}
	}

	public void setInitialized() {
		initialized = true;
	}

	public void setPlayerEntityID(int id) {
		this.id = id;
	}

	public void setSelectedHotbarSlot(int slot) {
		if (slot < 0 || slot > 8) {
			throw new IllegalArgumentException("Slot must be in [0-8]");
		}
		this.selectedHotbarSlot = slot;
	}

	public void updateHunger(int hunger, float saturation) {
		this.hunger = hunger;
		this.saturation = saturation;
	}

	public void updateXP(float progress, int level, int totalXP) {
		connection.getEventHandler()
				.broadcast(new XPChangeEvent(this.xpProgress, this.level, this.totalEXP, progress, level, totalXP));
		this.level = level;
		this.xpProgress = progress;
		this.totalEXP = totalXP;
	}

	public void setSprinting(boolean sprinting) {
		this.isSprinting = sprinting;
	}

	public boolean isSprinting() {
		return isSprinting;
	}

	public boolean isSneaking() {
		return isSneaking;
	}

	public void setSneaking(boolean sneaking) {
		this.isSneaking = sneaking;
	}

	/**
	 * Updates the players game mode
	 * 
	 * @param gameMode Game mode to update to
	 */
	public void setGameMode(GameMode gameMode) {
		this.gamemode = gameMode;
	}

	/**
	 * @return Game mode the player is in
	 */
	public GameMode getGameMode() {
		return gamemode;
	}

	/**
	 * @return Whether the player is playing hard core (perma death)
	 */
	public boolean isHardCore() {
		return hardCore;
	}

	/**
	 * Updates whether the player is playing hard core (perma death)
	 * 
	 * @param hardCore Updated hard core state
	 */
	public void setHardcore(boolean hardCore) {
		this.hardCore = hardCore;
	}

	/**
	 * Updates whether the player is currently dead
	 * 
	 * @param dead Is the player dead
	 */
	public void setDead(boolean dead) {
		this.isDead = dead;
	}

	/**
	 * A player may be dead even if their HP are not <= 0, because the server may
	 * change their HP after dying, but before respawning. This check properly
	 * respects that and should be used to determine whether a player is dead, not
	 * the current health
	 * 
	 * @return Whether the player is currently dead
	 */
	public boolean isDead() {
		return isDead;
	}

	@Override
	public AABB getBoundingBox() {
		return boundingBox;
	}

	public static UUID convertUUIDFromDashless(String withoutDash) {
		return UUID.fromString(
				withoutDash.substring(0, 8) + "-" + withoutDash.substring(8, 12) + "-" + withoutDash.substring(12, 16)
						+ "-" + withoutDash.substring(16, 24) + "-" + withoutDash.substring(24, 32));
	}
}
