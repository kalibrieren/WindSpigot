package com.windpvp.windspigot.antixray;

/**
 * Minecraft 1.8 map-chunk block encoding helpers.
 * <p>
 * Each block in the chunk payload is a little-endian {@code char}:
 * bits 0–3 = metadata, bits 4–15 = block id (0–4095). There is no separate
 * "Add" nibble array as in pre-1.8; extended ids live in those upper 12 bits.
 */
public final class ChunkBlockIds {

	/** Maximum numeric block id representable in the 1.8 chunk wire format. */
	public static final int MAX_BLOCK_ID = 4095;

	private ChunkBlockIds() {
	}

	/**
	 * @return true if {@code id} is in {@code [0, MAX_BLOCK_ID]}
	 */
	public static boolean isValidBlockId(int id) {
		return id >= 0 && id <= MAX_BLOCK_ID;
	}

	/**
	 * Byte offset of the little-endian char for block slot {@code blockIndex}.
	 */
	public static int byteOffset(int blockIndex) {
		return blockIndex << 1;
	}

	/**
	 * @return true if {@code blockIndex}'s two payload bytes fit in {@code buffer}
	 */
	public static boolean hasBlockEntry(byte[] buffer, int blockIndex) {
		int offset = byteOffset(blockIndex);
		return offset >= 0 && offset + 1 < buffer.length;
	}

	/**
	 * Read the 12-bit block id at {@code blockIndex} (metadata discarded).
	 *
	 * @throws IndexOutOfBoundsException if the entry does not fit in {@code buffer}
	 */
	public static int readBlockId(byte[] buffer, int blockIndex) {
		int offset = byteOffset(blockIndex);
		int packed = (buffer[offset] & 0xFF) | ((buffer[offset + 1] & 0xFF) << 8);
		return packed >>> 4;
	}

	/**
	 * Write {@code blockId} at {@code blockIndex}, clearing metadata (same as legacy
	 * AntiXray replacement behaviour).
	 *
	 * @throws IllegalArgumentException  if {@code blockId} is outside 0–4095
	 * @throws IndexOutOfBoundsException if the entry does not fit in {@code buffer}
	 */
	public static void writeBlockId(byte[] buffer, int blockIndex, int blockId) {
		if (!isValidBlockId(blockId)) {
			throw new IllegalArgumentException("block id out of 1.8 range: " + blockId);
		}
		int offset = byteOffset(blockIndex);
		int packed = blockId << 4;
		buffer[offset] = (byte) (packed & 0xFF);
		buffer[offset + 1] = (byte) ((packed >> 8) & 0xFF);
	}

	/**
	 * Pack id + metadata into the 1.8 char value (for tests / callers that need meta).
	 */
	public static char pack(int blockId, int meta) {
		if (!isValidBlockId(blockId)) {
			throw new IllegalArgumentException("block id out of 1.8 range: " + blockId);
		}
		if (meta < 0 || meta > 15) {
			throw new IllegalArgumentException("meta out of range: " + meta);
		}
		return (char) ((blockId << 4) | meta);
	}

	/**
	 * Write a packed little-endian char into {@code buffer} at {@code blockIndex}.
	 */
	public static void writePacked(byte[] buffer, int blockIndex, char packed) {
		int offset = byteOffset(blockIndex);
		buffer[offset] = (byte) (packed & 0xFF);
		buffer[offset + 1] = (byte) ((packed >> 8) & 0xFF);
	}
}
