/*
 * This file is part of ELKI:
 * Environment for Developing KDD-Applications Supported by Index-Structures
 *
 * Copyright (C) 2017
 * ELKI Development Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans;

import org.junit.Test;

import de.lmu.ifi.dbs.elki.algorithm.clustering.AbstractClusterAlgorithmTest;
import de.lmu.ifi.dbs.elki.data.Clustering;
import de.lmu.ifi.dbs.elki.data.DoubleVector;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.utilities.ClassGenericsUtil;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.ListParameterization;

/**
 * Performs a full KMeans run, and compares the result with a clustering derived
 * from the data set labels. This test ensures that KMeans's performance doesn't
 * unexpectedly drop on this data set (and also ensures that the algorithms
 * work, as a side effect).
 *
 * @author Erich Schubert
 * @since 0.4.0
 */
public class SingleAssignmentKMeansTest extends AbstractClusterAlgorithmTest {
  /**
   * Run KMeans with fixed parameters and compare the result to a golden
   * standard.
   */
  @Test
  public void testSingleAssignmentKMeans() {
    Database db = makeSimpleDatabase(UNITTEST + "different-densities-2d-no-noise.ascii", 1000);

    // Setup algorithm
    ListParameterization params = new ListParameterization();
    params.addParameter(KMeans.K_ID, 5);
    params.addParameter(KMeans.SEED_ID, 7);
    AbstractKMeans<DoubleVector, ?> kmeans = ClassGenericsUtil.parameterizeOrAbort(SingleAssignmentKMeans.class, params);
    testParameterizationOk(params);

    // run KMeans on database
    Clustering<?> result = kmeans.run(db);
    // Unsurprisingly, these results are much worse than normal k-means
    testFMeasure(db, result, 0.702733122);
    testClusterSizes(result, new int[] { 64, 95, 202, 306, 333 });
  }
}