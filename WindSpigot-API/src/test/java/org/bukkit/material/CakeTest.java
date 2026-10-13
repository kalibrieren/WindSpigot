package org.bukkit.material;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CakeTest {

	@Test
	public void setSlicesEatenBelowSixUpdatesData() {
		Cake cake = new Cake();
		cake.setSlicesEaten(3);
		assertEquals(3, cake.getSlicesEaten());
		assertEquals(3, cake.getSlicesRemaining());
	}

	@Test
	public void setSlicesEatenAtOrAboveSixClampsToFullyEaten() {
		Cake cake = new Cake();
		cake.setSlicesEaten(2);
		cake.setSlicesEaten(6);
		assertEquals(6, cake.getSlicesEaten());
		assertEquals(0, cake.getSlicesRemaining());

		cake.setSlicesEaten(99);
		assertEquals(6, cake.getSlicesEaten());
		assertEquals(0, cake.getSlicesRemaining());
	}

	@Test
	public void setSlicesRemainingZeroMatchesFullyEatenClamp() {
		Cake cake = new Cake();
		cake.setSlicesRemaining(0);
		assertEquals(6, cake.getSlicesEaten());
		assertEquals(0, cake.getSlicesRemaining());
	}
}
