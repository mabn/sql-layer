/**
 * Copyright (C) 2011 Akiban Technologies Inc.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses.
 */

package com.akiban.server.itests.slap;

import com.akiban.server.itests.ApiTestBase;
import com.akiban.server.mttests.mtutil.Timing;
import org.junit.Test;

import static org.junit.Assert.*;

@org.junit.Ignore("ignore this test until we know where to put it")
public final class LotsOfServicesIT extends ApiTestBase {
    @Test
    public void loop() throws Exception{

        stopTestServices(); // shut down ApiTestBase's @Before services

        final int LOOP_COUNT = 1000;
        int i=0;
        try {
            for (; i < LOOP_COUNT; ++i) {
                startTestServices();
                Timing.sleep(10);
                stopTestServices();
            }
        } catch (Throwable e) {
            throw new RuntimeException("At i="+i, e);
        }

        startTestServices(); // so that ApiTestBase's @After has something to shut down
    }

    @Test
    public void doNotCheckThisIn() {
        fail("don't check this class in without an @Ignore. It takes too long");
    }
}
