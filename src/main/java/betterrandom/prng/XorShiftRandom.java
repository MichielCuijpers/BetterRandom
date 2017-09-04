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

import betterrandom.RepeatableRandom;
import betterrandom.seed.DefaultSeedGenerator;
import betterrandom.seed.SeedException;
import betterrandom.seed.SeedGenerator;
import betterrandom.util.BinaryUtils;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Random;
import org.checkerframework.checker.initialization.qual.UnknownInitialization;

/**
 * <p>Very fast pseudo random number generator.  See <a href="http://school.anhb.uwa.edu.au/personalpages/kwessen/shared/Marsaglia03.html">this
 * page</a> for a description.  This RNG has a period of about 2^160, which is not as long as the
 * {@link MersenneTwisterRandom} but it is faster.</p>
 *
 * <p><em>NOTE: Because instances of this class require 160-bit seeds, it is not possible to seed
 * this RNG using the {@link #setSeed(long)} method inherited from {@link Random}.  Calls to this
 * method will have no effect. Instead the seed must be set by a constructor.</em></p>
 *
 * @author Daniel Dyer
 * @since 1.2
 */
public class XorShiftRandom extends BaseEntropyCountingRandom implements RepeatableRandom {

  private static final long serialVersionUID = 952521144304194886L;
  private static final int SEED_SIZE_BYTES = 20; // Needs 5 32-bit integers.

  // Previously used an array for state but using separate fields proved to be
  // faster.
  private int state1;
  private int state2;
  private int state3;
  private int state4;
  private int state5;

  @Override
  protected ToStringHelper addSubSubclassFields(ToStringHelper original) {
    return original
        .add("state1",state1)
        .add("state2",state2)
        .add("state3",state3)
        .add("state4",state4)
        .add("state5",state5);
  }

  /**
   * Creates an RNG and seeds it with the specified seed data.
   *
   * @param seed The seed data used to initialise the RNG.
   */
  public XorShiftRandom(byte[] seed) {
    super(seed);
  }

  public XorShiftRandom() throws SeedException {
    this(DefaultSeedGenerator.DEFAULT_SEED_GENERATOR);
  }

  public XorShiftRandom(SeedGenerator seedGenerator) throws SeedException {
    this(seedGenerator.generateSeed(SEED_SIZE_BYTES));
  }

  /**
   * {@inheritDoc}
   */
  public byte[] getSeed() {
    return seed.clone();
  }

  @SuppressWarnings("contracts.postcondition.not.satisfied")
  @Override
  public void setSeed(@UnknownInitialization(Random.class) XorShiftRandom this, long seed) {
    if (superConstructorFinished) {
      super.setSeed(seed);
    } // Otherwise ignore; it's Random.<init> calling us without a full-size seed
  }

  @Override
  public void setSeedInitial(@UnknownInitialization(Random.class) XorShiftRandom this, byte[] seed) {
    super.setSeedInitial(seed);
    int[] state = BinaryUtils.convertBytesToInts(this.seed);
    state1 = state[0];
    state2 = state[1];
    state3 = state[2];
    state4 = state[3];
    state5 = state[4];
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected int next(int bits) {
    lock.lock();
    try {
      int t = (state1 ^ (state1 >> 7));
      state1 = state2;
      state2 = state3;
      state3 = state4;
      state4 = state5;
      state5 = (state5 ^ (state5 << 6)) ^ (t ^ (t << 13));
      int value = (state2 + state2 + 1) * state5;
      recordEntropySpent(bits);
      return value >>> (32 - bits);
    } finally {
      lock.unlock();
    }
  }

  @Override
  public int getNewSeedLength() {
    return SEED_SIZE_BYTES;
  }
}
