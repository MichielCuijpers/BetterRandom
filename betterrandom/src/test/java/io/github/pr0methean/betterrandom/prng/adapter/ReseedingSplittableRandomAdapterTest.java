package io.github.pr0methean.betterrandom.prng.adapter;

import static io.github.pr0methean.betterrandom.seed.SecureRandomSeedGenerator.DEFAULT_INSTANCE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertSame;

import com.google.common.testing.SerializableTester;
import io.github.pr0methean.betterrandom.FlakyRetryAnalyzer;
import io.github.pr0methean.betterrandom.prng.BaseRandom;
import io.github.pr0methean.betterrandom.prng.RandomTestUtils;
import io.github.pr0methean.betterrandom.prng.RandomTestUtils.EntropyCheckMode;
import io.github.pr0methean.betterrandom.seed.FakeSeedGenerator;
import io.github.pr0methean.betterrandom.seed.SeedException;
import io.github.pr0methean.betterrandom.seed.SeedGenerator;
import io.github.pr0methean.betterrandom.seed.SemiFakeSeedGenerator;
import io.github.pr0methean.betterrandom.seed.SimpleRandomSeeder;
import io.github.pr0methean.betterrandom.util.BinaryUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ReseedingSplittableRandomAdapterTest
    extends SingleThreadSplittableRandomAdapterTest {

  private SimpleRandomSeeder thread;

  @Override protected SeedGenerator getTestSeedGenerator() {
    return semiFakeSeedGenerator;
  }

  @BeforeMethod public void setUp() {
    thread = new SimpleRandomSeeder(getTestSeedGenerator());
  }

  @AfterMethod public void tearDown() {
    thread.stopIfEmpty();
    thread = null;
  }

  @Override protected EntropyCheckMode getEntropyCheckMode() {
    return EntropyCheckMode.LOWER_BOUND;
  }

  @Override protected ReseedingSplittableRandomAdapter createRng() throws SeedException {
    return ReseedingSplittableRandomAdapter.getInstance(thread, getTestSeedGenerator());
  }

  @Override protected BaseRandom createRng(byte[] seed) throws SeedException {
    ReseedingSplittableRandomAdapter out = createRng();
    out.setSeed(seed);
    return out;
  }

  // FIXME: Why does this need more time than other PRNGs?!
  @Test(timeOut = 120_000) @Override public void testDistribution() throws SeedException {
    super.testDistribution();
  }

  @Override public void testInitialEntropy() {
    // This test needs a separate instance from all other tests, but createRng() doesn't provide one
    SimpleRandomSeeder newThread = new SimpleRandomSeeder(new FakeSeedGenerator("testInitialEntropy"));
    ReseedingSplittableRandomAdapter random
        = ReseedingSplittableRandomAdapter.getInstance(newThread, getTestSeedGenerator());
    assertEquals(random.getEntropyBits(), Long.SIZE, "Wrong initial entropy");
  }

  // FIXME: Why does this need more time than other PRNGs?!
  @Test(timeOut = 120_000) @Override public void testIntegerSummaryStats() throws SeedException {
    super.testIntegerSummaryStats();
  }

  @Override @Test public void testSerializable() throws SeedException {
    SeedGenerator generator = new FakeSeedGenerator("testSerializable");
    SimpleRandomSeeder thread = new SimpleRandomSeeder(generator);
    try {
      final BaseSplittableRandomAdapter adapter =
          ReseedingSplittableRandomAdapter.getInstance(thread, generator);
      final BaseSplittableRandomAdapter clone = SerializableTester.reserialize(adapter);
      assertSame(adapter, clone);
    } finally {
      thread.shutDown();
    }
  }

  @Override protected Class<? extends BaseRandom> getClassUnderTest() {
    return ReseedingSplittableRandomAdapter.class;
  }

  @Override @Test(enabled = false) public void testRepeatability() {
    // No-op.
  }

  @Override @Test(enabled = false) public void testRepeatabilityNextGaussian() {
    // No-op.
  }

  @Override @Test(retryAnalyzer = FlakyRetryAnalyzer.class)
  public void testReseeding() {
    SeedGenerator generator = new SemiFakeSeedGenerator(new SplittableRandomAdapter(), "testReseeding");
    SimpleRandomSeeder seeder = new SimpleRandomSeeder(generator);
    try {
      ReseedingSplittableRandomAdapter random =
          ReseedingSplittableRandomAdapter.getInstance(seeder, generator);
      RandomTestUtils.checkReseeding(generator, random, false);
    } finally {
      seeder.shutDown();
    }
  }

  /**
   * Test for crashes only, since setSeed is a no-op.
   */
  @Override @Test public void testSetSeedAfterNextLong() throws SeedException {
    final BaseRandom prng = createRng();
    prng.nextLong();
    prng.setSeed(getTestSeedGenerator().generateSeed(8));
    prng.setSeed(BinaryUtils.convertBytesToLong(getTestSeedGenerator().generateSeed(8)));
    prng.nextLong();
  }

  /**
   * Test for crashes only, since setSeed is a no-op.
   */
  @Override @Test public void testSetSeedAfterNextInt() throws SeedException {
    final BaseRandom prng = createRng();
    prng.nextInt();
    prng.setSeed(getTestSeedGenerator().generateSeed(8));
    prng.setSeed(BinaryUtils.convertBytesToLong(getTestSeedGenerator().generateSeed(8)));
    prng.nextInt();
  }

  /**
   * Assertion-free since reseeding may cause divergent output.
   */
  @Override @Test(timeOut = 10000) public void testSetSeedLong() {
    createRng().setSeed(0x0123456789ABCDEFL);
  }

  /**
   * setRandomSeeder doesn't work on this class and shouldn't pretend to.
   */
  @Override @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testRandomSeederThreadIntegration() {
    SimpleRandomSeeder thread = new SimpleRandomSeeder(DEFAULT_INSTANCE);
    try {
      createRng().setRandomSeeder(thread);
    } finally {
      thread.stopIfEmpty();
    }
  }

  @Test public void testSetSeedGeneratorNoOp() {
    ReseedingSplittableRandomAdapter.getInstance(thread, getTestSeedGenerator())
        .setRandomSeeder(thread);
  }

  @Override @Test(enabled = false) public void testSeedTooShort() {
    // No-op.
  }

  @Override @Test(enabled = false) public void testSeedTooLong() {
    // No-op.
  }

  @Override @Test public void testDump() throws SeedException {
    SimpleRandomSeeder thread = new SimpleRandomSeeder(DEFAULT_INSTANCE);
    try {
      ReseedingSplittableRandomAdapter baseInstance =
          ReseedingSplittableRandomAdapter.getInstance(thread, getTestSeedGenerator());
      SimpleRandomSeeder otherThread =
          new SimpleRandomSeeder(new FakeSeedGenerator("Different reseeder"));
      try {
        assertNotEquals(
            ReseedingSplittableRandomAdapter.getInstance(otherThread, getTestSeedGenerator())
                .dump(), baseInstance.dump());
      } finally {
        otherThread.shutDown();
      }
    } finally {
      thread.shutDown();
    }
  }

  /**
   * Assertion-free because thread-local.
   */
  @Override @Test public void testThreadSafety() {
    testThreadSafetyVsCrashesOnly(30, functionsForThreadSafetyTest);
  }
}
