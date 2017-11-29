package io.github.pr0methean.betterrandom.prng;

import static io.github.pr0methean.betterrandom.TestUtils.assertGreaterOrEqual;

import io.github.pr0methean.betterrandom.DeadlockWatchdogThread;
import io.github.pr0methean.betterrandom.seed.DefaultSeedGenerator;
import io.github.pr0methean.betterrandom.seed.RandomSeederThread;
import io.github.pr0methean.betterrandom.seed.SeedException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Random;
import java.util.function.Supplier;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class ReseedingThreadLocalRandomWrapperTest extends ThreadLocalRandomWrapperTest {

  @BeforeMethod public void setUp() {
    DeadlockWatchdogThread.ensureStarted();
    RandomSeederThread.setLoggingEnabled(true);
  }

  @Override public void testWrapLegacy() throws SeedException {
    ReseedingThreadLocalRandomWrapper
        .wrapLegacy(Random::new, DefaultSeedGenerator.DEFAULT_SEED_GENERATOR).nextInt();
  }

  @Override protected Class<? extends BaseRandom> getClassUnderTest() {
    return ReseedingThreadLocalRandomWrapper.class;
  }

  @Override @Test public void testReseeding() {
    final BaseRandom rng = createRng();
    // final byte[] oldSeed = rng.getSeed();
    while (rng.getEntropyBits() > Long.SIZE) {
      rng.nextLong();
    }
    try {
      long oldEntropy;
      long newEntropy = rng.getEntropyBits();
      byte[] newSeed;
      do {
        oldEntropy = newEntropy;
        rng.nextBoolean();
        Thread.sleep(100);
        newSeed = rng.getSeed();
        newEntropy = rng.getEntropyBits();
      } while (newEntropy <= oldEntropy);
      // Change once bug fixed: while (Arrays.equals(newSeed, oldSeed));
      assertGreaterOrEqual(rng.getEntropyBits(), newSeed.length * 8L - 1);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override protected BaseRandom createRng() throws SeedException {
    return new ReseedingThreadLocalRandomWrapper(DefaultSeedGenerator.DEFAULT_SEED_GENERATOR,
        (Serializable & Supplier<BaseRandom>) MersenneTwisterRandom::new);
  }
}
