/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hudi.integ.testsuite.writer;

import org.apache.avro.generic.GenericRecord;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * {@link org.apache.hadoop.hdfs.DistributedFileSystem} (or {@link org.apache.hadoop.fs.LocalFileSystem}) based delta generator.
 */
public class DFSDeltaWriterAdapter implements DeltaWriterAdapter<GenericRecord> {

  private DeltaInputWriter deltaInputGenerator;
  private List<DeltaWriteStats> metrics = new ArrayList<>();

  public DFSDeltaWriterAdapter(DeltaInputWriter<GenericRecord> deltaInputGenerator) {
    this.deltaInputGenerator = deltaInputGenerator;
  }

  @Override
  public List<DeltaWriteStats> write(Iterator<GenericRecord> input) throws IOException {
    while (input.hasNext()) {
      GenericRecord next = input.next();
      if (this.deltaInputGenerator.canWrite()) {
        this.deltaInputGenerator.writeData(next);
      } else if (input.hasNext()) {
        rollOver();
        this.deltaInputGenerator.writeData(next);
      }
    }
    close();
    return this.metrics;
  }

  public void rollOver() throws IOException {
    close();
    this.deltaInputGenerator = this.deltaInputGenerator.getNewWriter();
  }

  private void close() throws IOException {
    this.deltaInputGenerator.close();
    this.metrics.add(this.deltaInputGenerator.getDeltaWriteStats());
  }
}
