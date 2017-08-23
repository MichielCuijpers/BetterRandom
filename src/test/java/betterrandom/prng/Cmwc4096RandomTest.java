// ============================================================================
//   Copyright 2006-2012 Daniel W. Dyer
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
// ============================================================================
package betterrandom.prng;

import static betterrandom.prng.RandomTestUtils.assertEquivalentWhenSerializedAndDeserialized;
import static org.testng.Assert.assertEquals;

import betterrandom.seed.DefaultSeedGenerator;
import betterrandom.seed.SeedException;
import betterrandom.seed.SeedGenerator;
import java.io.IOException;
import org.testng.Reporter;
import org.testng.annotations.Test;

/**
 * Unit test for the Complementary Multiply With Carry (CMWC) RNG.
 *
 * @author Daniel Dyer
 */
public class Cmwc4096RandomTest {

  private final SeedGenerator seedGenerator = DefaultSeedGenerator.getInstance();

  /**
   * Test to ensure that two distinct RNGs with the same seed return the same sequence of numbers.
   */
  @Test(timeOut = 15000)
  public void testRepeatability() throws SeedException {
    Cmwc4096Random rng = new Cmwc4096Random(seedGenerator);
    // Create second RNG using same seed.
    Cmwc4096Random duplicateRNG = new Cmwc4096Random(rng.getSeed());
    assert RandomTestUtils
        .testEquivalence(rng, duplicateRNG, 1000) : "Generated sequences do not match.";
  }


  /**
   * Test to ensure that the output from the RNG is broadly as expected.  This will not detect the
   * subtle statistical anomalies that would be picked up by Diehard, but it provides a simple check
   * for major problems with the output.
   */
  @Test(timeOut = 15000, groups = "non-deterministic",
      dependsOnMethods = "testRepeatability")
  public void testDistribution() throws SeedException {
    Cmwc4096Random rng = new Cmwc4096Random(seedGenerator);
    double pi = RandomTestUtils.calculateMonteCarloValueForPi(rng, 100000);
    Reporter.log("Monte Carlo value for Pi: " + pi);
    assertEquals(pi, Math.PI, 0.01 * Math.PI, "Monte Carlo value for Pi is outside acceptable range:" + pi);
  }


  /**
   * Test to ensure that the output from the RNG is broadly as expected.  This will not detect the
   * subtle statistical anomalies that would be picked up by Diehard, but it provides a simple check
   * for major problems with the output.
   */
  @Test(timeOut = 15000, groups = "non-deterministic",
      dependsOnMethods = "testRepeatability")
  public void testStandardDeviation() throws SeedException {
    Cmwc4096Random rng = new Cmwc4096Random(seedGenerator);
    // Expected standard deviation for a uniformly distributed population of values in the range 0..n
    // approaches n/sqrt(12).
    int n = 100;
    double observedSD = RandomTestUtils.calculateSampleStandardDeviation(rng, n, 10000);
    double expectedSD = n / Math.sqrt(12);
    Reporter.log("Expected SD: " + expectedSD + ", observed SD: " + observedSD);
    assertEquals(observedSD, expectedSD, 0.02 * expectedSD,
        "Standard deviation is outside acceptable range: " + observedSD);
  }


  /**
   * Make sure that the RNG does not accept seeds that are too small since this could affect the
   * distribution of the output.
   */
  @Test(timeOut = 15000, expectedExceptions = IllegalArgumentException.class)
  public void testInvalidSeedSize() {
    new Cmwc4096Random(
        new byte[]{1, 2, 3}); // Not enough bytes, should cause an IllegalArgumentException.
  }


  /**
   * RNG must not accept a null seed otherwise it will not be properly initialised.
   */
  @Test(timeOut = 15000, expectedExceptions = IllegalArgumentException.class)
  public void testNullSeed() {
    new Cmwc4096Random((byte[]) null);
  }


  @SuppressWarnings("resource")
  @Test(timeOut = 15000)
  public void testSerializable() throws IOException, ClassNotFoundException, SeedException {
    // Serialise an RNG.
    Cmwc4096Random rng = new Cmwc4096Random();
    assertEquivalentWhenSerializedAndDeserialized(rng);
  }

  @Test(timeOut = 15000)
  public void testEquals() throws ReflectiveOperationException {
    RandomTestUtils.doEqualsSanityChecks(Cmwc4096Random.class.getConstructor());
  }

  @Test(timeOut = 15000)
  public void testHashCode() throws Exception {
    assert RandomTestUtils.testHashCodeDistribution(Cmwc4096Random.class.getConstructor())
        : "Too many hashCode collisions";
  }
}
