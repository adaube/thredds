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
 */

package ucar.nc2.ft.point.standard.plug;

import ucar.nc2.ft.point.standard.*;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.constants.AxisType;
import ucar.nc2.Dimension;

import java.util.Formatter;

/**
 * NOT DONE YET
 * NDBC (National Data Buoy Center) Convention with netcdf4 files
 * @author caron
 * @since Sept 6, 2011
 */
public class NdbcNetcdf4 extends TableConfigurerImpl  {

  public boolean isMine(FeatureType wantFeatureType, NetcdfDataset ds) {
    if (ds.getFileTypeId().equals("HDF5")) return false;

    String dataProvider = ds.findAttValueIgnoreCase(null, "data_provider", null);
    if (dataProvider == null)
      dataProvider = ds.findAttValueIgnoreCase(null, "institution", "");
    if (!dataProvider.contains("National Data Buoy Center"))
      return false;

    if (null == ds.findAttValueIgnoreCase(null, "station_name", null)) return false;
    if (null == ds.findAttValueIgnoreCase(null, "nominal_latitude", null)) return false;
    if (null == ds.findAttValueIgnoreCase(null, "nominal_longitude", null)) return false;

    return true;
  }

  /*
  <!-- C:/data/dt2/station/ndbc.nc -->
  <stationFeature>
    <stationId>":station"</stationId>
    <stationDesc>":description"</stationDesc>
    <coordAxis type="lat">lat</coordAxis>
    <coordAxis type="lon">lon</coordAxis>
    <coordAxis type="height">0</coordAxis>
    <table dim="time">
      <coordAxis type="time">time</coordAxis>
    </table>
  </stationFeature>
   */

   public TableConfig getConfig(FeatureType wantFeatureType, NetcdfDataset ds, Formatter errlog) {
    Dimension obsDim = ds.findDimension("time");
    if (obsDim == null) {
      CoordinateAxis axis = CoordSysEvaluator.findCoordByType(ds, AxisType.Time);
      if ((axis != null) && axis.isScalar())
        obsDim = axis.getDimension(0);
    }

    if (obsDim == null) {
      errlog.format("Must have an Observation dimension: unlimited dimension, or from Time Coordinate");
      return null;
    }
     boolean hasStruct = Evaluator.hasNetcdf3RecordStructure(ds);



    // otherwise, make it a Station
    TableConfig nt = new TableConfig(Table.Type.Top, "station");
    nt.featureType = FeatureType.STATION;

    nt.lat = CoordSysEvaluator.findCoordNameByType(ds, AxisType.Lat);
    nt.lon = CoordSysEvaluator.findCoordNameByType(ds, AxisType.Lon);

    nt.stnId = ds.findAttValueIgnoreCase(null, "station_name", null);
    nt.stnWmoId = ds.findAttValueIgnoreCase(null, "wmo_id", null);

    nt.stnDesc = ds.findAttValueIgnoreCase(null, "description", null);
    if (nt.stnDesc == null)
      nt.stnDesc = ds.findAttValueIgnoreCase(null, "comment", null);

    TableConfig obs = new TableConfig(Table.Type.Structure, hasStruct ? "record" : obsDim.getShortName());
    obs.structName = "record";
    obs.structureType = hasStruct ? TableConfig.StructureType.Structure : TableConfig.StructureType.PsuedoStructure;
    obs.dimName = obsDim.getShortName();
    obs.time = CoordSysEvaluator.findCoordNameByType(ds, AxisType.Time);
    nt.addChild(obs);

    return nt;
  }
}
