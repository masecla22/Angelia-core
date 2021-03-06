package com.github.maxopoly.angeliacore.connection.play.packets.in;

import com.github.maxopoly.angeliacore.connection.ServerConnection;
import com.github.maxopoly.angeliacore.libs.packetEncoding.EndOfPacketException;
import com.github.maxopoly.angeliacore.libs.packetEncoding.ReadOnlyPacket;

public class XPChangeHandler extends AbstractIncomingPacketHandler {

	public XPChangeHandler(ServerConnection connection) {
		super(connection, 0x40);
	}

	@Override
	public void handlePacket(ReadOnlyPacket packet) {
		try {
			float progress = packet.readFloat();
			int level = packet.readVarInt();
			int totalXP = packet.readVarInt();
			connection.getPlayerStatus().updateXP(progress, level, totalXP);
		} catch (EndOfPacketException e) {
			connection.getLogger().error("Failed to parse XP update packet", e);
		}
	}

}
