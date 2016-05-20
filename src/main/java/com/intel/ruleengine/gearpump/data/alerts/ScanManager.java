/*
 * Copyright (c) 2016 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.intel.ruleengine.gearpump.data.alerts;

import com.intel.ruleengine.gearpump.data.RowCreator;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.*;


public class ScanManager {

    public static final long DEFAULT_SCAN_LIMIT = 1;

    private Scan scan;


    private byte[] columnFamily;
    private byte[] columnName;
    private ScanProperties scanProperties;
    private final RowCreator rowCreator;

    public ScanManager(RowCreator rowCreator, ScanProperties scanProperties) {
        this.scanProperties = scanProperties;
        this.rowCreator = rowCreator;
    }

    public ScanManager withReverse() {
        scan.setReversed(true);

        return this;
    }

    public ScanManager create(byte[] columnFamily, byte[] columnName) {
        this.columnFamily = columnFamily;
        this.columnName = columnName;
        long stop = fixStopForExclusiveScan(scanProperties.getStart(), scanProperties.getStop());

        scan = new Scan(
                rowCreator.createRow(scanProperties.getStart()),
                rowCreator.createRow(stop)
        );

        scan.addColumn(this.columnFamily, this.columnName);

        setScanLimit();

        return this;
    }

    private void setScanLimit() {
        if (scanProperties.getLimit() != null) {
            scan.setFilter(new PageFilter(scanProperties.getLimit()));
        }

    }

    public ScanManager withValueFilter(byte[] value) {
        SingleColumnValueFilter valueFilter = new SingleColumnValueFilter(this.columnFamily, this.columnName, CompareFilter.CompareOp.EQUAL, value);
        addFilter(valueFilter);
        return this;
    }

    private void addFilter(Filter filter) {
        FilterList filterList = new FilterList(scan.getFilter());
        filterList.addFilter(filter);
        scan.setFilter(filterList);
    }

    public Scan getScan() {
        if (scan == null) {
            throw new IllegalStateException("Scan object not created yet.");
        }
        return scan;
    }

    private static long fixStopForExclusiveScan(long start, long stop) {
        if (stop > start && Long.MAX_VALUE - stop > 1.0) {
            return stop + 1;
        }
        return stop;
    }
}
