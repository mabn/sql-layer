/**
 * Copyright (C) 2009-2013 Akiban Technologies, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.akiban.server.test.it.qp;


import com.akiban.qp.expression.IndexBound;
import com.akiban.qp.expression.IndexKeyRange;
import com.akiban.qp.operator.API;
import com.akiban.qp.operator.ExpressionGenerator;
import com.akiban.qp.operator.Operator;
import com.akiban.qp.row.RowBase;
import com.akiban.server.api.dml.SetColumnSelector;
import com.akiban.server.api.dml.scan.NewRow;
import com.akiban.server.expression.Expression;
import com.akiban.server.expression.std.FieldExpression;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static com.akiban.qp.operator.API.ancestorLookup_Default;
import static com.akiban.qp.operator.API.cursor;
import static com.akiban.qp.operator.API.groupScan_Default;
import static com.akiban.qp.operator.API.ifEmpty_Default;
import static com.akiban.qp.operator.API.indexScan_Default;
import static com.akiban.server.test.ExpressionGenerators.field;
import static com.akiban.server.test.ExpressionGenerators.literal;

public class IfEmptyIT extends OperatorITBase
{
    @Override
    protected void setupPostCreateSchema() {
        super.setupPostCreateSchema();
        NewRow[] db = new NewRow[]{
            createNewRow(customer, 0L, "matrix"), // no orders
            createNewRow(customer, 2L, "foundation"), // two orders
            createNewRow(order, 200L, 2L, "david"),
            createNewRow(order, 201L, 2L, "david"),
        };
        use(db);
    }

    // Test argument validation

    @Test(expected = IllegalArgumentException.class)
    public void testInputNull()
    {
        ifEmpty_Default(null, customerRowType, Collections.<ExpressionGenerator>emptyList(), API.InputPreservationOption.KEEP_INPUT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRowTypeNull()
    {
        ifEmpty_Default(groupScan_Default(coi), null, Collections.<ExpressionGenerator>emptyList(), API.InputPreservationOption.KEEP_INPUT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExprsNull()
    {
        ifEmpty_Default(groupScan_Default(coi), customerRowType, null, API.InputPreservationOption.KEEP_INPUT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInputPreservationNull()
    {
        ifEmpty_Default(groupScan_Default(coi), customerRowType, Collections.<ExpressionGenerator>emptyList(), null);
    }

    // Test operator execution

    @Test
    public void testNonEmptyKeepInput()
    {
        Operator plan =
            ifEmpty_Default(
                ancestorLookup_Default(
                    indexScan_Default(orderCidIndexRowType, cidKeyRange(2), asc()),
                    coi,
                    orderCidIndexRowType,
                    Collections.singleton(orderRowType),
                    API.InputPreservationOption.DISCARD_INPUT),
                orderRowType, Arrays.asList(literal(999), literal(999), literal("herman")),
                API.InputPreservationOption.KEEP_INPUT);
        RowBase[] expected = new RowBase[]{
            row(orderRowType, 200L, 2L, "david"),
            row(orderRowType, 201L, 2L, "david"),
        };
        compareRows(expected, cursor(plan, queryContext, queryBindings));
    }

    @Test
    public void testNonEmptyDiscardInput()
    {
        Operator plan =
            ifEmpty_Default(
                ancestorLookup_Default(
                    indexScan_Default(orderCidIndexRowType, cidKeyRange(2), asc()),
                    coi,
                    orderCidIndexRowType,
                    Collections.singleton(orderRowType),
                    API.InputPreservationOption.DISCARD_INPUT),
                orderRowType, Arrays.asList(literal(999), literal(999), literal("herman")),
                API.InputPreservationOption.DISCARD_INPUT);
        RowBase[] expected = new RowBase[]{
        };
        compareRows(expected, cursor(plan, queryContext, queryBindings));
    }

    @Test
    public void testEmptyKeepInput()
    {
        Operator plan =
            ifEmpty_Default(
                ancestorLookup_Default(
                    indexScan_Default(orderCidIndexRowType, cidKeyRange(0), asc()),
                    coi,
                    orderCidIndexRowType,
                    Collections.singleton(orderRowType),
                API.InputPreservationOption.DISCARD_INPUT),
                orderRowType, Arrays.asList(literal(999), literal(999), literal("herman")),
                API.InputPreservationOption.KEEP_INPUT);
        RowBase[] expected = new RowBase[]{
            row(orderRowType, 999L, 999L, "herman"),
        };
        compareRows(expected, cursor(plan, queryContext, queryBindings));
    }

    @Test
    public void testEmptyDiscardInput()
    {
        Operator plan =
            ifEmpty_Default(
                ancestorLookup_Default(
                    indexScan_Default(orderCidIndexRowType, cidKeyRange(0), asc()),
                    coi,
                    orderCidIndexRowType,
                    Collections.singleton(orderRowType),
                API.InputPreservationOption.DISCARD_INPUT),
                orderRowType, Arrays.asList(literal(999), literal(999), literal("herman")),
                API.InputPreservationOption.DISCARD_INPUT);
        RowBase[] expected = new RowBase[]{
            row(orderRowType, 999L, 999L, "herman"),
        };
        compareRows(expected, cursor(plan, queryContext, queryBindings));
    }

    @Test
    public void testCursorNonEmptyDefaultKeepInput()
    {
        Operator plan =
            ifEmpty_Default(
                ancestorLookup_Default(
                    indexScan_Default(orderCidIndexRowType, cidKeyRange(2), asc()),
                    coi,
                    orderCidIndexRowType,
                    Collections.singleton(orderRowType),
                API.InputPreservationOption.DISCARD_INPUT),
                orderRowType,
                Arrays.asList(literal(999), literal(999), literal("herman")),
                API.InputPreservationOption.KEEP_INPUT);
        CursorLifecycleTestCase testCase = new CursorLifecycleTestCase()
        {
            @Override
            public RowBase[] firstExpectedRows()
            {
                return new RowBase[] {
                    row(orderRowType, 200L, 2L, "david"),
                    row(orderRowType, 201L, 2L, "david"),
                };
            }
        };
        testCursorLifecycle(plan, testCase);
    }

    @Test
    public void testCursorNonEmptyDefaultDiscardInput()
    {
        Operator plan =
            ifEmpty_Default(
                ancestorLookup_Default(
                    indexScan_Default(orderCidIndexRowType, cidKeyRange(2), asc()),
                    coi,
                    orderCidIndexRowType,
                    Collections.singleton(orderRowType),
                API.InputPreservationOption.DISCARD_INPUT),
                orderRowType,
                Arrays.asList(literal(999), literal(999), literal("herman")),
                API.InputPreservationOption.DISCARD_INPUT);
        CursorLifecycleTestCase testCase = new CursorLifecycleTestCase()
        {
            @Override
            public RowBase[] firstExpectedRows()
            {
                return new RowBase[] {
                };
            }
        };
        testCursorLifecycle(plan, testCase);
    }

    @Test
    public void testCursorEmptyKeepInput()
    {
        Operator plan =
            ifEmpty_Default(
                ancestorLookup_Default(
                    indexScan_Default(orderCidIndexRowType, cidKeyRange(0), asc()),
                    coi,
                    orderCidIndexRowType,
                    Collections.singleton(orderRowType),
                    API.InputPreservationOption.DISCARD_INPUT),
                orderRowType,
                Arrays.asList(literal(999), literal(999), literal("herman")),
                API.InputPreservationOption.KEEP_INPUT);
        CursorLifecycleTestCase testCase = new CursorLifecycleTestCase()
        {
            @Override
            public RowBase[] firstExpectedRows()
            {
                return new RowBase[] {
                    row(orderRowType, 999L, 999L, "herman"),
                };
            }
        };
        testCursorLifecycle(plan, testCase);
    }

    @Test
    public void testCursorEmptyDiscardInput()
    {
        Operator plan =
            ifEmpty_Default(
                ancestorLookup_Default(
                    indexScan_Default(orderCidIndexRowType, cidKeyRange(0), asc()),
                    coi,
                    orderCidIndexRowType,
                    Collections.singleton(orderRowType),
                    API.InputPreservationOption.DISCARD_INPUT),
                orderRowType,
                Arrays.asList(literal(999), literal(999), literal("herman")),
                API.InputPreservationOption.DISCARD_INPUT);
        CursorLifecycleTestCase testCase = new CursorLifecycleTestCase()
        {
            @Override
            public RowBase[] firstExpectedRows()
            {
                return new RowBase[] {
                    row(orderRowType, 999L, 999L, "herman"),
                };
            }
        };
        testCursorLifecycle(plan, testCase);
    }

    private IndexKeyRange cidKeyRange(int cid)
    {
        IndexBound bound = new IndexBound(row(orderCidIndexRowType, cid), new SetColumnSelector(0));
        return IndexKeyRange.bounded(orderCidIndexRowType, bound, true, bound, true);
    }

    private API.Ordering asc()
    {
        API.Ordering ordering = new API.Ordering();
        ordering.append(field(orderCidIndexRowType, 0), true);
        return ordering;
    }
}
