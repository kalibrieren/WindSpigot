package com.windpvp.windspigot.chunk;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for Packet51 cuboid validation / indexing used by
 * {@code CraftPlayer#sendChunkChange}.
 */
public class Packet51CuboidTest {

	private static byte[] buffer(int sx, int sy, int sz) {
		return new byte[Packet51Cuboid.expectedDataLength(sx, sy, sz)];
	}

	/** Packs block id + metadata (low nibble) into a Packet51 cuboid buffer. */
	private static byte[] cuboidWith(int sx, int sy, int sz, int dx, int dy, int dz, int blockId, int meta) {
		byte[] data = buffer(sx, sy, sz);
		int size = sx * sy * sz;
		int index = Packet51Cuboid.blockIndex(dx, dy, dz, sy, sz);
		data[index] = (byte) blockId;
		int byteIndex = size + (index >> 1);
		int shift = (index & 1) << 2;
		data[byteIndex] = (byte) ((data[byteIndex] & ~(15 << shift)) | ((meta & 15) << shift));
		return data;
	}

	@Test
	public void expectedLengthMatchesApiFormula() {
		Assert.assertEquals((2 * 2 * 2 * 5) / 2, Packet51Cuboid.expectedDataLength(2, 2, 2));
		Assert.assertEquals((16 * 128 * 16 * 5) / 2, Packet51Cuboid.expectedDataLength(16, 128, 16));
	}

	@Test
	public void acceptsValidSingleChunkCuboid() {
		byte[] data = buffer(2, 2, 2);
		Assert.assertTrue(Packet51Cuboid.isValid(0, 64, 0, 2, 2, 2, data));
	}

	@Test
	public void rejectsNullData() {
		Assert.assertFalse(Packet51Cuboid.isValid(0, 0, 0, 2, 2, 2, null));
	}

	@Test
	public void rejectsNonPositiveDimensions() {
		byte[] data = buffer(2, 2, 2);
		Assert.assertFalse(Packet51Cuboid.isValid(0, 0, 0, 0, 2, 2, data));
		Assert.assertFalse(Packet51Cuboid.isValid(0, 0, 0, 2, -1, 2, data));
	}

	@Test
	public void rejectsOddVolume() {
		// 1*1*1 is odd — cannot pack 2.5 bytes/block to an integer length
		Assert.assertFalse(Packet51Cuboid.isValid(0, 0, 0, 1, 1, 1, new byte[2]));
	}

	@Test
	public void rejectsCrossChunkCuboid() {
		byte[] data = buffer(2, 2, 2);
		// starts at x=15, size 2 → spans chunk boundary
		Assert.assertFalse(Packet51Cuboid.isValid(15, 64, 0, 2, 2, 2, data));
	}

	@Test
	public void rejectsOutOfWorldHeight() {
		byte[] data = buffer(2, 2, 2);
		Assert.assertFalse(Packet51Cuboid.isValid(0, -1, 0, 2, 2, 2, data));
		Assert.assertFalse(Packet51Cuboid.isValid(0, Packet51Cuboid.MAX_WORLD_HEIGHT - 1, 0, 2, 2, 2, data));
	}

	@Test
	public void acceptsFullHeightColumnIn18() {
		// Historical stub capped at 128; 1.8 Anvil allows 256
		byte[] data = buffer(2, 256, 2);
		Assert.assertTrue(Packet51Cuboid.isValid(0, 0, 0, 2, 256, 2, data));
	}

	@Test
	public void rejectsWrongBufferLength() {
		Assert.assertFalse(Packet51Cuboid.isValid(0, 0, 0, 2, 2, 2, new byte[1]));
	}

	@Test
	public void blockIndexYFastestThenZThenX() {
		// sx=2, sy=3, sz=2 → indices for (dx,dy,dz)
		Assert.assertEquals(0, Packet51Cuboid.blockIndex(0, 0, 0, 3, 2));
		Assert.assertEquals(1, Packet51Cuboid.blockIndex(0, 1, 0, 3, 2));
		Assert.assertEquals(2, Packet51Cuboid.blockIndex(0, 2, 0, 3, 2));
		Assert.assertEquals(3, Packet51Cuboid.blockIndex(0, 0, 1, 3, 2));
		Assert.assertEquals(6, Packet51Cuboid.blockIndex(1, 0, 0, 3, 2));
	}

	@Test
	public void forEachBlockReadsIdAndMetadata() {
		byte[] data = cuboidWith(2, 2, 2, 1, 0, 1, 5 /* wood */, 3 /* meta */);
		List<int[]> hits = new ArrayList<int[]>();
		Packet51Cuboid.forEachBlock(10, 20, 30, 2, 2, 2, data, (x, y, z, id, meta) -> {
			if (id != 0 || meta != 0) {
				hits.add(new int[] { x, y, z, id, meta });
			}
		});
		Assert.assertEquals(1, hits.size());
		Assert.assertArrayEquals(new int[] { 11, 20, 31, 5, 3 }, hits.get(0));
	}

	@Test
	public void nibblePackingMatchesNibbleArray() {
		byte[] data = buffer(2, 2, 2); // size=8, metadata at offset 8
		int size = 8;
		// Set metadata for even index 0 = 7 (low nibble), odd index 1 = 9 (high)
		data[size] = (byte) ((9 << 4) | 7);
		Assert.assertEquals(7, Packet51Cuboid.metadata(data, size, 0));
		Assert.assertEquals(9, Packet51Cuboid.metadata(data, size, 1));
	}

	@Test
	public void forEachBlockVisitsEveryCell() {
		byte[] data = buffer(2, 2, 2);
		final int[] count = { 0 };
		Packet51Cuboid.forEachBlock(0, 0, 0, 2, 2, 2, data, (x, y, z, id, meta) -> count[0]++);
		Assert.assertEquals(8, count[0]);
	}
}
