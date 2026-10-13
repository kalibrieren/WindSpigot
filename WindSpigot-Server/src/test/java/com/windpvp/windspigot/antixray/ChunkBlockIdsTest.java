package com.windpvp.windspigot.antixray;

import org.junit.Assert;
import org.junit.Test;

/**
 * Contract tests for 1.8 map-chunk 12-bit block id packing used by AntiXray.
 */
public class ChunkBlockIdsTest {

	@Test
	public void maxBlockIdIsTwelveBits() {
		Assert.assertEquals(4095, ChunkBlockIds.MAX_BLOCK_ID);
	}

	@Test
	public void isValidBlockIdBounds() {
		Assert.assertTrue(ChunkBlockIds.isValidBlockId(0));
		Assert.assertTrue(ChunkBlockIds.isValidBlockId(255));
		Assert.assertTrue(ChunkBlockIds.isValidBlockId(256));
		Assert.assertTrue(ChunkBlockIds.isValidBlockId(4095));
		Assert.assertFalse(ChunkBlockIds.isValidBlockId(-1));
		Assert.assertFalse(ChunkBlockIds.isValidBlockId(4096));
	}

	@Test
	public void roundTripVanillaIdPreservesValueAndClearsMeta() {
		byte[] buffer = new byte[2];
		ChunkBlockIds.writePacked(buffer, 0, ChunkBlockIds.pack(56, 7)); // diamond ore + stray meta
		Assert.assertEquals(56, ChunkBlockIds.readBlockId(buffer, 0));

		ChunkBlockIds.writeBlockId(buffer, 0, 1); // stone replacement clears meta
		Assert.assertEquals(1, ChunkBlockIds.readBlockId(buffer, 0));
		char packed = (char) ((buffer[0] & 0xFF) | ((buffer[1] & 0xFF) << 8));
		Assert.assertEquals(0, packed & 0xF);
	}

	@Test
	public void roundTripExtendedIdAbove255() {
		byte[] buffer = new byte[4];
		int extended = 1000;
		ChunkBlockIds.writePacked(buffer, 0, ChunkBlockIds.pack(extended, 3));
		Assert.assertEquals(extended, ChunkBlockIds.readBlockId(buffer, 0));

		// Second slot: ensure indexing uses 2 bytes per block
		ChunkBlockIds.writeBlockId(buffer, 1, 3000);
		Assert.assertEquals(3000, ChunkBlockIds.readBlockId(buffer, 1));
		Assert.assertEquals(extended, ChunkBlockIds.readBlockId(buffer, 0));
	}

	@Test
	public void writeBlockIdRejectsOutOfRange() {
		byte[] buffer = new byte[2];
		try {
			ChunkBlockIds.writeBlockId(buffer, 0, 4096);
			Assert.fail("expected IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
			// ok
		}
	}

	@Test
	public void hasBlockEntryRespectsTwoByteStride() {
		byte[] buffer = new byte[4]; // two block slots
		Assert.assertTrue(ChunkBlockIds.hasBlockEntry(buffer, 0));
		Assert.assertTrue(ChunkBlockIds.hasBlockEntry(buffer, 1));
		Assert.assertFalse(ChunkBlockIds.hasBlockEntry(buffer, 2));

		// Legacy bug: comparing block index to byte length would wrongly accept index 3
		Assert.assertFalse(ChunkBlockIds.hasBlockEntry(buffer, 3));
	}

	@Test
	public void packEncodesIdInUpperTwelveBits() {
		char packed = ChunkBlockIds.pack(1000, 5);
		Assert.assertEquals(1000, packed >>> 4);
		Assert.assertEquals(5, packed & 0xF);
	}

	@Test
	public void obfuscateStyleRewriteKeepsExtendedReplacementId() {
		// Simulate engine-mode-2 rewrite of a hidden extended id with another extended id
		byte[] buffer = new byte[2];
		ChunkBlockIds.writePacked(buffer, 0, ChunkBlockIds.pack(2000, 0));
		Assert.assertTrue(ChunkBlockIds.isValidBlockId(ChunkBlockIds.readBlockId(buffer, 0)));
		Assert.assertTrue(ChunkBlockIds.readBlockId(buffer, 0) > 255);

		int replacement = 3500;
		ChunkBlockIds.writeBlockId(buffer, 0, replacement);
		Assert.assertEquals(replacement, ChunkBlockIds.readBlockId(buffer, 0));
		// Must not have been truncated to 8 bits (3500 & 0xFF == 172)
		Assert.assertNotEquals(replacement & 0xFF, ChunkBlockIds.readBlockId(buffer, 0));
	}
}
