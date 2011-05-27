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

package com.akiban.server.test.it.alter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.akiban.ais.model.GroupTable;
import com.akiban.ais.model.UserTable;
import com.akiban.ais.util.DDLGenerator;
import com.akiban.server.InvalidOperationException;
import com.akiban.server.api.common.NoSuchTableException;
import com.akiban.server.api.ddl.IndexAlterException;
import com.akiban.server.api.dml.scan.NewRow;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

public final class DropIndexesIT extends AlterTestBase {
    private void checkDDL(Integer tableId, String expected) {
        final UserTable table = getUserTable(tableId);
        DDLGenerator gen = new DDLGenerator();
        String actual = gen.createTable(table);
        assertEquals(table.getName() + "'s create statement", expected, actual);
    }

    
    @Test
    public void emptyIndexList() throws InvalidOperationException {
        int tid = createTable("test", "t", "id int key");
        ddl().dropIndexes(session(), tableName(tid), Collections.<String>emptyList());
    }
    
    @Test(expected=NoSuchTableException.class)
    public void unknownTable() throws InvalidOperationException {
        ddl().dropIndexes(session(), tableName("test","bar"), Arrays.asList("bar"));
    }
    
    @Test(expected=IndexAlterException.class)
    public void unknownIndex() throws InvalidOperationException {
        int tId = createTable("test", "t", "id int primary key, name varchar(255)");
        ddl().dropIndexes(session(), tableName(tId), Arrays.asList("name"));
    }

    @Test(expected=IndexAlterException.class)
    public void hiddenPrimaryKey() throws InvalidOperationException {
        int tId = createTable("test", "t", "id int, name varchar(255)");
        ddl().dropIndexes(session(), tableName(tId), Arrays.asList("PRIMARY"));
    }
    
    @Test(expected=IndexAlterException.class)
    public void declaredPrimaryKey() throws InvalidOperationException {
        int tId = createTable("test", "t", "id int primary key, name varchar(255)");
        ddl().dropIndexes(session(), tableName(tId), Arrays.asList("PRIMARY"));
    }

    @Test
    public void basicConfirmNotInAIS() throws InvalidOperationException {
        int tId = createTable("test", "t", "id int primary key, name varchar(255), index name(name)");
        ddl().dropIndexes(session(), tableName(tId), Arrays.asList("name"));

        // Index should be gone from UserTable
        UserTable uTable = getUserTable("test", "t");
        assertNotNull(uTable);
        assertNull(uTable.getIndex("name"));

        // Index should be gone from GroupTable
        GroupTable gTable = uTable.getGroup().getGroupTable();
        assertNotNull(gTable);
        assertNull(gTable.getIndex("t$name"));
    }
    
    @Test
    public void nonUniqueVarchar() throws InvalidOperationException {
        int tId = createTable("test", "t", "id int primary key, name varchar(255), index name(name)");
        dml().writeRow(session(), createNewRow(tId, 1, "bob"));
        dml().writeRow(session(), createNewRow(tId, 2, "jim"));
        ddl().dropIndexes(session(), tableName(tId), Arrays.asList("name"));
        updateAISGeneration();

        checkDDL(tId, "create table `test`.`t`(`id` int, `name` varchar(255), PRIMARY KEY(`id`)) engine=akibandb");

        List<NewRow> rows = scanAll(scanAllRequest(tId));
        assertEquals("rows from table scan", 2, rows.size());
    }
    
    @Test
    public void nonUniqueVarcharMiddleOfGroup() throws InvalidOperationException {
        int cId = createTable("coi", "c", "cid int key, name varchar(32)");
        int oId = createTable("coi", "o", "oid int key, c_id int, tag varchar(32), key tag(tag), CONSTRAINT __akiban_fk_c FOREIGN KEY __akiban_fk_c (c_id) REFERENCES c(cid)");
        int iId = createTable("coi", "i", "iid int key, o_id int, idesc varchar(32), CONSTRAINT __akiban_fk_i FOREIGN KEY __akiban_fk_i (o_id) REFERENCES o(oid)");

        // One customer, two orders, 5 items
        dml().writeRow(session(), createNewRow(cId, 1, "bob"));
        dml().writeRow(session(), createNewRow(oId, 1, 1, "supplies"));
        dml().writeRow(session(), createNewRow(oId, 2, 1, "random"));
        dml().writeRow(session(), createNewRow(iId, 1, 1, "foo"));
        dml().writeRow(session(), createNewRow(iId, 2, 1, "bar"));
        dml().writeRow(session(), createNewRow(iId, 3, 2, "zap"));
        dml().writeRow(session(), createNewRow(iId, 4, 2, "fob"));
        dml().writeRow(session(), createNewRow(iId, 5, 2, "baz"));
        
        ddl().dropIndexes(session(), tableName(oId), Arrays.asList("tag"));
        updateAISGeneration();
        
        checkDDL(oId, "create table `coi`.`o`(`oid` int, `c_id` int, `tag` varchar(32), PRIMARY KEY(`oid`), "+
                      "CONSTRAINT `__akiban_fk_c` FOREIGN KEY `__akiban_fk_c`(`c_id`) REFERENCES `c`(`cid`)) engine=akibandb");

        List<NewRow> rows = scanAll(scanAllRequest(cId));
        assertEquals("customers from table scan", 1, rows.size());
        rows = scanAll(scanAllRequest(oId));
        assertEquals("orders from table scan", 2, rows.size());
        rows = scanAll(scanAllRequest(iId));
        assertEquals("items from table scan", 5, rows.size());
    }
    
    @Test
    public void nonUniqueCompoundVarcharVarchar() throws InvalidOperationException {
        int tId = createTable("test", "t", "id int primary key, first varchar(255), last varchar(255), key name(first,last)");
        dml().writeRow(session(), createNewRow(tId, 1, "foo", "bar"));
        dml().writeRow(session(), createNewRow(tId, 2, "zap", "snap"));
        dml().writeRow(session(), createNewRow(tId, 3, "baz", "fob"));
        ddl().dropIndexes(session(), tableName(tId), Arrays.asList("name"));
        updateAISGeneration();
        
        checkDDL(tId, "create table `test`.`t`(`id` int, `first` varchar(255), `last` varchar(255), PRIMARY KEY(`id`)) engine=akibandb");

        List<NewRow> rows = scanAll(scanAllRequest(tId));
        assertEquals("rows from table scan", 3, rows.size());
    }
    
    @Test
    public void uniqueChar() throws InvalidOperationException {
        int tId = createTable("test", "t", "id int primary key, state char(2), unique state(state)");
        dml().writeRow(session(), createNewRow(tId, 1, "IA"));
        dml().writeRow(session(), createNewRow(tId, 2, "WA"));
        dml().writeRow(session(), createNewRow(tId, 3, "MA"));
        
        ddl().dropIndexes(session(), tableName(tId), Arrays.asList("state"));
        updateAISGeneration();
        
        checkDDL(tId, "create table `test`.`t`(`id` int, `state` char(2), PRIMARY KEY(`id`)) engine=akibandb");

        List<NewRow> rows = scanAll(scanAllRequest(tId));
        assertEquals("rows from table scan", 3, rows.size());
    }
    
    @Test
    public void uniqueIntNonUniqueDecimal() throws InvalidOperationException {
        int tId = createTable("test", "t", "id int primary key, otherId int, price decimal(10,2), unique otherId(otherId), key price(price)");
        dml().writeRow(session(), createNewRow(tId, 1, 1337, "10.50"));
        dml().writeRow(session(), createNewRow(tId, 2, 5000, "10.50"));
        dml().writeRow(session(), createNewRow(tId, 3, 47000, "9.99"));
        
        ddl().dropIndexes(session(), tableName(tId), Arrays.asList("otherId", "price"));
        updateAISGeneration();
        
        checkDDL(tId, "create table `test`.`t`(`id` int, `otherId` int, `price` decimal(10, 2), PRIMARY KEY(`id`)) engine=akibandb");

        List<NewRow> rows = scanAll(scanAllRequest(tId));
        assertEquals("rows from table scan", 3, rows.size());
    }
}
