package io.github.pr0methean.betterrandom.prng;

import static io.github.pr0methean.betterrandom.TestUtils.assertGreaterOrEqual;

import io.github.pr0methean.betterrandom.prng.RandomTestUtils.EntropyCheckMode;
import io.github.pr0methean.betterrandom.seed.RandomSeederThread;
import io.github.pr0methean.betterrandom.seed.SeedException;
import java.util.Arrays;
import org.testng.annotations.Test;

public class ReseedingThreadLocalRandomWrapperMersenneTwisterTest
    extends ThreadLocalRandomWrapperMersenneTwisterTest {

  @Override protected EntropyCheckMode getEntropyCheckMode() {
    return EntropyCheckMode.LOWER_BOUND;
  }

  @Override protected Class<? extends BaseRandom> getClassUnderTest() {
    return ReseedingThreadLocalRandomWrapper.class;
  }

  @SuppressWarnings("BusyWait") @Override @Test public void testReseeding() {
    final BaseRandom rng = new ReseedingThreadLocalRandomWrapper(SEMIFAKE_SEED_GENERATOR,
        new MersenneTwisterRandomColonColonNew());
    rng.nextLong();
    try {
      Thread.sleep(1000);
    } catch (final InterruptedException e) {
      throw new RuntimeException(e);
    }
    final byte[] oldSeed = rng.getSeed();
    byte[] newSeed;
    RandomSeederThread.setPriority(SEMIFAKE_SEED_GENERATOR, Thread.MAX_PRIORITY);
    try {
      do {
        rng.nextLong();
        Thread.sleep(10);
        newSeed = rng.getSeed();
      } while (Arrays.equals(newSeed, oldSeed));
      Thread.sleep(100);
      assertGreaterOrEqual(rng.getEntropyBits(), (newSeed.length * 8L) - 1);
    } catch (final InterruptedException e) {
      throw new RuntimeException(e);
    } finally {
      RandomSeederThread.setPriority(SEMIFAKE_SEED_GENERATOR, Thread.NORM_PRIORITY);
    }
  }

  /** Assertion-free since reseeding may cause divergent output. */
  @Override @Test(timeOut = 10000) public void testSetSeedLong() {
    createRng().setSeed(0x0123456789ABCDEFL);
  }

  /** Test for crashes only, since setSeed is a no-op. */
  @Override @Test public void testSetSeedAfterNextLong() throws SeedException {
    final BaseRandom prng = createRng();
    prng.nextLong();
    prng.setSeed(SEMIFAKE_SEED_GENERATOR.generateSeed(8));
    prng.nextLong();
  }

  /** Test for crashes only, since setSeed is a no-op. */
  @Override @Test public void testSetSeedAfterNextInt() throws SeedException {
    final BaseRandom prng = createRng();
    prng.nextInt();
    prng.setSeed(SEMIFAKE_SEED_GENERATOR.generateSeed(8));
    prng.nextInt();
  }

  @Override protected BaseRandom createRng() throws SeedException {
    return new ReseedingThreadLocalRandomWrapper(SEMIFAKE_SEED_GENERATOR,
        new MersenneTwisterRandomColonColonNew());
  }
}
