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
package io.github.pr0methean.betterrandom.seed;

import static io.github.pr0methean.betterrandom.seed.RandomDotOrgSeedGenerator.setProxy;
import static io.github.pr0methean.betterrandom.seed.RandomDotOrgUtils.canRunRandomDotOrgLargeTest;
import static io.github.pr0methean.betterrandom.seed.RandomDotOrgUtils.haveApiKey;
import static org.mockito.ArgumentMatchers.any;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

import java.net.InetAddress;
import java.net.Proxy;
import java.net.UnknownHostException;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Unit test for the seed generator that connects to random.org to get seed data.
 * @author Daniel Dyer
 * @author Chris Hennick
 */
@Test(singleThreaded = true)
public class RandomDotOrgSeedGeneratorLiveTest extends AbstractSeedGeneratorTest {

  protected final Proxy proxy = RandomDotOrgUtils.createTorProxy();

  public RandomDotOrgSeedGeneratorLiveTest() {
    super(RandomDotOrgSeedGenerator.RANDOM_DOT_ORG_SEED_GENERATOR);
  }

  @Test(timeOut = 120000) public void testGeneratorOldApi() throws SeedException {
    if (canRunRandomDotOrgLargeTest()) {
      RandomDotOrgSeedGenerator.setApiKey(null);
      SeedTestUtils.testGenerator(seedGenerator);
    } else {
      throw new SkipException("Test can't run on this platform");
    }
  }

  @Test(timeOut = 120000) public void testGeneratorNewApi() throws SeedException {
    if (canRunRandomDotOrgLargeTest() && haveApiKey()) {
      RandomDotOrgUtils.setApiKey();
      SeedTestUtils.testGenerator(seedGenerator);
    } else {
      throw new SkipException("Test can't run on this platform");
    }
  }

  /**
   * Try to acquire a large number of bytes, more than are cached internally by the seed generator
   * implementation.
   */
  @Test(timeOut = 120000) public void testLargeRequest() throws SeedException {
    if (canRunRandomDotOrgLargeTest()) {
      RandomDotOrgUtils.setApiKey();
      // Request more bytes than are cached internally.
      final int seedLength = 626;
      assertEquals(seedGenerator.generateSeed(seedLength).length, seedLength,
          "Failed to generate seed of length " + seedLength);
    } else {
      throw new SkipException("Test can't run on this platform");
    }
  }

  @Override public void testToString() {
    super.testToString();
    Assert.assertNotNull(RandomDotOrgSeedGenerator.DELAYED_RETRY.toString());
  }

  @Test
  public void testSetProxyReal() throws Exception {
    if (!canRunRandomDotOrgLargeTest()) {
      throw new SkipException("Test can't run on this platform");
    }
    setProxy(proxy);
    try {
      SeedTestUtils.testGenerator(seedGenerator);
    } finally {
      setProxy(null);
    }
  }

  @BeforeClass
  public void setUpClass() {
    RandomDotOrgUtils.maybeSetMaxRequestSize();
    // when using Tor, DNS seems to be unreliable, so it may take several tries to get the address
    InetAddress address = null;
    long failedLookups = 0;
    while (address == null) {
      try {
        address = InetAddress.getByName("api.random.org");
      } catch (UnknownHostException e) {
        failedLookups++;
      }
    }
    if (failedLookups > 0) {
      Reporter.log("Failed to look up api.random.org address on the first " + failedLookups + " attempts");
    }
  }

  @AfterMethod
  public void tearDownMethod() {
    RandomDotOrgSeedGenerator.setApiKey(null);
  }

}