/* Copyright (c) 2011 Danish Maritime Authority.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dk.dma.ais.view;

import com.beust.jcommander.Parameter;
import com.google.common.util.concurrent.AbstractScheduledService;
import com.google.inject.Injector;
import dk.dma.ais.reader.AisReaderGroup;
import dk.dma.ais.reader.AisReaders;
import dk.dma.ais.store.cli.baseclients.AisStoreDaemon;
import dk.dma.ais.store.job.JobManager;
import dk.dma.ais.tracker.targetTracker.TargetTracker;
import dk.dma.ais.tracker.targetTracker.TargetTrackerFileBackupService;
import dk.dma.ais.view.common.util.CacheManager;
import dk.dma.ais.view.rest.WebServer;
import dk.dma.commons.web.rest.AbstractResource;
import dk.dma.db.cassandra.CassandraConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * AIS viewer daemon
 */
public class AisViewDaemon extends AisStoreDaemon {
    /** The logger */
    static final Logger LOG = LoggerFactory.getLogger(AisViewDaemon.class);

    @Parameter(names = "-port", description = "The port to run AisView at")
    int port = 8090;

    @Parameter(names = "-backup", description = "The backup directory")
    File backup = new File("aisview-backup");

    @Parameter(names = "-nodatabase", description = "Do not attempt to connect to any cassandra cluster")
    boolean noCassandra = false;

    @Parameter(description = "A list of AIS sources (sourceName=host:port,host:port sourceName=host:port ...")
    List<String> sources;

    @Override
    protected void runDaemon(Injector injector) throws Exception {
        // Tracking of live data
        final TargetTracker targetTracker = new TargetTracker();
        
        // A cache manager where caches can be held and retrieved
        final CacheManager cacheManager = new CacheManager();

        // Setup the readers
        AisReaderGroup g = AisReaders.createGroup("AisView",
                sources == null ? Collections.<String> emptyList() : sources);
        AisReaders.manageGroup(g);

        // A job manager that takes care of tracking ongoing jobs
        final JobManager jobManager = new JobManager();

        // Setup the backup process
        // Files.createDirectories(backup);
        backup.mkdirs();
        if(!backup.isDirectory()) {
            throw new IOException("Unable to create directories for " + backup);
        }

        start(new TargetTrackerFileBackupService(targetTracker, backup.toPath()));

        // start tracking
        targetTracker.subscribeToPacketStream(g.stream());
        
        //target tracking cleanup service
        start(new AbstractScheduledService() {
            
            @Override
            protected Scheduler scheduler() {
                return Scheduler.newFixedDelaySchedule(1, 10, TimeUnit.MINUTES);
            }
            
            @Override
            protected void runOneIteration() throws Exception {
                final Date satellite = new Date(new Date().getTime()-(1000*60*60*48));
                final Date live = new Date(new Date().getTime()-(1000*60*60*12));
                
                targetTracker.removeAll((t, u) -> {
                    switch(t.getSourceType()) {
                    case SATELLITE:
                        return !u.hasPositionInfo() || new Date(u.getPositionTimestamp()).before(satellite);
                    default:
                        return !u.hasPositionInfo() || new Date(u.getPositionTimestamp()).before(live);
                    }
                });
            }
        });
        
        //target cleanup missing static data
        start(new AbstractScheduledService() {
            @Override
            protected Scheduler scheduler() {
                return Scheduler.newFixedDelaySchedule(1, 24, TimeUnit.HOURS);
            }
            
            @Override
            protected void runOneIteration() throws Exception {
                targetTracker.removeAll(u -> !u.hasStaticInfo());
            }
        });
        
        start(g.asService());

        // Start Ais Store Connection
        final CassandraConnection con = connect();

        WebServer ws = new WebServer(port);
        ws.getContext().setAttribute(
                AbstractResource.CONFIG,
                AbstractResource.create(g, con, targetTracker, cacheManager, jobManager));

        ws.start();
        LOG.info("AisView started");
        ws.join();
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }

    public static void main(String[] args) throws Exception {
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                LOG.error(
                        "Uncaught exception in thread "
                                + t.getClass().getCanonicalName() + ": "
                                + e.getMessage(), e);
                System.exit(-1);
            }
        });
        new AisViewDaemon().execute(args);
    }

}
