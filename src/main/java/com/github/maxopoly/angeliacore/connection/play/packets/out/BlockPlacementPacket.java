package com.github.maxopoly.angeliacore.connection.play.packets.out;

import java.io.IOException;

import com.github.maxopoly.angeliacore.libs.packetEncoding.WriteOnlyPacket;
import com.github.maxopoly.angeliacore.model.location.BlockFace;
import com.github.maxopoly.angeliacore.model.location.Location;

public class BlockPlacementPacket extends WriteOnlyPacket {

	public BlockPlacementPacket(Location blockLoc, BlockFace face) throws IOException {
		this(blockLoc, face, face.placementVectorX(), face.placementVectorY(), face.placementVectorZ());
	}

	public BlockPlacementPacket(Location blockLoc, BlockFace face, int cursorX, int cursorY, int cursorZ)
			throws IOException {
		super(0x1F);
		writePosition((int) blockLoc.getX(), (int) blockLoc.getY(), (int) blockLoc.getZ());
		writeByte(face.getEnumByte());
		writeVarInt(0);
		writeFloat(0.5f);
		writeFloat(0.5f);
		writeFloat(0.5f);
	}

}
