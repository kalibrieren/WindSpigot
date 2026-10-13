package com.windpvp.windspigot.inventory;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

/**
 * CraftResultListenerTest: owning-player resolution must ignore listener order.
 */
public class CraftResultListenerTest {

	@Test
	public void returnsOwnerWhenListeningEvenIfNotFirst() {
		Object other = new Object();
		Object owner = new Object();
		List<Object> listeners = Arrays.asList(other, owner);

		assertSame(owner, CraftResultListener.findOwningListener(owner, listeners, Object.class));
	}

	@Test
	public void returnsNullWhenListenersEmpty() {
		Object owner = new Object();
		assertNull(CraftResultListener.findOwningListener(owner, Collections.emptyList(), Object.class));
	}

	@Test
	public void returnsNullWhenOwnerNotListeningYet() {
		Object owner = new Object();
		List<Object> listeners = new ArrayList<Object>();
		listeners.add(new Object());

		assertNull(CraftResultListener.findOwningListener(owner, listeners, Object.class));
	}

	@Test
	public void returnsNullWhenOwnerWrongType() {
		Object owner = new Object();
		List<Object> listeners = Collections.singletonList(owner);

		assertNull(CraftResultListener.findOwningListener(owner, listeners, String.class));
	}

	@Test
	public void returnsNullForNullOwnerOrListeners() {
		Object owner = new Object();
		assertNull(CraftResultListener.findOwningListener(null, Collections.singletonList(owner), Object.class));
		assertNull(CraftResultListener.findOwningListener(owner, null, Object.class));
	}
}
