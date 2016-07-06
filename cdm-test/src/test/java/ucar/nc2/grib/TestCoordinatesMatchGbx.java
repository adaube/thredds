/*
 * Copyright 1998-2015 John Caron and University Corporation for Atmospheric Research/Unidata
 *
 *  Portions of this software were developed by the Unidata Program at the
 *  University Corporation for Atmospheric Research.
 *
 *  Access and use of this software shall impose the following obligations
 *  and understandings on the user. The user is granted the right, without
 *  any fee or cost, to use, copy, modify, alter, enhance and distribute
 *  this software, and any derivative works thereof, and its supporting
 *  documentation for any purpose whatsoever, provided that this entire
 *  notice appears in all copies of the software, derivative works and
 *  supporting documentation.  Further, UCAR requests that the user credit
 *  UCAR/Unidata in any publications that result from the use of this
 *  software or in any product that includes this software. The names UCAR
 *  and/or Unidata, however, may not be used in any advertising or publicity
 *  to endorse or promote any products or commercial entity unless specific
 *  written permission is obtained from UCAR/Unidata. The user also
 *  understands that UCAR/Unidata is not obligated to provide the user with
 *  any support, consulting, training or assistance of any kind with regard
 *  to the use, operation and performance of this software nor to provide
 *  the user with any updates, revisions, new versions or "bug fixes."
 *
 *  THIS SOFTWARE IS PROVIDED BY UCAR/UNIDATA "AS IS" AND ANY EXPRESS OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL UCAR/UNIDATA BE LIABLE FOR ANY SPECIAL,
 *  INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING
 *  FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
 *  NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION
 *  WITH THE ACCESS, USE OR PERFORMANCE OF THIS SOFTWARE.
 *
 */
package ucar.nc2.grib;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import ucar.nc2.grib.collection.Grib;
import ucar.nc2.util.DebugFlagsImpl;
import ucar.unidata.util.test.TestDir;
import ucar.unidata.util.test.category.NeedsCdmUnitTest;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

/**
 * Test reading grib coordinates match gbx
 */
@Category(NeedsCdmUnitTest.class)
public class TestCoordinatesMatchGbx {
  private static final boolean showFileCounters = false;

  @BeforeClass
  static public void before() {
    Grib.setDebugFlags(new DebugFlagsImpl("Grib/debugGbxIndexOnly"));
    countersAll = GribCoordsMatchGbx.getCounters();
  }

  @AfterClass
  static public void after() {
    Grib.setDebugFlags(new DebugFlagsImpl());
    System.out.printf("countersAll = %s%n", countersAll);
  }

  static ucar.nc2.util.Counters countersAll;
  ucar.nc2.util.Counters counterCurrent;

  @Test
  public void readGrib1Files() throws Exception {
    counterCurrent = countersAll.makeSubCounters();
    int fail = readAllDir(TestDir.cdmUnitTestDir + "formats/grib1", null, false);
    System.out.printf("readGrib1Files = %s%n", counterCurrent);
    countersAll.addTo(counterCurrent);
    Assert.assertEquals(0, fail);
  }

  @Test
  public void readGrib2Files() throws Exception {
    counterCurrent = countersAll.makeSubCounters();
    int fail = readAllDir(TestDir.cdmUnitTestDir + "formats/grib2", null, false);
    System.out.printf("readGrib2Files = %s%n", counterCurrent);
    countersAll.addTo(counterCurrent);
    Assert.assertEquals(0, fail);
  }

  @Test
  public void readNcepFiles() throws Exception {
    counterCurrent = countersAll.makeSubCounters();
    int fail = readAllDir(TestDir.cdmUnitTestDir + "tds/ncep", null, true);
    System.out.printf("readNcepFiles = %s%n", counterCurrent);
    countersAll.addTo(counterCurrent);
    Assert.assertEquals(0, fail);
  }

  @Test
  public void readFnmocFiles() throws Exception {
    counterCurrent = countersAll.makeSubCounters();
    int fail = readAllDir(TestDir.cdmUnitTestDir + "tds/fnmoc", null, true);
    System.out.printf("readFnmocFiles = %s%n", counterCurrent);
    countersAll.addTo(counterCurrent);
    Assert.assertEquals(0, fail);
  }

  @Test
  public void testProblem() throws IOException {
    ucar.nc2.util.Counters counters = GribCoordsMatchGbx.getCounters();
    // String filename = "D:/cdmUnitTest/formats/grib1/complex_packing.grib1";
    //String filename = "D:/cdmUnitTest/formats/grib1/QPE.20101005.009.157";
    String filename = "D:/cdmUnitTest/formats/grib2/berkes.grb2";
    GribCoordsMatchGbx helper = new GribCoordsMatchGbx(filename, counters);
    helper.readGridDataset();
    helper.readCoverageDataset();
    System.out.printf("counters= %s%n", counters);
  }

  int readAllDir(String dirName, String suffix, boolean recurse) throws Exception {
    return TestDir.actOnAll(dirName, new GribFilter(), new GribAct(), recurse);
  }

  class GribFilter implements FileFilter {

    @Override
    public boolean accept(File file) {
      if (file.isDirectory()) return false;
      String name = file.getName();
      if (name.contains(".gbx")) return false;
      if (name.contains(".ncx")) return false;
      if (name.contains(".ncml")) return false;
      return true;
    }
  }

  class GribAct implements TestDir.Act {

    @Override
    public int doAct(String filename) throws IOException {
      int fail =0;
      int fail2 = 0;
      ucar.nc2.util.Counters fileCounters = counterCurrent.makeSubCounters();
      GribCoordsMatchGbx helper = new GribCoordsMatchGbx(filename, fileCounters);
      fail = helper.readGridDataset();
      fail2 = helper.readCoverageDataset();
      if (showFileCounters) System.out.printf("fileCounters= %s%n", fileCounters);
      counterCurrent.addTo(fileCounters);
      return fail + fail2;
    }

  }
}