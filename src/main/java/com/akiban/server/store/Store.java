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

package com.akiban.server.store;

import com.akiban.ais.model.AkibanInformationSchema;
import com.akiban.ais.model.Group;
import com.akiban.ais.model.Index;
import com.akiban.ais.model.Sequence;
import com.akiban.ais.model.TableName;
import com.akiban.ais.model.UserTable;
import com.akiban.qp.operator.StoreAdapter;
import com.akiban.qp.rowtype.Schema;
import com.akiban.server.TableStatistics;
import com.akiban.server.api.dml.ColumnSelector;
import com.akiban.server.api.dml.scan.ScanLimit;
import com.akiban.server.rowdata.RowData;
import com.akiban.server.rowdata.RowDef;
import com.akiban.server.service.session.Session;
import com.akiban.server.service.tree.KeyCreator;
import com.akiban.server.service.tree.TreeLink;
import com.akiban.server.store.statistics.IndexStatisticsService;

import java.util.Collection;

public interface Store extends KeyCreator {

    /** Get the RowDef for the given ID. Note, a transaction should be active before calling this. */
    RowDef getRowDef(Session session, int rowDefID);
    RowDef getRowDef(Session session, TableName tableName);
    AkibanInformationSchema getAIS(Session session);

    void writeRow(Session session, RowData row);
    void deleteRow(Session session, RowData row, boolean deleteIndexes, boolean cascadeDelete);

    /** newRow can be partial, as specified by selector, but oldRow must be fully present. */
    void updateRow(Session session, RowData oldRow, RowData newRow, ColumnSelector selector, Index[] indexes);

    /**
     * Create a new RowCollector.
     * 
     * @param session Session to use.
     * @param scanFlags Flags specifying collection parameters (see flags in {@link RowCollector})
     * @param rowDefId ID specifying the type of row to that will be collected.
     * @param indexId The indexId from the given rowDef to collect on or 0 for table scan
     * @param columnBitMap
     * @param start RowData containing values to begin the scan from.
     * @param startColumns ColumnSelector indicating which fields are set in <code>start</code>
     * @param end RowData containing values to stop the scan at.
     * @param endColumns ColumnSelector indicating which fields are set in <code>end</code>
     * @throws Exception 
     */
    RowCollector newRowCollector(Session session,
                                 int scanFlags,
                                 int rowDefId,
                                 int indexId,
                                 byte[] columnBitMap,
                                 RowData start,
                                 ColumnSelector startColumns,
                                 RowData end,
                                 ColumnSelector endColumns,
                                 ScanLimit scanLimit);
    /**
     * Get the previously saved RowCollector for the specified tableId. Used in
     * processing the ScanRowsMoreRequest message.
     * 
     * @param tableId
     * @return
     */
    RowCollector getSavedRowCollector(Session session, int tableId);


    /**
     * Push a RowCollector onto a stack so that it can subsequently be
     * referenced by getSavedRowCollector.
     * 
     * @param rc
     */
    void addSavedRowCollector(Session session, RowCollector rc);

    /***
     * Remove a previously saved RowCollector. Must the the most recently added
     * RowCollector for a table.
     *
     * @param rc
     */
    void removeSavedRowCollector(Session session, RowCollector rc);

    long getRowCount(Session session, boolean exact,
            RowData start, RowData end, byte[] columnBitMap);

    TableStatistics getTableStatistics(Session session, int tableId);

    /**
     * Delete all data associated with the group. This includes
     * all indexes from all tables, group indexes, and the group itself.
     */
    void dropGroup(Session session, Group group);

    /**
     * Truncate the given group. This includes indexes from all tables, group
     * indexes, the group itself, and all table statuses.
     */
    void truncateGroup(Session session, Group group);

    void truncateTableStatus(Session session, int rowDefId);

    void deleteIndexes(Session session, Collection<? extends Index> indexes);
    void buildIndexes(Session session, Collection<? extends Index> indexes, boolean deferIndexes);

    void deleteSequences (Session session, Collection<? extends Sequence> sequences);
    /**
     * Remove all trees, and their contents, associated with the given table.
     * @param session Session
     * @param table Table
     * @throws Exception
     */
    void removeTrees(Session session, UserTable table);
    void removeTree(Session session, TreeLink treeLink);
    void truncateTree(Session session, TreeLink treeLink);

    /**
     * Low level operation. Removes the given trees and <i>only</i> the given trees.
     * To ensure metadata and other state is updated, check if another method for
     * specific entities is more appropriate (e.g. {@link #deleteIndexes(Session, Collection)}).
     */
    void removeTrees(Session session, Collection<? extends TreeLink> treeLinks);

    /** Get the underlying {@link PersistitStore}. */
    public PersistitStore getPersistitStore();

    void truncateIndexes(Session session, Collection<? extends Index> indexes);

    void setIndexStatistics(IndexStatisticsService indexStatistics);

    StoreAdapter createAdapter(Session session, Schema schema);
}
