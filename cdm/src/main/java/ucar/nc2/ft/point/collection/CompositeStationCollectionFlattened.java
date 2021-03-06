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
package ucar.nc2.ft.point.collection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;

import thredds.inventory.TimedCollection;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.ft.DsgFeatureCollection;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.ft.FeatureDatasetPoint;
import ucar.nc2.ft.PointFeature;
import ucar.nc2.ft.PointFeatureCollection;
import ucar.nc2.ft.PointFeatureIterator;
import ucar.nc2.ft.StationTimeSeriesFeatureCollection;
import ucar.nc2.ft.point.PointCollectionImpl;
import ucar.nc2.ft.point.PointIteratorAbstract;
import ucar.nc2.ft.point.StationFeature;
import ucar.nc2.time.CalendarDateRange;
import ucar.nc2.time.CalendarDateUnit;
import ucar.unidata.geoloc.LatLonRect;

/**
 * CompositeStationCollection that has been flattened into a PointCollection
 *
 * @author caron
 * @since Aug 28, 2009
 */

public class CompositeStationCollectionFlattened extends PointCollectionImpl {
  static private org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CompositeStationCollectionFlattened.class);

  private TimedCollection stnCollections;
  private LatLonRect bbSubset;
  private List<String> stationsSubset;
  private CalendarDateRange dateRange;
  private List<VariableSimpleIF> varList;
  private boolean wantStationsubset = false;

  protected CompositeStationCollectionFlattened(String name, CalendarDateUnit timeUnit, String altUnits, List<String> stations, CalendarDateRange dateRange,
                                                List<VariableSimpleIF> varList, TimedCollection stnCollections) throws IOException {
    super(name, timeUnit, altUnits);
    this.stationsSubset = stations; // note these will be from the original collection, must transfer
    this.dateRange = dateRange;
    this.varList = varList;
    this.stnCollections = stnCollections;

    wantStationsubset = (stations != null) && (stations.size() > 0);
  }

  protected CompositeStationCollectionFlattened(String name, CalendarDateUnit timeUnit, String altUnits, LatLonRect bbSubset, CalendarDateRange dateRange, TimedCollection stnCollections) throws IOException {
    super(name, timeUnit, altUnits);
    this.bbSubset = bbSubset;
    this.dateRange = dateRange;
    this.stnCollections = stnCollections;
  }

  @Override
  public PointFeatureIterator getPointFeatureIterator() throws IOException {
    return new PointIterator();
  }

  private class PointIterator extends PointIteratorAbstract {
    private boolean finished = false;
    private Iterator<TimedCollection.Dataset> iter;
    private FeatureDatasetPoint currentDataset;
    private PointFeatureIterator pfIter = null;

    PointIterator() {
      iter = stnCollections.getDatasets().iterator();
    }

    private PointFeatureIterator getNextIterator() throws IOException {
      if (!iter.hasNext()) return null;
      TimedCollection.Dataset td = iter.next();
      Formatter errlog = new Formatter();

      // open the next dataset
      currentDataset = (FeatureDatasetPoint) FeatureDatasetFactoryManager.open(FeatureType.STATION, td.getLocation(), null, errlog);
      if (currentDataset == null) {
        logger.error("FeatureDatasetFactoryManager failed to open: " + td.getLocation() + " \nerrlog = " + errlog);
        return getNextIterator();
      }

      if (CompositeDatasetFactory.debug)
        System.out.printf("CompositeStationCollectionFlattened.Iterator open new dataset: %s%n", td.getLocation());

      // it will have a StationTimeSeriesFeatureCollection
      List<DsgFeatureCollection> fcList = currentDataset.getPointFeatureCollectionList();
      StationTimeSeriesFeatureCollection stnCollection = (StationTimeSeriesFeatureCollection) fcList.get(0);

      PointFeatureCollection pc;
      if (wantStationsubset) {
        pc = stnCollection.flatten(stationsSubset, dateRange, varList);
      } else if (bbSubset == null) {
        pc = stnCollection.flatten(null, dateRange, null);
      } else {
        List<StationFeature> stations = stnCollection.getStationFeatures(bbSubset);
        List<String> names = new ArrayList<>();
        for (StationFeature s : stations) names.add(s.getName());

        pc = stnCollection.flatten(names, dateRange, null);
      }

      return pc.getPointFeatureIterator();
    }

    @Override
    public boolean hasNext() {
      try {
        if (pfIter == null) {
          pfIter = getNextIterator();
          if (pfIter == null) {
            close();
            return false;
          }
        }

        if (!pfIter.hasNext()) {
          pfIter.close();
          currentDataset.close();
          if (CompositeDatasetFactory.debug)
            System.out.printf("CompositeStationCollectionFlattened.Iterator close dataset: %s%n", currentDataset.getLocation());
          pfIter = getNextIterator();
          return hasNext();
        }
        return true;

      } catch (IOException ioe) {
        throw new RuntimeException(ioe);
      }
    }

    @Override
    public PointFeature next() {
      PointFeature pf =  pfIter.next();
      calcBounds(pf);
      return pf;
    }

    @Override
    public void close() {
      if (finished) return;

      if (pfIter != null)
        pfIter.close();
      finishCalcBounds();

      if (currentDataset != null)
        try {
          currentDataset.close();
          if (CompositeDatasetFactory.debug)
            System.out.printf("CompositeStationCollectionFlattened close dataset: %s%n", currentDataset.getLocation());
        } catch (IOException e) {
          throw new RuntimeException(e);
        }

      finished = true;
    }
  }

}

